package jhs.math.util;

import java.util.Random;

public class RandomExt {
	private final Random random;

	public RandomExt(Random random) {
		super();
		this.random = random;
	}
	
	/**
	 * Distribution declines exponentially from 0 to 1.
	 * @param coeff The coefficient of exponential decline.
	 */
	public double nextExponentialToOne(double coeff) {
		if(coeff <= 0) {
			throw new IllegalArgumentException("Coefficient must be positive.");
		}
		double relativeArea = this.random.nextDouble();
		double actualArea = (1.0 / coeff) * (1.0 - Math.exp(-coeff)) * relativeArea;
		return -Math.log(1 - coeff * actualArea) / coeff;
	}
	
	public double nextExponential(double coeff) {
		if(coeff <= 0) {
			throw new IllegalArgumentException("Coefficient must be positive.");
		}
		double relativeArea = this.random.nextDouble();
		return -Math.log(1 - relativeArea) / coeff;
	}
	
	public double nextExponentialSymmetric(double coeff) {
		if(coeff <= 0) {
			throw new IllegalArgumentException("Coefficient must be positive.");
		}
		Random r = this.random;
		double relativeArea = r.nextDouble();
		return (r.nextBoolean() ? +1.0 : -1.0) * Math.log(1 - relativeArea) / coeff;
	}
}
