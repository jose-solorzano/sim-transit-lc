package jhs.lc.tools.inputs;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.opt.img.ImageOpacityFunctionSource;

public class ImageOptMethod extends AbstractOptMethod {
	private String imageFilePath;
	private boolean aspectRatioPreserved;
	private double positionFlexibility;
	private double minImageWidth;
	private double minImageHeight;
	private double maxImageWidth;
	private double maxImageHeight;

	public final boolean isAspectRatioPreserved() {
		return aspectRatioPreserved;
	}

	public final void setAspectRatioPreserved(boolean aspectRatioPreserved) {
		this.aspectRatioPreserved = aspectRatioPreserved;
	}

	@JsonProperty(required = true)
	public final double getPositionFlexibility() {
		return positionFlexibility;
	}

	public final void setPositionFlexibility(double positionFlexibility) {
		this.positionFlexibility = positionFlexibility;
	}

	@JsonProperty(required = true)
	public final String getImageFilePath() {
		return imageFilePath;
	}

	public final void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}

	@Override
	public ParametricTransitFunctionSource createFluxFunctionSource(File context) throws Exception {
		if (this.imageFilePath == null) {
			throw new IllegalStateException("imageFilePath must be provided in image method specification.");
		}
		File imageFile;
		if (new File(this.imageFilePath).exists()) {
			imageFile = new File(this.imageFilePath);
		} else {
			File parent = context == null ? new File(".") : (context.isDirectory() ? context : context.getParentFile());
			imageFile = new File(parent, this.imageFilePath);
		}
		BufferedImage image = ImageIO.read(imageFile);
		return ImageOpacityFunctionSource.create(image, this.aspectRatioPreserved, this.positionFlexibility,
				minImageWidth, minImageHeight, maxImageWidth, maxImageHeight);
	}

	@JsonProperty(required = true)
	public final double getMinImageWidth() {
		return minImageWidth;
	}

	public final void setMinImageWidth(double minImageWidth) {
		this.minImageWidth = minImageWidth;
	}

	@JsonProperty(required = true)
	public final double getMinImageHeight() {
		return minImageHeight;
	}

	public final void setMinImageHeight(double minImageHeight) {
		this.minImageHeight = minImageHeight;
	}

	@JsonProperty(required = true)
	public final double getMaxImageWidth() {
		return maxImageWidth;
	}

	public final void setMaxImageWidth(double maxImageWidth) {
		this.maxImageWidth = maxImageWidth;
	}

	@JsonProperty(required = true)
	public final double getMaxImageHeight() {
		return maxImageHeight;
	}

	public final void setMaxImageHeight(double maxImageHeight) {
		this.maxImageHeight = maxImageHeight;
	}
}
