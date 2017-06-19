package jhs.math.smoothing;


public class GaussianSmoother implements SeriesSmoother {
	private final FilterSmoother filterSmoother;

	public GaussianSmoother(double xSD, double minP) {
		if(minP >= 1 || minP <= 0) {
			throw new IllegalArgumentException("minP must be between 0 and 1, exclusive.");
		}
		double denominator = 2 * xSD * xSD;
		int x = 1;
		for(; ; x++) {
			double p = Math.exp(-(x*x) / denominator);
			if(p < minP) {
				x--;
				break;
			}
		}
		int fl = x * 2 + 1;
		double[] filter = new double[fl];
		int midX = fl / 2;
		filter[midX] = 1.0;
		for(int i = 1; i <= midX; i++) {
			filter[midX+i] = filter[midX-i] = Math.exp(-(i*i) / denominator); 
		}
		this.filterSmoother = new FilterSmoother(filter);
	}

	public final double[] smooth(double[] series) {
		return this.filterSmoother.smooth(series);
	}
}
