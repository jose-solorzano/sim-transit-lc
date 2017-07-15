package jhs.math.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class TestMathUtil {
	@Test
	public void testUniformSymmetric() {
		Random random = new Random(11);
		double sd = 1.0;
		int length = 200;
		double[] values = MathUtil.sampleUniformSymmetric(random, sd, length);
		assertEquals(MathUtil.mean(values), 0, 0.03);
		double esd = MathUtil.standardDev(values, 0);
		assertEquals(sd, esd, 0.03);
	}
	
	@Test
	public void tesTriangular() {
		Random random = new Random(11);		
		int length = 10000;
		double[] unifValues = MathUtil.sampleUniform(random, length);
		double[] values = MathUtil.sampleTriangular(random, length);
		assertEquals(MathUtil.mean(values), 0.5, 0.03);
		double usd = MathUtil.standardDev(unifValues, 0.5);		
		double tsd = MathUtil.standardDev(values, 0.5);		
		assertTrue(tsd > usd + 0.04);
		double min = MathUtil.min(values);
		double max = MathUtil.max(values);
		assertEquals(0, min, 0.03);
		assertEquals(1, max, 0.03);
	}
			
}
