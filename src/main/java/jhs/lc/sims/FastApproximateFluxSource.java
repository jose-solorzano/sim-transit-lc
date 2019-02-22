package jhs.lc.sims;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import jhs.lc.geom.TransitFunction;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.SolidSphere;
import jhs.lc.geom.Sphere;

public class FastApproximateFluxSource implements SimulatedFluxSource {
	private static final double MAX_ANGLE_SUPPORTED = 0.245;
	
	private final double[] timestamps;
	private final LimbDarkeningParams ldParams;
	private final double inclineAngle;
	private final double orbitalPeriod;
	private final int frameWidthPixels, frameHeightPixels;
	
	public FastApproximateFluxSource(double[] timestamps, LimbDarkeningParams ldParams, double inclineAngle,
			double orbitalPeriod, int frameWidthPixels, int frameHeightPixels) throws AngleUnsupportedException {
		if(Math.abs(inclineAngle) > MAX_ANGLE_SUPPORTED) {
			throw new AngleUnsupportedException("inclineAngle", inclineAngle);
		}
		if(timestamps.length < 2) {
			throw new IllegalArgumentException("Timestamps array must have at least 2 elements.");
		}
		if(!ArrayUtil.isSorted(timestamps)) {
			throw new IllegalArgumentException("Timestamps array must be ordered.");			
		}
		double timeSpan = timestamps[timestamps.length - 1] - timestamps[0];
		double cycleFraction = timeSpan / orbitalPeriod;				
		double angularRange = Math.PI * 2 * cycleFraction;
		if(Math.abs(angularRange) > MAX_ANGLE_SUPPORTED) {
			throw new AngleUnsupportedException("angularRange", angularRange);
		}
		this.timestamps = timestamps;
		this.ldParams = ldParams;
		this.inclineAngle = inclineAngle;
		this.orbitalPeriod = orbitalPeriod;
		this.frameWidthPixels = frameWidthPixels;
		this.frameHeightPixels = frameHeightPixels;
	}

	@Override
	public final SimulatedFlux produceModeledFlux(double peakFraction, TransitFunction brightnessFunction, double orbitRadius) {
		double[] timestamps = this.timestamps;
		int length = timestamps.length;
		double[] fluxArray = new double[length];
		Rectangle2D boundingBox = brightnessFunction.getBoundingBox();
		if(boundingBox.isEmpty()) {
			Arrays.fill(fluxArray, 1.0);
			return new SimulatedFlux(fluxArray, ImageElementInfo.blankClusteringPosition());
		}
		
		Sphere star = new SolidSphere(1.0, this.ldParams);

		double yoffset = -orbitRadius * Math.sin(this.inclineAngle);		

		ImageElementInfo imageElementInfo = ImageElementInfo.createImageFrameElements(brightnessFunction, this.frameWidthPixels, this.frameHeightPixels, yoffset, boundingBox);
		ImageElement[] elements = imageElementInfo.elements;

		double baseFlux = this.estimateBaseFlux(star, boundingBox);
		Arrays.fill(fluxArray, baseFlux);
		
		double startTimestamp = timestamps[0];
		double endTimestamp = timestamps[length - 1];
		double timeSpan = endTimestamp - startTimestamp;
		double cycleFraction = timeSpan / this.orbitalPeriod;
				
		double angularRange = Math.PI * 2 * cycleFraction;
		double startAngle = -angularRange * peakFraction;
		double timeToAngleFactor = angularRange / timeSpan;

		double[] displacedImageXArray = this.getDisplacedImageXArray(timestamps, boundingBox.getX(), orbitRadius, startAngle, timeToAngleFactor);
		
		int ne = elements.length;
		for(int i = 0; i < ne; i++) {
			ImageElement element = elements[i];
			this.alterFluxArray(fluxArray, star, timestamps, displacedImageXArray, boundingBox, element, orbitRadius, yoffset, startAngle, timeToAngleFactor);
		}
		
		this.normalizeFluxArray(fluxArray, baseFlux);

		return new SimulatedFlux(fluxArray, imageElementInfo.clusteringPosition);
	}
	
	private double[] getDisplacedImageXArray(double[] timestamps, double imageX, double orbitRadius, double startAngle, double timeToAngleFactor) {
		double startTimestamp = timestamps[0];
		int length = timestamps.length;
		double[] result = new double[length];
		for (int i = 0; i < length; i++) {
			double timestamp = timestamps[i];
			double rotationAngle = startAngle + (timestamp - startTimestamp) * timeToAngleFactor;
			double xoffset = orbitRadius * Math.sin(rotationAngle);
			result[i] = imageX + xoffset;
		}
		return result;
	}
	
	private void normalizeFluxArray(double[] fluxArray, double baseFlux) {
		double maxFlux = MathUtil.max(fluxArray);
		double actualBaseFlux = Math.max(baseFlux, maxFlux);
		for(int i = 0; i < fluxArray.length; i++) {
			fluxArray[i] /= actualBaseFlux;
		}
	}
	
	private void alterFluxArray(double[] fluxArray, Sphere star, double[] timestamps, double[] displacedImageXArray, Rectangle2D imageBounds, ImageElement element, double orbitRadius, double yoffset, double startAngle, double timeToAngleFactor) {
		int length = timestamps.length;
		double startTimestamp = timestamps[0];
		double imageX = imageBounds.getX();
		double imageY = imageBounds.getY();
		double imageWidth = imageBounds.getWidth();
		double imageHeight = imageBounds.getHeight();
		double displacedImageY = imageY + yoffset;
		int widthPixels = this.frameWidthPixels;
		int heightPixels = this.frameHeightPixels;
		double widthFactor = imageWidth / widthPixels;
		double heightFactor = imageHeight / heightPixels;
		
		int rowIdx = element.rowIdx;
		int colIdx = element.colIdx;
		double wfciTerm = widthFactor * (colIdx + 0.5);
		double elementYInStar = displacedImageY + heightFactor * (rowIdx + 0.5);

		double elementBrightness = element.brightness;		

		int fromIndex, toIndex;
		if(elementBrightness > 0) {
			fromIndex = 0;
			toIndex = length;
		}
		else {
			// Range of relevant timestamps where element has an effect.
			fromIndex = lowerTimestampIndex(timestamps, imageX, widthFactor, colIdx, orbitRadius, startTimestamp, startAngle, timeToAngleFactor);
			toIndex = upperTimestampIndex(timestamps, imageX, widthFactor, colIdx, orbitRadius, startTimestamp, startAngle, timeToAngleFactor);
		}
		for (int i = fromIndex; i < toIndex; i++) {
			double elementXInStar = displacedImageXArray[i] + wfciTerm;
			double starPointBrightness = star.getBrightness(elementXInStar, elementYInStar, true);
			double diff;
			if (elementBrightness > 0) {
				if (starPointBrightness >= 0) {
					diff = elementBrightness - starPointBrightness;
				} else { // starPointBrightness < 0 or NaN
					diff = elementBrightness;
				}
			} else if (elementBrightness <= 0) {
				if (starPointBrightness > 0) {
					double newBrightness = starPointBrightness * (-elementBrightness);
					diff = newBrightness - starPointBrightness;
				} else { // starPointBrightness <= 0 or NaN
					diff = 0;
				}
			} else { // elementBrightness is NaN
				diff = 0;
			}
			fluxArray[i] += diff;
		}
	}
	
	public static int upperTimestampIndex(double[] timestamps, double imageX, double widthFactor, int colIdx, double orbitRadius, double startTimestamp, double startAngle, double timeToAngleFactor) {
		double term1 = imageX + widthFactor * (colIdx + 0.5);
		double upperSin = (+1.0 - term1) / orbitRadius;
		int toIndex;
		if(upperSin < -1 || upperSin > +1) {
			toIndex = timestamps.length;
		}
		else {
			double upperTimestamp = startTimestamp + (Math.asin(upperSin) - startAngle) / timeToAngleFactor;
			toIndex = Arrays.binarySearch(timestamps, upperTimestamp);
			if(toIndex < 0) {
				toIndex = -toIndex - 1;
			}
			else {
				toIndex++;
				if(toIndex > timestamps.length) {
					toIndex = timestamps.length;
				}
			}
		}
		return toIndex;
	}
	
	public static int lowerTimestampIndex(double[] timestamps, double imageX, double widthFactor, int colIdx, double orbitRadius, double startTimestamp, double startAngle, double timeToAngleFactor) {
		double term1 = imageX + widthFactor * (colIdx + 0.5);
		double lowerSin = (-1.0 - term1) / orbitRadius;
		int fromIndex;
		if(lowerSin < -1 || lowerSin > +1) {
			fromIndex = 0;
		}
		else {
			double lowerTimestamp = startTimestamp + (Math.asin(lowerSin) - startAngle) / timeToAngleFactor;
			fromIndex = Arrays.binarySearch(timestamps, lowerTimestamp);
			if(fromIndex < 0) {
				fromIndex = -fromIndex - 1;
			}
			if(fromIndex > timestamps.length) {
				fromIndex = timestamps.length;
			}
		}
		return fromIndex;
	}

	
	private double estimateBaseFlux(Sphere star, Rectangle2D imageBounds) {
		double imageWidth = imageBounds.getWidth();
		double imageHeight = imageBounds.getHeight();
		int pixelWidth = this.frameWidthPixels;
		int pixelHeight = this.frameHeightPixels;
		double starPixelWidth = pixelWidth * 2.0 / imageWidth;
		double starPixelHeight = pixelHeight * 2.0 / imageHeight;		
		int ceilStarPixelWidth = (int) Math.ceil(starPixelWidth);
		int ceilStarPixelHeight = (int) Math.ceil(starPixelHeight);
		double xf = 2.0 / ceilStarPixelWidth;
		double yf = 2.0 / ceilStarPixelHeight;
		double boxFlux = 0;
		for(int c = 0; c < ceilStarPixelWidth; c++) {
			double x = -1.0 + (c + 0.5) * xf;
			for(int r = 0; r < ceilStarPixelHeight; r++) {
				double y = -1.0 + (r + 0.5) * yf;
				double b = star.getBrightness(x, y, true);
				if(!Double.isNaN(b)) {
					boxFlux += b;
				}
			}
		}
		return boxFlux * (starPixelWidth * starPixelHeight) / (ceilStarPixelWidth * ceilStarPixelHeight);
	}

	@Override
	public ImageElementInfo createImageElementInfo(TransitFunction brightnessFunction, double orbitRadius) {
		double yoffset = -orbitRadius * Math.sin(this.inclineAngle);		
		return ImageElementInfo.createImageFrameElements(brightnessFunction, this.frameWidthPixels, this.frameHeightPixels, yoffset);
	}

	@Override
	public double numPixelsInTimeSpanArc(TransitFunction brightnessFunction, double orbitRadius) {
		double[] timestamps = this.timestamps;
		int length = timestamps.length;
		Rectangle2D boundingBox = brightnessFunction.getBoundingBox();
		double startTimestamp = timestamps[0];
		double endTimestamp = timestamps[length - 1];
		double timeSpan = endTimestamp - startTimestamp;
		double cycleFraction = timeSpan / this.orbitalPeriod;		
		double angularRange = Math.PI * 2 * cycleFraction;
		double arcDistance = orbitRadius * angularRange;
		return this.frameWidthPixels * arcDistance / boundingBox.getWidth();
	}	
}
