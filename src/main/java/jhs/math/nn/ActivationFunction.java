package jhs.math.nn;

public interface ActivationFunction extends java.io.Serializable {
	public int getNumParameters(int numInputs);
	public double activation(double[] inputs, double[] parameters, int paramIndex);
}
