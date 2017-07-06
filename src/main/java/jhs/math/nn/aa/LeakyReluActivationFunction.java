package jhs.math.nn.aa;

public class LeakyReluActivationFunction extends AbstractSimpleActivationFunction {	
	private static final long serialVersionUID = 1L;
	private final double a;
	private final double b;
	
	public LeakyReluActivationFunction(int numInputs) {
		this(numInputs, 1.07, -0.43);
	}

	public LeakyReluActivationFunction(int numInputs, double a, double b) {
		super(numInputs);
		this.a = a;
		this.b = b;
	}

	@Override
	protected final double activation(double dotProduct, double[] parameters, int extraParamIndex) {
		return (dotProduct >= 0 ? dotProduct : dotProduct * parameters[extraParamIndex]) * this.a + this.b;
	}

	@Override
	protected final int getNumExtraParams(int numInputs) {
		return 1;
	}

}
