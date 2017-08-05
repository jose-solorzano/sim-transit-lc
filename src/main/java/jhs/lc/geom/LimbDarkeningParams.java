package jhs.lc.geom;

import jhs.math.smoothing.GaussianSmoother;
import jhs.math.util.MathUtil;

/**
 * Implements polynomial limb darkening law as used by Aizawa et al. (2017).
 */
public final class LimbDarkeningParams implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final double[] a;
	
	public static final LimbDarkeningParams NONE = new LimbDarkeningParams();	
	public static final LimbDarkeningParams SUN = new LimbDarkeningParams(0.47, 0.23);	

	public LimbDarkeningParams(double ... a1etc) {
		this.a = a1etc;
	}

	public final double getLimbDarkeningFactor(double z, double radius) {
		// Assuming observer at an infinite distance.
		double cosPhi1M = 1 - z / radius;
		double[] a = this.a;
		int length = a.length;
		if(length == 0) {
			return 1.0;
		}
		double sum = 1.0;
		double currentFactor = cosPhi1M;
		for(int i = 0; i < length; i++) {
			sum -= a[i] * currentFactor;
			currentFactor *= currentFactor;
		}
		if(sum < 0) {
			sum = 0;
		}
		return sum;
	}	
	
	public final double[] producePointLimbDarkeningCurve(double[] timestamps, double[] obsFluxArray, double orbitRadius, double cycleFraction) {
		int length = timestamps.length;
		if(length < 2) {
			throw new IllegalArgumentException("Too few timestamps.");
		}
		double startTimestamp = timestamps[0];
		double endTimestamp = timestamps[length - 1];		
		double timeSpan = endTimestamp - startTimestamp;
		double halfAngularRange = Math.PI * cycleFraction;		
		double startAngle = -halfAngularRange;
		double timeToAngleFactor = halfAngularRange * 2 / timeSpan;
		double halfViewportAngle = Math.asin(1.0 / orbitRadius);
		int minIndex = minIndex(obsFluxArray);
		double timestampOfMin = timestamps[minIndex];
		double minFlux = obsFluxArray[minIndex];
		double angleOfMin = startAngle + (timestampOfMin - startTimestamp) * timeToAngleFactor;
		
		double viewportStartAngle = angleOfMin - halfViewportAngle;
		double viewportEndAngle = angleOfMin + halfViewportAngle;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			double timestamp = timestamps[i];
			double rotationAngle = startAngle + (timestamp - startTimestamp) * timeToAngleFactor;
			if(rotationAngle >= viewportStartAngle && rotationAngle <= viewportEndAngle) {
				double x = orbitRadius * Math.sin(rotationAngle - angleOfMin);
				double z = Math.sqrt(1.0 - x * x);
				double ldf = Double.isNaN(z) ? 0 : this.getLimbDarkeningFactor(z, 1.0);
				result[i] = 1.0 - ldf * (1.0 - minFlux);
			}
			else {
				result[i] = 1.0;
			}
		}
		return result;
	}	
	
	public static int minIndex(double[] series) {
		GaussianSmoother smoother = new GaussianSmoother(series.length * 0.04, 0.01);
		double[] smoothSeries = smoother.smooth(series);
		return MathUtil.minIndex(smoothSeries);
	}
}
