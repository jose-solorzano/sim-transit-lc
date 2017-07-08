package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;
import jhs.math.util.MathUtil;
import jhs.math.util.TriangularDistanceEstimator;

public class RbfLogActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	private final RbfType rbfType;
	private final TriangularDistanceEstimator tde;

	public RbfLogActivationFunction(int numInputs, RbfType rbfType) {
		if(numInputs <= 0) {
			throw new IllegalArgumentException("numInputs: " + numInputs);
		}
		this.rbfType = rbfType;
		this.tde = rbfType == RbfType.TRIANGULAR ? new TriangularDistanceEstimator() : null;
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return numInputs;
	}

	@Override
	public final double activation(double[] inputs, int inputIndex, int numInputs, double[] parameters, int paramIndex) {
		double distance;
		double factor = 1.0;
		double bias = 0;
		switch(this.rbfType) {
		case EUCLIDEAN:
			distance = Math.sqrt(MathUtil.euclideanDistanceSquared(inputs, inputIndex, numInputs, parameters, paramIndex));
			factor = 1.0 / 0.656;
			bias = 0.617;
			break;
		case MANHATTAN:
			distance = MathUtil.manhattanDistance(inputs, inputIndex, numInputs, parameters, paramIndex);
			factor = 1.0 / 0.663;
			bias = 0.967; 
			break;
		case SQUARE:
			distance = MathUtil.squareDistance(inputs, inputIndex, numInputs, parameters, paramIndex);
			factor = 1.0 / 0.664;
			bias = 0.444;
			break;
		case TRIANGULAR:
			distance = this.tde.triangularDistance(inputs, inputIndex, numInputs, parameters, paramIndex);
			factor = 1.0 / 0.682;
			bias = 0.285;
			break;
		default:
			throw new IllegalStateException(this.rbfType.name());
		}
		return -Math.log(distance + 0.00001) * factor + bias;
	}
}
