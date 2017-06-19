package jhs.math.nn.aa;

public final class GaussianActivationFunction extends AbstractSimpleActivationFunction {
	private static final long serialVersionUID = 1L;
	private final double a;
	private final double b;
	
	public GaussianActivationFunction(int numInputs) {
		this(numInputs, 2.97, -1.73);
	}
	
	public GaussianActivationFunction(int numInputs, double a, double b) {
		super(numInputs);
		this.a = a;
		this.b = b;
	}

	@Override
	protected final int getNumExtraParams(int numInputs) {
		return 0;
	}

	@Override
	protected final double activation(int numInputs, double dotProduct, double[] parameters, int extraParamIndex) {
		double p = Math.exp(-(dotProduct * dotProduct));
		return p * this.a + this.b;
	}
}
