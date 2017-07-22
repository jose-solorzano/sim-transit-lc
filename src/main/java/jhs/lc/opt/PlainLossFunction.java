package jhs.lc.opt;

import jhs.math.smoothing.GaussianSmoother;
import jhs.math.util.MathUtil;

public class PlainLossFunction extends AbstractLossFunction {
	private static final double MSE_FACTOR = 1E7;
	private final double[] targetFluxArray;
	private final double variance;
	
	public PlainLossFunction(SolutionSampler sampler, double[] fluxArray) {
		super(sampler, 1.0);
		this.targetFluxArray = fluxArray;
		this.variance = MathUtil.variance(fluxArray);
	}
	
	@Override
	protected final double baseLoss(double[] testFluxArray) {
		return Math.log1p(MSE_FACTOR * MathUtil.mse(testFluxArray, this.targetFluxArray) / this.variance);
	}
}
