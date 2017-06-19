package jhs.lc.data;

import jhs.math.util.ArrayUtil;
import jhs.math.util.ComparableValueHolder;

public class LightCurvePoint extends ComparableValueHolder<LightCurvePoint> {
	private final double timestamp;
	private final double flux;
	
	public LightCurvePoint(double timestamp, double flux) {
		super();
		this.timestamp = timestamp;
		this.flux = flux;
	}

	public double getTimestamp() {
		return timestamp;
	}

	public double getFlux() {
		return flux;
	}

	@Override
	protected final double getValue() {
		return this.timestamp;
	}
	
	public static double[] timestamps(LightCurvePoint[] points) {
		return ArrayUtil.doubleValueVector(points, point -> point.getTimestamp());
	}
	
	public static double[] fluxArray(LightCurvePoint[] points) {
		return ArrayUtil.doubleValueVector(points, point -> point.getFlux());
	}
}
