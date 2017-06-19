package jhs.math.optimization;

public interface ParameterDistribution {
	public int getNumParameters();
	public double sample(int paramIndex);
}
