package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;
import jhs.math.util.MathUtil;

public final class MaxActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private static final double K = Math.sqrt(2);
	private final double b, c;

	public MaxActivationFunction(int numInputs) {
		if(numInputs <= 0) {
			throw new IllegalArgumentException("numInputs: " + numInputs);
		}
		this.b = Math.sqrt(K * Math.log(numInputs));
		this.c = 1.0 / Math.sqrt(1 + Math.log(numInputs));
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return numInputs;
	}

	@Override
	public final double activation(double[] inputs, double[] parameters, int paramIndex) {
		int maxIndex = MathUtil.maxIndex(inputs);
		double max = maxIndex == -1 ? Double.NEGATIVE_INFINITY : inputs[maxIndex];
		return ((max - this.b) / this.c + parameters[paramIndex + maxIndex]) / K;
	}
}
