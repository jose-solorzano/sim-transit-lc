package jhs.lc.opt.img;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import jhs.lc.geom.TransitFunction;
import jhs.lc.geom.ImageUtil;

public class ImageOpacityFunction implements TransitFunction {
	private static final long serialVersionUID = 1L;
	private final float[][] transmittanceMatrix;
	private final float topLeftX, topLeftY;
	private final int numColumns, numRows;
	private final double imageWidth, imageHeight;

	public ImageOpacityFunction(float[][] transmittanceMatrix, float x, float y, int numColumns, int numRows,
			double imageWidth, double imageHeight) {
		super();
		this.transmittanceMatrix = transmittanceMatrix;
		this.topLeftX = x;
		this.topLeftY = y;
		this.numColumns = numColumns;
		this.numRows = numRows;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	public static ImageOpacityFunction createOpacitySource(BufferedImage image, double imageWidth, double imageHeight) {
		int numColumns = image.getWidth();
		int numRows = image.getHeight();
		float[][] brightnessMatrix = ImageUtil.blackOnWhiteToTransmittanceMatrix(image);
		return new ImageOpacityFunction(brightnessMatrix, (float) (-imageWidth / 2), (float) (-imageHeight / 2), numColumns, numRows, imageWidth, imageHeight);
	}

	@Override
	public double fluxOrTransmittance(double x, double y, double z) {
		int nc = this.numColumns;
		int nr = this.numRows;
		double column = nc * (x - this.topLeftX) / this.imageWidth;
		double row = nr * (y - this.topLeftY) / this.imageHeight;
		int columnInt = (int) Math.floor(column);
		int rowInt = (int) Math.floor(row);
		if(columnInt < 0 || columnInt >= nc || rowInt < 0 || rowInt >= nr) {
			return Double.NaN;
		}
		return -this.transmittanceMatrix[columnInt][rowInt];
	}

	@Override
	public Rectangle2D getBoundingBox() {
		double imageWidth = this.imageWidth;
		double imageHeight = this.imageHeight;
		return new Rectangle2D.Double(this.topLeftX, this.topLeftY, imageWidth, imageHeight);
	}
	
	@Override
	public final double getExtraOptimizerError() {
		return 0;
	}
}
