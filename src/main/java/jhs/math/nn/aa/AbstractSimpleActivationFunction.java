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
	
	protected abstract double activation(double dotProduct, double[] parameters, int extraParamIndex);
	protected abstract int getNumExtraParams(int numInputs);
	
	@Override
	public final int getNumParameters(int numInputs) {
		return numInputs + 1 + this.getNumExtraParams(numInputs);
	}

	@Override
	public final double activation(double[] inputs, int inputIndex, int numInputs, double[] parameters, int paramIndex) {
		double sum = 0;
		for(int i = 0; i < numInputs; i++) {
			sum += inputs[i + inputIndex] * parameters[i + paramIndex];
		}
		return this.activation(sum * this.sumFactor + parameters[paramIndex + numInputs] * this.biasFactor, parameters, paramIndex + numInputs + 1);
	}
}
