package jhs.math.nn.aa;

public final class IdentityActivationFunction extends AbstractSimpleActivationFunction {
	private static final long serialVersionUID = 1L;

	public IdentityActivationFunction(int numInputs) {
		super(numInputs);
	}

	@Override
	protected final int getNumExtraParams(int numInputs) {
		return 0;
	}

	@Override
	protected final double activation(int numInputs, double dotProduct, double[] parameters, int extraParamIndex) {
		return dotProduct;
	}
}
