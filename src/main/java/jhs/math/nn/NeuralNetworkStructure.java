package jhs.math.nn;

public interface NeuralNetworkStructure {
	public abstract boolean isHiddenLayerParameter(int paramIndex);

	public abstract boolean isOutputLayerParameter(int paramIndex);

	public abstract boolean isFirstLayerParameter(int paramIndex);

	public abstract int getNumOutputs();

	public abstract int getNumLayers();

	public abstract int getNumParameters();

	public abstract int getNumInputsOfParamNeuron(int paramIndex);
	
	public abstract int getNumInputs();
	
	public abstract int getNumInputsOfLayer(int layerIndex);

	public abstract int getOutputArrayLength();
	
	public abstract int getNumNeuronsInLayer(int layerIndex);
	
	public abstract double[] allocateOutputArray();

	public abstract double[][] allocateLayerNeuronDoubleMatrix();
	
	public abstract Layer[] getLayers();
	
	public abstract Layer getLayer(int layerIndex);

	public abstract void populateActivations(double[][] activations, double[] parameters, double[] inputData);
}