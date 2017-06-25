package jhs.lc.tools;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.protocol.FileTypeDescriptor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.math.MathException;

import jhs.lc.data.DataSet;
import jhs.lc.data.LightCurvePoint;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.jmf.BufferedImageVideoProducer;
import jhs.lc.opt.CSLightCurveFitter;
import jhs.lc.opt.Solution;
import jhs.lc.opt.SolutionSampler;
import jhs.lc.sims.AngleUnsupportedException;
import jhs.lc.sims.AngularFluxSource;
import jhs.lc.sims.FastApproximateFluxSource;
import jhs.lc.sims.SimulatedFluxSource;
import jhs.lc.tools.inputs.AbstractOptMethod;
import jhs.lc.tools.inputs.OptResultsSpec;
import jhs.lc.tools.inputs.OptSpec;
import jhs.lc.tools.inputs.SpecMapper;

public class SolveLightCurve extends AbstractTool {
	private static final Logger logger = Logger.getLogger(SolveLightCurve.class.getName());
	private static final int DEF_MAX_ITERATIONS = 100;
	private static final int DEF_MAX_AGD_ITERATIONS = 100;
	private static final int DEF_POP_SIZE = 100;
	private static final double DEF_VIDEO_DURATION = 60;
	private static final int DEF_OUT_NUM_PIXELS = 100000;

	private void run(CommandLine cmdLine) throws Exception {
		String[] args = cmdLine.getArgs();
		Level level = Level.WARNING;
		try {
			String logText = cmdLine.getOptionValue("log");
			if(logText != null) {
				level = Level.parse(logText);
			}
		} finally {
			logger.setLevel(level);
		}
		if(args.length != 1) {
			logger.info("run(): Command line arguments: " + Arrays.toString(args));
			throw new IllegalArgumentException("One command line argument is required: The optimization specification JSON file.");
		}
		String specFileName = args[0];
		logger.info("run(): Specification file: " + specFileName);
		File specFile = new File(specFileName);
		OptSpec optSpec = SpecMapper.parseOptSpec(specFile);
		this.validateSpec(optSpec);
		String inputFileText = cmdLine.getOptionValue("i");
		if(inputFileText == null) {
			throw new IllegalStateException("A CSV input file with light curve data is required. Use option -i.");
		}
		File inputFile = new File(inputFileText);
		LightCurvePoint[] lightCurve = DataSet.load(inputFile);
		double[] fluxArray = LightCurvePoint.fluxArray(lightCurve);
		double[] timestamps = LightCurvePoint.timestamps(lightCurve);
		String seedText = cmdLine.getOptionValue("seed");
		long seed = seedText == null ? 1 : Long.parseLong(seedText);
		Random random = new Random(seed * 7 - 11);
		
		double minIndex = LimbDarkeningParams.minIndex(fluxArray);
		double peakFraction = (minIndex + 0.5) / fluxArray.length;
		logger.info("Transit peak estimated to occur at index " + minIndex + " of the flux sequence, whose length is " + fluxArray.length + ".");
		LimbDarkeningParams ldParams = optSpec.getLimbDarkeningParams() == null ? LimbDarkeningParams.SUN : new LimbDarkeningParams(optSpec.getLimbDarkeningParams());
		SolutionSampler sampler = this.getSampler(random, timestamps, fluxArray, peakFraction, ldParams, optSpec, cmdLine, specFile);
		int populationSize = this.getOptionInt(cmdLine, "pop", DEF_POP_SIZE);
		int numClusteringIterations = this.getOptionInt(cmdLine, "noi", DEF_MAX_ITERATIONS);
		int numGradientDescentIterations = this.getOptionInt(cmdLine, "nagd", DEF_MAX_AGD_ITERATIONS);
		logger.info("Population size: " + populationSize + ".");
		logger.info("Max iterations: " + numClusteringIterations + ".");
		long time1 = System.currentTimeMillis();
		Solution solution = this.solve(lightCurve, sampler, populationSize, numClusteringIterations, numGradientDescentIterations);		
		long time2 = System.currentTimeMillis();
		double elapsedSeconds = (time2 - time1) / 1000.0;
		
		logger.info("Elapsed: " + elapsedSeconds + " seconds.");

		String outFilePath = cmdLine.getOptionValue("o");
		if(outFilePath != null) {
			this.writeData(lightCurve, solution, outFilePath);
		}

		String resultsFilePath = cmdLine.getOptionValue("or");
		if(resultsFilePath != null) {
			this.writeResults(resultsFilePath, optSpec, sampler, lightCurve, solution, fluxArray, elapsedSeconds);
		}		

		String transitImageFileName = cmdLine.getOptionValue("oi");
		if(transitImageFileName != null) {
			this.writeTransitImageFile(transitImageFileName, timestamps, peakFraction, solution, optSpec.getWidthPixels(), optSpec.getHeightPixels());
		}		

		String videoFileName = cmdLine.getOptionValue("video");
		if(videoFileName != null) {
			String timeCaption = cmdLine.getOptionValue("tcaption");
			if(timeCaption == null) {
				timeCaption = "Day";
			}
			double videoDuration = this.getOptionDouble(cmdLine, "vd", DEF_VIDEO_DURATION);
			if(videoDuration <= 0) {
				throw new IllegalStateException("Invalid video duration: " + videoDuration + ".");
			}
			this.writeVideo(videoFileName, timeCaption, videoDuration, timestamps, peakFraction, solution, optSpec, ldParams);
		}
	}
	
	private void writeTransitImageFile(String imageFileName, double[] timestamps, double peakFraction, Solution solution, int transitWidthPixels, int transitHeightPixels) throws IOException {
		int numPixels = DEF_OUT_NUM_PIXELS;
		BufferedImage image = solution.produceDepiction(numPixels);
		File outFile = new File(imageFileName);
		ImageIO.write(image, "png", outFile);
		System.out.println("Wrote " + outFile);
	}	
	
	private void writeVideo(String videoFileName, String timeCaption, double videoDuration, double[] timestamps, double peakFraction, Solution solution, OptSpec optSpec, LimbDarkeningParams ldParams) throws Exception {
		int numPixels = DEF_OUT_NUM_PIXELS;
		Iterator<BufferedImage> iterator = solution.produceModelImages(optSpec.getInclineAngle(), optSpec.getOrbitPeriod(), ldParams, timestamps, peakFraction, timeCaption, numPixels);
		double frameRate = timestamps.length / videoDuration;
		Dimension dimension = solution.suggestImageDimension(numPixels);
        BufferedImageVideoProducer producer = new BufferedImageVideoProducer(dimension.width, dimension.height, (float) frameRate, FileTypeDescriptor.QUICKTIME);
        File outFile = new File(videoFileName);
        producer.writeToFile(outFile, iterator);
        System.out.println("Wrote " + outFile);		
	}
		
	private Solution solve(LightCurvePoint[] lightCurve, SolutionSampler sampler, int populationSize, int numClusteringIterations, int numGradientDescentIterations) throws MathException {
		//TODO: Get from command line
		CSLightCurveFitter fitter = new CSLightCurveFitter(sampler, populationSize) {
			@Override
			protected void informProgress(String stage, int iteration, double error) {
				if(logger.isLoggable(Level.INFO)) {
					logger.info("[" + stage + "] Iteration " + iteration + ": error=" + error);
				}
			}
		};
		// TODO: configure with options
		fitter.setCircuitShuffliness(0.5);
		fitter.setDisplacementFactor(0.04);
		fitter.setExpansionFactor(3.0);
		fitter.setMaxCSIterationsWithClustering(numClusteringIterations);
		fitter.setMaxExtraCSIterations(50);
		fitter.setMaxGradientDescentIterations(numGradientDescentIterations);
				
		Solution solution = fitter.optimize(lightCurve);
		return solution;
	}
	
	private void writeResults(String resultsFilePath, OptSpec optSpec, SolutionSampler sampler, LightCurvePoint[] lightCurve, Solution solution, double[] fluxArray, double elapsedSeconds) throws Exception {
		double[] weights = sampler.createFluxWeights(fluxArray);
		double mse = CSLightCurveFitter.meanSquaredError(lightCurve, weights, solution);
		if(logger.isLoggable(Level.INFO)) {
			logger.info("writeResults(): rmse=" + Math.sqrt(mse));
		}
		OptResultsSpec spec = new OptResultsSpec();
		spec.setOrbitRadius(solution.getOrbitRadius());
		spec.setOptElapsedSeconds(elapsedSeconds);
		spec.setRmse(Math.sqrt(mse));
		spec.setParameters(solution.getOpacityFunctionParameters());
		spec.setMethod(optSpec.getMethod());
		File resultsFile = new File(resultsFilePath);
		SpecMapper.writeOptResultsSpec(resultsFile, spec);
		System.out.println("Wrote solution info to " + resultsFile);
	}
	
	private void validateSpec(OptSpec optSpec) {
		if(optSpec.getOrbitPeriod() <= 0) {
			throw new IllegalStateException("orbitPeriod must be greater than zero.");
		}
		if(optSpec.getOrbitRadius() < 1.0) {
			throw new IllegalStateException("orbitRadius must be at least 1.");			
		}
		if(optSpec.getLimbDarkeningParams() != null && optSpec.getLimbDarkeningParams().length == 0) {
			throw new IllegalStateException("limbDarkeningParams must be a non-empty array.");								
		}
	}

	private void writeData(LightCurvePoint[] lightCurve, Solution solution, String outFilePath) throws IOException {
		double[] obsFluxArray = LightCurvePoint.fluxArray(lightCurve);
		double[] modeledFlux = solution.produceModeledFlux();
		File file = new File(outFilePath);
		PrintWriter out = new PrintWriter(file);
		try {
			out.println("Timestamp,Flux,ModeledFlux");
			for(int i = 0; i < lightCurve.length; i++) {
				LightCurvePoint p = lightCurve[i];
				out.println(p.getTimestamp() + "," + obsFluxArray[i] + "," + modeledFlux[i]);
			}
		} finally {
			out.close();
		}		
		System.out.println("Wrote light curve data to " + file);
	}

	private SolutionSampler getSampler(Random random, double[] timestamps, double[] fluxArray, double peakFraction, LimbDarkeningParams ldParams, OptSpec optSpec, CommandLine cmdLine, File contextFile) throws Exception {
		AbstractOptMethod method = optSpec.getMethod();
		ParametricFluxFunctionSource ffs = method.createFluxFunctionSource(contextFile);
		SimulatedFluxSource fluxSource = this.getFluxSource(cmdLine, optSpec, peakFraction, timestamps, ldParams, contextFile);
		double orf = optSpec.getOrbitRadiusFlexibility();
		double logRadiusSD = Math.log(1 + orf);
		SolutionSampler ss = new SolutionSampler(
			random, 
			optSpec.getOrbitRadius(), 
			logRadiusSD, 
			fluxSource, 
			ffs
		);
		return ss;
	}
	
	private SimulatedFluxSource getFluxSource(CommandLine cmdLine, OptSpec optSpec, double peakFraction, double[] timestamps, LimbDarkeningParams ldParams, File context) {
		double inclineAngle = optSpec.getInclineAngle();
		double orbitalPeriod = optSpec.getOrbitPeriod();
		int widthPixels = optSpec.getWidthPixels();
		int heightPixels = optSpec.getHeightPixels();
		if(cmdLine.hasOption("angular")) {
			return new AngularFluxSource(timestamps, peakFraction, widthPixels, heightPixels, inclineAngle, orbitalPeriod, ldParams);
		}
		else {
			try {
				return new FastApproximateFluxSource(timestamps, ldParams, inclineAngle, orbitalPeriod, peakFraction, widthPixels, heightPixels);
			} catch(AngleUnsupportedException au) {
				throw new IllegalStateException("Cannot handle a rotation of " + au.getValue() + " radians with 'fast' optimization. Use -angular option instead.");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Options options = getOptions();
		try {
			CommandLine cmdLine = new PosixParser().parse(options, args, false);
			if(cmdLine.hasOption("help")) {
				printHelp(options);
			}
			else {
				new SolveLightCurve().run(cmdLine);
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
	
	protected static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("lc-opt [options] -i <lightcurve>.csv <opt-spec>.json", options);
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
		Option inCsvOption = OptionBuilder.withArgName("csv-file")
				.hasArg()
				.withDescription("Sets path of CSV file where input light curve data is read from.")
				.create("i");
		Option outCsvOption = OptionBuilder.withArgName("csv-file")
				.hasArg()
				.withDescription("Sets path of CSV file where estimated light curve data will be written.")
				.create("o");
		Option outResultsOption = OptionBuilder.withArgName("json-file")
				.hasArg()
				.withDescription("Sets path of JSON file where model properties will be written.")
				.create("or");
		Option outImageOption = OptionBuilder.withArgName("png-file")
				.hasArg()
				.withDescription("Sets path of PNG file where modeled transit image will be written.")
				.create("oi");
		Option outVideoOption = OptionBuilder.withArgName("mov-file")
				.hasArg()
				.withDescription("Sets path of MOV file where estimated transit video will be written.")
				.create("video");
		Option nsOption = OptionBuilder.withArgName("n")
				.hasArg()
				.withDescription("Sets the number of main optimizer iterations/steps. Default is " + DEF_MAX_ITERATIONS + ".")
				.create("noi");
		Option nagdOption = OptionBuilder.withArgName("n")
				.hasArg()
				.withDescription("Sets the number of approximate gradient descent (last push) iterations/steps. Default is " + DEF_MAX_AGD_ITERATIONS + ".")
				.create("nagd");
		Option popOption = OptionBuilder.withArgName("n")
				.hasArg()
				.withDescription("Sets the optimizer's population size. Default is " + DEF_POP_SIZE + ".")
				.create("pop");
		Option logOption = OptionBuilder.withArgName("level")
				.hasArg()
				.withDescription("Sets the java.util.logging level.")
				.create("log");
		Option angOption = OptionBuilder
				.hasArg(false)
				.withDescription("Disables 'fast' approximate optimization, and performs accurate rotations.")
				.create("angular");
		Option videoDurationOption = OptionBuilder.withArgName("seconds")
				.hasArg()
				.withDescription("Sets the video duration in seconds. Default is " + DEF_VIDEO_DURATION + ".")
				.create("vd");
		
		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inCsvOption);
		options.addOption(outCsvOption);
		options.addOption(outResultsOption);
		options.addOption(outImageOption);
		options.addOption(outVideoOption);
		options.addOption(seedOption);
		options.addOption(timeUnitOption);
		options.addOption(logOption);
		options.addOption(nsOption);
		options.addOption(nagdOption);
		options.addOption(popOption);
		options.addOption(angOption);		
		options.addOption(videoDurationOption);

		return options;
	}
}
