package jhs.math.util;

public final class TriangularDistanceEstimator {
	private static final double[] SIN_TERMS = { Math.sin(Math.PI * 2 / 3), Math.sin(Math.PI * 4 / 3) };
	private static final double[] COS_TERMS = { Math.cos(Math.PI * 2 / 3), Math.cos(Math.PI * 4 / 3) };
	
	public final double triangularDistance(double[] point1, double[] point2Container, int point2Index) {
		int length = point1.length;
		if(length == 0) {
			return 0;
		}
		double maxDistance = point2Container[point2Index] - point1[0];
		int pivot = 0;
		for(int r = 0; r < length; r++) {
			double rx = 0;
			double sinTerm = SIN_TERMS[r % 2];
			double cosTerm = COS_TERMS[r % 2];
			for(int i = 0; i < length; i++) {
				double direction = point1[i] - point2Container[point2Index + i];
				rx += direction * (i == pivot ? cosTerm : sinTerm);
			}
			if(-rx > maxDistance) {
				maxDistance = -rx;
			}
			if((r + 1) % 2 == 0) {
				pivot++;
			}			
		}
		return maxDistance;
	}
}
