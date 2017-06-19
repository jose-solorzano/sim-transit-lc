package jhs.math.nn;

public interface NeuralNetwork {
	public abstract NeuralNetworkStructure getStructure();
	public abstract double[] getParameters();
	public abstract double[] activations(double[] inputData);
}
