package jhs.lc.sims;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import jhs.lc.geom.TransitFunction;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.SolidSphere;
import jhs.lc.geom.Sphere;

public class PixelatedStarFluxSource implements SimulatedFluxSource {
	private static final double MAX_ANGLE_SUPPORTED = 0.245;
	
	private final double[] timestamps;
	private final double sinInclineAngle;
	private final double orbitalPeriod;
	private final StarCellInfo starCellInfo;
	
	public PixelatedStarFluxSource(double[] timestamps, LimbDarkeningParams ldParams, double inclineAngle,
			double orbitalPeriod, int starWidthPixels, int starHeightPixels) throws AngleUnsupportedException {
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
		this.sinInclineAngle = Math.sin(inclineAngle);
		this.orbitalPeriod = orbitalPeriod;
		this.starCellInfo = StarCellInfo.create(ldParams, starWidthPixels, starHeightPixels);
	}

	@Override
	public final SimulatedFlux produceModeledFlux(double peakFraction, TransitFunction brightnessFunction, double orbitRadius) {
		double[] timestamps = this.timestamps;
		int length = timestamps.length;
		double[] fluxArray = new double[length];
		Rectangle2D boundingBox = brightnessFunction.getBoundingBox();
		if(boundingBox.isEmpty()) {
			Arrays.fill(fluxArray, 1.0);
			return new SimulatedFlux(fluxArray);
		}
		
		StarCellInfo starCellInfo = this.starCellInfo;
		double baseFlux = starCellInfo.getTotalFlux();
		Arrays.fill(fluxArray, baseFlux);
		
		double startTimestamp = timestamps[0];
		double endTimestamp = timestamps[length - 1];
		double timeSpan = endTimestamp - startTimestamp;
		
		double minFx = boundingBox.getX();
		double maxFx = minFx + boundingBox.getWidth();
		double circumference = orbitRadius * Math.PI * 2;
		double transitSpeed = circumference / this.orbitalPeriod;
		
		double peakTime = startTimestamp + timeSpan * peakFraction;
		double transitXAtTimeZero = transitSpeed * peakTime;
		
		double yoffset = orbitRadius * sinInclineAngle;		

		
		for(StarCell cell : starCellInfo.getCells()) {
			this.alterFluxArray(fluxArray, cell, timestamps, minFx, maxFx, brightnessFunction, transitSpeed, transitXAtTimeZero, yoffset);
		}		
		this.normalizeFluxArray(fluxArray, baseFlux);
		return new SimulatedFlux(fluxArray);
	}
	
	private void normalizeFluxArray(double[] fluxArray, double baseFlux) {
		double maxFlux = MathUtil.max(fluxArray);
		double actualBaseFlux = Math.max(baseFlux, maxFlux);
		for(int i = 0; i < fluxArray.length; i++) {
			fluxArray[i] /= actualBaseFlux;
		}
	}
	
	private void alterFluxArray(double[] fluxArray, StarCell cell, double[] timestamps, double minFx, double maxFx, TransitFunction transitFunction, double transitSpeed, double transitXAtTimeZero, double yoffset) {
		double scx = cell.x;
		double scy = cell.y;
		double fy = scy + yoffset;
		double scxBiased = scx + transitXAtTimeZero;
		double cellFlux = cell.flux;
		int fromIndex = lowerTimestampIndex(timestamps, maxFx, scxBiased, transitSpeed, transitXAtTimeZero);
		int toIndex = upperTimestampIndex(timestamps, minFx, scxBiased, transitSpeed, transitXAtTimeZero);
		for (int i = fromIndex; i < toIndex; i++) {
			double timestamp = timestamps[i];
			double fx = scxBiased - timestamp * transitSpeed;
			double negTransmittance = transitFunction.fluxOrTransmittance(fx, fy, 1.0);
			if(negTransmittance > 0) {
				throw new UnsupportedOperationException("Bright transit regions unsupported by " + this.getClass().getSimpleName() + ".");
			}
			if(negTransmittance > -1.0) { // and not NaN
				double newFlux = cellFlux * (-negTransmittance);
				fluxArray[i] += (newFlux - cellFlux);
			}
		}
	}
	
	public static int lowerTimestampIndex(double[] timestamps, double maxFx, double scxBiased, double transitSpeed, double transitXAtTimeZero) {
		double tOfMin = (scxBiased - maxFx) / transitSpeed;
		int fromIndex = Arrays.binarySearch(timestamps, tOfMin);
		if(fromIndex < 0) {
			fromIndex = -fromIndex - 1;
		}
		if(fromIndex > timestamps.length) {
			fromIndex = timestamps.length;
		}
		return fromIndex;
	}

	public static int upperTimestampIndex(double[] timestamps, double minFx, double scxBiased, double transitSpeed, double transitXAtTimeZero) {
		double tOfMax = (scxBiased - minFx) / transitSpeed;
		int toIndex = Arrays.binarySearch(timestamps, tOfMax);
		if(toIndex < 0) {
			toIndex = -toIndex - 1;
		}
		else {
			toIndex++;
			if(toIndex > timestamps.length) {
				toIndex = timestamps.length;
			}
		}
		return toIndex;
	}

	/*
	@Override
	public double numPixelsInTimeSpanArc(TransitFunction brightnessFunction, double orbitRadius) {
		double[] timestamps = this.timestamps;
		int length = timestamps.length;
		double startTimestamp = timestamps[0];
		double endTimestamp = timestamps[length - 1];
		double timeSpan = endTimestamp - startTimestamp;
		double cycleFraction = timeSpan / this.orbitalPeriod;				
		double angularRange = Math.PI * 2 * cycleFraction;
		double arcDistance = orbitRadius * angularRange;
		double starArcDistance = 2.0;
		return this.starCellInfo.getWidthPixels() * arcDistance / starArcDistance;
	}
	*/	
}
