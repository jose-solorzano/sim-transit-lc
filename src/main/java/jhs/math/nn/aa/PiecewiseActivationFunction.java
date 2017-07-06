package jhs.math.nn.aa;

public final class PiecewiseActivationFunction extends AbstractSimpleActivationFunction {
	private static final long serialVersionUID = 1L;
	private final double a;
	
	public PiecewiseActivationFunction(int numInputs) {
		this(numInputs, 1.41);
	}
	
	public PiecewiseActivationFunction(int numInputs, double a) {
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
		return (dotProduct > +1 ? +1 : (dotProduct < -1 ? -1 : dotProduct)) * a;
	}
}
