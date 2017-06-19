package jhs.lc.sims;

import jhs.lc.geom.FluxOrOpacityFunction;

public interface SimulatedFluxSource {
	public double[] produceModeledFlux(FluxOrOpacityFunction brightnessFunction, double orbitRadius);
}
