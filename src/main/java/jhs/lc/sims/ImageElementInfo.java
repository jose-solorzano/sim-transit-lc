package jhs.lc.sims;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jhs.lc.geom.TransitFunction;
import jhs.math.util.MathUtil;

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

	public static ImageElementInfo createImageFrameElements(TransitFunction brightnessFunction, int withInPixels, int heightInPixels, double yoffset) {
		return createImageFrameElements(brightnessFunction, withInPixels, heightInPixels, yoffset, brightnessFunction.getBoundingBox());
	}

	/**
	 * Gets pixel elements of the simulated image that would affect overall brightness. Transparent pixels are not included.
	 * Out of bounds pixels are only included if they are bright.
	 */
	public static ImageElementInfo createImageFrameElements(TransitFunction brightnessFunction, int withInPixels, int heightInPixels, double yoffset, Rectangle2D boundingBox) {
		double imageWidth = boundingBox.getWidth();
		double imageHeight = boundingBox.getHeight();
		double fromX = boundingBox.getX();
		double fromY = boundingBox.getY();
		double xcw = imageWidth / withInPixels;
		double ycw = imageHeight / heightInPixels;
		//double xcpf = CP_BOX_LENGTH / imageWidth;
		//double ycpf = CP_BOX_LENGTH / imageHeight;
		//double[] clusteringPosition = new double[CP_BOX_NPIXELS];
		List<ImageElement> elementList = new ArrayList<>();
		double totalPositiveFlux = 0;
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(int c = 0; c < withInPixels; c++) {
			double x = fromX + (c + 0.5) * xcw;
			//int cpc = (int) ((x - fromX) * xcpf);
			for(int r = 0; r < heightInPixels; r++) {
				double y = fromY + (r + 0.5)  * ycw;
				//int cpr = (int) ((y - fromY) * ycpf);
				double b = brightnessFunction.fluxOrTransmittance(x, y, 1.0);
				double yInStar = y + yoffset;
				if(b > 0 || (b <= 0 && b > -1.0 && yInStar <= 1.0 && yInStar >= -1.0)) { // also, not NaN
					//clusteringPosition[cpr * CP_BOX_LENGTH + cpc] += (b + 1.0);
					elementList.add(new ImageElement(x, y, c, r, b));
					if(b > 0) {
						totalPositiveFlux += b;
					}
					if(x < minX) {
						minX = x;
					}
					if(x > maxX) {
						maxX = x;
					}
					if(y < minY) {
						minY = y;
					}
					if(y > maxY) {
						maxY = y;
					}
				}
			}
		}
		ImageElement[] elements = elementList.toArray(new ImageElement[elementList.size()]);
		double[] clusteringPosition = getClusteringPosition(elements, minX, minY, maxX, maxY);
		return new ImageElementInfo(totalPositiveFlux, elements, clusteringPosition);
	}
	
	private static double[] getClusteringPosition(ImageElement[] elements, double minX, double minY, double maxX, double maxY) {
		double[] position = new double[CP_BOX_NPIXELS];
		if(elements.length == 0) {
			return position;
		}
		double pixelWidth = (maxX - minX) / (CP_BOX_LENGTH - 1);
		double fromX = minX - pixelWidth * 0.5;
		double pixelHeight = (maxY - minY) / (CP_BOX_LENGTH - 1);
		double fromY = minY - pixelHeight * 0.5;
		for(ImageElement element : elements) {
			int cpc = (int) ((element.x - fromX) / pixelWidth);
			int cpr = (int) ((element.y - fromY) / pixelHeight);
			position[cpr * CP_BOX_LENGTH + cpc] += (element.brightness + 1.0);
		}
		return position;
	}
	
	public static double[] blankClusteringPosition() {
		return new double[CP_BOX_NPIXELS];
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