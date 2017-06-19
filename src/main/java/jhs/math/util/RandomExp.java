package jhs.math.util;

import java.util.Random;

public class RandomExp {
	private final Random random;
	private final double coefficient;
	private final double areaAt1;

	public RandomExp(Random random, double coefficient) {
		if(coefficient <= 0) {
			throw new IllegalArgumentException("Coefficient must be positive.");
		}
		this.random = random;
		this.coefficient = coefficient;
		this.areaAt1 = (1.0 / coefficient) * (1.0 - Math.exp(-coefficient));
	}
	
	public final double nextDouble() {
		double relativeArea = this.random.nextDouble();
		double coeff = this.coefficient;
		double actualArea = this.areaAt1 * relativeArea;
		return -Math.log(1 - coeff * actualArea) / coeff;
	}
}
