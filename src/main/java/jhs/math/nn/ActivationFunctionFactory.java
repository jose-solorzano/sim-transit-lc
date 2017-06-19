package jhs.math.nn;

public interface ActivationFunctionFactory {
	public ActivationFunction createActivationFunction(int numInputs, int layerIndex, int unitIndex);
	public ActivationFunction createOutputActivationFunction(int numInputs);
}
