package jhs.lc.opt;

import jhs.lc.data.LightCurve;
import jhs.math.util.MathUtil;

public class FlexibleLossFunction extends AbstractLossFunction {
	private static final double MSE_FACTOR = 1E7;
	private static final double WLF = 0.09;
	private static final double MAX_IGNORE_ERROR_FRACTION = 0.03;
	private static final double BASELINE_FLUX = 1.0;
	private static final double BASELINE_TREND_CHANGE = 0;
		
	private final double trendChangeWeight;
	private final double flexibleMatchWeight;

	private final double maxIgnoreError;
	private final double fluxCenterOfMass;
	private final double fluxMassDeviation;
	
	private final double[] targetFluxArray;
	private final double[] targetTrendChangeArray;
	private final double fluxVariance;
	private final double trendChangeVariance;

	public FlexibleLossFunction(SolutionSampler sampler, double[] targetFluxArray, double trendChangeWeight, double flexibleMatchWeight) {
		super(sampler, 1.0, targetFluxArray);
		this.fluxVariance = MathUtil.variance(targetFluxArray);
		if(this.fluxVariance == 0) {
			throw new IllegalArgumentException("Flux series has zero variance.");
		}
		double fluxStdDev = Math.sqrt(this.fluxVariance);
		this.maxIgnoreError = fluxStdDev * MAX_IGNORE_ERROR_FRACTION;
		this.fluxCenterOfMass = SeriesUtil.centerOfMass(targetFluxArray, this.maxIgnoreError, BASELINE_FLUX);
		this.fluxMassDeviation = SeriesUtil.massDeviation(targetFluxArray, this.maxIgnoreError, BASELINE_FLUX, this.fluxCenterOfMass);
		this.trendChangeWeight = trendChangeWeight;
		this.flexibleMatchWeight = flexibleMatchWeight;
		this.targetFluxArray = targetFluxArray;
		double[] targetTrendArray = trendProfile(targetFluxArray);
		this.targetTrendChangeArray = trendProfile(targetTrendArray);
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
	
	@Override
	protected final double baseLoss(double[] testFluxArray) {
		double[] testTrendArray = trendProfile(testFluxArray);
		double[] testTrendChangeArray = trendProfile(testTrendArray);

		double fmw = this.flexibleMatchWeight;
		double nonFlexibleMse = fmw == 1 ? 0 : this.nonFlexibleMse(testFluxArray, testTrendChangeArray);
		double flexibleMse = fmw == 0 ? 0 : this.flexibleMse(testFluxArray, testTrendChangeArray);
		
		double combinedMse = fmw * flexibleMse + (1 - fmw) * nonFlexibleMse;
		
		return Math.log1p(MSE_FACTOR * combinedMse);
	}	
	
	private double nonFlexibleMse(double[] testFluxArray, double[] testTrendChangeArray) {
		double sMse = MathUtil.mse(testFluxArray, this.targetFluxArray) / this.fluxVariance;
		double stcMse = MathUtil.mse(testTrendChangeArray, this.targetTrendChangeArray) / this.trendChangeVariance;
		double tcw = this.trendChangeWeight;
		return sMse * (1 - tcw) + stcMse * tcw;
	}
	
	private double flexibleMse(double[] testFluxArray, double[] testTrendChangeArray) {
		double testCom = SeriesUtil.centerOfMass(testFluxArray, this.maxIgnoreError, BASELINE_FLUX);
		double testMassDeviation = SeriesUtil.massDeviation(testFluxArray, this.maxIgnoreError, BASELINE_FLUX, testCom);
		double indexFactor = SeriesUtil.getIndexFactor(this.fluxMassDeviation, testMassDeviation);
		double indexOffset = SeriesUtil.getIndexOffset(indexFactor, this.fluxCenterOfMass, testCom);
		double[] stretchedTestFluxArray = SeriesUtil.stretchSeries(testFluxArray, BASELINE_FLUX, indexFactor, indexOffset);
		double[] stretchedTestTrendChangeArray = SeriesUtil.stretchSeries(testTrendChangeArray, BASELINE_TREND_CHANGE, indexFactor, indexOffset);
		double sMse = MathUtil.mse(stretchedTestFluxArray, this.targetFluxArray) / this.fluxVariance;
		double stcMse = MathUtil.mse(stretchedTestTrendChangeArray, this.targetTrendChangeArray) / this.trendChangeVariance;
		double tcw = this.trendChangeWeight;
		return sMse * (1 - tcw) + stcMse * tcw;
	}
}
