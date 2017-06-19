package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;

public abstract class AbstractSimpleActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private final double sumFactor;
	private final double biasFactor;
	
	public AbstractSimpleActivationFunction(int numInputs) {
		double paramFactor = 1.0 / Math.sqrt(2);
		this.sumFactor = paramFactor / Math.sqrt(numInputs);
		this.biasFactor = paramFactor;
	}
	
	protected abstract double activation(int numInputs, double dotProduct, double[] parameters, int extraParamIndex);
	protected abstract int getNumExtraParams(int numInputs);
	
	@Override
	public final int getNumParameters(int numInputs) {
		return numInputs + 1 + this.getNumExtraParams(numInputs);
	}

	@Override
	public final double activation(double[] inputs, double[] parameters, int paramIndex) {
		int len = inputs.length;
		double sum = 0;
		for(int i = 0; i < len; i++) {
			sum += inputs[i] * parameters[i + paramIndex];
		}
		return this.activation(len, sum * this.sumFactor + parameters[len + paramIndex] * this.biasFactor, parameters, paramIndex + len + 1);
	}
}
