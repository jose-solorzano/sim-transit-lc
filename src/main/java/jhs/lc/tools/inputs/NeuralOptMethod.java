package jhs.lc.tools.inputs;

import java.io.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.opt.nn.ActivationFunctionType;
import jhs.lc.opt.nn.InputType;
import jhs.lc.opt.nn.NNFluxFunctionSource;
import jhs.lc.opt.nn.NNFluxOrOpacityFunction;
import jhs.lc.opt.nn.OutputType;
import jhs.math.nn.ActivationFunction;
import jhs.math.nn.ActivationFunctionFactory;
import jhs.math.nn.FullyConnectedNeuralStructure;
import jhs.math.nn.NeuralNetworkStructure;
import jhs.math.nn.aa.SigmoidActivationFunction;
import jhs.math.nn.aa.SignActivationFunction;

public class NeuralOptMethod extends AbstractOptMethod {
	private int numNetworks;
	private double imageWidth;
	private double imageHeight;
	private InputType inputType = InputType.QUADRATIC;
	private OutputType outputType;	
	private NeuralLayerSpec[] hiddenLayers;
	private ActivationFunctionType outputActivationFunction;

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
		for(int i = 0; i < layerSpecs.length; i++) {
			int numUnits = layerSpecs[i].getNumUnits();
			if(numUnits <= 0) {
				throw new IllegalStateException("Layer " + i + " has " + numUnits + " units.");
			}
			hiddenLayerCounts[i] = numUnits;
		}		
		NeuralNetworkStructure structure = FullyConnectedNeuralStructure.create(hiddenLayerCounts, numOutputs, numVars, afFactory);
		int num = this.numNetworks;
		if(num <= 0) {
			throw new IllegalStateException("Invalid number of neural networks: " + num + ".");
		}
		return new NNFluxFunctionSource(structure, inputType, outputType, imageWidth, imageHeight, num);
	}
	
	private ActivationFunctionFactory getActivationFunctionFactory() {
		final NeuralLayerSpec[] layerSpecs = this.hiddenLayers;
		if(layerSpecs == null) {
			throw new IllegalStateException("No hidden layer specifications.");
		}
		ActivationFunctionType oafType = this.outputActivationFunction;
		if(oafType == null) {
			oafType = ActivationFunctionType.SIMPLE_MIN;
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
}
