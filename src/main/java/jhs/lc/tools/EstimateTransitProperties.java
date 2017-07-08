package jhs.lc.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import jhs.lc.data.DataSet;
import jhs.lc.data.LightCurve;
import jhs.lc.data.LightCurvePoint;
import jhs.math.util.MathUtil;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class EstimateTransitProperties extends AbstractTool {
	private static final Logger logger = Logger.getLogger(EstimateTransitProperties.class.getName());
	private static final int DEFAULT_WL = 7;
	
	private void run(CommandLine cmdLine) throws Exception {
		this.configureLoggingLevel(cmdLine);
		Double orbitPeriod = this.getOptionDouble(cmdLine, "p", null);
		if(orbitPeriod == null) {
			throw new IllegalStateException("Option -p (orbit period) is required.");
		}
		String inputFilePath = cmdLine.getOptionValue("i");
		if(inputFilePath == null) {
			throw new IllegalStateException("A CSV input file with light curve data is required. Use option -i.");
		}
		File inputFile = new File(inputFilePath);
		LightCurvePoint[] lightCurve = DataSet.load(inputFile);
		double[] timestamps = LightCurvePoint.timestamps(lightCurve);
		double[] fluxArray = LightCurvePoint.fluxArray(lightCurve);
		int windowLength = getOptionInt(cmdLine, "wl", DEFAULT_WL);
		double[] trendChangeProfile = LightCurve.trendChangeProfile(fluxArray, windowLength);
		String outputFilePath = cmdLine.getOptionValue("o");
		if(outputFilePath != null) {
			this.writeData(lightCurve, trendChangeProfile, outputFilePath);
		}
		double transitTime = this.estimateTransitTime(timestamps, fluxArray, trendChangeProfile);
		System.out.println("Transit time: " + transitTime);
		double orbitRadius = this.estimateOrbitRadius(orbitPeriod.doubleValue(), transitTime);
		System.out.println("Orbit radius: " + orbitRadius);		
	}
	
	private double estimateOrbitRadius(double period, double transitTime) {
		double angle = 2 * Math.PI * transitTime / period;
		return 1.0 / Math.sin(angle / 2);
	}
	
	private double estimateTransitTime(double[] timestamps, double[] fluxArray, double[] trendChangeProfile) {
		int minFluxIndex = MathUtil.minIndex(fluxArray);
		if(trendChangeProfile[minFluxIndex] <= 0) {
			throw new IllegalStateException("Tool expects trend change to be positive at transit peak.");
		}
		int idx1 = indexOfFirstNegative(trendChangeProfile, minFluxIndex, -1);
		int idx2 = indexOfFirstNegative(trendChangeProfile, minFluxIndex, +1);
		if(idx1 == -1 || idx2 == -1) {
			throw new IllegalStateException("Light curve does not contain negative flux trend changes both before and after transit peak.");
		}
		double time1 = interpolateTime(timestamps, trendChangeProfile, idx1, idx1 + 1);
		double time2 = interpolateTime(timestamps, trendChangeProfile, idx2 - 1, idx2);
		return time2 - time1;
	}
	
	private double interpolateTime(double[] timestamps, double[] trendChangeProfile, int index1, int index2) {
		double change1 = trendChangeProfile[index1];
		double change2 = trendChangeProfile[index2];
		double zi = -change1 / (change2 - change1);
		double t1 = timestamps[index1];
		double t2 = timestamps[index2];
		return t2 * zi + t1 * (1 - zi);
	}
	
	private int indexOfFirstNegative(double[] trendChangeProfile, int startIndex, int increment) {
		int index = startIndex;
		for(;;) {
			if(index < 0 || index >= trendChangeProfile.length) {
				return -1;
			}
			if(trendChangeProfile[index] < 0) {
				return index;
			}
			index += increment;
		}
	}
	
	private void writeData(LightCurvePoint[] lightCurve, double[] trendChangeProfile, String outFilePath) throws IOException {
		double[] timestamps = LightCurvePoint.timestamps(lightCurve);
		double[] obsFluxArray = LightCurvePoint.fluxArray(lightCurve);
		File file = new File(outFilePath);
		PrintWriter out = new PrintWriter(file);
		try {
			out.println("Timestamp,Flux,TrendChange");
			for(int i = 0; i < lightCurve.length; i++) {
				out.println(timestamps[i] + "," + obsFluxArray[i] + "," + trendChangeProfile[i]);
			}
		} finally {
			out.close();
		}		
		System.out.println("Wrote light curve data to " + file);
	}

	public static void main(String[] args) throws Exception {
		Options options = getOptions();
		CommandLine cmdLine = new PosixParser().parse(options, args, false);
		try {
			if(cmdLine.hasOption("help")) {
				printHelp(options);
			}
			else {
				new EstimateTransitProperties().run(cmdLine);
			}
		} catch(Exception err) {
			System.out.println("err: " + err.getMessage());
			if(logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Tool error.", err);
			}
			printHelp(options);
			System.exit(1);
		}
	}
	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("lc-tp [options] -i <lightcurve>.csv", options);
	}
	
	@SuppressWarnings("static-access")
	private static Options getOptions() {
		Option helpOption = OptionBuilder
				.withDescription("Prints tool help.")
				.create("help");
		Option inCsvOption = OptionBuilder.withArgName("csv-file")
				.hasArg()
				.withDescription("Sets path of CSV file where input light curve data is read from.")
				.create("i");
		Option outCsvOption = OptionBuilder.withArgName("csv-file")
				.hasArg()
				.withDescription("Sets path of CSV file where trend change profile will be written.")
				.create("o");
		Option wlOption = OptionBuilder.withArgName("n")
				.hasArg()
				.withDescription("Sets length of window used to estimate trends. Default is " + DEFAULT_WL + ".")
				.create("wl");
		Option periodOption = OptionBuilder.withArgName("period")
				.hasArg()
				.withDescription("Sets orbit period. Required.")
				.create("p");
		Option logOption = OptionBuilder.withArgName("level")
				.hasArg()
				.withDescription("Sets the java.util.logging level.")
				.create("log");
		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inCsvOption);
		options.addOption(outCsvOption);
		options.addOption(wlOption);
		options.addOption(periodOption);
		options.addOption(logOption);
		return options;
	}
}
