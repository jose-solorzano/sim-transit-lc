package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;
import jhs.math.util.MathUtil;

public final class SimpleMinActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private static final double K = Math.sqrt(2);
	private final double b, c;

	public SimpleMinActivationFunction(int numInputs) {
		if(numInputs <= 0) {
			throw new IllegalArgumentException("numInputs: " + numInputs);
		}
		this.b = Math.sqrt(K * Math.log(numInputs));
		this.c = 1.0 / Math.sqrt(1 + Math.log(numInputs));
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return 1;
	}

	@Override
	public double activation(double[] inputs, int inputIndex, int numInputs,
			double[] parameters, int paramIndex) {
		double min = MathUtil.min(inputs, inputIndex, numInputs);
		return ((min + this.b) / this.c + parameters[paramIndex]) / K;
	}
}
