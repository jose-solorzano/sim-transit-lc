package jhs.lc.tools.inputs;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.opt.nn.ActivationFunctionType;
import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.lc.opt.nn.NeuralNetworkMetaInfo;
import jhs.lc.opt.nn.OutputType;
import jhs.math.nn.ActivationFunction;
import jhs.math.nn.ActivationFunctionFactory;
import jhs.math.nn.DefaultNeuralStructure;
import jhs.math.nn.NeuralNetwork;
import jhs.math.nn.NeuralNetworkStructure;
import jhs.math.nn.PlainNeuralNetwork;
import jhs.math.nn.aa.SigmoidActivationFunction;
import jhs.math.nn.aa.SignActivationFunction;
import jhs.math.util.MathUtil;

public class NeuralNetworkSpec {
	private static final Logger logger = Logger.getLogger(NeuralNetworkSpec.class.getName());
	private static final int NUM_TESTS = 999;
	
	private OutputType outputType;	
	private NeuralLayerSpec[] hiddenLayers;
	private ActivationFunctionType outputActivationFunction;

	private double imageOpacityExpectation = 0.05;
	
	private String comment;
	
	public final String getComment() {
		return comment;
	}

	public final void setComment(String comment) {
		this.comment = comment;
	}

	public final double getImageOpacityExpectation() {
		return imageOpacityExpectation;
	}

	public final void setImageOpacityExpectation(double imageOpacityExpectation) {
		this.imageOpacityExpectation = imageOpacityExpectation;
	}

	@JsonProperty(required = true)
	public final OutputType getOutputType() {
		return outputType;
	}

	public final void setOutputType(OutputType outputType) {
		this.outputType = outputType;
	}

	public final ActivationFunctionType getOutputActivationFunction() {
		return outputActivationFunction;
	}

	public final void setOutputActivationFunction(ActivationFunctionType outputActivationFunction) {
		this.outputActivationFunction = outputActivationFunction;
	}

	@JsonProperty(required = true)
	public final NeuralLayerSpec[] getHiddenLayers() {
		return hiddenLayers;
	}

	public final void setHiddenLayers(NeuralLayerSpec[] hiddenLayers) {
		this.hiddenLayers = hiddenLayers;
	}

	public NeuralNetworkMetaInfo createNeuralMetaInfo(File context, InputFilterFactory inputFilterFactory, double imageWidth, double imageHeight) throws Exception {
		int numOutputs = 1;
		int numVars = inputFilterFactory.getNumTransformedInputs();
		ActivationFunctionFactory afFactory = this.getActivationFunctionFactory();
		NeuralLayerSpec[] layerSpecs = this.hiddenLayers;
		if(layerSpecs == null) {
			throw new IllegalStateException("No hidden layer specifications.");
		}
		int[] hiddenLayerCounts = new int[layerSpecs.length];
		int[] maxInputsPerUnitCounts = new int[layerSpecs.length];
		for(int i = 0; i < layerSpecs.length; i++) {
			NeuralLayerSpec layerSpec = layerSpecs[i];
			int numUnits = layerSpec.getNumUnits();
			if(numUnits <= 0) {
				throw new IllegalStateException("Layer " + i + " has " + numUnits + " units.");
			}
			int maxInputsPerUnit = layerSpec.getMaxInputsPerUnit();
			if(maxInputsPerUnit <= 0) {
				throw new IllegalStateException("Layer " + i + " has " + maxInputsPerUnit + " max inputs per unit.");				
			}
			maxInputsPerUnitCounts[i] = maxInputsPerUnit;
			hiddenLayerCounts[i] = numUnits;
		}		
		NeuralNetworkStructure structure = DefaultNeuralStructure.create(hiddenLayerCounts, numOutputs, numVars, afFactory, maxInputsPerUnitCounts);		
		if(logger.isLoggable(Level.INFO)) {
			logger.info("Number of parameters in neural network structure: " + structure.getNumParameters());
		}
		double outputBias = this.estimateOutputBias(structure, imageWidth, imageHeight, inputFilterFactory);
		return new NeuralNetworkMetaInfo(structure, outputType, outputBias);
	}
	
	private ActivationFunctionFactory getActivationFunctionFactory() {
		final NeuralLayerSpec[] layerSpecs = this.hiddenLayers;
		if(layerSpecs == null) {
			throw new IllegalStateException("No hidden layer specifications.");
		}
		ActivationFunctionType oafType = this.outputActivationFunction;
		if(oafType == null) {
			oafType = ActivationFunctionType.SIMPLE_MAX;
		}
		final ActivationFunctionType oafTypeFinal = oafType;
		return new ActivationFunctionFactory() {			
			@Override
			public ActivationFunction createActivationFunction(int numInputs, int layerIndex, int unitIndex) {
				NeuralLayerSpec nls = layerSpecs[layerIndex];
				ActivationFunctionType[] aft = nls.getActivationFunctions();
				if(aft == null || aft.length == 0) {
					return layerIndex == layerSpecs.length - 1 ? new SignActivationFunction(numInputs) : new SigmoidActivationFunction(numInputs);
				}
				ActivationFunctionType afType = aft[(unitIndex + layerIndex) % aft.length];
				return afType.getActivationFunction(numInputs);
			}

			@Override
			public ActivationFunction createOutputActivationFunction(int numInputs) {
				return oafTypeFinal.getActivationFunction(numInputs);
			}
		};
	}
	
	private double estimateOutputBias(NeuralNetworkStructure structure, double imageWidth, double imageHeight, InputFilterFactory inputFilterFactory) {
		double ioe = this.imageOpacityExpectation;
		if(ioe < 0 || ioe > 1.0 || Double.isNaN(ioe)) {
			throw new IllegalStateException("Image opacity expectation: " + ioe);
		}
		Random random = new Random(1759); 
		int numInputFilterParams = inputFilterFactory.getNumParameters();
		int numParams = structure.getNumParameters();
		double[] results = new double[NUM_TESTS];
		for(int i = 0; i < NUM_TESTS; i++) {
			double[] ifParams = MathUtil.sampleGaussian(random, 1.0, numInputFilterParams);
			InputFilter inputFilter = inputFilterFactory.createInputFilter(ifParams);
			double[] randomParams = MathUtil.sampleGaussian(random, 1.0, numParams);
			NeuralNetwork nn = new PlainNeuralNetwork(structure, randomParams);
			double x = -imageWidth / 2 + random.nextDouble() * imageWidth;
			double y = -imageHeight / 2 + random.nextDouble() * imageHeight;
			double[] inputData = inputFilter.getInput(x, y);
			double[] activations = nn.activations(inputData);
			if(activations.length != 1) {
				throw new IllegalStateException("Expecting one output.");
			}
			results[i] = activations[0];
		}
		Arrays.sort(results);
		double median = results[results.length / 2];
		int oi = (int) Math.floor(results.length * (1 - ioe));
		if(oi < 0) {
			oi = 0;
		}
		else if(oi >= results.length) {
			oi = results.length - 1;
		}
		double threshold = results[oi];
		double bias = median - threshold;
		if(logger.isLoggable(Level.INFO)) {
			logger.info("Estimated output bias of " + bias + " for image opacity expectation of " + ioe + ".");
		}		
		return bias;
	}
}
