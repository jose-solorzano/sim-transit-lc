package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;
import jhs.math.util.MathUtil;

public final class PlainSumActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private final double b;

	public PlainSumActivationFunction(int numInputs) {
		if(numInputs <= 0) {
			throw new IllegalArgumentException("numInputs: " + numInputs);
		}
		this.b = Math.sqrt(numInputs);
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return 0;
	}

	@Override
	public final double activation(double[] inputs, int inputIndex, int numInputs, double[] parameters, int paramIndex) {
		double sum = MathUtil.sum(inputs, inputIndex, numInputs);
		return sum / this.b;
	}
}
