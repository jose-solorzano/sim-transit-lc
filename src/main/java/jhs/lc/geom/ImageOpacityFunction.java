package jhs.lc.geom;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ImageOpacityFunction implements FluxOrOpacityFunction {
	private final float[][] brightnessMatrix;
	private final float xoffset, yoffset;
	private final int numColumns, numRows;
	private final double imageWidth, imageHeight;

	public ImageOpacityFunction(float[][] brightnessMatrix, float xoffset, float yoffset, int numColumns, int numRows,
			double imageWidth, double imageHeight) {
		super();
		this.brightnessMatrix = brightnessMatrix;
		this.xoffset = xoffset;
		this.yoffset = yoffset;
		this.numColumns = numColumns;
		this.numRows = numRows;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	public static ImageOpacityFunction createOpacitySource(BufferedImage image, double imageWidth, double imageHeight) {
		int numColumns = image.getWidth();
		int numRows = image.getHeight();
		float[][] brightnessMatrix = ImageUtil.blackOnWhiteToOpacityMatrix(image);
		return new ImageOpacityFunction(brightnessMatrix, 0, 0, numColumns, numRows, imageWidth, imageHeight);
	}

	@Override
	public double fluxOrOpacity(double x, double y, double z) {
		int nc = this.numColumns;
		int nr = this.numRows;
		double column = nc * (0.5 + (x + this.xoffset) / this.imageWidth);
		double row = nr * (0.5 - (y + this.yoffset) / this.imageHeight);
		int columnInt = (int) Math.floor(column);
		int rowInt = (int) Math.floor(row);
		if(columnInt < 0 || columnInt >= nc || rowInt < 0 || rowInt >= nr) {
			return Double.NaN;
		}
		return this.brightnessMatrix[columnInt][rowInt];
	}

	@Override
	public Rectangle2D getBoundingBox() {
		double imageWidth = this.imageWidth;
		double imageHeight = this.imageHeight;
		return new Rectangle2D.Double(this.xoffset - imageWidth / 2.0, this.yoffset - imageHeight / 2.0, imageWidth, imageHeight);
	}
}
