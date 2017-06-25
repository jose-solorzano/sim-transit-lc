package jhs.lc.opt;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import jhs.math.util.MathUtil;

public class TestCircuitSearchOptimizer {
	@Test
	public void testMultiMinimaOptimization() throws Exception {
		int populationSize = 20;
		int vectorLength = 3;
		Random random = new Random(29 + 1001);
		CircuitSearchOptimizer optimizer = new CircuitSearchOptimizer(random, populationSize) {
			@Override
			protected void informProgress(int iteration, RealPointValuePair pointValue) {
				System.out.println("   -- At iteration " + iteration + ": " + pointValue.getValue());
			}
		};
		optimizer.setExpansionFactor(3.0);
		optimizer.setMaxIterationsWithClustering(200);
		optimizer.setDisplacementFactor(0.03);
		optimizer.setCircuitShuffliness(1.0);
		CustomErrorFunction errorFunction = new CustomErrorFunction();
		RealPointValuePair rsr = this.randomSearch(errorFunction, random, vectorLength);
		RealPointValuePair result = optimizer.optimize(errorFunction, vectorLength);
		System.out.println("RandSearch: " + Arrays.toString(rsr.getPoint()) + " | " + rsr.getValue());
		System.out.println("Result: " + Arrays.toString(result.getPoint()) + " | " + result.getValue());
		assertTrue(result.getValue() < rsr.getValue());
	}

	@Test
	public void testConcaveFunctionOptimization() throws Exception {
		int populationSize = 11;
		int vectorLength = 10;
		Random random = new Random(17 + 1006);
		CircuitSearchOptimizer optimizer = new CircuitSearchOptimizer(random, populationSize) {
			@Override
			protected void informProgress(int iteration, RealPointValuePair pointValue) {
				System.out.println("   -- At iteration " + iteration + ": " + pointValue.getValue());
			}
		};
		optimizer.setMaxIterationsWithClustering(0);
		optimizer.setDisplacementFactor(0.3);
		optimizer.setCircuitShuffliness(0);
		optimizer.setExpansionFactor(2.0);
		optimizer.setConvergeDistance(0.003);
		double[] minimum = MathUtil.sampleGaussian(random, 1.0, vectorLength);
		CircuitSearchEvaluator errorFunction = new DistanceSqErrorFunction(minimum);
		RealPointValuePair result = optimizer.optimize(errorFunction, vectorLength);
		double[] resultPoint = result.getPointRef();
		int numMatches = 0;
		for(int i = 0; i < minimum.length; i++) {
			double diff = Math.abs(resultPoint[i] - minimum[i]);
			if(diff < 0.01) {
				numMatches++;
			}
		}
		double distanceSq = MathUtil.euclideanDistanceSquared(resultPoint, minimum) / vectorLength;
		System.out.println("Distance/N: " + Math.sqrt(distanceSq));
		System.out.println("NumMatches: " + numMatches);
		assertTrue((double) numMatches / minimum.length > 0.95);
		assertEquals(0, distanceSq, 0.01);
	}

	private RealPointValuePair randomSearch(CustomErrorFunction function, Random random, int vectorLength) throws Exception {
		int n = 200;
		double minError = Double.POSITIVE_INFINITY;
		double[] bestPoint = null;
		for(int i = 0; i < n; i++) {
			double[] point = MathUtil.sampleGaussian(random, 1.0, vectorLength);
			double error = function.value(point);
			if(error < minError) {
				minError = error;
				bestPoint = point;
			}
		}
		return new RealPointValuePair(bestPoint, minError);
	}
	
	private static class DistanceSqErrorFunction implements CircuitSearchEvaluator {
		private final double[] minimum;
		
		public DistanceSqErrorFunction(double[] minimum) {
			super();
			this.minimum = minimum;
		}
		
		@Override
		public CircuitSearchParamEvaluation evaluate(double[] params) {
			return new CircuitSearchParamEvaluation(
					MathUtil.euclideanDistanceSquared(params, this.minimum),
					params);
		}		
	}
	
	private static class CustomErrorFunction implements CircuitSearchEvaluator {
		@Override
		public CircuitSearchParamEvaluation evaluate(double[] params) throws FunctionEvaluationException, IllegalArgumentException {
			return new CircuitSearchParamEvaluation(this.value(params), params);
		}
		
		public final double value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
			double x = point[0];
			double y = point[1];
			double z = point[2];
			return 
				1.5 * Math.sin(x * 4 * Math.PI + 0.3) +
				1.4 * Math.sin(y * 3 * Math.PI + 0.4) +
				1.3 * Math.sin(z * 2 * Math.PI + 0.5) +
				MathUtil.square(x - 0.7) * 0.1 +
				MathUtil.square(y + 0.8) * 0.2 +
				MathUtil.square(z - 0.9) * 0.3;
		}		
	}

}
