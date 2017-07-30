package jhs.lc.opt;

import jhs.lc.data.LightCurve;
import jhs.math.util.MathUtil;
import org.apache.commons.math.FunctionEvaluationException;

public class WeightedLossFunction extends AbstractLossFunction {
	private final double[] targetFluxArray;
	private final double[] weights;
	
	public WeightedLossFunction(SolutionSampler sampler, double[] targetFluxArray, WeightType weightType, double wlf) {
		super(sampler, 1.0);
		this.targetFluxArray = targetFluxArray;
		switch(weightType) {
		case TREND:
			this.weights = MathUtil.abs(trendProfile(targetFluxArray, wlf));
			break;
		case TREND_CHANGE:
			this.weights = MathUtil.abs(trendProfile(trendProfile(targetFluxArray, wlf), wlf));
			break;
		default:
			throw new IllegalArgumentException("weightType: " + weightType);
		}
	}

	private static double[] trendProfile(double[] fluxArray, double wlf) {
		int wl = (int) Math.round(wlf * fluxArray.length);
		if(wl < 3) {
			wl = 3;
		}
		return LightCurve.trendProfile(fluxArray, wl);
	}
	
	@Override
	protected final double baseLoss(double[] testFluxArray) {
		double[] targetFluxArray = this.targetFluxArray;		
		double[] weights = this.weights;
		int length = targetFluxArray.length;
		if(length != testFluxArray.length) {
			throw new IllegalArgumentException("Length: " + testFluxArray.length);
		}
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < length; i++) {
			double weight = weights[i];
			double diff = testFluxArray[i] - targetFluxArray[i];
			sum += (diff * diff) * weight;
			weightSum += weight;
		}
		return weightSum == 0 ? 0 : sum / weightSum;				
	}	
}
