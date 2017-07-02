package jhs.math.nn.aa;

public final class LinearActivationFunction extends AbstractSimpleActivationFunction {
	private static final long serialVersionUID = 1L;

	public LinearActivationFunction(int numInputs) {
		super(numInputs);
	}

	@Override
	protected final int getNumExtraParams(int numInputs) {
		return 0;
	}

	@Override
	protected final double activation(double dotProduct, double[] parameters, int extraParamIndex) {
		return dotProduct;
	}
}
