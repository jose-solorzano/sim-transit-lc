package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;

public final class MaxActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private static final double K = Math.sqrt(2);
	private final double b, c;

	public MaxActivationFunction(int numInputs) {
		if(numInputs <= 0) {
			throw new IllegalArgumentException("numInputs: " + numInputs);
		}
		this.b = Math.sqrt(Math.sqrt(Math.log(numInputs)) * 1.31 * Math.log(numInputs));
		this.c = K / Math.sqrt(1 + Math.log(numInputs));
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return numInputs;
	}

	@Override
	public final double activation(double[] inputs, double[] parameters, int paramIndex) {
		double max;
		int n = inputs.length;
		if(n == 0) {
			max = 0;
		}
		else {
			max = Double.NEGATIVE_INFINITY;
			for(int i = 0; i < n; i++) {
				double v = inputs[i] + parameters[paramIndex + i];
				if(v > max) {
					max = v;
				}
			}
		}
		return (max - this.b) / this.c;
	}
}
