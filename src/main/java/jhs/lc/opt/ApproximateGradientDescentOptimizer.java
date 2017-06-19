package jhs.lc.opt;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;

import jhs.math.optimization.GradientReductionConvergenceChecker;
import jhs.math.regression.linear.WeightedLinearRegression;

public class ApproximateGradientDescentOptimizer {
	private final Random random;
	private int numPointsForGradientEst = 20;
	private int maxIterations = 2000;
	private int maxSearchIterations = 10;
	private double epsilon = 1E-5;
	private double initialGradientFactor = 0.1;
	private double gfAlpha = 0.1;
	private RealConvergenceChecker convergenceChecker = new GradientReductionConvergenceChecker(0.1, 0.003);
	
	public ApproximateGradientDescentOptimizer(Random random) {
		super();
		this.random = random;
	}
	
	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public RealPointValuePair optimize(MultivariateRealFunction errorFunction, double[] initialPoint) throws MathException {
		double error = errorFunction.value(initialPoint);
		RealPointValuePair current = new RealPointValuePair(initialPoint, error);
		this.informProgress(0, current);
		int n = this.maxIterations;
		RealConvergenceChecker cc = this.convergenceChecker;
		double gf = this.initialGradientFactor;
		double gfAlpha = this.gfAlpha;
		for(int i = 1; i <= n; i++) {
			AdvanceResults advance = this.advance(errorFunction, current, gf);		
			if(advance != null) {
				RealPointValuePair nextPoint = advance.pointValue;
				if(nextPoint.getValue() <= current.getValue()) {
					this.informProgress(i, nextPoint);
					if(cc.converged(i, current, nextPoint)) {
						break;
					}
					current = nextPoint;
					gf = gfAlpha * advance.recommendedGradientFactor + (1 - gfAlpha) * gf;
				}
			}
		}
		return current;
	}
	
	protected void informProgress(int iteration, RealPointValuePair pointValue) {		
	}
	
	private AdvanceResults advance(MultivariateRealFunction errorFunction, RealPointValuePair pointValue, double gradientFactor) throws MathException {
		double[] gradient = this.gradient(pointValue, errorFunction);
		return this.advance(errorFunction, pointValue, gradient, gradientFactor);
	}
	
	private AdvanceResults advance(MultivariateRealFunction errorFunction, RealPointValuePair pointValue, double[] gradient, double gradientFactor) throws MathException {
		double[] basePoint = changeParameters(pointValue.getPointRef(), gradient, gradientFactor);
		double baseError = errorFunction.value(basePoint);
		double factor = baseError < pointValue.getValue() ? 2.0 : 0.5;
		double gf = gradientFactor;
		int n = this.maxSearchIterations;
		for(int i = 0; i < n; i++) {
			double newGf = gf * factor;
			double[] testPoint = changeParameters(pointValue.getPointRef(), gradient, newGf);
			double testError = errorFunction.value(testPoint);
			if(testError < baseError) {
				basePoint = testPoint;
				baseError = testError;
				gf = newGf;
			}
			else {
				break;
			}
		}			
		return new AdvanceResults(new RealPointValuePair(basePoint, baseError), gf);
	}

	
	private static double[] changeParameters(double[] parameters, double[] gradients, double gradientFactor) {
		int len = parameters.length;
		double[] newParameters = Arrays.copyOf(parameters, len);
		for(int i = 0; i < len; i++) {
			newParameters[i] = parameters[i] - gradients[i] * gradientFactor; 
		}
		return newParameters;
	}

	public final double[] gradient(RealPointValuePair pointValue, MultivariateRealFunction errorFunction) throws MathException {
		int np = this.numPointsForGradientEst;
		RealPointValuePair[] pointValues = new RealPointValuePair[np];
		pointValues[0] = pointValue;
		for(int i = 1; i < np; i++) {
			pointValues[i] = this.smallDisplacement(pointValue, errorFunction);
		}
		return this.gradient(pointValues, pointValue.getPointRef().length);
	}
	
	private double[] gradient(RealPointValuePair[] pointValues, int numParameters) {
		double[] gradient = new double[numParameters];
		for(int p = 0; p < numParameters; p++) {
			WeightedLinearRegression regression = new WeightedLinearRegression();
			for(RealPointValuePair pv : pointValues) {
				regression.addData(1.0, pv.getPointRef()[p], pv.getValue());
			}
			double slope = regression.getSlope();
			if(Double.isNaN(slope)) {
				throw new IllegalStateException("Slope is NaN!");
			}
			gradient[p] = slope;
		}
		return gradient;
	}
	
	private RealPointValuePair smallDisplacement(RealPointValuePair point, MultivariateRealFunction errorFunction) throws MathException {
		Random r = this.random;
		double[] vector = point.getPointRef();
		int length = vector.length;
		double e = this.epsilon / Math.sqrt(length);
		double[] newVector = new double[length];
		for(int i = 0; i < length; i++) {
			newVector[i] = vector[i] + r.nextGaussian() * e;
		}
		double error = errorFunction.value(newVector);
		return new RealPointValuePair(newVector, error);
	}
	
	private static class AdvanceResults {
		private final RealPointValuePair pointValue;
		private final double recommendedGradientFactor;
		
		public AdvanceResults(RealPointValuePair pointValue,
				double recommendedGradientFactor) {
			super();
			this.pointValue = pointValue;
			this.recommendedGradientFactor = recommendedGradientFactor;
		}
	}
}
