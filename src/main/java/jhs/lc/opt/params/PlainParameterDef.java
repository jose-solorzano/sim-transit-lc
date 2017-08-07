package jhs.lc.opt.params;

public class PlainParameterDef implements ParameterDef {
	private static final double MAX_PARAM_VALUE = 1.733;
	private static final double MIN_PARAM_VALUE = -MAX_PARAM_VALUE;
	private static final double PARAM_RANGE = MAX_PARAM_VALUE - MIN_PARAM_VALUE;
			
	private final int numParameters;
	private final int baseIndex;
	private final double a, b;
	
	public PlainParameterDef(int numParameters, double min, double max, int baseIndex) {
		this.numParameters = numParameters;
		this.baseIndex = baseIndex;
		double maxMinDiff = max - min;
		this.a = maxMinDiff / PARAM_RANGE;
		this.b = min - MIN_PARAM_VALUE * this.a;
	}

	public final double getValue(double[] parameterPool, int indexOffset) {
		double pv = parameterPool[this.baseIndex + indexOffset];
		if(pv < MIN_PARAM_VALUE) {
			pv = MIN_PARAM_VALUE;
		}
		else if(pv > MAX_PARAM_VALUE) {
			pv = MAX_PARAM_VALUE;
		}
		return pv * this.a + this.b;
 	}
	
	public final double[] getValues(double[] parameterPool) {
		int n = this.numParameters;
		int bi = this.baseIndex;
		double[] values = new double[n];
		for(int i = 0; i < n; i++) {
			double pv = parameterPool[bi + i];
			if(pv < MIN_PARAM_VALUE) {
				pv = MIN_PARAM_VALUE;
			}
			else if(pv > MAX_PARAM_VALUE) {
				pv = MAX_PARAM_VALUE;
			}
			values[i] = pv * this.a + this.b;
		}
		return values;
 	}
	
	public final double getSquaredDeviationFromRange(double[] parameterPool) {
		int from = this.baseIndex;
		int to = from + this.numParameters;
		double sum = 0;
		for(int i = from; i < to; i++) {
			double value = parameterPool[i];
			double diff = 0;
			if(value < MIN_PARAM_VALUE) {
				diff = value - MIN_PARAM_VALUE;
			}
			else if(value > MAX_PARAM_VALUE) {
				diff = value - MAX_PARAM_VALUE;
			}
			sum += diff * diff;
		}
		return sum;
	}
}
