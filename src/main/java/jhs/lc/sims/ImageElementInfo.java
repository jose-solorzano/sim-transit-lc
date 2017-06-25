package jhs.lc.sims;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jhs.lc.geom.FluxOrOpacityFunction;

public final class ImageElementInfo {
	final double totalPositiveFlux;
	final ImageElement[] elements;
	
	public ImageElementInfo(double totalPositiveFlux, ImageElement[] elements) {
		super();
		this.totalPositiveFlux = totalPositiveFlux;
		this.elements = elements;
	}

	public static ImageElementInfo createImageFrameElements(FluxOrOpacityFunction brightnessFunction, int withInPixels, int heightInPixels) {
		return createImageFrameElements(brightnessFunction, withInPixels, heightInPixels, brightnessFunction.getBoundingBox());
	}

	public static ImageElementInfo createImageFrameElements(FluxOrOpacityFunction brightnessFunction, int withInPixels, int heightInPixels, Rectangle2D boundingBox) {
		double imageWidth = boundingBox.getWidth();
		double imageHeight = boundingBox.getHeight();
		double fromX = boundingBox.getX();
		double fromY = boundingBox.getY();
		double xcw = imageWidth / withInPixels;
		double ycw = imageHeight / heightInPixels;
		List<ImageElement> elementList = new ArrayList<>();
		double totalPositiveFlux = 0;
		for(int c = 0; c < withInPixels; c++) {
			double x = fromX + c * xcw;
			for(int r = 0; r < heightInPixels; r++) {
				double y = fromY + r * ycw;
				double b = brightnessFunction.fluxOrOpacity(x, y, 1.0);
				if(b > -1.0) { // also, not NaN
					elementList.add(new ImageElement(c, r, b));
					if(b > 0) {
						totalPositiveFlux += b;
					}
				}
			}
		}
		ImageElement[] elements = elementList.toArray(new ImageElement[elementList.size()]);
		return new ImageElementInfo(totalPositiveFlux, elements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(elements);
		long temp;
		temp = Double.doubleToLongBits(totalPositiveFlux);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ImageElementInfo))
			return false;
		ImageElementInfo other = (ImageElementInfo) obj;
		if (Double.doubleToLongBits(totalPositiveFlux) != Double.doubleToLongBits(other.totalPositiveFlux))
			return false;
		if (!Arrays.equals(elements, other.elements))
			return false;
		return true;
	}

}