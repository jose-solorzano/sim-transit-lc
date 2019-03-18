package jhs.lc.sims;

import jhs.lc.geom.TransitFunction;

public interface SimulatedFluxSource {
	public SimulatedFlux produceModeledFlux(double peakFraction, TransitFunction brightnessFunction, double orbitRadius);
	/*
	public ImageElementInfo createImageElementInfo(TransitFunction brightnessFunction, double orbitRadius);
	public double numPixelsInTimeSpanArc(TransitFunction brightnessFunction, double orbitRadius);
	*/
}
