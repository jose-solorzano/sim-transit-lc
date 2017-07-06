package jhs.lc.opt;

public class SeriesUtil {
	public static double centerOfSquaredDev(double[] series, double mean) {
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < series.length; i++) {
			double dev = series[i] - mean;
			double weight = dev * dev;
			weightSum += weight;
			sum += weight * i;
		}
		return weightSum == 0 ? series.length / 2.0 : sum / weightSum;
	}
	
	public static double seriesWidth(double[] series, double mean, double cosd) {
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < series.length; i++) {
			double dev = series[i] - mean;
			double posDev = i - cosd;
			double weight = Math.abs(dev);
			weightSum += weight;
			sum += weight * (posDev * posDev);
		}
		return weightSum == 0 ? 0 : Math.sqrt(sum / weightSum);		
	}	
	
	public static double[] skewSeries(double[] series, double mean, double expectedCosd, double expectedWidth) {
		if(expectedWidth == 0) {
			throw new IllegalArgumentException("expectedWidth: " + expectedWidth);
		}
		double cosd = centerOfSquaredDev(series, mean);
		double width = seriesWidth(series, mean, cosd);
		int length = series.length;
		double factor = width / expectedWidth;
		double[] skewedSeries = new double[length];
		for(int i = 0; i < length; i++) {
			double xFromCosd = i - expectedCosd;
			double xOrigFromCosd = xFromCosd * factor;
			double origIndex = cosd + xOrigFromCosd;
			int origIndexFloor = (int) Math.floor(origIndex);
			int origIndexCeil = (int) Math.ceil(origIndex);
			if(origIndexFloor < 0 || origIndexCeil >= length) {
				skewedSeries[i] = mean;
			}
			else {
				double k = origIndex - origIndexFloor;
				double f1 = series[origIndexFloor];
				double f2 = series[origIndexCeil];
				skewedSeries[i] = f1 * (1 - k) + f2 * k;
			}
		}
		return skewedSeries;
	}
}
