package jhs.lc.data;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import jhs.math.regression.linear.WeightedLinearRegression;


public class LightCurve {
	private final LightCurvePoint[] points;

	public LightCurve(LightCurvePoint[] points) {
		super();
		this.points = points;
	}

	public final LightCurvePoint[] getPoints() {
		return points;
	}
	
	public void write(File outFile) throws IOException {
		PrintWriter out = new PrintWriter(outFile);
		try {
			out.println("Timestamp,Flux"); 
			for(LightCurvePoint point : this.points) {
				out.println(point.getTimestamp() + "," + point.getFlux());
			}
		} finally {
			out.close();
		}
	}

	public static double massDeviationAsFraction(double[] fluxArray, double centerOfMass) {
		double mdev = massDeviation(fluxArray, centerOfMass);
		return mdev / fluxArray.length;
	}

	public static double massDeviation(double[] fluxArray, double centerOfMass) {
		return Math.sqrt(massVariance(fluxArray, centerOfMass));
	}
		
	public static double massVariance(double[] fluxArray, double centerOfMass) {		
		int length = fluxArray.length;
		double sumDev = 0;
		double sumWeight = 0;
		for(int i = 0; i < length; i++) {
			double weight = 1.0 - fluxArray[i];
			if(weight < 0) {
				weight = 0;
			}
			sumWeight += weight;
			double posDiff = i - centerOfMass;
			sumDev += posDiff * posDiff * weight;
		}
		return sumWeight == 0 ? 0 : sumDev / sumWeight;
	}

	public static double centerOfMassAsFraction(double[] fluxArray) {
		double com = centerOfMass(fluxArray);
		return com / (fluxArray.length - 1);
	}

	public static double centerOfMass(double[] fluxArray) {
		int length = fluxArray.length;
		double sumPos = 0;
		double sumWeight = 0;
		for(int i = 0; i < length; i++) {
			double weight = 1.0 - fluxArray[i];
			if(weight < 0) {
				weight = 0;
			}
			sumWeight += weight;
			sumPos += i * weight;
		}
		return sumWeight == 0 ? 0.5 * fluxArray.length : sumPos / sumWeight;
	}

	public static double mass(double[] fluxArray) {
		int length = fluxArray.length;
		double sumMass = 0;
		for(int i = 0; i < length; i++) {
			double mass = 1.0 - fluxArray[i];
			if(mass < 0) {
				mass = 0;
			}
			sumMass += mass;
		}
		return sumMass / length;
	}

	public static double[] trendProfile(double[] series, int windowLength) {
		int length = series.length;
		int hwl = windowLength / 2;
		int start = hwl;
		int end = length - hwl;
		double[] trends = new double[length];
		for(int i = start; i < end; i++) {
			trends[i] = trend(series, i - hwl, i + hwl);
		}
		return trends;		
	}

	public static double[] trendChangeProfile(double[] fluxArray, int windowLength) {
		double[] trends = trendProfile(fluxArray, windowLength);
		return trendProfile(trends, windowLength);
	}
		
	private static double trend(double[] series, int firstIndex, int lastIndex) {
		WeightedLinearRegression regression = new WeightedLinearRegression();
		for(int x = firstIndex; x <= lastIndex; x++) {
			regression.addData(1.0, x, series[x]);
		}
		return regression.getSlope();
	}
}
