package jhs.math.nn.aa;

public final class SigmoidActivationFunction extends AbstractSimpleActivationFunction {
	private static final long serialVersionUID = 1L;
	private final double a;
	
	public SigmoidActivationFunction(int numInputs) {
		this(numInputs, 2.41);
	}
	
	public SigmoidActivationFunction(int numInputs, double a) {
		super(numInputs);
		this.a = a;
	}

	@Override
	protected final int getNumExtraParams(int numInputs) {
		return 0;
	}

	@Override
	protected final double activation(double dotProduct, double[] parameters, int extraParamIndex) {
		double a = this.a;
		return -a + 2 * a / (1.0 + StrictMath.exp(-dotProduct));
	}
}
