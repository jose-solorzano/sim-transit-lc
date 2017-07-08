package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;

public final class LinearNoBiasActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private final double sumFactor;
	
	public LinearNoBiasActivationFunction(int numInputs) {
		this.sumFactor = 1.0 / Math.sqrt(numInputs);
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return numInputs;
	}

	@Override
	public final double activation(double[] inputs, int inputIndex, int numInputs, double[] parameters, int paramIndex) {
		double sum = 0;
		for(int i = 0; i < numInputs; i++) {
			sum += inputs[i + inputIndex] * parameters[i + paramIndex];
		}
		return sum * this.sumFactor;
	}
}
