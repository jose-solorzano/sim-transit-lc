package jhs.math.optimization;

import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;

import jhs.math.util.MathUtil;

public class GradientReductionConvergenceChecker implements RealConvergenceChecker {
	private final double alpha;
	private final double ratioThreshold;
	private double gradientRunningAverage;
	private RealPointValuePair firstPointValue;

	public GradientReductionConvergenceChecker() {
		this(0.1, 0.003);
	}

	public GradientReductionConvergenceChecker(double alpha, double ratioThreshold) {
		super();
		this.alpha = alpha;
		this.ratioThreshold = ratioThreshold;
	}

	public boolean converged(int iteration, RealPointValuePair oldPointValue, RealPointValuePair newPointValue) {
		RealPointValuePair fpv = this.firstPointValue;
		if(iteration <= 0 || fpv == null) {
			this.firstPointValue = newPointValue;
			if(oldPointValue != null) {
				double valueDiff = newPointValue.getValue() - oldPointValue.getValue();
				double distance = MathUtil.euclideanDistance(oldPointValue.getPointRef(), newPointValue.getPointRef());
				double gradient = distance == 0 ? 0 : valueDiff / distance;
				this.gradientRunningAverage = gradient;
			}
			else {
				this.gradientRunningAverage = 0;
			}
			return false;
		}
		else {
			double alpha = this.alpha;
			double currentRawGradient = gradient(oldPointValue, newPointValue);
			if(!Double.isNaN(currentRawGradient)) {
				double newGradientRunningAverage = currentRawGradient * alpha + this.gradientRunningAverage * (1 - alpha);
				this.gradientRunningAverage = newGradientRunningAverage;
				double totalGradient = gradient(fpv, newPointValue);
				if(!Double.isNaN(totalGradient) && totalGradient != 0) {
					double ratio = newGradientRunningAverage / totalGradient;
					return Math.abs(ratio) <= this.ratioThreshold;
				}
			}
			return false;
		}
	}
	
	private static double gradient(RealPointValuePair oldPointValue, RealPointValuePair newPointValue) {
		double valueDiff = newPointValue.getValue() - oldPointValue.getValue(); 
		double distance = MathUtil.euclideanDistance(oldPointValue.getPointRef(), newPointValue.getPointRef());
		double gradient = distance == 0 ? Double.NaN : valueDiff / distance;
		return gradient;
	}

}
