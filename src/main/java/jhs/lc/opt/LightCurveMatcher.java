package jhs.lc.opt;

import jhs.lc.data.LightCurve;
import jhs.math.util.MathUtil;
import org.apache.commons.math.FunctionEvaluationException;

public class LightCurveMatcher {
	private static final double WLF = 0.08;
	
	private static final double SW = 1;
	private static final double STW = 0;
	private static final double STCW = 1;
	
	private final double[] targetFluxArray;
	private final double[] targetTrendArray;
	private final double[] targetTrendChangeArray;
	private final double fluxVariance;
	private final double trendVariance;
	private final double trendChangeVariance;

	public LightCurveMatcher(double[] targetFluxArray) {
		this.targetFluxArray = targetFluxArray;
		this.targetTrendArray = trendProfile(targetFluxArray);
		this.targetTrendChangeArray = trendProfile(this.targetTrendArray);
		this.fluxVariance = MathUtil.variance(targetFluxArray);
		if(this.fluxVariance == 0) {
			throw new IllegalArgumentException("Flux series has zero variance.");
		}
		this.trendVariance = MathUtil.variance(this.targetTrendArray);
		if(this.trendVariance == 0) {
			throw new IllegalArgumentException("Flux trend series has zero variance.");
		}		
		this.trendChangeVariance = MathUtil.variance(this.targetTrendChangeArray);
		if(this.trendChangeVariance == 0) {
			throw new IllegalArgumentException("Flux trend change series has zero variance.");			
		}
	}

	public static double[] trendChangeProfile(double[] fluxArray) {
		return trendProfile(trendProfile(fluxArray));
	}

	public static double[] trendProfile(double[] fluxArray) {
		int wl = (int) Math.round(WLF * fluxArray.length);
		if(wl < 3) {
			wl = 3;
		}
		return LightCurve.trendProfile(fluxArray, wl);
	}
	
	public final double loss(double[] testFluxArray) {
		double[] testTrendArray = trendProfile(testFluxArray);
		double[] testTrendChangeArray = trendProfile(testTrendArray);
		double sMse = MathUtil.mse(testFluxArray, this.targetFluxArray) / this.fluxVariance;
		double stMse = MathUtil.mse(testTrendArray, this.targetTrendArray) / this.trendVariance;
		double stcMse = MathUtil.mse(testTrendChangeArray, this.targetTrendChangeArray) / this.trendChangeVariance;
		return (sMse * SW + stMse * STW + stcMse * STCW) / (SW + STW + STCW);
	}
	
	public double fluxLoss(double[] testFluxArray) {
		return MathUtil.mse(testFluxArray, this.targetFluxArray) / this.fluxVariance;
	}
	
	public double trendChangeLoss(double[] testFluxArray) {
		double[] testTrendArray = trendProfile(testFluxArray);
		double[] testTrendChangeArray = trendProfile(testTrendArray);
		return MathUtil.mse(testTrendChangeArray, this.targetTrendChangeArray) / this.trendChangeVariance;
	}
}
