package jhs.lc.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.protocol.FileTypeDescriptor;

import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.jmf.BufferedImageVideoProducer;
import jhs.lc.sims.AngularSimulation;
import jhs.lc.tools.inputs.AbstractTransitShape;
import jhs.lc.tools.inputs.SimSpec;
import jhs.lc.tools.inputs.SpecMapper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class GenerateSimulatedData extends AbstractTool {
	private static final Logger logger = Logger.getLogger(GenerateSimulatedData.class.getName());
	private static final double DEF_VIDEO_DURATION = 60;
	
	private void run(CommandLine cmdLine) throws Exception {
		String[] args = cmdLine.getArgs();
		if(args.length != 1) {
			throw new IllegalArgumentException("One command line argument is required: The simulation specification JSON file.");
		}
		Level level = Level.WARNING;
		try {
			String logText = cmdLine.getOptionValue("log");
			if(logText != null) {
				level = Level.parse(logText);
			}
		} finally {
			logger.setLevel(level);
		}
		String specFileName = args[0];
		File specFile = new File(specFileName);
		SimSpec simSpec = SpecMapper.parseSimSpec(specFile);
		this.validateSpec(simSpec);
		this.generateSimulationResults(simSpec, cmdLine, specFile);
	}	
	
	private void generateSimulationResults(SimSpec simSpec, CommandLine cmdLine, File contextFile) throws Exception {
		String seedText = cmdLine.getOptionValue("seed");
		long seed = seedText == null ? 1 : Long.parseLong(seedText);
		Random random = new Random(seed * 5 - 19);
		AngularSimulation sim = this.getSimulation(simSpec, contextFile);
		double[] timestamps = AngularSimulation.timestamps(simSpec.getStartTime(), simSpec.getEndTime(), simSpec.getNumSteps());
		int width = simSpec.getWidthPixels();
		int height = simSpec.getHeightPixels();
		double noiseFraction = simSpec.getNoiseFraction();
		double noiseSd = Math.log(1.0 + noiseFraction);
		String csvFileName = cmdLine.getOptionValue("o");
		if(csvFileName != null) {
			double peakTimespanFraction = 0.5;
			double[] fluxArray = sim.produceModeledFlux(timestamps, peakTimespanFraction, width, height);
			File outFile = new File(csvFileName);
			PrintWriter out = new PrintWriter(outFile);
			out.println("Timestamp,Flux"); 
			for(int i = 0; i < timestamps.length; i++) {
				double normFluxPN = fluxArray[i];
				if(noiseSd != 0) {
					normFluxPN *= Math.exp(random.nextGaussian() * noiseSd);
				}
				out.println(timestamps[i] + "," + normFluxPN);
			}
			out.close();
			System.out.println("Wrote " + outFile);		
		}
		String videoFileName = cmdLine.getOptionValue("video");
		if(videoFileName != null) {
			String timeCaption = cmdLine.getOptionValue("tcaption");
			if(timeCaption == null) {
				timeCaption = "Day";
			}
			double peakTimespanFraction = 0.5;
			double logitNoiseSd = noiseSd * 10;
			Iterator<BufferedImage> iterator = sim.produceModelImages(timestamps, peakTimespanFraction, width, height, logitNoiseSd, timeCaption, simSpec.getStartTime(), simSpec.getEndTime());
			double videoDuration = this.getOptionDouble(cmdLine, "vd", DEF_VIDEO_DURATION);
			if(videoDuration <= 0) {
				throw new IllegalStateException("Invalid video duration: " + videoDuration + ".");
			}
			double frameRate = timestamps.length / videoDuration;
	        BufferedImageVideoProducer producer = new BufferedImageVideoProducer(width, height, (float) frameRate, FileTypeDescriptor.QUICKTIME);
	        File outFile = new File(videoFileName);
	        producer.writeToFile(outFile, iterator);
	        System.out.println("Wrote " + outFile);
		}
	}
	
	private AngularSimulation getSimulation(SimSpec simSpec, File contextFile) throws Exception {		
		AbstractTransitShape transitShape = simSpec.getTransitShape();
		if(transitShape == null) {
			throw new IllegalStateException("No transitShape.");
		}
		RotationAngleSphereFactory sphereFactory = transitShape.createSphereFactory(contextFile);
		LimbDarkeningParams ldParams = simSpec.getLimbDarkeningParams() == null ? LimbDarkeningParams.SUN : new LimbDarkeningParams(simSpec.getLimbDarkeningParams());
		AngularSimulation sim = new AngularSimulation(
				simSpec.getInclineAngle(), 
				simSpec.getOrbitRadius(), 
				simSpec.getOrbitPeriod(), 
				ldParams, 
				sphereFactory
		);
		Double vpw = simSpec.getViewportWidth();
		Double vph = simSpec.getViewportHeight();
		if(vpw != null) {
			sim.setBoxWidth(vpw.doubleValue());
		}
		if(vph != null)	{
			sim.setBoxHeight(vph.doubleValue());
		}
		return sim;
	}
	
	private void validateSpec(SimSpec simSpec) {
		if(simSpec.getNumSteps() < 2) {
			throw new IllegalStateException("numSteps must be at least 2.");
		}
		if(simSpec.getOrbitPeriod() <= 0) {
			throw new IllegalStateException("orbitPeriod must be greater than zero.");
		}
		if(simSpec.getOrbitRadius() < 1.0) {
			throw new IllegalStateException("orbitRadius must be at least 1.");			
		}
		if(simSpec.getWidthPixels() <= 0) {
			throw new IllegalStateException("widthPixels must be greater than zero.");						
		}
		if(simSpec.getHeightPixels() <= 0) {
			throw new IllegalStateException("heightPixels must be greater than zero.");						
		}
		if(simSpec.getLimbDarkeningParams() != null && simSpec.getLimbDarkeningParams().length == 0) {
			throw new IllegalStateException("limbDarkeningParams must be a non-empty array.");								
		}
		if(simSpec.getNoiseFraction() < 0) {
			throw new IllegalStateException("noiseFraction must be zero or greater.");
		}
	}
	
	public static void main(String[] args) throws Exception {
		Options options = getOptions();
		CommandLine cmdLine = new PosixParser().parse(options, args, false);
		try {
			if(cmdLine.hasOption("help")) {
				printHelp(options);
			}
			else {
				new GenerateSimulatedData().run(cmdLine);
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
		formatter.printHelp("lc-sim [options] <sim-spec>.json", options);		
	}
	
	@SuppressWarnings("static-access")
	private static Options getOptions() {
		Option helpOption = OptionBuilder
				.withDescription("Prints tool help.")
				.create("help");
		Option seedOption = OptionBuilder.withArgName("integer")
				.hasArg()
				.withDescription("Sets seed for random number generation.")
				.create("seed");
		Option timeUnitOption = OptionBuilder.withArgName("unit")
				.hasArg()
				.withDescription("Video caption of timestamp. Default is 'Day'.")
				.create("tcaption");
		Option noiseSdOption = OptionBuilder.withArgName("number")
				.hasArg()
				.withDescription("Sets standard deviation of noise added to results. Default is zero.")
				.create("nsd");
		Option outCsvOption = OptionBuilder.withArgName("csv-file")
				.hasArg()
				.withDescription("Sets name of CSV file where light curve data will be written.")
				.create("o");
		Option outVideoOption = OptionBuilder.withArgName("mov-file")
				.hasArg()
				.withDescription("Sets name of MOV file where the simulation video will be written.")
				.create("video");
		Option videoDurationOption = OptionBuilder.withArgName("seconds")
				.hasArg()
				.withDescription("Sets the video duration in seconds. Default is " + DEF_VIDEO_DURATION + ".")
				.create("vd");
		Option logOption = OptionBuilder.withArgName("level")
				.hasArg()
				.withDescription("Sets the java.util.logging level.")
				.create("log");
		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(outCsvOption);
		options.addOption(outVideoOption);
		options.addOption(seedOption);
		options.addOption(timeUnitOption);
		options.addOption(noiseSdOption);
		options.addOption(logOption);
		options.addOption(videoDurationOption);
		return options;
	}
}
