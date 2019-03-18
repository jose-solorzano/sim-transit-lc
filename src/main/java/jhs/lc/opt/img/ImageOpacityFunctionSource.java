package jhs.lc.opt.img;

import java.awt.image.BufferedImage;

import jhs.lc.geom.TransitFunction;
import jhs.lc.geom.ImageUtil;
import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.math.classification.ClassificationUtil;

public class ImageOpacityFunctionSource implements ParametricTransitFunctionSource {
	private final float[][] brightnessMatrix;
	private final boolean aspectRatioPreserved;
	private final int numColumns, numRows;
	private final double positionFlexibility;
	private final double minWidth, minHeight;
	private final double maxWidth, maxHeight;

	public ImageOpacityFunctionSource(float[][] brightnessMatrix, boolean aspectRatioPreserved, int numColumns,
			int numRows, double positionFlexibility, double minWidth, double minHeight, double maxWidth,
			double maxHeight) {
		super();
		this.brightnessMatrix = brightnessMatrix;
		this.aspectRatioPreserved = aspectRatioPreserved;
		this.numColumns = numColumns;
		this.numRows = numRows;
		this.positionFlexibility = positionFlexibility;
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}

	public static ImageOpacityFunctionSource create(BufferedImage image, boolean aspectRatioPreserved, double positionFlexibility, double minWidth, double minHeight, double maxWidth, double maxHeight) {
		int numColumns = image.getWidth();
		int numRows = image.getHeight();
		float[][] transmittanceMatrix = ImageUtil.blackOnWhiteToTransmittanceMatrix(image);
		return new ImageOpacityFunctionSource(transmittanceMatrix, aspectRatioPreserved, numColumns, numRows, positionFlexibility, minWidth, minHeight, maxWidth, maxHeight);
	}
	
	@Override
	public TransitFunction getTransitFunction(double[] parameters) {
		double xoffset = parameters[0];
		double yoffset = parameters[1];
		double imageWidthLogit = parameters[2];
		double imageHeightLogit = this.aspectRatioPreserved ? imageWidthLogit : parameters[3];
		double imageWidthP = ClassificationUtil.logitToProbability(imageWidthLogit);
		double imageHeightP = this.aspectRatioPreserved ? imageWidthP : ClassificationUtil.logitToProbability(imageHeightLogit);
		double imageWidth = this.minWidth * (1 - imageWidthP) + this.maxWidth * imageWidthP;
		double imageHeight = this.minHeight * (1 - imageHeightP) + this.maxHeight * imageHeightP;
		double topLeftX = xoffset - imageWidth / 2;
		double topLeftY = yoffset - imageHeight / 2;
		return new ImageOpacityFunction(brightnessMatrix, (float) topLeftX, (float) topLeftY, numColumns, numRows, imageWidth, imageHeight);
	}

	@Override
	public final int getNumParameters() {
		// 0: center x
		// 1: center y
		// 2: width
		// 3: height
		return this.aspectRatioPreserved ? 3 : 4;
	}

	@Override
	public final double getParameterScale(int paramIndex) {
		switch(paramIndex) {
		case 0:
		case 1:
			return this.positionFlexibility;
		default:
			return 1.0;
		}
	}
}
