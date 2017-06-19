package jhs.lc.geom;

import java.awt.geom.Rectangle2D;

import jhs.math.util.MathUtil;
import jhs.math.util.MatrixUtil;

public class SphereViewport {
	private static final double TOLERANCE = 0.001;
	private final Sphere sphere;
	private final double boxWidth, boxHeight;

	public SphereViewport(Sphere sphere) {
		this(sphere, 2.0, 2.0);
	}

	public SphereViewport(Sphere sphere, double boxWidth, double boxHeight) {
		super();
		this.sphere = sphere;
		this.boxWidth = boxWidth;
		this.boxHeight = boxHeight;
	}

	public void populateBrightness(double[][] targetMatrix, boolean frontOnly) {
		final double bw = this.boxWidth;
		final double bh = this.boxHeight;
		Rectangle2D boundingBox = this.sphere.getBoundingBox();

		double fromX = boundingBox == null ? -(bw / 2.0) : boundingBox.getX();
		double toX = boundingBox == null ? +(bw / 2.0) : fromX + boundingBox.getWidth();
		double fromY = boundingBox == null ? -(bh / 2.0) : boundingBox.getY();
		double toY = boundingBox == null ? +(bh / 2.0) : fromY + boundingBox.getHeight();

		int numCols = targetMatrix.length;
		if(numCols == 0) {
			return;
		}
		int numRows = targetMatrix[0].length;
		double xstep = bw / numCols;
		int fromC = (int) Math.floor((fromX + (bw / 2)) * numCols / bw - TOLERANCE);
		fromC = MathUtil.bounded(fromC, 0, numCols);
		int toC = (int) Math.ceil((toX + (bw / 2)) * numCols / bw + TOLERANCE);
		toC = MathUtil.bounded(toC, 0, numCols);
		double ystep = bh / numRows;
		int fromR = (int) Math.floor((fromY + (bh / 2)) * numRows / bh - TOLERANCE);
		fromR = MathUtil.bounded(fromR, 0, numRows);
		int toR = (int) Math.ceil((toY + (bh / 2)) * numRows / bh + TOLERANCE);
		toR = MathUtil.bounded(toR, 0, numRows);
		Sphere sphere = this.sphere;
		if(!frontOnly) {
			for(int c = fromC; c < toC; c++) {
				double x = (c + 0.5) * xstep - (bw / 2);
				if(x >= fromX && x < toX) {
					double[] column = targetMatrix[c];
					for(int r = fromR; r < toR; r++) {
						if(Double.isNaN(column[r])) {
							double y = (r + 0.5) * ystep - (bh / 2);
							if(y >= fromY && y < toY) {
								double b = sphere.getBrightness(x, y, false);
								if(b >= 0) {
									column[r] = b;
								}
							}
						}
					}
				}
			}			
		}		
		for(int c = fromC; c < toC; c++) {
			double x = (c + 0.5) * xstep - (bw / 2);
			if(x >= fromX && x < toX) {
				double[] column = targetMatrix[c];
				for(int r = fromR; r < toR; r++) {
					double y = (r + 0.5) * ystep - (bh / 2);
					if(y >= fromY && y < toY) {
						double b = sphere.getBrightness(x, y, true);
						if(b >= 0) {
							column[r] = b;
						}
						else if(b < 0) {
							// But b not NaN
							column[r] *= (-b);
						}
					}
				}
			}
		}
	}

	public double fluxDifference(double[][] targetMatrix, boolean frontOnly) {
		final double bw = this.boxWidth;
		final double bh = this.boxHeight;
		Rectangle2D boundingBox = this.sphere.getBoundingBox();

		double fromX = boundingBox == null ? -(bw / 2.0) : boundingBox.getX();
		double toX = boundingBox == null ? +(bw / 2.0) : fromX + boundingBox.getWidth();
		double fromY = boundingBox == null ? -(bh / 2.0) : boundingBox.getY();
		double toY = boundingBox == null ? +(bh / 2.0) : fromY + boundingBox.getHeight();

		int numCols = targetMatrix.length;
		if(numCols == 0) {
			return 0;
		}
		int numRows = targetMatrix[0].length;
		double xstep = bw / numCols;
		int fromC = (int) Math.floor((fromX + (bw / 2)) * numCols / bw - TOLERANCE);
		fromC = MathUtil.bounded(fromC, 0, numCols);
		int toC = (int) Math.ceil((toX + (bw / 2)) * numCols / bw + TOLERANCE);
		toC = MathUtil.bounded(toC, 0, numCols);
		double ystep = bh / numRows;
		int fromR = (int) Math.floor((fromY + (bh / 2)) * numRows / bh - TOLERANCE);
		fromR = MathUtil.bounded(fromR, 0, numRows);
		int toR = (int) Math.ceil((toY + (bh / 2)) * numRows / bh + TOLERANCE);
		toR = MathUtil.bounded(toR, 0, numRows);
		Sphere sphere = this.sphere;
		double[][] changed = null;
		if(!frontOnly) {
			for(int c = fromC; c < toC; c++) {
				double x = (c + 0.5) * xstep - (bw / 2);
				if(x >= fromX && x < toX) {
					double[] column = targetMatrix[c];
					for(int r = fromR; r < toR; r++) {
						if(Double.isNaN(column[r])) {
							double y = (r + 0.5) * ystep - (bh / 2);
							if(y >= fromY && y < toY) {
								double b = sphere.getBrightness(x, y, false);
								if(b >= 0) {
									if(changed == null) {
										changed = new double[numCols][numRows];
										MatrixUtil.fill(changed, Double.NaN);
									}
									changed[c][r] = b;
								}
							}
						}
					}
				}
			}			
		}				
		double diffSum = 0;
		for(int c = fromC; c < toC; c++) {
			double x = (c + 0.5) * xstep - (bw / 2);
			if(x >= fromX && x < toX) {
				double[] column = targetMatrix[c];
				for(int r = fromR; r < toR; r++) {
					double y = (r + 0.5) * ystep - (bh / 2);
					if(y >= fromY && y < toY) {
						double b = sphere.getBrightness(x, y, true);
						if(!Double.isNaN(b)) {
							double oldFlux = column[r];
							double priorFlux;
							if(changed == null) {
								priorFlux = oldFlux;
							}
							else {
								priorFlux = changed[c][r];
								if(Double.isNaN(priorFlux)) {
									priorFlux = oldFlux;
								}												
							}
							double newFlux = b >= 0 ? b : priorFlux * (-b);
							double diff = newFlux - (Double.isNaN(oldFlux) ? 0 : oldFlux);
							if(!Double.isNaN(diff)) {
								diffSum += diff;
							}
						}
						else {
							if(changed != null && !Double.isNaN(changed[c][r])) {
								diffSum += changed[c][r];
							}
						}
					}
				}
			}
		}
		return diffSum;
	}

	public static final double totalBrightness(double[][] matrix, int width, int height) {
		double total = 0;
		for(int c = 0; c < width; c++) {
			double[] column = matrix[c];
			for(int r = 0; r < height; r++) {
				double f = column[r];
				if(!Double.isNaN(f)) {
					total += f;
				}
			}
		}
		return total;
	}

}
