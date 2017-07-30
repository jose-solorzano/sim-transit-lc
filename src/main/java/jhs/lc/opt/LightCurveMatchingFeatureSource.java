package jhs.lc.opt;

import jhs.lc.data.LightCurve;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

public class LightCurveMatchingFeatureSource {
	private static final double[] WLF = { 0.05, 0.08, 0.12 };
		
	private final double[] targetFluxArray;
	private final double[][] targetTrendArrays;
	private final double[][] targetTrendChangeArrays;
	private final double fluxVariance;
	private final double[] trendVariances;
	private final double[] trendChangeVariance;
	private final double mass;
	private final double centerOfMass;
	private final double massVariance;

	public LightCurveMatchingFeatureSource(double[] targetFluxArray) {
		this.centerOfMass = LightCurve.centerOfMass(targetFluxArray);
		this.mass = LightCurve.mass(targetFluxArray);
		this.massVariance = LightCurve.massVariance(targetFluxArray, this.centerOfMass);
		this.targetFluxArray = targetFluxArray;
		this.fluxVariance = MathUtil.variance(targetFluxArray);
		if(this.fluxVariance == 0) {
			throw new IllegalArgumentException("Flux series has zero variance.");
		}
		this.targetTrendArrays = new double[WLF.length][];
		this.targetTrendChangeArrays = new double[WLF.length][];
		this.trendVariances = new double[WLF.length];
		this.trendChangeVariance = new double[WLF.length];
		for(int i = 0; i < WLF.length; i++) {
			double wlfi = WLF[i];
			this.targetTrendArrays[i] = trendProfile(targetFluxArray, wlfi);
			this.targetTrendChangeArrays[i] = trendProfile(this.targetTrendArrays[i], wlfi);
			this.trendVariances[i] = MathUtil.variance(this.targetTrendArrays[i]);
			this.trendChangeVariance[i] = MathUtil.variance(this.targetTrendChangeArrays[i]);
		}
	}

	public static double[] trendChangeProfile(double[] fluxArray, double wlf) {
		return trendProfile(trendProfile(fluxArray, wlf), wlf);
	}

	private static double[] trendProfile(double[] fluxArray, double wlf) {
		int wl = (int) Math.round(wlf * fluxArray.length);
		if(wl < 3) {
			wl = 3;
		}
		return LightCurve.trendProfile(fluxArray, wl);
	}
	
	private static double normMse(double[] targetSeries, double[] testSeries, double variance, boolean onlyNegative) {
		int length = targetSeries.length;
		if(length != testSeries.length) {
			throw new IllegalArgumentException("Length: " + testSeries.length);
		}
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < length; i++) {
			double targetValue = targetSeries[i];
			if(targetValue < 0 || !onlyNegative) {
				double diff = testSeries[i] - targetValue;
				double weight = 1.0;
				sum += (diff * diff) * weight;
				weightSum += weight;
			}
		}
		return (weightSum == 0 ? 0 : sum / weightSum) / variance;				
	}

	public final double normMse(double[] testFluxArray) {
		return MathUtil.mse(this.targetFluxArray, testFluxArray) / this.fluxVariance;
	}

	public final double rmse(double[] testFluxArray) {
		return Math.sqrt(MathUtil.mse(this.targetFluxArray, testFluxArray));
	}

	private double sizeLoss(double[] testFluxArray) {
		double testMass = LightCurve.mass(testFluxArray);
		double diff = testMass - this.mass;
		return diff * diff / this.fluxVariance;
	}

	private double positionLoss(double[] testFluxArray) {
		double testCom = LightCurve.centerOfMass(testFluxArray);
		double diff = testCom - this.centerOfMass;
		return diff * diff / this.massVariance;
	}

	public double[] getFeatureValues(double[] testFluxArray) {
		double[] base = new double[] {
			this.normMse(testFluxArray),
			this.sizeLoss(testFluxArray),
			this.positionLoss(testFluxArray)
		};
		double[][] targetTrendArrays = this.targetTrendArrays;
		double[][] targetTrendChangeArrays = this.targetTrendChangeArrays;
		double[] trendVariances = this.trendVariances;
		double[] trendChangeVariances = this.trendChangeVariance;
		int nt = WLF.length;
		double[] tf = new double[nt * 4];
		for(int i = 0; i < nt; i++) {
			double wlfi = WLF[i];
			double[] targetTrendArray = targetTrendArrays[i];
			double[] targetTrendChangeArray = targetTrendChangeArrays[i];
			double[] testTrendArray = trendProfile(testFluxArray, wlfi);
			double[] testTrendChangeArray = trendProfile(testTrendArray, wlfi);
			int fi = i * 2;
			tf[fi] = normMse(targetTrendArray, testTrendArray, trendVariances[i], false);
			tf[fi + 1] = normMse(targetTrendChangeArray, testTrendChangeArray, trendChangeVariances[i], false);
			tf[fi + 2] = normMse(targetTrendArray, testTrendArray, trendVariances[i], true);
			tf[fi + 3] = normMse(targetTrendChangeArray, testTrendChangeArray, trendChangeVariances[i], true);
		}
		return ArrayUtil.concat(base, tf);
	}
}
