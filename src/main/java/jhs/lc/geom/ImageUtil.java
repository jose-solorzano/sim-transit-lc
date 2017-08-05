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
	
	public static BufferedImage buildImage(double[][] brightnessMatrix, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < width; x++) {
			double[] column = brightnessMatrix[x];
			for(int y = 0; y < height; y++) {
				double b = column[height - y - 1];
				if(b >= 0) {
					int color = (int) Math.round(255 * b);
					int rgb = (color << 16) | (color << 8) | (color / 2);
					image.setRGB(x, y, rgb);
				}
			}
		}
		return image;
	}
}
