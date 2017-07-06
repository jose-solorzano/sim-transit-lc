package jhs.math.nn.aa;

public class SignActivationFunction extends AbstractSimpleActivationFunction {	
	private static final long serialVersionUID = 1L;

	public SignActivationFunction(int numInputs) {
		super(numInputs);
	}

	@Override
	protected final double activation(double dotProduct, double[] parameters, int extraParamIndex) {
		return dotProduct >= 0 ? +1.0 : -1.0;
	}

	@Override
	protected final int getNumExtraParams(int numInputs) {
		return 0;
	}
}
