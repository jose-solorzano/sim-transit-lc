package jhs.lc.sims;

import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.RotationAngleSphereFactory;

public final class AngularFluxSource implements SimulatedFluxSource {
	private final double[] timestamps;
	private final double peakTimespanFraction;
	private final int width, height;
	private final double inclineAngle;
	private final double orbitalPeriod;
	private final LimbDarkeningParams ldParams;
	
	public AngularFluxSource(double[] timestamps, double peakTimespanFraction, int width, int height,
			double inclineAngle, double orbitalPeriod, LimbDarkeningParams ldParams) {
		super();
		this.timestamps = timestamps;
		this.peakTimespanFraction = peakTimespanFraction;
		this.width = width;
		this.height = height;
		this.inclineAngle = inclineAngle;
		this.orbitalPeriod = orbitalPeriod;
		this.ldParams = ldParams;
	}

	@Override
	public final double[] produceModeledFlux(FluxOrOpacityFunction brightnessFunction, double orbitRadius) {
		RotationAngleSphereFactory sphereFactory = new EvaluatableSurfaceSphereFactory(brightnessFunction);
		AngularSimulation simulation = new AngularSimulation(inclineAngle, orbitRadius, orbitalPeriod, ldParams, sphereFactory);
		return simulation.produceModeledFlux(timestamps, peakTimespanFraction, width, height);
	}
	
	@Override
	public ImageElementInfo createImageElementInfo(FluxOrOpacityFunction brightnessFunction) {
		return ImageElementInfo.createImageFrameElements(brightnessFunction, this.width, this.height);
	}	
}
