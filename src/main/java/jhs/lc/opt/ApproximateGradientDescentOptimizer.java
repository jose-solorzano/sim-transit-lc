package jhs.lc.opt;


import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;

import jhs.math.optimization.GradientReductionConvergenceChecker;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

public class ApproximateGradientDescentOptimizer {
	private final Random random;
	private int maxIterations = 2000;
	private int maxSearchIterations = 10;
	private double initialGradientFactor = 0.1;
	private double gfAlpha = 0.2;
	private double searchFactor = 2.0;
	
	private RealConvergenceChecker convergenceChecker = new GradientReductionConvergenceChecker(0.1, 0.003);
	
	private int numEvaluations = 0;
	
	public ApproximateGradientDescentOptimizer(Random random) {
		super();
		this.random = random;
	}
	
	public final int getNumEvaluations() {
		return numEvaluations;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public int getMaxSearchIterations() {
		return maxSearchIterations;
	}

	public void setMaxSearchIterations(int maxSearchIterations) {
		this.maxSearchIterations = maxSearchIterations;
	}

	public double getInitialGradientFactor() {
		return initialGradientFactor;
	}

	public void setInitialGradientFactor(double initialGradientFactor) {
		this.initialGradientFactor = initialGradientFactor;
	}

	public double getGfAlpha() {
		return gfAlpha;
	}

	public void setGfAlpha(double gfAlpha) {
		this.gfAlpha = gfAlpha;
	}

	public double getSearchFactor() {
		return searchFactor;
	}

	public void setSearchFactor(double searchFactor) {
		this.searchFactor = searchFactor;
	}

	public RealConvergenceChecker getConvergenceChecker() {
		return convergenceChecker;
	}

	public void setConvergenceChecker(RealConvergenceChecker convergenceChecker) {
		this.convergenceChecker = convergenceChecker;
	}

	public Random getRandom() {
		return random;
	}

	public void setNumEvaluations(int numEvaluations) {
		this.numEvaluations = numEvaluations;
	}

	public RealPointValuePair optimize(MultivariateRealFunction errorFunction, double[] initialPoint, double[] epsilon) throws FunctionEvaluationException {
		this.numEvaluations = 1;
		double error = errorFunction.value(initialPoint);
		RealPointValuePair current = new RealPointValuePair(initialPoint, error);
		this.informProgress(0, current);
		int n = this.maxIterations;
		RealConvergenceChecker cc = this.convergenceChecker;
		double gf = this.initialGradientFactor;
		double gfAlpha = this.gfAlpha;
		for(int i = 1; i <= n; i++) {
			AdvanceResults advance = this.advance(errorFunction, current, gf, epsilon);		
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

	public RealPointValuePair doOneStep(RealPointValuePair pointValue, MultivariateRealFunction errorFunction, double gradientFactor, double[] epsilon) throws MathException {
		AdvanceResults advance = this.advance(errorFunction, pointValue, gradientFactor, epsilon);		
		if(advance != null) {
			RealPointValuePair nextPoint = advance.pointValue;
			if(nextPoint.getValue() <= pointValue.getValue()) {
				return nextPoint;				
			}
		}
		return pointValue;
	}
	
	private AdvanceResults advance(MultivariateRealFunction errorFunction, RealPointValuePair pointValue, double gradientFactor, double[] epsilon) throws FunctionEvaluationException {
		double[] gradient = this.gradient(pointValue, errorFunction, epsilon);
		return this.searchInGradient(errorFunction, pointValue, gradient, gradientFactor);
	}
	
	private AdvanceResults searchInGradient(MultivariateRealFunction errorFunction, RealPointValuePair pointValue, double[] gradient, double gradientFactor) throws FunctionEvaluationException {
		double[] basePoint = changeParameters(pointValue.getPointRef(), gradient, gradientFactor);
		this.numEvaluations++;
		double baseError = errorFunction.value(basePoint);
		double factor = baseError < pointValue.getValue() ? this.searchFactor : 1.0 / this.searchFactor;
		
		double gf = gradientFactor;
		int n = this.maxSearchIterations;
		for(int i = 0; i < n; i++) {
			double newGf = gf * factor;
			double[] testPoint = changeParameters(pointValue.getPointRef(), gradient, newGf);
			this.numEvaluations++;
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

	public final double[] gradient(RealPointValuePair pointValue, MultivariateRealFunction errorFunction, double[] epsilon) throws FunctionEvaluationException {
		int numParams = pointValue.getPointRef().length;
		int numPoints = (int) Math.ceil((1 + Math.sqrt(1 + 8 * numParams)) / 2); 
		RealPointValuePair[] pointValues = new RealPointValuePair[numPoints];
		pointValues[0] = pointValue;
		for(int i = 1; i < numPoints; i++) {
			pointValues[i] = this.smallDisplacement(pointValue, errorFunction, epsilon);
		}
		return this.gradient(numParams, pointValues, pointValue.getPointRef().length);
	}
	
	private double[] gradient(int numParams, RealPointValuePair[] pointValues, int numParameters) {
		int count = 0;
		double[] vectorSum = ArrayUtil.repeat(0.0, numParams);
		for(int i = 0; i < pointValues.length; i++) {
			for(int j = i + 1; j < pointValues.length; j++) {
				double[] vector = this.gradientVector(pointValues[i], pointValues[j]);
				MathUtil.addInPlace(vectorSum, vector);
				count++;
			}
		}
		if(count < numParams) {
			throw new IllegalStateException();
		}
		return vectorSum;
	}
	
	private double[] gradientVector(RealPointValuePair pointValue1, RealPointValuePair pointValue2) {
		double errorDiff = pointValue2.getValue() - pointValue1.getValue();
		double[] diffVector = MathUtil.subtract(pointValue2.getPointRef(), pointValue1.getPointRef());
		double diffMagnitude = MathUtil.vectorMagnitude(diffVector);
		if(diffMagnitude == 0) {
			return ArrayUtil.repeat(0.0, diffVector.length);
		}
		double slope = errorDiff / diffMagnitude;
		MathUtil.multiplyInPlace(diffVector, slope / diffMagnitude);		
		return diffVector;
	}
	
	private RealPointValuePair smallDisplacement(RealPointValuePair point, MultivariateRealFunction errorFunction, double[] epsilon) throws FunctionEvaluationException {
		Random r = this.random;
		double[] vector = point.getPointRef();
		int length = vector.length;
		double[] newVector = new double[length];
		for(int i = 0; i < length; i++) {
			newVector[i] = vector[i] + r.nextGaussian() * epsilon[i];
		}
		this.numEvaluations++;
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
