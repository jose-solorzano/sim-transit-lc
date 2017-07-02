package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;

public final class MinActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private static final double K = Math.sqrt(2);
	private final double b, c;

	public MinActivationFunction(int numInputs) {
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
	public double activation(double[] inputs, int inputIndex, int numInputs,
			double[] parameters, int paramIndex) {
		double min;
		if(numInputs == 0) {
			min = 0;
		}
		else {
			min = Double.POSITIVE_INFINITY;
			for(int i = 0; i < numInputs; i++) {
				double v = inputs[inputIndex + i] + parameters[paramIndex + i];
				if(v < min) {
					min = v;
				}
			}
		}
		return (min + this.b) / this.c;
	}
}
