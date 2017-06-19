package jhs.lc.opt;

import java.awt.image.BufferedImage;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ImageOpacityFunction;
import jhs.lc.geom.ImageUtil;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.math.classification.ClassificationUtil;

public class ImageOpacityFunctionSource implements ParametricFluxFunctionSource {
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
		float[][] brightnessMatrix = ImageUtil.blackOnWhiteToOpacityMatrix(image);
		return new ImageOpacityFunctionSource(brightnessMatrix, aspectRatioPreserved, numColumns, numRows, positionFlexibility, minWidth, minHeight, maxWidth, maxHeight);
	}
	
	@Override
	public FluxOrOpacityFunction getFluxOrOpacityFunction(double[] parameters) {
		final double xoffset = parameters[0];
		final double yoffset = parameters[1];
		double imageWidthLogit = parameters[2];
		double imageHeightLogit = this.aspectRatioPreserved ? imageWidthLogit : parameters[3];
		double imageWidthP = ClassificationUtil.logitToProbability(imageWidthLogit);
		double imageHeightP = this.aspectRatioPreserved ? imageWidthP : ClassificationUtil.logitToProbability(imageHeightLogit);
		final double imageWidth = this.minWidth * (1 - imageWidthP) + this.maxWidth * imageWidthP;
		final double imageHeight = this.minHeight * (1 - imageHeightP) + this.maxHeight * imageHeightP;
		return new ImageOpacityFunction(brightnessMatrix, (float) xoffset, (float) yoffset, numColumns, numRows, imageWidth, imageHeight);
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
