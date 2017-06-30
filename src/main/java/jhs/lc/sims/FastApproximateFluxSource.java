package jhs.lc.sims;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.SolidSphere;
import jhs.lc.geom.Sphere;

public class FastApproximateFluxSource implements SimulatedFluxSource {
	private static final double MAX_ANGLE_SUPPORTED = 0.245;
	
	private final double[] timestamps;
	private final LimbDarkeningParams ldParams;
	private final double inclineAngle;
	private final double orbitalPeriod;
	private final double peakFraction;
	private final int frameWidthPixels, frameHeightPixels;
	
	public FastApproximateFluxSource(double[] timestamps, LimbDarkeningParams ldParams, double inclineAngle,
			double orbitalPeriod, double peakFraction, int frameWidthPixels, int frameHeightPixels) throws AngleUnsupportedException {
		if(Math.abs(inclineAngle) > MAX_ANGLE_SUPPORTED) {
			throw new AngleUnsupportedException("inclineAngle", inclineAngle);
		}
		if(timestamps.length < 2) {
			throw new IllegalArgumentException("Timestamps array must have at least 2 elements.");
		}
		double timeSpan = timestamps[timestamps.length - 1] - timestamps[0];
		double cycleFraction = timeSpan / orbitalPeriod;				
		double angularRange = Math.PI * 2 * cycleFraction;
		double startAngle = -angularRange * peakFraction;
		double endAngle = angularRange * (1 - peakFraction);
		if(Math.abs(startAngle) > MAX_ANGLE_SUPPORTED) {
			throw new AngleUnsupportedException("startAngle", startAngle);
		}
		if(Math.abs(endAngle) > MAX_ANGLE_SUPPORTED) {
			throw new AngleUnsupportedException("endAngle", startAngle);
		}		
		this.timestamps = timestamps;
		this.ldParams = ldParams;
		this.inclineAngle = inclineAngle;
		this.orbitalPeriod = orbitalPeriod;
		this.peakFraction = peakFraction;
		this.frameWidthPixels = frameWidthPixels;
		this.frameHeightPixels = frameHeightPixels;
	}

	@Override
	public final SimulatedFlux produceModeledFlux(FluxOrOpacityFunction brightnessFunction, double orbitRadius) {
		double[] timestamps = this.timestamps;
		int length = timestamps.length;
		Rectangle2D boundingBox = brightnessFunction.getBoundingBox();
		Sphere star = new SolidSphere(1.0, this.ldParams);
		double baseFlux = this.estimateBaseFlux(star, boundingBox);
		
		ImageElementInfo imageElementInfo = ImageElementInfo.createImageFrameElements(brightnessFunction, this.frameWidthPixels, this.frameHeightPixels, boundingBox);
		
		double startTimestamp = timestamps[0];
		double endTimestamp = timestamps[length - 1];
		double timeSpan = endTimestamp - startTimestamp;
		double cycleFraction = timeSpan / this.orbitalPeriod;
				
		double angularRange = Math.PI * 2 * cycleFraction;
		double startAngle = -angularRange * this.peakFraction;
		double timeToAngleFactor = angularRange / timeSpan;
		
		double yoffset = -orbitRadius * Math.sin(this.inclineAngle);
		
		double[] fluxArray = new double[length];
		for(int i = 0; i < length; i++) {
			double timestamp = timestamps[i];
			double rotationAngle = startAngle + (timestamp - startTimestamp) * timeToAngleFactor;
			double xoffset = orbitRadius * Math.sin(rotationAngle);
			double fluxDiff = this.fluxDifference(star, imageElementInfo, boundingBox, xoffset, yoffset);
			double flux = baseFlux + fluxDiff;
			double normFlux = flux / baseFlux;
			if(Double.isNaN(normFlux)) {
				throw new IllegalStateException("Flux is NaN: baseFlux=" + baseFlux + ", flux=" + flux);
			}
			fluxArray[i] = normFlux;
		}
		return new SimulatedFlux(fluxArray, imageElementInfo.clusteringPosition);
	}
	
	private double fluxDifference(Sphere star, ImageElementInfo imageElementInfo, Rectangle2D imageBounds, double xoffset, double yoffset) {
		double diffSum = 0;
		double imageX = imageBounds.getX();
		double imageY = imageBounds.getY();
		double imageWidth = imageBounds.getWidth();
		double imageHeight = imageBounds.getHeight();
		
		double displacedImageX = imageX + xoffset;
		double displacedImageY = imageY + yoffset;
		if(this.starOutsideImageView(displacedImageX, displacedImageY, imageWidth, imageHeight)) {
			return imageElementInfo.totalPositiveFlux;
		}

		int widthPixels = this.frameWidthPixels;
		int heightPixels = this.frameHeightPixels;
		double widthFactor = imageWidth / widthPixels;
		double heightFactor = imageHeight / heightPixels;
		for(ImageElement element : imageElementInfo.elements) {
			double elementBrightness = element.brightness;
			int colIdx = element.colIdx;
			int rowIdx = element.rowIdx;
			double starX = displacedImageX + widthFactor * (colIdx + 0.5);
			double starY = displacedImageY + heightFactor * (rowIdx + 0.5);
			if(starX <= -1 || starX >= +1 || starY <= -1 || starY >= +1) {
				if(elementBrightness > 0) {
					diffSum += elementBrightness;
				}
			}
			else {
				double starPointBrightness = star.getBrightness(starX, starY, true);				
				double diff;
				if(elementBrightness > 0) {
					if(starPointBrightness >= 0) {
						diff = elementBrightness - starPointBrightness;
					}
					else { // starPointBrightness < 0 or NaN
						diff = elementBrightness;
					}
				}
				else if(elementBrightness <= 0) {
					if(starPointBrightness > 0) {
						double newBrightness = starPointBrightness * (-elementBrightness);
						diff = newBrightness - starPointBrightness;
					}
					else { // starPointBrightness <= 0 or NaN
						diff = 0;
					}
				}
				else { // elementBrightness is NaN
					diff = 0;
				}
				diffSum += diff;
			}
		}		
		return diffSum;
	}
	
	private boolean starOutsideImageView(double imageX, double imageY, double imageWidth, double imageHeight) {
		return imageX >= +1.0 || imageX + imageWidth <= -1.0 || imageY >= +1.0 || imageY + imageHeight <= -1.0;
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
	public ImageElementInfo createImageElementInfo(FluxOrOpacityFunction brightnessFunction) {
		return ImageElementInfo.createImageFrameElements(brightnessFunction, this.frameWidthPixels, this.frameHeightPixels);
	}

	@Override
	public double numPixelsInTimeSpanArc(FluxOrOpacityFunction brightnessFunction, double orbitRadius) {
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
