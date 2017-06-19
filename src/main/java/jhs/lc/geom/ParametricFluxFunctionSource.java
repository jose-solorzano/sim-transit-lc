package jhs.lc.geom;

public interface ParametricFluxFunctionSource {
	public FluxOrOpacityFunction getFluxOrOpacityFunction(double[] parameters);
	public int getNumParameters();
	public double getParameterScale(int paramIndex);
}
