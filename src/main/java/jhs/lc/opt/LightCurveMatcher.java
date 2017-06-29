package jhs.lc.opt;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestFailure;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.RealPointValuePair;

public class LightCurveMatcher {
	private static final double MAX_FLUX = 1.0;
	private static final int MAX_AGD_ITERATIONS = 30;
	private final Random random;
	private final double[] targetFluxArray;
	private final double[] weights;
	private final double targetCenterOfMass;
	private final double targetMassDeviation;

	public LightCurveMatcher(Random random, double[] targetFluxArray,
			double[] weights) {
		super();
		this.random = random;
		this.targetFluxArray = targetFluxArray;
		this.weights = weights;
		this.targetCenterOfMass = this.centerOfMass(targetFluxArray);
		this.targetMassDeviation = this.massDeviation(targetFluxArray, this.targetCenterOfMass);
	}

	public final double ordinaryMeanSquaredError(double[] testFluxArray) {
		double[] targetFluxArray = this.targetFluxArray;		
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
	
	public final FlexibleLightCurveMatchingResults flexibleMeanSquaredError(final double[] testFluxArray, final boolean shiftOnly) throws FunctionEvaluationException {
		double testCenterOfMass = this.centerOfMass(testFluxArray);
		double testMassDeviation = shiftOnly ? Double.NaN : this.massDeviation(testFluxArray, testCenterOfMass);
		double initSkewA = shiftOnly ? 1.0 : testMassDeviation / this.targetMassDeviation;				
		double initSkewB = testCenterOfMass - this.targetCenterOfMass * initSkewA;
		ApproximateGradientDescentOptimizer agd = new ApproximateGradientDescentOptimizer(random) {
			@Override
			protected final void informProgress(int iteration, RealPointValuePair pointValue) {
			}			
		};
		agd.setMaxIterations(MAX_AGD_ITERATIONS);
		double length = this.targetFluxArray.length;
		double[] initialPoint = shiftOnly ? new double[] { initSkewB } : new double[] { initSkewB, initSkewA };
		double[] epsilon = shiftOnly ? new double[] { 1.0 } : new double[] { 1.0, 1.0 / length };
		MultivariateRealFunction errorFunction = new MultivariateRealFunction() {
			@Override
			public final double value(double[] params) throws FunctionEvaluationException {
				double skewB = params[0];
				double skewA = shiftOnly ? 1.0 : params[1];
				return meanSquaredError(testFluxArray, skewA, skewB);
			}
		};
		RealPointValuePair result = agd.optimize(errorFunction, initialPoint, epsilon);
		double minimizedError = result.getValue();
		double[] params = result.getPointRef();
		double skewB = params[0];
		double skewA = shiftOnly ? 1.0 : params[1];
		double skewADiff = skewA - 1.0;
		double skewBDiff = skewB / length;
		double bendMetric = (skewADiff * skewADiff + skewBDiff * skewBDiff) / 2.0;
		return new FlexibleLightCurveMatchingResults(skewA, skewB, minimizedError, bendMetric);
	}

	private double meanSquaredError(double[] testFluxArray, double skewA, double skewB) {
		double[] targetFluxArray = this.targetFluxArray;		
		int length = targetFluxArray.length;
		double[] skewedTestFluxArray = new double[length];
		for(int i = 0; i < length; i++) {
			double origIndex = i * skewA + skewB;
			int origIndexFloor = (int) Math.floor(origIndex);
			int origIndexCeil = (int) Math.ceil(origIndex);
			if(origIndexFloor < 0 || origIndexCeil >= testFluxArray.length) {
				skewedTestFluxArray[i] = MAX_FLUX;
			}
			else {
				double f1 = testFluxArray[origIndexFloor];
				double f2 = testFluxArray[origIndexCeil];
				double k = origIndex - origIndexFloor;
				skewedTestFluxArray[i] = f1 * (1 - k) + f2 * k;
			}
		}
		return this.ordinaryMeanSquaredError(skewedTestFluxArray);
	}
	
	private double centerOfMass(double[] fluxArray) {
		int length = fluxArray.length;
		double sumPos = 0;
		double sumWeight = 0;
		for(int i = 0; i < length; i++) {
			double weight = 1.0 - fluxArray[i];
			if(weight < 0) {
				weight = 0;
			}
			sumWeight += weight;
			sumPos += i * weight;
		}
		return sumWeight == 0 ? 0.5 * fluxArray.length : sumPos / sumWeight;
	}
	
	private double massDeviation(double[] fluxArray, double centerOfMass) {
		int length = fluxArray.length;
		double sumDev = 0;
		double sumWeight = 0;
		for(int i = 0; i < length; i++) {
			double weight = 1.0 - fluxArray[i];
			if(weight < 0) {
				weight = 0;
			}
			sumWeight += weight;
			double posDiff = i - centerOfMass;
			sumDev += posDiff * posDiff * weight;
		}
		return sumWeight == 0 ? 0 : Math.sqrt(sumDev / sumWeight);
	}
}
