package jhs.lc.opt.params;

import jhs.math.util.MathUtil;

public class ExpParameterDef implements ParameterDef {
	private final PlainParameterDef delegate;
	
	public ExpParameterDef(int numParameters, double min, double max, int baseIndex) {
		if(min <= 0 || max <= 0) {
			throw new IllegalArgumentException("Exponential scale variable can't have bounds that are negative or zero: [" + min + ", " + max + "].");
		}
		this.delegate = new PlainParameterDef(numParameters, Math.log(min), Math.log(max), baseIndex);
	}

	public final double getValue(double[] parameterPool, int indexOffset) {
		double logValue = this.delegate.getValue(parameterPool, indexOffset);
		return Math.exp(logValue);
 	}
	
	public final double[] getValues(double[] parameterPool) {
		double[] logValues = this.delegate.getValues(parameterPool);
		return MathUtil.exp(logValues);
 	}
	
	public final double getSquaredDeviationFromRange(double[] parameterPool) {
		return this.delegate.getSquaredDeviationFromRange(parameterPool);
	}
}
