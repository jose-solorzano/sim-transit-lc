package jhs.lc.opt;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import jhs.math.util.MathUtil;

public class TestSeriesUtil {
	@Test
	public void testMaxIgnoreError() {
		Random random = new Random(17);
		int length = 300;
		double[] baseSeries = this.series(random, length);
		double[] noiseSeries = MathUtil.addWhiteNoise(random, baseSeries, 0.001);
		double maxIgnoreError = 0.004;
		double baseCom = SeriesUtil.centerOfMass(baseSeries, maxIgnoreError, 0);
		double baseDev = SeriesUtil.massDeviation(baseSeries, maxIgnoreError, 0, baseCom);
		double noiseCom = SeriesUtil.centerOfMass(noiseSeries, maxIgnoreError, 0);
		double noiseDev = SeriesUtil.massDeviation(noiseSeries, maxIgnoreError, 0, noiseCom);
		
		System.out.println("ComDiff: " + (noiseCom - baseCom));
		System.out.println("DevDiff: " + (noiseDev - baseDev));
		
		assertEquals(baseCom, noiseCom, 0.004);
		assertEquals(baseDev, noiseDev, 0.004);
	}

	@Test
	public void testSkewSeries() {
		Random random = new Random(17);
		int length = 200;
		double[] baseSeries = this.series(random, length);
		double baseCosd = SeriesUtil.centerOfMass(baseSeries, 0, 0);
		double baseWidth = SeriesUtil.massDeviation(baseSeries, 0, 0, baseCosd);
		double[] baseNormSeries = SeriesUtil.stretchToMatch(baseSeries, 0, 0, baseCosd, baseWidth);
		assertArrayEquals(baseSeries, baseNormSeries, 0.000001);
		System.out.println("BaseCosd: " + baseCosd);
		System.out.println("BaseWidth: " + baseWidth);
		for(int i = 0; i < 20; i++) {
			double[] testSeries = this.series(random, length);
			double[] normSeries = SeriesUtil.stretchToMatch(testSeries, 0, 0, baseCosd, baseWidth);
			assertArrayEquals(baseSeries, normSeries, 0.03);
			double mse = MathUtil.mse(baseSeries, normSeries);
			assertEquals(0, mse, 0.00003);
		}
	}

	private final double[] series(Random random, int length) {
		double[] series = new double[length];
		double portion = 0.3 + 0.6 * random.nextDouble();
		double start = random.nextDouble() * (1.0 - portion);
		for(int i = 0; i < length; i++) {
			double x = (double) i / length;
			double sx = (x - start) * 3 * Math.PI / portion;
			if(sx >= 0 && sx <= 3 * Math.PI) {
				series[i] = Math.sin(sx);
			}
		}
		return series;
	}
}
