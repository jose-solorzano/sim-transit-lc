package jhs.lc.opt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import jhs.math.util.ArrayUtil;

import org.apache.commons.math.MathException;
import org.junit.Test;

public class TestLightCurveMatcher {
	@Test
	public void testFlexibleMatching() throws MathException {
		this.testFlexibleMatching(false);
		this.testFlexibleMatching(true);
	}
		
	private void testFlexibleMatching(boolean shiftOnly) throws MathException {		
		Random random = new Random(11);
		int length = 500;
		double baseSd = 40.0;
		double baseSeparation = 50;
		double[] targetFluxArray = this.gaussianFluxArray(random, 0.4, length, baseSd, baseSeparation);
		double[] weights = ArrayUtil.repeat(1.0, length);
		LightCurveMatcher matcher = new LightCurveMatcher(random, targetFluxArray, weights);
				
		for(int t = 0; t < 30; t++) {
			double sd = shiftOnly ? baseSd : random.nextDouble() * 20.0 + 25.0;
			double separation = baseSeparation * sd / baseSd;
			double[] compressed1 = this.gaussianFluxArray(random, 0.4, length, sd, separation);
			FlexibleLightCurveMatchingResults results1 = matcher.flexibleMeanSquaredError(compressed1, shiftOnly);
			assertEquals(sd / baseSd, results1.getA(), 0.08);
			assertEquals(0, results1.getMinimizedError(), 0.0007);
			assertTrue(results1.getBendMetric() < 0.3);
		}
		double[] compressed2 = this.gaussianFluxArray(random, 0.1, length, 40, 10);
		FlexibleLightCurveMatchingResults results2 = matcher.flexibleMeanSquaredError(compressed2, shiftOnly);
		assertTrue(results2.getMinimizedError() > 0.01);
	}
	
	private double[] gaussianFluxArray(Random random, double maxDrop, int length, double sdDips, double separation) {
		double[] fluxArray = new double[length];
		double minX = 3 * sdDips;
		double maxX = length - minX - separation;
		double p = random.nextDouble();
		double centerX1 = maxX * p + minX * (1 - p);
		double centerX2 = centerX1 + separation;
		double varT2 = 2 * sdDips * sdDips;
		for(int i = 0; i < length; i++) {
			double diff1 = i - centerX1;
			double diff2 = i - centerX2;
			double p1 = Math.exp(-(diff1 * diff1) / varT2);
			double p2 = Math.exp(-(diff2 * diff2) / varT2);
			fluxArray[i] = 1.0 - (p1 + p2) * maxDrop;
		}
		return fluxArray;
	}
}
