package jhs.math.nn.aa;

public final class AtanActivationFunction extends AbstractSimpleActivationFunction {
	private static final long serialVersionUID = 1L;
	private final double a;
	
	public AtanActivationFunction(int numInputs) {
		this(numInputs, 1.5);
	}
	
	public AtanActivationFunction(int numInputs, double a) {
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
		return StrictMath.atan(dotProduct) * a;
 	}
}
