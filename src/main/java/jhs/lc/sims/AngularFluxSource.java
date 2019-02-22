package jhs.lc.sims;

import java.awt.geom.Rectangle2D;

import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.TransitFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.RotationAngleSphereFactory;

public final class AngularFluxSource implements SimulatedFluxSource {
	private final double[] timestamps;
	private final int width, height;
	private final double inclineAngle;
	private final double orbitalPeriod;
	private final LimbDarkeningParams ldParams;
	
	public AngularFluxSource(double[] timestamps, int width, int height, double inclineAngle, double orbitalPeriod, LimbDarkeningParams ldParams) {
		this.timestamps = timestamps;
		this.width = width;
		this.height = height;
		this.inclineAngle = inclineAngle;
		this.orbitalPeriod = orbitalPeriod;
		this.ldParams = ldParams;
	}

	@Override
	public final SimulatedFlux produceModeledFlux(double peakFraction, TransitFunction brightnessFunction, double orbitRadius) {
		RotationAngleSphereFactory sphereFactory = new EvaluatableSurfaceSphereFactory(brightnessFunction);
		AngularSimulation simulation = new AngularSimulation(inclineAngle, orbitRadius, orbitalPeriod, ldParams, sphereFactory);
		double[] fluxArray = simulation.produceModeledFlux(timestamps, peakFraction, width, height);
		// Note: Doesn't support image-based clustering positions.
		return new SimulatedFlux(fluxArray, fluxArray);
	}
	
	@Override
	public ImageElementInfo createImageElementInfo(TransitFunction brightnessFunction, double orbitRadius) {
		double yoffset = -orbitRadius * Math.sin(this.inclineAngle);		
		return ImageElementInfo.createImageFrameElements(brightnessFunction, this.width, this.height, yoffset);
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
		return this.width * arcDistance / boundingBox.getWidth();
	}	
}
