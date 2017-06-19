package jhs.math.nn;

public interface Neuron extends java.io.Serializable {
	public double activation(double[] parameters, double[] layerInputData);
	public ActivationFunction getActivationFunction();
}
