package jhs.math.nn.aa;

public final class MonodActivationFunction extends AbstractSimpleActivationFunction {
	private static final long serialVersionUID = 1L;
	private final double a;
	private final double b;
	
	public MonodActivationFunction(int numInputs) {
		this(numInputs, 5.4, -3.333);
	}
	
	public MonodActivationFunction(int numInputs, double a, double b) {
		super(numInputs);
		this.a = a;
		this.b = b;
	}

	@Override
	protected final int getNumExtraParams(int numInputs) {
		return 0;
	}

	@Override
	protected final double activation(double dotProduct, double[] parameters, int extraParamIndex) {
		double p = 1.0 / (1.0 + StrictMath.abs(dotProduct));
		return p * this.a + this.b;
	}
}
