package jhs.math.smoothing;

public class FilterSmoother implements SeriesSmoother {
	private final double[] filter;
	
	public FilterSmoother(double[] filter) {
		super();
		this.filter = filter;
	}
	
	public static final double[] gaussianFilter(double sd, int length) {
		double varT2 = 2 * sd * sd;
		double[] filter = new double[length];
		double mean = (double) (length - 1) / 2.0;
		for(int i = 0; i < length; i++) {
			double diff = i - mean;
			filter[i] = Math.exp(- (diff * diff) / varT2);
		}
		return filter;
	}

	public final double[] smooth(double[] series) {
		double[] filter = this.filter;
		int minOffset = -filter.length / 2;
		int filterLength = filter.length;
		int length = series.length;
		double[] newSeries = new double[length];
		for(int x = 0; x < length; x++) {
			double sum = 0;
			double weightSum = 0;
			for(int i = 0; i < filterLength; i++) {
				int xAtOffset = x + minOffset + i;
				if(xAtOffset >= 0 && xAtOffset < length) {
					double weight = filter[i];
					sum += series[xAtOffset] * weight;
					weightSum += weight;
				}
			}
			newSeries[x] = sum / weightSum;
		}
		return newSeries;
	}
}
