package jhs.math.nn;

import java.util.ArrayList;
import java.util.List;

import jhs.math.util.ArrayUtil;

public class DefaultNeuralStructure implements NeuralNetworkStructure, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	// Layers go from the first hidden layer, up to and including the output layer.
	private final FCLayer[] layers;
	private final int numParameters;
	private final int numInputs;
	private final int numOutputs;
	private final int[] layerOfParam;
	private final int[] neuronIndexOfParam;

	private DefaultNeuralStructure(FCLayer[] layers, int numParameters, int numInputs, int numOutputs, int[] layerOfParam, int[] neuronIndexOfParam) {
		this.layers = layers;
		this.numParameters = numParameters;
		this.numInputs = numInputs;
		this.numOutputs = numOutputs;
		this.layerOfParam = layerOfParam;
		this.neuronIndexOfParam = neuronIndexOfParam;
	}

	public static DefaultNeuralStructure create(int[] hiddenLayers, int numOutputs, int numVars, ActivationFunctionFactory afFactory) {		
		return create(hiddenLayers, numOutputs, numVars, afFactory, null);
	}

	public static DefaultNeuralStructure create(int[] hiddenLayers, int numOutputs, int numVars, ActivationFunctionFactory afFactory, int[] maxInputsPerUnit) {		
		int numHiddenLayers = hiddenLayers.length;
		int numLayers = numHiddenLayers + 1;
		FCLayer[] layers = new FCLayer[numLayers];
		int paramIndex = 0;
		int priorLayerNumUnits = numVars;
		List<Integer> layerOfParamList = new ArrayList<Integer>();
		List<Integer> neuronIndexOfParamList = new ArrayList<Integer>();
		for(int layerIndex = 0; layerIndex < numLayers; layerIndex++) {
			boolean outputLayer = layerIndex == numLayers - 1;
			int numNeuronsThisLayer = outputLayer ? numOutputs : hiddenLayers[layerIndex];
			int numInputsOfLayer = maxInputsPerUnit == null || outputLayer ? priorLayerNumUnits : Math.min(priorLayerNumUnits, maxInputsPerUnit[layerIndex]);
			if(numInputsOfLayer <= 0) {
				throw new IllegalArgumentException("Layer " + layerIndex + " has " + numInputsOfLayer + " inputs.");
 			}
			FCNeuron[] neurons = new FCNeuron[numNeuronsThisLayer];
			int neuronInputIndex = 0;
			int maxInputIndex = priorLayerNumUnits - numInputsOfLayer;
			int inputIndexIncrement = numNeuronsThisLayer <= 1 ? 0 : maxInputIndex / (numNeuronsThisLayer - 1);
			if(inputIndexIncrement <= 0) {
				inputIndexIncrement = 1;
			}
			for(int j = 0; j < numNeuronsThisLayer; j++) {
				int firstNeuronParamIndex = paramIndex;
				ActivationFunction af = outputLayer ? afFactory.createOutputActivationFunction(numInputsOfLayer) : afFactory.createActivationFunction(numInputsOfLayer, layerIndex, j);
				int numParams = af.getNumParameters(numInputsOfLayer);
				for(int p = 0; p < numParams; p++) {
					neuronIndexOfParamList.add(j);
					layerOfParamList.add(layerIndex);
					paramIndex++;
				}
				neurons[j] = new FCNeuron(neuronInputIndex, numInputsOfLayer, firstNeuronParamIndex, af);
				neuronInputIndex += inputIndexIncrement;
				if(neuronInputIndex + numInputsOfLayer > priorLayerNumUnits) {
					neuronInputIndex = 0;
				}
			}			
			layers[layerIndex] = new FCLayer(neurons);
			priorLayerNumUnits = numNeuronsThisLayer;
		}
		int[] layerOfParam = ArrayUtil.fromIntCollection(layerOfParamList);
		int[] neuronIndexOfParam = ArrayUtil.fromIntCollection(neuronIndexOfParamList);
		return new DefaultNeuralStructure(layers, paramIndex, numVars, numOutputs, layerOfParam, neuronIndexOfParam);
	}
	
	@Override
	public final Layer[] getLayers() {
		return this.layers;
	}

	@Override
	public final int getNumInputsOfLayer(int layerIndex) {
		if(layerIndex == 0) {
			return this.numInputs;
		}
		FCLayer prevLayer = this.layers[layerIndex - 1];
		return prevLayer.neurons.length;
	}

	@Override
	public final Layer getLayer(int layerIndex) {
		return this.layers[layerIndex];
	}

	FCNeuron getNeuron(int layerIndex, int unitIndex) {
		FCLayer layer = this.layers[layerIndex];
		return layer.neurons[unitIndex];
	}
	
	public final void populateLayerParameters(double[] parameters, int layerIndex, double[][] layerParamMatrix) {
		FCLayer layer = this.layers[layerIndex];
		FCNeuron[] neurons = layer.neurons;
		int numUnits = neurons.length;
		if(numUnits != layerParamMatrix.length) {
			throw new IllegalArgumentException("Mismatch between number of units in layer and parameter matrix width.");			
		}
		int priorLayerNumUnits = layerIndex == 0 ? this.numInputs : this.layers[layerIndex - 1].neurons.length;
		int numParamsPerNeuron = priorLayerNumUnits + 1;
		for(int u = 0; u < numUnits; u++) {
			double[] unitInputParams = layerParamMatrix[u];
			if(unitInputParams.length != numParamsPerNeuron) {
				throw new IllegalArgumentException("Mismatch between number of units in prior layer and number of input params for unit " + u + ".");
			}
			FCNeuron neuron = neurons[u];
			int firstParamIndex = neuron.firstParamIndex;
			System.arraycopy(unitInputParams, 0, parameters, firstParamIndex, numParamsPerNeuron);
		}
	}

	public final double[][] allocateLayerNeuronDoubleMatrix() {
		FCLayer[] layers = this.layers;
		int nl = layers.length;
		double[][] matrix = new double[nl][];
		for(int l = 0; l < nl; l++) {
			matrix[l] = new double[layers[l].neurons.length];
		}
		return matrix;
	}
	
	public final double[] allocateOutputArray() {
		FCLayer[] layers = this.layers;
		return new double[layers[layers.length - 1].neurons.length];
	}

	public final int getNeuronFirstParamIndex(int layerIdx, int neuronIdx) {
		FCLayer layer = this.layers[layerIdx];
		FCNeuron neuron = layer.neurons[neuronIdx];
		return neuron.firstParamIndex;
	}
	
	public final int getNumInputs() {
		return this.numInputs;
	}
	
	public final int getNumNeuronsInLayer(int layerIndex) {
		return this.layers[layerIndex].neurons.length;
	}
	
	public final boolean isHiddenLayerParameter(int paramIndex) {
		int layerIndex = this.layerOfParam[paramIndex];
		return layerIndex != this.layers.length - 1;		
	}

	public final boolean isOutputLayerParameter(int paramIndex) {
		int layerIndex = this.layerOfParam[paramIndex];
		return layerIndex == this.layers.length - 1;		
	}

	public final boolean isFirstLayerParameter(int paramIndex) {
		int layerIndex = this.layerOfParam[paramIndex];
		return layerIndex == 0;
	}

	public final int getNumOutputs() {
		return numOutputs;
	}

	public final int getNumLayers() {
		return this.layers.length;
	}
	
	public final int getNumParameters() {
		return this.numParameters;
	}
	
	public final int getLayerIndexOfParam(int paramIndex) {
		return this.layerOfParam[paramIndex];
	}

	public final int getTargetNeuronIndexOfParam(int paramIndex) {
		return this.neuronIndexOfParam[paramIndex];
	}

	public final int getNumInputsOfParamNeuron(int paramIndex) {
		int layerIndex = this.layerOfParam[paramIndex];		
		if(layerIndex <= 0) {
			return this.numInputs;
		}
		FCLayer priorLayer = this.layers[layerIndex-1];
		return priorLayer.neurons.length;
	}
	
	public final void populateActivations(double[][] activations, double[] parameters, double[] inputData) {
		FCLayer[] layers = this.layers;
		int nl = layers.length;
		double[] priorLayerData = inputData;
		for(int layerIndex = 0; layerIndex < nl; layerIndex++) {
			FCLayer layer = layers[layerIndex];
			FCNeuron[] neurons = layer.neurons;
			int nnl = neurons.length;
			double[] layerData = activations[layerIndex];
			for(int j = 0; j < nnl; j++) {
				FCNeuron neuron = neurons[j];
				layerData[j] = neuron.activation(parameters, priorLayerData);
			}
			priorLayerData = layerData;
		}		
	}

	public final int getOutputArrayLength() {
		return this.numOutputs;
	}

	private static final class FCLayer implements java.io.Serializable, Layer {
		private static final long serialVersionUID = 1L;
		private final FCNeuron[] neurons;
		
		public FCLayer(FCNeuron[] neurons) {
			super();
			this.neurons = neurons;
		}

		@Override
		public final Neuron[] getNeurons() {
			return this.neurons;
		}
	}
	
	static final class FCNeuron implements java.io.Serializable, Neuron {
		private static final long serialVersionUID = 1L;
		final int fisrtInputIndex;		
		final int numInputs;
		final int firstParamIndex;
		final ActivationFunction activationFunction;		

		public FCNeuron(int fisrtInputIndex, int numInputs,
				int firstParamIndex, ActivationFunction activationFunction) {
			super();
			this.fisrtInputIndex = fisrtInputIndex;
			this.numInputs = numInputs;
			this.firstParamIndex = firstParamIndex;
			this.activationFunction = activationFunction;
		}

		@Override
		public final ActivationFunction getActivationFunction() {
			return this.activationFunction;
		}

		@Override
		public final double activation(double[] parameters, double[] priorLayerData) {
			return this.activationFunction.activation(priorLayerData, this.fisrtInputIndex, this.numInputs, parameters, this.firstParamIndex);
		}
	}	
}
