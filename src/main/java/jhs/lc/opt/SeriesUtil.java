package jhs.lc.opt;

public class SeriesUtil {
	public static double centerOfMass(double[] series, double maxIgnoreError, double meanValue) {
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < series.length; i++) {
			double dev = series[i] - meanValue;
			double weight = Math.abs(dev);
			if(weight > maxIgnoreError) {
				weightSum += weight;
				sum += weight * i;
			}
		}
		return weightSum == 0 ? series.length / 2.0 : sum / weightSum;
	}
	
	public static double massVariance(double[] series, double maxIgnoreError, double meanValue, double centerOfMass) {
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < series.length; i++) {
			double dev = series[i] - meanValue;
			double weight = Math.abs(dev);
			if(weight > maxIgnoreError) {
				double posDev = i - centerOfMass;
				weightSum += weight;
				sum += weight * (posDev * posDev);
			}
		}
		return weightSum == 0 ? 0 : sum / weightSum;		
	}	

	public static final double massDeviation(double[] series, double maxIgnoreError, double meanValue, double centerOfMass) {
		return Math.sqrt(massVariance(series, maxIgnoreError, meanValue, centerOfMass));
	}

	public static final double[] stretchToMatch(double[] series, double maxIgnoreError, double meanValue, double expectedCenterOfMass, double expectedDeviation) {
		if(expectedDeviation == 0) {
			throw new IllegalArgumentException("expectedDeviation: " + expectedDeviation);
		}
		double com = centerOfMass(series, maxIgnoreError, meanValue);
		double deviation = massDeviation(series, maxIgnoreError, meanValue, com);
		double indexFactor = deviation / expectedDeviation;
		double offset = com - expectedCenterOfMass * indexFactor;
		return stretchSeries(series, meanValue, indexFactor, offset);
	}
	
	public static double getIndexFactor(double expectedDeviation, double deviation) {
		if(expectedDeviation == 0) {
			throw new IllegalArgumentException("expectedDeviation: " + expectedDeviation);
		}
		return deviation / expectedDeviation;
	}
	
	public static double getIndexOffset(double indexFactor, double expectedCenterOfMass, double centerOfMass) {
		return centerOfMass - expectedCenterOfMass * indexFactor;		
	}
	
	public static double[] stretchSeries(double[] series, double meanValue, double indexFactor, double offset) {
		int length = series.length;
		double[] skewedSeries = new double[length];
		for(int i = 0; i < length; i++) {
			double origIndex = i * indexFactor + offset;
			int origIndexFloor = (int) Math.floor(origIndex);
			int origIndexCeil = (int) Math.ceil(origIndex);
			if(origIndexFloor < 0 || origIndexCeil >= length) {
				skewedSeries[i] = meanValue;
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
