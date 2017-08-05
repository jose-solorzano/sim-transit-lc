package jhs.lc.geom;

public interface ParametricTransitFunctionSource {
	public TransitFunction getTransitFunction(double[] parameters);
	public int getNumParameters();
	public double getParameterScale(int paramIndex);
}
