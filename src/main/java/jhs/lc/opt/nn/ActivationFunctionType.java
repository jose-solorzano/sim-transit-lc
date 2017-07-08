package jhs.lc.opt.nn;

import jhs.math.nn.ActivationFunction;
import jhs.math.nn.aa.AtanActivationFunction;
import jhs.math.nn.aa.GaussianActivationFunction;
import jhs.math.nn.aa.LinearActivationFunction;
import jhs.math.nn.aa.LinearNoBiasActivationFunction;
import jhs.math.nn.aa.LeakyReluActivationFunction;
import jhs.math.nn.aa.MaxActivationFunction;
import jhs.math.nn.aa.MinActivationFunction;
import jhs.math.nn.aa.MonodActivationFunction;
import jhs.math.nn.aa.PiecewiseActivationFunction;
import jhs.math.nn.aa.PulseActivationFunction;
import jhs.math.nn.aa.RbfActivationFunction;
import jhs.math.nn.aa.RbfLogActivationFunction;
import jhs.math.nn.aa.RbfOriginActivationFunction;
import jhs.math.nn.aa.RbfOriginLogActivationFunction;
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
	LINEAR_NO_BIAS,
	GAUSSIAN,
	RBF_C,
	RBF_S,
	RBF_T,
	RBF0_C,
	RBF0_S,
	RBF0_T,
	SUM, 
	PULSE;
	
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
			return new LinearActivationFunction(numInputs);
		case LINEAR_NO_BIAS:
			return new LinearNoBiasActivationFunction(numInputs);
		case PULSE:
			return new PulseActivationFunction(numInputs);
		case GAUSSIAN:
			return new GaussianActivationFunction(numInputs);
		case RBF_C:
			return new RbfLogActivationFunction(numInputs, RbfType.EUCLIDEAN);
		case RBF_S:
			return new RbfLogActivationFunction(numInputs, RbfType.SQUARE);
		case RBF_T:
			return new RbfLogActivationFunction(numInputs, RbfType.TRIANGULAR);
		case RBF0_C:
			return new RbfOriginLogActivationFunction(numInputs, RbfType.EUCLIDEAN);
		case RBF0_S:
			return new RbfOriginLogActivationFunction(numInputs, RbfType.SQUARE);
		case RBF0_T:
			return new RbfOriginLogActivationFunction(numInputs, RbfType.TRIANGULAR);
		default:
			throw new IllegalStateException("No AF for " + this + ".");
		}
	}	
}
