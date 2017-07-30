package jhs.lc.opt;

import jhs.lc.data.LightCurve;
import jhs.math.util.MathUtil;
import org.apache.commons.math.FunctionEvaluationException;

public class PrimaryLossFunction extends AbstractLossFunction {
	private static final double MSE_FACTOR = 1E7;
	private static final double WLF = 0.09;
		
	private final double w0, w1, w2;
	private final double[] targetFluxArray;
	private final double[] targetTrendArray;
	private final double[] targetTrendChangeArray;
	private final double fluxVariance;
	private final double trendVariance;
	private final double trendChangeVariance;

	public PrimaryLossFunction(SolutionSampler sampler, double[] targetFluxArray, double w0, double w1, double w2) {
		super(sampler, 1.0);
		this.w0 = w0;
		this.w1 = w1;
		this.w2 = w2;
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
	
	@Override
	protected final double baseLoss(double[] testFluxArray) {
		double[] testTrendArray = trendProfile(testFluxArray);
		double[] testTrendChangeArray = trendProfile(testTrendArray);
		double sMse = MathUtil.mse(testFluxArray, this.targetFluxArray) / this.fluxVariance;
		double stMse = MathUtil.mse(testTrendArray, this.targetTrendArray) / this.trendVariance;
		double stcMse = MathUtil.mse(testTrendChangeArray, this.targetTrendChangeArray) / this.trendChangeVariance;
		return Math.log1p(MSE_FACTOR * (sMse * w0 + stMse * w1 + stcMse * w2) / (w0 + w1 + w2));
	}
	
	public double fluxLoss(double[] testFluxArray) {
		return MathUtil.mse(testFluxArray, this.targetFluxArray) / this.fluxVariance;
	}
	
	public double trendLoss(double[] testFluxArray) {
		double[] testTrendArray = trendProfile(testFluxArray);
		return MathUtil.mse(testTrendArray, this.targetTrendArray) / this.trendVariance;
	}
	
	public double trendChangeLoss(double[] testFluxArray) {
		double[] testTrendChangeArray = trendChangeProfile(testFluxArray);
		return MathUtil.mse(testTrendChangeArray, this.targetTrendChangeArray) / this.trendChangeVariance;
	}

}
