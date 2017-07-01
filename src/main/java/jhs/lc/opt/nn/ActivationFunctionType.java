package jhs.lc.opt.nn;

import jhs.math.nn.ActivationFunction;
import jhs.math.nn.aa.AtanActivationFunction;
import jhs.math.nn.aa.GaussianActivationFunction;
import jhs.math.nn.aa.IdentityActivationFunction;
import jhs.math.nn.aa.LeakyReluActivationFunction;
import jhs.math.nn.aa.MaxActivationFunction;
import jhs.math.nn.aa.MinActivationFunction;
import jhs.math.nn.aa.MonodActivationFunction;
import jhs.math.nn.aa.PiecewiseActivationFunction;
import jhs.math.nn.aa.RbfActivationFunction;
import jhs.math.nn.aa.RbfType;
import jhs.math.nn.aa.SigmoidActivationFunction;
import jhs.math.nn.aa.SignActivationFunction;
import jhs.math.nn.aa.SimpleMaxActivationFunction;
import jhs.math.nn.aa.SimpleMinActivationFunction;
import jhs.math.nn.aa.SumActivationFunction;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ActivationFunctionType {
	SIGMOID, 
	SIGN, 
	PIECEWISE, 
	MONOD,
	LEAKY_RELU,
	MIN,
	MAX,
	SIMPLE_MIN,
	SIMPLE_MAX,
	ATAN,
	LINEAR,
	GAUSSIAN,
	RBF_C,
	RBF_S,
	RBF_T,
	SUM;
	
	private ActivationFunctionType() {
	}

	@JsonCreator
	public static ActivationFunctionType create(String value) {
		return ActivationFunctionType.valueOf(value.toUpperCase());
	}

	public final ActivationFunction getActivationFunction(int numInputs) {
		switch(this) {
		case SIGMOID:
			return new SigmoidActivationFunction(numInputs);
		case SIGN:
			return new SignActivationFunction(numInputs);
		case PIECEWISE:
			return new PiecewiseActivationFunction(numInputs);
		case MONOD:
			return new MonodActivationFunction(numInputs);
		case LEAKY_RELU:
			return new LeakyReluActivationFunction(numInputs);
		case MIN:
			return new MinActivationFunction(numInputs);
		case MAX:
			return new MaxActivationFunction(numInputs);
		case SIMPLE_MIN:
			return new SimpleMinActivationFunction(numInputs);
		case SIMPLE_MAX:
			return new SimpleMaxActivationFunction(numInputs);
		case SUM:
			return new SumActivationFunction(numInputs);
		case ATAN:
			return new AtanActivationFunction(numInputs);
		case LINEAR:
			return new IdentityActivationFunction(numInputs);
		case GAUSSIAN:
			return new GaussianActivationFunction(numInputs);
		case RBF_C:
			return new RbfActivationFunction(numInputs, RbfType.EUCLIDEAN);
		case RBF_S:
			return new RbfActivationFunction(numInputs, RbfType.MANHATTAN);
		case RBF_T:
			return new RbfActivationFunction(numInputs, RbfType.TRIANGULAR);
		default:
			throw new IllegalStateException("No AF for " + this + ".");
		}
	}	
}
