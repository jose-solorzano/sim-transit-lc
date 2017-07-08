package jhs.math.nn.aa;

import jhs.math.nn.ActivationFunction;
import jhs.math.util.MathUtil;
import jhs.math.util.TriangularDistanceEstimator;

public class RbfOriginActivationFunction implements ActivationFunction {
	private static final long serialVersionUID = 1L;
	//private static final double K = Math.sqrt(2);
	private final RbfType rbfType;
	private final TriangularDistanceEstimator tde;
	private final double[] origin;
	private final double k1;
	private final double k2;
	private final double k3;
	private final double k4;
	private final double k5;
	private final double k6;
	private final double k7;

	public RbfOriginActivationFunction(int numInputs, RbfType rbfType) {
		if(numInputs <= 0) {
			throw new IllegalArgumentException("numInputs: " + numInputs);
		}
		this.rbfType = rbfType;
		this.origin = new double[numInputs];
		this.tde = rbfType == RbfType.TRIANGULAR ? new TriangularDistanceEstimator() : null;
		this.k1 = Math.sqrt(numInputs * 2);
		this.k2 = Math.sqrt(numInputs / (1.42 * 2));
		this.k3 = Math.sqrt(numInputs * 1.81);
		this.k4 = Math.sqrt(numInputs / (1.81 * 2));
		this.k5 = 1.4 + 0.14 * Math.sqrt(Math.log(numInputs));
		this.k6 = 4.3 / (Math.sqrt(2) * (5.0 + Math.sqrt(Math.log(numInputs))));
		this.k7 = 1.294 + 2.15 * Math.sqrt(Math.log(numInputs));
	}

	@Override
	public final int getNumParameters(int numInputs) {
		return 0;
	}

	@Override
	public final double activation(double[] inputs, int inputIndex, int numInputs, double[] parameters, int paramIndex) {
		double distance;
		switch(this.rbfType) {
		case EUCLIDEAN:
			distance = Math.sqrt(MathUtil.euclideanDistanceSquared(inputs, inputIndex, numInputs, this.origin, 0) * 2) - this.k1;
			break;
		case MANHATTAN:
			distance = MathUtil.manhattanDistance(inputs, inputIndex, numInputs, this.origin, 0) / this.k2 - this.k3;
			break;
		case SQUARE:
			distance = MathUtil.squareDistance(inputs, inputIndex, numInputs, this.origin, 0) / this.k6 - this.k7;
			break;
		case TRIANGULAR:
			distance = this.tde.triangularDistance(inputs, inputIndex, numInputs, this.origin, 0) / this.k4 - this.k5;
			break;
		default:
			throw new IllegalStateException(this.rbfType.name());
		}
		return -distance;
	}
}
