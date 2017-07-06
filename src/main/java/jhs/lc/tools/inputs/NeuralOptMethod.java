package jhs.lc.tools.inputs;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.opt.nn.ActivationFunctionType;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.lc.opt.nn.InputFilterType;
import jhs.lc.opt.nn.NNFluxFunctionSource;
import jhs.lc.opt.nn.NNFluxOrOpacityFunction;
import jhs.lc.opt.nn.OutputType;
import jhs.math.nn.ActivationFunction;
import jhs.math.nn.ActivationFunctionFactory;
import jhs.math.nn.FullyConnectedNeuralStructure;
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
	
	private double imageWidth;
	private double imageHeight;

	private InputFilterType inputFilter = InputFilterType.PLAIN;
	
	private NeuralNetworkSpec[] networks;

	@JsonProperty(required = true)
	public final NeuralNetworkSpec[] getNetworks() {
		return networks;
	}

	public final void setNetworks(NeuralNetworkSpec[] networks) {
		this.networks = networks;
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

	@JsonProperty(required = false)
	public final InputFilterType getInputFilter() {
		return inputFilter;
	}

	public final void setInputFilter(InputFilterType inputType) {
		this.inputFilter = inputType;
	}

	@Override
	public ParametricFluxFunctionSource createFluxFunctionSource(File context) throws Exception {
		int numOutputs = 1;
		InputFilterType inputType = this.inputFilter;
		InputFilterFactory inputFilterFactory = inputType.getFactory();
		NeuralNetworkStructure structure = FullyConnectedNeuralStructure.create(hiddenLayerCounts, numOutputs, numVars, afFactory);
		int num = this.numNetworks;
		if(num <= 0) {
			throw new IllegalStateException("Invalid number of neural networks: " + num + ".");
		}
		if(logger.isLoggable(Level.INFO)) {
			logger.info("Number of parameters in neural network structure: " + structure.getNumParameters());
		}
		double outputBias = this.estimateOutputBias(structure, imageWidth, imageHeight, inputType);
		return new NNFluxFunctionSource(structures, inputType, outputTypes, outputBiases, imageWidth, imageHeight);
	}
	
	
}
