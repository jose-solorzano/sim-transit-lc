package jhs.lc.sims;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jhs.lc.geom.FluxOrOpacityFunction;

public final class ImageElementInfo {
	private static final int CP_BOX_LENGTH = 12;
	private static final int CP_BOX_NPIXELS = CP_BOX_LENGTH * CP_BOX_LENGTH;
	
	final double totalPositiveFlux;
	final ImageElement[] elements;
	final double[] clusteringPosition;
	
	public ImageElementInfo(double totalPositiveFlux, ImageElement[] elements, double[] clusteringPosition) {
		super();
		this.totalPositiveFlux = totalPositiveFlux;
		this.elements = elements;
		this.clusteringPosition = clusteringPosition;
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
		double xcpf = CP_BOX_LENGTH / imageWidth;
		double ycpf = CP_BOX_LENGTH / imageHeight;
		double[] clusteringPosition = new double[CP_BOX_NPIXELS];
		List<ImageElement> elementList = new ArrayList<>();
		double totalPositiveFlux = 0;
		for(int c = 0; c < withInPixels; c++) {
			double x = fromX + (c + 0.5) * xcw;
			int cpc = (int) ((x - fromX) * xcpf);
			for(int r = 0; r < heightInPixels; r++) {
				double y = fromY + (r + 0.5)  * ycw;
				int cpr = (int) ((y - fromY) * ycpf);
				double b = brightnessFunction.fluxOrOpacity(x, y, 1.0);
				if(b > -1.0) { // also, not NaN
					clusteringPosition[cpr * CP_BOX_LENGTH + cpc] += (b + 1.0);
					elementList.add(new ImageElement(c, r, b));
					if(b > 0) {
						totalPositiveFlux += b;
					}
				}
			}
		}
		ImageElement[] elements = elementList.toArray(new ImageElement[elementList.size()]);
		return new ImageElementInfo(totalPositiveFlux, elements, clusteringPosition);
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