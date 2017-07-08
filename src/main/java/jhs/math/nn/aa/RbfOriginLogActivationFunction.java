package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;
import jhs.math.util.MathUtil;
import jhs.math.util.TriangularDistanceEstimator;

public class RbfOriginLogActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	//private static final double K = Math.sqrt(2);
	private final RbfType rbfType;
	private final TriangularDistanceEstimator tde;
	private final double[] origin;

	public RbfOriginLogActivationFunction(int numInputs, RbfType rbfType) {
		if(numInputs <= 0) {
			throw new IllegalArgumentException("numInputs: " + numInputs);
		}
		this.rbfType = rbfType;
		this.origin = new double[numInputs];
		this.tde = rbfType == RbfType.TRIANGULAR ? new TriangularDistanceEstimator() : null;
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return 0;
	}

	@Override
	public final double activation(double[] inputs, int inputIndex, int numInputs, double[] parameters, int paramIndex) {
		double distance;
		double factor = 1.0;
		double bias = 0;
		switch(this.rbfType) {
		case EUCLIDEAN:
			distance = Math.sqrt(MathUtil.euclideanDistanceSquared(inputs, inputIndex, numInputs, this.origin, 0));
			factor = 1 / 0.68;
			bias = 0.096;
			break;
		case MANHATTAN:
			distance = MathUtil.manhattanDistance(inputs, inputIndex, numInputs, this.origin, 0);
			factor = 1 / 0.653;
			bias = 0.456;
			break;
		case SQUARE:
			distance = MathUtil.squareDistance(inputs, inputIndex, numInputs, this.origin, 0);
			factor = 1 / 0.65;
			bias = -0.08;
			break;
		case TRIANGULAR:
			distance = this.tde.triangularDistance(inputs, inputIndex, numInputs, this.origin, 0);
			factor = 1 / 0.675;
			bias = -0.22; 
			break;
		default:
			throw new IllegalStateException(this.rbfType.name());
		}
		double nld = -Math.log(distance + 0.00001);
		return nld * factor + bias;
	}
}
