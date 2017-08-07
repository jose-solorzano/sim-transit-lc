package jhs.lc.opt.params;

public interface ParameterDef {
	double getValue(double[] parameterPool, int indexOffset);
	double[] getValues(double[] parameterPool);
	public double getSquaredDeviationFromRange(double[] parameterPool);	
}
