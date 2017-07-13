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
}
