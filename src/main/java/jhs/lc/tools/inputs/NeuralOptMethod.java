package jhs.lc.tools.inputs;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.opt.nn.ActivationFunctionType;
import jhs.lc.opt.nn.InputType;
import jhs.lc.opt.nn.NNFluxFunctionSource;
import jhs.lc.opt.nn.NNFluxOrOpacityFunction;
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

import com.fasterxml.jackson.annotation.JsonProperty;

public class NeuralOptMethod extends AbstractOptMethod {
	private static final Logger logger = Logger.getLogger(NeuralOptMethod.class.getName());
	private static final int NUM_TESTS = 999;
	
	private int numNetworks;
	private double imageWidth;
	private double imageHeight;
	private OutputType outputType;	
	private NeuralLayerSpec[] hiddenLayers;
	private ActivationFunctionType outputActivationFunction;

	private double imageOpacityExpectation = 0.05;
	private InputType inputType = InputType.QUADRATIC;

	@JsonProperty(required = true)
	public final int getNumNetworks() {
		return numNetworks;
	}

	public final void setNumNetworks(int numNetworks) {
		this.numNetworks = numNetworks;
	}

	@JsonProperty(required = true)
	public final double getImageWidth() {
		return imageWidth;
	}

	public final void setImageWidth(double imageWidth) {
		this.imageWidth = imageWidth;
	}

	@JsonProperty(required = true)
	public final double getImageHeight() {
		return imageHeight;
	}

	public final void setImageHeight(double imageHeight) {
		this.imageHeight = imageHeight;
	}

	public final double getImageOpacityExpectation() {
		return imageOpacityExpectation;
	}

	public final void setImageOpacityExpectation(double imageOpacityExpectation) {
		this.imageOpacityExpectation = imageOpacityExpectation;
	}

	@JsonProperty(required = false)
	public final InputType getInputType() {
		return inputType;
	}

	public final void setInputType(InputType inputType) {
		this.inputType = inputType;
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

	@Override
	public ParametricFluxFunctionSource createFluxFunctionSource(File context) throws Exception {
		int numOutputs = 1;
		InputType inputType = this.inputType;
		int numVars = NNFluxOrOpacityFunction.getNumInputs(inputType);
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
		int num = this.numNetworks;
		if(num <= 0) {
			throw new IllegalStateException("Invalid number of neural networks: " + num + ".");
		}
		if(logger.isLoggable(Level.INFO)) {
			logger.info("Number of parameters in neural network structure: " + structure.getNumParameters());
		}
		double outputBias = this.estimateOutputBias(structure, imageWidth, imageHeight, inputType);
		return new NNFluxFunctionSource(structure, inputType, outputType, imageWidth, imageHeight, num, outputBias);
	}
	
	private ActivationFunctionFactory getActivationFunctionFactory() {
		final NeuralLayerSpec[] layerSpecs = this.hiddenLayers;
		if(layerSpecs == null) {
			throw new IllegalStateException("No hidden layer specifications.");
		}
		ActivationFunctionType oafType = this.outputActivationFunction;
		if(oafType == null) {
			oafType = ActivationFunctionType.MIN;
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
	
	private double estimateOutputBias(NeuralNetworkStructure structure, double imageWidth, double imageHeight, InputType inputType) {
		double ioe = this.imageOpacityExpectation;
		if(ioe < 0 || ioe > 1.0 || Double.isNaN(ioe)) {
			throw new IllegalStateException("Image opacity expectation: " + ioe);
		}
		Random random = new Random(1759); 
		int numParams = structure.getNumParameters();
		double[] results = new double[NUM_TESTS];
		for(int i = 0; i < NUM_TESTS; i++) {
			double[] randomParams = MathUtil.sampleGaussian(random, 1.0, numParams);
			NeuralNetwork nn = new PlainNeuralNetwork(structure, randomParams);
			double x = -imageWidth / 2 + random.nextDouble() * imageWidth;
			double y = -imageHeight / 2 + random.nextDouble() * imageHeight;
			double[] inputData = NNFluxOrOpacityFunction.getInputData(x, y, inputType);
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
