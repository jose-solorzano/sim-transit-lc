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
	private int maxSubspaceSize = 4;
	private double initialGradientFactor = 0.1;
	private double gfAlpha = 0.2;
	private double searchFactor = 2.0;
	private double numPointsFactor = 2.0;
	
	private RealConvergenceChecker convergenceChecker = new GradientReductionConvergenceChecker(0.1, 0.003);
	
	private int numEvaluations = 0;
	
	public ApproximateGradientDescentOptimizer(Random random) {
		super();
		this.random = random;
	}
	
	public final int getMaxSubspaceSize() {
		return maxSubspaceSize;
	}

	public final void setMaxSubspaceSize(int maxSubspaceSize) {
		this.maxSubspaceSize = maxSubspaceSize;
	}

	public final double getNumPointsFactor() {
		return numPointsFactor;
	}

	public final void setNumPointsFactor(double numPointsFactor) {
		this.numPointsFactor = numPointsFactor;
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
		int[] subspace = this.createSubspace(epsilon.length);
		GradientInfo gradientInfo = this.gradient(pointValue, errorFunction, epsilon, subspace);
		AdvanceResults ar = this.searchInGradient(errorFunction, pointValue, gradientInfo.gradient, gradientFactor);
		if(ar == null || ar.pointValue.getValue() > pointValue.getValue()) {
			ar = this.selectBest(gradientInfo.testPointValues, gradientFactor);
		}
		return ar;
	}
	
	private AdvanceResults selectBest(RealPointValuePair[] pointValues, double gradientFactor) {
		RealPointValuePair minPv = MathUtil.min(pointValues, pv -> pv.getValue());
		return new AdvanceResults(minPv, gradientFactor);
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

	public final GradientInfo gradient(RealPointValuePair pointValue, MultivariateRealFunction errorFunction, double[] epsilon, int[] subspace) throws FunctionEvaluationException {
		int numPoints = (int) Math.ceil(this.numPointsFactor * (1 + Math.sqrt(1 + 8 * subspace.length)) / 2); 
		RealPointValuePair[] pointValues = new RealPointValuePair[numPoints];
		pointValues[0] = pointValue;
		for(int i = 1; i < numPoints; i++) {
			pointValues[i] = this.smallDisplacement(pointValue, errorFunction, epsilon, subspace);
		}
		int numParams = epsilon.length;
		double[] gradient = this.gradient(numParams, pointValues);
		return new GradientInfo(gradient, pointValues);
	}
	
	private double[] gradient(int numParams, RealPointValuePair[] pointValues) {
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
		MathUtil.divideInPlace(vectorSum, (double) count / numParams);
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
	
	private RealPointValuePair smallDisplacement(RealPointValuePair point, MultivariateRealFunction errorFunction, double[] epsilon, int[] subspace) throws FunctionEvaluationException {
		double factor = 1.0 / Math.sqrt(subspace.length);
		Random r = this.random;
		double[] vector = point.getPointRef();
		double[] newVector = Arrays.copyOf(vector, vector.length);
		for(int si = 0; si < subspace.length; si++) {
			int i = subspace[si];
			newVector[i] = vector[i] + r.nextGaussian() * epsilon[i] * factor;
		}
		this.numEvaluations++;
		double error = errorFunction.value(newVector);
		return new RealPointValuePair(newVector, error);
	}

	private int[] createSubspace(int vectorLength) {
		int[] vars = ArrayUtil.indexIdentity(vectorLength);
		int maxSS = this.maxSubspaceSize;
		if(maxSS >= vectorLength) {
			return vars;
		}
		ArrayUtil.shuffle(vars, random);
		return Arrays.copyOf(vars, maxSS);
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
	
	private static class GradientInfo {
		private final double[] gradient;
		private final RealPointValuePair[] testPointValues;
		
		public GradientInfo(double[] gradient, RealPointValuePair[] testPointValues) {
			super();
			this.gradient = gradient;
			this.testPointValues = testPointValues;
		}
	}
}
