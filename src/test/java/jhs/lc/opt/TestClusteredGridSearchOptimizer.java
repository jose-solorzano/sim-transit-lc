package jhs.lc.opt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import jhs.lc.opt.ClusteredGridSearchOptimizer.Phase;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

public class TestClusteredGridSearchOptimizer {
	@Test
	public void testMultiMinimaOptimization() throws Exception {
		int numClusters = 7;
		int numParticlesPerCluster = 10;
		int vectorLength = 3;
		Random random = new Random(2042 + 1001);
		ClusteredGridSearchOptimizer optimizer = new ClusteredGridSearchOptimizer(random, numClusters, numParticlesPerCluster) {			
			@Override
			protected void informProgress(Phase phase, int iteration, RealPointValuePair pointValue) {
				System.out.println("   -- At iteration " + iteration + ": " + pointValue.getValue());
			}
		};
		optimizer.setMaxIterations(200);
		CustomErrorFunction errorFunction = new CustomErrorFunction();
		RealPointValuePair rsr = this.randomSearch(errorFunction, random, vectorLength);
		RealPointValuePair result = optimizer.optimize(vectorLength, errorFunction);
		System.out.println("RandSearch: " + Arrays.toString(rsr.getPoint()) + " | " + rsr.getValue());
		System.out.println("Result: " + Arrays.toString(result.getPoint()) + " | " + result.getValue());
		assertTrue(result.getValue() < rsr.getValue());
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
	
	private static class CustomErrorFunction implements ClusteredEvaluator {
		@Override
		public ClusteredParamEvaluation evaluate(double[] params) throws FunctionEvaluationException, IllegalArgumentException {
			return new ClusteredParamEvaluation(this.value(params), params);
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

		@Override
		public double[] recommendEpsilon(double[] params) {
			return ArrayUtil.repeat(1.0, params.length);
		}		
	}

}
