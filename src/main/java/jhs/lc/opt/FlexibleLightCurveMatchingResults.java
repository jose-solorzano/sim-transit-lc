package jhs.lc.opt;

public class FlexibleLightCurveMatchingResults {
	private final double a, b;
	private final double minimizedError;
	private final double bendMetric;

	public FlexibleLightCurveMatchingResults(double a, double b,
			double minimizedError, double bendMetric) {
		super();
		this.a = a;
		this.b = b;
		this.minimizedError = minimizedError;
		this.bendMetric = bendMetric;
	}

	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}

	public double getMinimizedError() {
		return minimizedError;
	}

	public double getBendMetric() {
		return bendMetric;
	}

	@Override
	public String toString() {
		return "FlexibleLightCurveMatchingResults [a=" + a + ", b=" + b
				+ ", minimizedError=" + minimizedError + ", bendMetric="
				+ bendMetric + "]";
	}
}
