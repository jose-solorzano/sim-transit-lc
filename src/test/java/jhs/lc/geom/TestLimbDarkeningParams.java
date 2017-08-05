package jhs.lc.geom;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestLimbDarkeningParams {
	@Test
	public void testAverageBrightness() {
		LimbDarkeningParams ldParams = LimbDarkeningParams.SUN;
		SolidSphere star = new SolidSphere(1.0, ldParams);
		int numCols = 100;
		int numRows = numCols;
		double xFactor = 2.0 / numCols;
		double yFactor = 2.0 / numRows;
		double sum = 0;
		int count = 0;
		for(int c = 0; c < numCols; c++) {
			double x = -1.0 + (c + 0.5) * xFactor;
			for(int r = 0; r < numRows; r++) {
				double y = -1.0 + (r + 0.5) * yFactor;
				double b = star.getBrightness(x, y, true);
				if(!Double.isNaN(b)) {
					double value = Math.max(0, b);
					sum += value;
					count++;
				}
			}
		}
		double avgBrightness = sum / count;
		System.out.println("Average: " + avgBrightness);
		assertEquals(0.805, avgBrightness, 0.01);
	}
}
