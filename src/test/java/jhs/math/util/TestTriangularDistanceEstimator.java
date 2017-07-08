package jhs.math.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class TestTriangularDistanceEstimator {
	@Test
	public void testDistance() {
		Random random = new Random(11);
		double d0 = this.distance(random, 0, 0);
		assertEquals(0, d0, 0.001);
		double d1 = this.distance(random, 1.0, 0);
		assertEquals(0.5, d1, 0.001);
		double d2 = this.distance(random, -0.5, 0.8);
		assertEquals(0.5, d2, 0.001);
		double d3 = this.distance(random, -0.5, -0.8);
		assertEquals(0.5, d3, 0.001);
		double d4 = this.distance(random, -0.5, 0);
		assertEquals(0.5, d4, 0.001);
		double tf = Math.tan(Math.PI / 6);
		double d5 = this.distance(random, 0, -tf);
		assertEquals(0.5, d5, 0.001);
		double d6 = this.distance(random, 0, +tf);
		assertEquals(0.5, d6, 0.001);
	}
	
	@Test
	public void testNonNegative() {
		Random random = new Random(17);
		TriangularDistanceEstimator est = new TriangularDistanceEstimator();
		int numVars = 3;
		int numTests = 500;
		for(int i = 0; i < numTests; i++) {
			double[] point1 = MathUtil.sampleGaussian(random, 1.0, numVars);
			double[] point2 = MathUtil.sampleGaussian(random, 1.0, numVars);
			double d = est.triangularDistance(point1, 0, point1.length, point2, 0);
			assertTrue(d >= 0);
		}
	}

	private double distance(Random random, double x, double y) {
		double[] origin = MathUtil.sampleGaussian(random, 1.0, 2);
		double[] point = MathUtil.add(origin, new double[] { x, y });
		return new TriangularDistanceEstimator().triangularDistance(point, 0, point.length, origin, 0);
	}
	
}
