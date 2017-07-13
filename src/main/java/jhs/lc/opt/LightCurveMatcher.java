package jhs.lc.opt;

import jhs.lc.data.LightCurve;
import jhs.math.util.MathUtil;
import org.apache.commons.math.FunctionEvaluationException;

public class LightCurveMatcher {
	private static final double MAX_FLUX = 1.0;
	private static final double WLF = 0.08;
	private static final double NTCW = 0.67; //TODO %%% TESTING
	private final double[] targetFluxArray;
	private final double[] targetTrendChangeArray;
	private final double targetCenterOfMass;
	private final double targetMassDeviation;
	private final double fluxVariance;
	private final double trendChangeVariance;
	private final double trendChangeWeight;

	public LightCurveMatcher(double[] targetFluxArray, double trendChangeWeight) {
		if(trendChangeWeight < 0 || trendChangeWeight > 1) {
			throw new IllegalArgumentException("trendChangeWeight: " + trendChangeWeight);
		}
		this.trendChangeWeight = trendChangeWeight;
		this.targetFluxArray = targetFluxArray;
		this.targetTrendChangeArray = trendChangeProfile(targetFluxArray);
		this.targetCenterOfMass = LightCurve.centerOfMass(targetFluxArray);
		this.targetMassDeviation = LightCurve.massDeviation(targetFluxArray, this.targetCenterOfMass);
		this.fluxVariance = MathUtil.variance(targetFluxArray);
		if(this.fluxVariance == 0) {
			throw new IllegalArgumentException("Flux series has zero variance.");
		}
		this.trendChangeVariance = MathUtil.variance(this.targetTrendChangeArray);
		if(this.trendChangeVariance == 0) {
			throw new IllegalArgumentException("Trend change series has zero variance.");			
		}
	}
	
	public static double[] trendChangeProfile(double[] fluxArray) {
		int wl = (int) Math.round(WLF * fluxArray.length);
		if(wl < 3) {
			wl = 3;
		}
		return LightCurve.trendChangeProfile(fluxArray, wl);
	}

	public final double ordinaryFluxMeanSquaredError(double[] testFluxArray) {
		double[] targetFluxArray = this.targetFluxArray;		
		int length = targetFluxArray.length;
		if(length != testFluxArray.length) {
			throw new IllegalArgumentException("Length: " + testFluxArray.length);
		}
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < length; i++) {
			double weight = 1.0;
			double diff = testFluxArray[i] - targetFluxArray[i];
			sum += (diff * diff) * weight;
			weightSum += weight;
		}
		return (weightSum == 0 ? 0 : sum / weightSum) / this.fluxVariance;
	}

	public final double weightedTrendChangeMeanSquaredError(double[] testTrendChangeArray) {
		double[] targetTrendChangeArray = this.targetTrendChangeArray;		
		int length = targetTrendChangeArray.length;
		if(length != testTrendChangeArray.length) {
			throw new IllegalArgumentException("Length: " + testTrendChangeArray.length);
		}
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < length; i++) {
			double targetTrendChange = targetTrendChangeArray[i];
			double weight = targetTrendChange < 0 ? NTCW : (1 - NTCW);
			double diff = testTrendChangeArray[i] - targetTrendChange;
			sum += (diff * diff) * weight;
			weightSum += weight;
		}
		return (weightSum == 0 ? 0 : sum / weightSum) / this.trendChangeVariance;
	}
	
	public final double meanSquaredError(double[] testFluxArray, double[] testTrendChangeArray) {
		double fluxError = this.ordinaryFluxMeanSquaredError(testFluxArray);
		double trendChangeError = this.weightedTrendChangeMeanSquaredError(testTrendChangeArray);
		double tcw = this.trendChangeWeight;
		return fluxError * (1 - tcw) + trendChangeError * tcw;
	}
	
	public final double meanSquaredError(double[] testFluxArray) {
		double[] testTrendChangeArray = trendChangeProfile(testFluxArray);
		return this.meanSquaredError(testFluxArray, testTrendChangeArray);
	}

	public final FlexibleLightCurveMatchingResults flexibleMeanSquaredError(final double[] testFluxArray, final boolean shiftOnly) throws FunctionEvaluationException {
		double[] testTrendChangeArray = trendChangeProfile(testFluxArray);
		double testCenterOfMass = LightCurve.centerOfMass(testFluxArray);
		double testMassDeviation = shiftOnly ? Double.NaN : LightCurve.massDeviation(testFluxArray, testCenterOfMass);
		double skewA = shiftOnly ? 1.0 : testMassDeviation / this.targetMassDeviation;				
		double skewB = testCenterOfMass - this.targetCenterOfMass * skewA;
		double length = this.targetFluxArray.length;
		double skewADiff = skewA - 1.0;
		double skewBDiff = skewB / length;
		double bendMetric = (skewADiff * skewADiff + skewBDiff * skewBDiff) / 2.0;
		double minimizedError = this.meanSquaredError(testFluxArray, testTrendChangeArray, skewA, skewB);
		return new FlexibleLightCurveMatchingResults(skewA, skewB, minimizedError, bendMetric);
	}

	private double meanSquaredError(double[] testFluxArray, double[] testTrendChangeArray, double skewA, double skewB) {
		double[] targetFluxArray = this.targetFluxArray;
		int length = targetFluxArray.length;
		double[] skewedTestFluxArray = new double[length];
		double[] skewedTestTrendChangeArray = new double[length];
		for(int i = 0; i < length; i++) {
			double origIndex = i * skewA + skewB;
			int origIndexFloor = (int) Math.floor(origIndex);
			int origIndexCeil = (int) Math.ceil(origIndex);
			if(origIndexFloor < 0 || origIndexCeil >= testFluxArray.length) {
				skewedTestFluxArray[i] = MAX_FLUX;
				skewedTestTrendChangeArray[i] = 0;
			}
			else {
				double k = origIndex - origIndexFloor;
				double f1 = testFluxArray[origIndexFloor];
				double f2 = testFluxArray[origIndexCeil];
				skewedTestFluxArray[i] = f1 * (1 - k) + f2 * k;
				double tc1 = testTrendChangeArray[origIndexFloor];
				double tc2 = testTrendChangeArray[origIndexCeil];
				skewedTestTrendChangeArray[i] = tc1 * (1 - k) + tc2 * k;
			}
		}
		return this.meanSquaredError(skewedTestFluxArray, skewedTestTrendChangeArray);
	}
}
