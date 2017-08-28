package jhs.lc.tools;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.media.protocol.FileTypeDescriptor;

import jhs.lc.data.DataSet;
import jhs.lc.data.LightCurve;
import jhs.lc.data.LightCurvePoint;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.jmf.BufferedImageVideoProducer;
import jhs.lc.opt.LightCurveFitter;
import jhs.lc.opt.EvaluationInfo;
import jhs.lc.opt.LightCurveMatchingFeatureSource;
import jhs.lc.opt.PrimaryLossFunction;
import jhs.lc.opt.Solution;
import jhs.lc.opt.SolutionSampler;
import jhs.lc.sims.AngleUnsupportedException;
import jhs.lc.sims.AngularFluxSource;
import jhs.lc.sims.FastApproximateFluxSource;
import jhs.lc.sims.SimulatedFluxSource;
import jhs.lc.tools.inputs.AbstractOptMethod;
import jhs.lc.tools.inputs.OptResultsSpec;
import jhs.lc.tools.inputs.OptSpec;
import jhs.lc.tools.inputs.SolutionSpec;
import jhs.lc.tools.inputs.SpecMapper;
import jhs.math.util.ListUtil;
import jhs.math.util.MathUtil;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.math.MathException;
import org.apache.commons.math.optimization.RealPointValuePair;

public class SolveLightCurve extends AbstractTool {
	private static final Logger logger = Logger.getLogger(SolveLightCurve.class.getName());
	
	private static final int DEF_MAX_WP_ITERATIONS = 50;
	private static final int DEF_MAX_ITERATIONS = 300;
	private static final int DEF_MAX_CONS_ITERATIONS = 0;
	private static final int DEF_MAX_AGD_ITERATIONS = 50;	
	private static final int DEF_POP_SIZE = 100;
	private static final int DEF_TEST_DEPICT_NUM_PIXELS = 40000;
	
	private static final double DEF_VIDEO_DURATION = 60;

	private void run(CommandLine cmdLine) throws Exception {
		String[] args = cmdLine.getArgs();
		this.configureLoggingLevel(cmdLine);
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
		long seed = seedText == null ? 201707081111L : Long.parseLong(seedText);
		Random random = new Random(seed * 7 - 11);
		
		//double minIndex = LimbDarkeningParams.minIndex(fluxArray);
		//double peakFraction = (minIndex + 0.5) / fluxArray.length;
		//logger.info("Transit peak estimated to occur at index " + minIndex + " of the flux sequence, whose length is " + fluxArray.length + ".");
		LimbDarkeningParams ldParams = optSpec.getLimbDarkeningParams() == null ? LimbDarkeningParams.SUN : new LimbDarkeningParams(optSpec.getLimbDarkeningParams());
		SolutionSampler sampler = this.getSampler(random, timestamps, fluxArray, ldParams, optSpec, cmdLine, specFile);
		int populationSize = this.getOptionInt(cmdLine, "pop", DEF_POP_SIZE);
		int numWarmUpIterations = this.getOptionInt(cmdLine, "nwi", DEF_MAX_WP_ITERATIONS);
		int numClusteringIterations = this.getOptionInt(cmdLine, "noi", DEF_MAX_ITERATIONS);
		int numPostClusteringIterations = this.getOptionInt(cmdLine, "npci", DEF_MAX_CONS_ITERATIONS);
		int numGradientDescentIterations = this.getOptionInt(cmdLine, "nagd", DEF_MAX_AGD_ITERATIONS);
		
		String warmUpDepictionsPath = cmdLine.getOptionValue("owpz");
		String clusteringDepictionsPath = cmdLine.getOptionValue("ocz");
		
		logger.info("Number of optimization parameters: " + sampler.getNumParameters() + ".");
		logger.info("Population size: " + populationSize + ".");
		logger.info("Max iterations: " + numClusteringIterations + ".");
		logger.info("Initial orbit radius: " + optSpec.getOrbitRadius());
		long time1 = System.currentTimeMillis();
		Solution solution = this.solve(optSpec, lightCurve, sampler, populationSize, numWarmUpIterations, numClusteringIterations, numPostClusteringIterations, numGradientDescentIterations, warmUpDepictionsPath, clusteringDepictionsPath);		
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
			int depictionNumPixels = this.getOptionInt(cmdLine, "oinp", DEF_OUT_NUM_PIXELS);
			this.writeTransitImageFile(transitImageFileName, timestamps, solution, depictionNumPixels);
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
			this.writeVideo(videoFileName, timeCaption, videoDuration, timestamps, solution, optSpec, ldParams);
		}
	}
	
	private void writeTransitImageFile(String imageFileName, double[] timestamps, Solution solution, int depictionNumPixels) throws IOException {
		BufferedImage image = solution.produceDepiction(depictionNumPixels);
		File outFile = new File(imageFileName);
		try(OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), 100000)) {
			ImageIO.write(image, "png", out);
		}
		System.out.println("Wrote " + outFile);
	}	
	
	private void writeVideo(String videoFileName, String timeCaption, double videoDuration, double[] timestamps, Solution solution, OptSpec optSpec, LimbDarkeningParams ldParams) throws Exception {
		int numPixels = DEF_OUT_NUM_PIXELS;
		Iterator<BufferedImage> iterator = solution.produceModelImages(optSpec.getInclineAngle(), optSpec.getOrbitPeriod(), ldParams, timestamps, solution.getPeakFraction(), timeCaption, numPixels);
		double frameRate = timestamps.length / videoDuration;
		Dimension dimension = solution.suggestImageDimension(numPixels);
        BufferedImageVideoProducer producer = new BufferedImageVideoProducer(dimension.width, dimension.height, (float) frameRate, FileTypeDescriptor.QUICKTIME);
        File outFile = new File(videoFileName);
        producer.writeToFile(outFile, iterator);
        System.out.println("Wrote " + outFile);		
	}
		
	private Solution solve(OptSpec optSpec, LightCurvePoint[] lightCurve, SolutionSampler sampler, int populationSize, int numWarmUpIterations, int numClusteringIterations, int numPostClusteringIterations, int numGradientDescentIterations, String warmUpDepictionsPath, String clusteringDepictionsPath) throws MathException {
		LightCurveFitter fitter = new LightCurveFitter(sampler, populationSize) {
			@Override
			protected void informProgress(String stage, int iteration, double error) {
				if(logger.isLoggable(Level.INFO)) {
					logger.info("[" + stage + "] Iteration " + iteration + ": error=" + error);
				}
			}

			@Override
			protected void informEndOfWarmUpPhase(SolutionSampler sampler, List<RealPointValuePair> pointValues) {
				if(warmUpDepictionsPath != null) {
					dumpModelDepictionsToZipFile(optSpec, sampler, lightCurve, pointValues, warmUpDepictionsPath);
				}
			}

			@Override
			protected void informEndOfClusteringPhase(SolutionSampler sampler, List<RealPointValuePair> pointValues) {
				if(clusteringDepictionsPath != null) {
					dumpModelDepictionsToZipFile(optSpec, sampler, lightCurve, pointValues, clusteringDepictionsPath);					
				}
			}
		};
		// TODO: configure with options
		fitter.setInitialPoolSize(populationSize);
		fitter.setCircuitShuffliness(0.1);
		fitter.setDisplacementFactor(0.04);
		fitter.setExpansionFactor(2.0);
		fitter.setMaxCSWarmUpIterations(numWarmUpIterations);
		fitter.setMaxCSIterationsWithClustering(numClusteringIterations);
		fitter.setMaxExtraCSIterations(numPostClusteringIterations);
		fitter.setMaxEliminationIterations(0);
		fitter.setMaxGradientDescentIterations(numGradientDescentIterations);
				
		Solution solution = fitter.optimize(lightCurve);
		return solution;
	}
	
	private void dumpModelDepictionsToZipFile(OptSpec optSpec, SolutionSampler sampler, LightCurvePoint[] lightCurve, List<RealPointValuePair> pointValues, String zipFilePath) {
		if(logger.isLoggable(Level.INFO)) {
			logger.info("Dumping " + pointValues.size() + " transit model depictions to " + zipFilePath);
		}
		double[] targetFluxArray = LightCurvePoint.fluxArray(lightCurve);
		LightCurveMatchingFeatureSource fs = new LightCurveMatchingFeatureSource(targetFluxArray);
		try {
			try(OutputStream out = new FileOutputStream(zipFilePath)) {
				try(ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(out, 100000))) {
					zout.putNextEntry(new ZipEntry("optspec.json"));
					try {
						SpecMapper.writeObject(zout, optSpec);						
					} finally {
						zout.closeEntry();
					}
					int count = 0;
					for(RealPointValuePair pv : pointValues) {
						Solution solution = sampler.parametersAsSolution(pv.getPointRef());
						double[] testFluxArray = solution.produceModeledFlux().getFluxArray();
						double[] lossFeatures = fs.getFeatureValues(testFluxArray);
						BufferedImage depiction = solution.produceDepiction(DEF_TEST_DEPICT_NUM_PIXELS, false);
						String entryName = "transit-" + (count++);
						ZipEntry folderEntry = new ZipEntry(entryName);
						zout.putNextEntry(folderEntry);
						zout.putNextEntry(new ZipEntry(entryName + "/transit.png"));
						try {
							ImageIO.write(depiction, "png", zout);
						} finally {
							zout.closeEntry();
						}
						zout.putNextEntry(new ZipEntry(entryName + "/info.json"));
						SolutionSpec spec = new SolutionSpec(fs.rmse(testFluxArray), lossFeatures, pv.getPointRef(), solution.getOrbitRadius());
						try {
							SpecMapper.writeObject(zout, spec);
						} finally {
							zout.closeEntry();
						}
						if(logger.isLoggable(Level.INFO)) {
							logger.info("Wrote " + entryName + " to zip file.");
						}
					}
				}
			}
		} catch(IOException ioe) {
			logger.log(Level.SEVERE, "Unable to dump model depictions.", ioe);
		}		
	}
	
	private void writeResults(String resultsFilePath, OptSpec optSpec, SolutionSampler sampler, LightCurvePoint[] lightCurve, Solution solution, double[] fluxArray, double elapsedSeconds) throws Exception {
		EvaluationInfo ei = sampler.getEvaluationInfo(fluxArray, solution);
		double[] ofParameters = solution.getOpacityFunctionParameters();
		double paramStdev = MathUtil.standardDev(ofParameters, 0);
		if(logger.isLoggable(Level.INFO)) {
			logger.info("orbitRadius=" + solution.getOrbitRadius() + " (from " + optSpec.getOrbitRadius() + ").");
			logger.info("rmse=" + ei.getRmse());
			logger.info("trendLoss=" + ei.getTrendLoss());
			logger.info("trendChangeLoss=" + ei.getTrendChangeLoss());
			logger.info("Parameter SD: " + paramStdev);
		}
		OptResultsSpec spec = new OptResultsSpec();
		spec.setOrbitRadius(solution.getOrbitRadius());
		spec.setOptElapsedSeconds(elapsedSeconds);
		spec.setRmse(ei.getRmse());
		spec.setParameters(ofParameters);
		spec.setParamStandardDev(paramStdev);
		spec.setMethod(optSpec.getMethod());
		spec.setTransitFunctionAsText(solution.getBrightnessFunction().toString());
		File resultsFile = new File(resultsFilePath);
		SpecMapper.writeObject(resultsFile, spec);
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
		double[] modeledFlux = solution.produceModeledFlux().getFluxArray();
		double[] obsTrendChangeProfile = PrimaryLossFunction.trendChangeProfile(obsFluxArray);
		double[] modeledTrendChangeProfile = PrimaryLossFunction.trendChangeProfile(modeledFlux);
		File file = new File(outFilePath);
		PrintWriter out = new PrintWriter(file);
		try {
			out.println("Timestamp,Flux,ModeledFlux,TCPFlux,TCPModeledFlux");
			for(int i = 0; i < lightCurve.length; i++) {
				LightCurvePoint p = lightCurve[i];
				out.println(p.getTimestamp() + "," + obsFluxArray[i] + "," + modeledFlux[i] + "," + obsTrendChangeProfile[i] + "," + modeledTrendChangeProfile[i]);
			}
		} finally {
			out.close();
		}		
		System.out.println("Wrote light curve data to " + file);
	}

	private SolutionSampler getSampler(Random random, double[] timestamps, double[] fluxArray, LimbDarkeningParams ldParams, OptSpec optSpec, CommandLine cmdLine, File contextFile) throws Exception {
		AbstractOptMethod method = optSpec.getMethod();
		ParametricTransitFunctionSource ffs = method.createFluxFunctionSource(contextFile);
		SimulatedFluxSource fluxSource = this.getFluxSource(cmdLine, optSpec, timestamps, ldParams, contextFile);
		double baseRadius = optSpec.getOrbitRadius();
		double orf = optSpec.getOrbitRadiusFlexibility();
		if(orf < 0) {
			throw new IllegalStateException("orbitRadiusFlexibility cannot be negative.");
		}
		double minRadius = orf == 0 ? baseRadius : baseRadius / (1 + orf);
		double maxRadius = orf == 0 ? baseRadius : baseRadius * (1 + orf);
		logger.info("minOrbitRadius=" + minRadius + ", maxOrbitRadius=" + maxRadius);
		SolutionSampler ss = new SolutionSampler(
			random, 
			fluxSource, 
			ffs,
			minRadius,
			maxRadius
		);
		return ss;
	}
	
	private SimulatedFluxSource getFluxSource(CommandLine cmdLine, OptSpec optSpec, double[] timestamps, LimbDarkeningParams ldParams, File context) {
		double inclineAngle = optSpec.getInclineAngle();
		double orbitalPeriod = optSpec.getOrbitPeriod();
		int widthPixels = optSpec.getWidthPixels();
		int heightPixels = optSpec.getHeightPixels();
		logger.info("getFluxSource(): inclineAngle=" + inclineAngle + ", orbitalPeriod=" + orbitalPeriod + ", withPixels=" + widthPixels + ", heightPixels=" + heightPixels);
		if(cmdLine.hasOption("angular")) {
			return new AngularFluxSource(timestamps, widthPixels, heightPixels, inclineAngle, orbitalPeriod, ldParams);
		}
		else {
			try {
				return new FastApproximateFluxSource(timestamps, ldParams, inclineAngle, orbitalPeriod, widthPixels, heightPixels);
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
		Option oinpOption = OptionBuilder.withArgName("n")
				.hasArg()
				.withDescription("Sets the number of pixels of the transit depiction image. Default is " + DEF_OUT_NUM_PIXELS + ".")
				.create("oinp");
		Option outVideoOption = OptionBuilder.withArgName("mov-file")
				.hasArg()
				.withDescription("Sets path of MOV file where modeled transit video will be written.")
				.create("video");
		Option nsOption = OptionBuilder.withArgName("n")
				.hasArg()
				.withDescription("Sets the number of main optimizer iterations/steps. Default is " + DEF_MAX_ITERATIONS + ".")
				.create("noi");
		Option nwiOption = OptionBuilder.withArgName("n")
				.hasArg()
				.withDescription("Sets the number of warmup iterations/steps used to produce initial shapes. Default is " + DEF_MAX_WP_ITERATIONS + ".")
				.create("nwi");
		Option npcOption = OptionBuilder.withArgName("n")
				.hasArg()
				.withDescription("Sets the number of post-clustering (consolidation) iterations/steps. Default is " + DEF_MAX_CONS_ITERATIONS + ".")
				.create("npci");
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
		Option owpzOption = OptionBuilder.withArgName("zip-file")
				.hasArg()
				.withDescription("Sets the path of the ZIP file where post-warm-up model depictions are written. This is used in troubleshooting and optimizer analysis.")
				.create("owpz");
		Option oczOption = OptionBuilder.withArgName("zip-file")
				.hasArg()
				.withDescription("Sets the path of the ZIP file where post-clustering model depictions are written. This is used in troubleshooting and optimizer analysis.")
				.create("ocz");
		
		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inCsvOption);
		options.addOption(outCsvOption);
		options.addOption(outResultsOption);
		options.addOption(outImageOption);
		options.addOption(oinpOption);
		options.addOption(outVideoOption);
		options.addOption(seedOption);
		options.addOption(timeUnitOption);
		options.addOption(logOption);
		options.addOption(nwiOption);
		options.addOption(nsOption);
		options.addOption(npcOption);
		options.addOption(nagdOption);
		options.addOption(popOption);
		options.addOption(angOption);		
		options.addOption(videoDurationOption);
		options.addOption(owpzOption);
		options.addOption(oczOption);

		return options;
	}
}
