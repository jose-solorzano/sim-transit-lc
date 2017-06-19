package jhs.lc.geom;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

public class GridSphere implements Sphere {
	private static final double PI2 = Math.PI * 2;
	private final double radius;
	private final double[][] brightnessMatrix;
	private final double[] longSearchArray;
	private final double[] latSearchArray;
	
	private double longRotation = 0;
	
	public GridSphere(double radius, double[][] brightnessMatrix, double[] longSearchArray, double[] latSearchArray) {
		this.radius = radius;
		this.brightnessMatrix = brightnessMatrix;
		this.longSearchArray = longSearchArray;
		this.latSearchArray = latSearchArray;
	}
	
	public static GridSphere create(double radius, double[][] brightnessMatrix) {
		int matrixWidth = brightnessMatrix.length;
		int matrixHeight = matrixWidth == 0 ? 0 : brightnessMatrix[0].length;
		double[] longSearchArray = searchArray(0, PI2, matrixWidth);
		double[] latSearchArray = searchArray(-Math.PI / 2, Math.PI / 2, matrixHeight);
		return new GridSphere(radius, brightnessMatrix, longSearchArray, latSearchArray);
	}
	
	public double getLongRotation() {
		return longRotation;
	}

	public void setLongRotation(double longRotation) {
		this.longRotation = longRotation;
	}

	private static double[] searchArray(double from, double to, int length) {
		double step = (to - from) / length;
		double[] array = new double[length];
		for(int i = 0; i < length; i++) {
			array[i] = from + step * (i + 1);
		}
		return array;
	}

	@Override
	public final double getRadius() {
		return this.radius;
	}

	@Override
	public final double getBrightness(double x, double y, boolean front) {
		double r = this.radius;
		double absZ = Math.sqrt(r * r - x * x - y * y);
		double z = front ? +absZ : -absZ;
		double longitude = Math.atan2(z, x);
		double xz = Math.sqrt(x * x + z * z);
		double latitude = Math.atan2(y, xz);
		return this.getAngularBrightness(longitude, latitude);
	}

	@Override
	public Rectangle2D getBoundingBox() {
		return null;
	}

	private final double getAngularBrightness(double longRadians, double latRadians) {
		double actualLong = longRadians - this.longRotation;
		while(actualLong >= PI2) {
			actualLong -= PI2;
		}
		while(actualLong < 0) {
			actualLong += PI2;
		}
		int longCell = Arrays.binarySearch(this.longSearchArray, actualLong);
		if(longCell < 0) {
			longCell = -longCell - 1;
		}
		int latCell = Arrays.binarySearch(this.latSearchArray, latRadians);
		if(latCell < 0) {
			latCell = -latCell - 1;
		}
		double[][] b = this.brightnessMatrix;
		if(longCell >= b.length) {
			return Double.NaN;
		}
		double[] column = b[longCell];
		if(latCell >= column.length) {
			return Double.NaN;
		}
		return column[latCell];
	}
}
