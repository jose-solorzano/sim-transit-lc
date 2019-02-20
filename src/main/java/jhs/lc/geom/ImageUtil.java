package jhs.lc.geom;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import jhs.math.util.MathUtil;

public class ImageUtil {
	public static float[][] blackOnWhiteToOpacityMatrix(BufferedImage image) {
		int numColumns = image.getWidth();
		int numRows = image.getHeight();
		Raster data = image.getData();
		int numBands = data.getNumBands();
		double[] pixel = new double[numBands];
		int numBandsToAverage = Math.min(3, numBands);
		float[][] brightnessMatrix = new float[numColumns][numRows];
		double maxB = Double.NEGATIVE_INFINITY;
		for(int x = 0; x < numColumns; x++) {
			for(int y = 0; y < numRows; y++) {
				pixel = data.getPixel(x, y, pixel);
				double color = MathUtil.mean(pixel, 0, numBandsToAverage);
				brightnessMatrix[x][numRows - y - 1] = (float) -(color / 255.0);
				if(brightnessMatrix[x][numRows - y - 1] > maxB) {
					maxB = brightnessMatrix[x][numRows - y - 1];
				}
			}
		}
		return brightnessMatrix;
	}
}
