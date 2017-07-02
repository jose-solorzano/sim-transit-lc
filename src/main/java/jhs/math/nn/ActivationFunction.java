package jhs.math.nn;

public interface ActivationFunction extends java.io.Serializable {
	public int getNumParameters(int numInputs);
	public double activation(double[] inputs, int inputIndex, int numInputs, double[] parameters, int paramIndex);
}
