package jhs.lc.opt;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class TestSeriesUtil {

	@Test
	public void testSkewSeries() {
		Random random = new Random(17);
		int length = 200;
		double[] baseSeries = this.series(random, length);
		double baseCosd = SeriesUtil.centerOfSquaredDev(baseSeries, 0);
		double baseWidth = SeriesUtil.seriesWidth(baseSeries, 0, baseCosd);
		double[] baseNormSeries = SeriesUtil.skewSeries(baseSeries, 0, baseCosd, baseWidth);
		assertArrayEquals(baseSeries, baseNormSeries, 0.000001);
		System.out.println("BaseCosd: " + baseCosd);
		System.out.println("BaseWidth: " + baseWidth);
		for(int i = 0; i < 20; i++) {
			double[] testSeries = this.series(random, length);
			double[] normSeries = SeriesUtil.skewSeries(testSeries, 0, baseCosd, baseWidth);
			assertArrayEquals(baseSeries, normSeries, 0.03);
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
