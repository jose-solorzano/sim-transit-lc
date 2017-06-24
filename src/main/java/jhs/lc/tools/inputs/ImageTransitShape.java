package jhs.lc.tools.inputs;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ImageOpacityFunction;
import jhs.lc.geom.RotationAngleSphereFactory;

public class ImageTransitShape extends AbstractTransitShape {
	private String imageFilePath;
	private double imageWidth;
	private double imageHeight;

	@JsonProperty(required = true)
	public final String getImageFilePath() {
		return imageFilePath;
	}

	public final void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}

	@JsonProperty(required = true)
	public final double getImageWidth() {
		return imageWidth;
	}

	public final void setImageWidth(double imageWidth) {
		this.imageWidth = imageWidth;
	}

	@JsonProperty(required = true)
	public final double getImageHeight() {
		return imageHeight;
	}

	public final void setImageHeight(double imageHeight) {
		this.imageHeight = imageHeight;
	}

	@Override
	public RotationAngleSphereFactory createSphereFactory(File context) throws Exception {
		File imageFile;
		if(new File(this.imageFilePath).exists()) {
			imageFile = new File(this.imageFilePath);
		}
		else {
			File parent = context == null ? new File(".") : (context.isDirectory() ? context : context.getParentFile());
			imageFile = new File(parent, this.imageFilePath);
		}
		if(!imageFile.exists()) {
			throw new IllegalStateException("Image file not found: " + imageFile);
		}
		BufferedImage image = ImageIO.read(imageFile);
		FluxOrOpacityFunction bf = ImageOpacityFunction.createOpacitySource(image, this.imageWidth, this.imageHeight);
		return new EvaluatableSurfaceSphereFactory(bf);
	}
}
