package jhs.lc.opt;

import jhs.math.smoothing.GaussianSmoother;
import jhs.math.util.MathUtil;

public class SmoothLossFunction extends AbstractLossFunction {
	private final GaussianSmoother smoother;
	private final double[] smoothTargetFluxArray;
	
	public SmoothLossFunction(SolutionSampler sampler, double[] fluxArray, double sdFraction) {
		super(sampler, 0, fluxArray);
		double xSD = sdFraction * fluxArray.length;
		this.smoother = new GaussianSmoother(xSD, 0.001);
		this.smoothTargetFluxArray = this.smoother.smooth(fluxArray);
	}
	
	@Override
	protected final double baseLoss(double[] testFluxArray) {
		double[] smoothTestFluxArray = this.smoother.smooth(testFluxArray);
		return Math.sqrt(MathUtil.mse(smoothTestFluxArray, this.smoothTargetFluxArray));
	}
}
