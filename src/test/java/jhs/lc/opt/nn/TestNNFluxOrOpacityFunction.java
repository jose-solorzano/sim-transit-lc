package jhs.lc.opt.nn;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import jhs.lc.opt.nn.NNFluxFunctionSource;
import jhs.lc.opt.nn.NNFluxOrOpacityFunction;
import jhs.math.util.MathUtil;

public class TestNNFluxOrOpacityFunction {
	private static double SR2 = Math.sqrt(2);
	
	@Test
	public void testScaling() {
		double imageWidth = 7.0;
		double imageHeight = 3.0;
		double dim = Math.sqrt((imageWidth * imageWidth + imageHeight * imageHeight) / 2);
		double scale = NNFluxOrOpacityFunction.SF / dim;
		Random random = new Random(11);
		int n = 5000;
		double[] values = new double[n];
		for(int i = 0; i < n; i++) {
			double x = imageWidth * (random.nextDouble() - 0.5);
			double y = imageHeight * (random.nextDouble() - 0.5);
			double scaledX = x * scale;
			double scaledY = y * scale;
			values[i] = (scaledX + scaledY) / SR2;
		}
		double mean = MathUtil.mean(values);
		double sd = MathUtil.standardDev(values, mean);
		assertEquals(0, mean, 0.01);
		assertEquals(1.0, sd, 0.01);
	}
}
