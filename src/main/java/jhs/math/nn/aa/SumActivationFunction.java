package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;
import jhs.math.util.MathUtil;

public final class SumActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private static final double K = Math.sqrt(2);
	private final double b;

	public SumActivationFunction(int numInputs) {
		if(numInputs <= 0) {
			throw new IllegalArgumentException("numInputs: " + numInputs);
		}
		this.b = Math.sqrt(numInputs);
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return 1;
	}

	@Override
	public final double activation(double[] inputs, double[] parameters, int paramIndex) {
		double sum = MathUtil.sum(inputs);
		return (sum / this.b + parameters[paramIndex]) / K;
	}
}
