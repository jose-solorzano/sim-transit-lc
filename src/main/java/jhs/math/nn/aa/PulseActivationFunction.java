package jhs.math.nn.aa;

public final class PulseActivationFunction extends AbstractSimpleActivationFunction {	
	private static final long serialVersionUID = 1L;

	public PulseActivationFunction(int numInputs) {
		super(numInputs);
	}

	@Override
	protected final double activation(double dotProduct, double[] parameters, int extraParamIndex) {
		return (dotProduct <= +0.25 && dotProduct >= -0.25 ? + 1.0 : -1.0) / 0.794 + 0.765;
	}

	@Override
	protected final int getNumExtraParams(int numInputs) {
		return 0;
	}
}
