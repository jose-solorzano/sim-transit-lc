package jhs.lc.opt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.sims.ImageElementInfo;
import jhs.lc.sims.SimulatedFlux;
import jhs.lc.sims.SimulatedFluxSource;

import org.junit.Test;

public class TestSolutionSampler {
	private static final int NP1 = 7;
	
	@Test
	public void testParametersVsSolution() {
		Random random = new Random(11);
		double baseRadius = 50;
		double logRadiusSD = 0.01;
		ParametricFluxFunctionSource opacitySource = this.getOpacitySource();	
		SimulatedFluxSource fluxSource = new SimulatedFluxSource() {			
			@Override
			public SimulatedFlux produceModeledFlux(double peakFraction,
					FluxOrOpacityFunction brightnessFunction, double orbitRadius) {
				return null;
			}
			
			@Override
			public double numPixelsInTimeSpanArc(
					FluxOrOpacityFunction brightnessFunction, double orbitRadius) {
				return 0;
			}
			
			@Override
			public ImageElementInfo createImageElementInfo(
					FluxOrOpacityFunction brightnessFunction) {
				return null;
			}
		};
		SolutionSampler sampler = new SolutionSampler(random, baseRadius, logRadiusSD, fluxSource, opacitySource);
		int np = sampler.getNumParameters();
		assertEquals(opacitySource.getNumParameters() + 1, np);
		assertEquals(NP1 + 1, np);
		double[] parameters = new double[np];
		for(int i = 0; i < np; i++) {
			parameters[i] = random.nextGaussian();
		}
		Solution solution = sampler.parametersAsSolution(parameters);
		assertEquals(solution.getOrbitRadius(), baseRadius, 1.0);
		double[] sp = sampler.solutionAsParameters(solution);
		assertArrayEquals(parameters, sp, 0.0001);
	}
	
	private ParametricFluxFunctionSource getOpacitySource() {
		return new ParametricFluxFunctionSource() {			
			@Override
			public double getParameterScale(int paramIndex) {
				return 3.0 + paramIndex;
			}
			
			@Override
			public int getNumParameters() {
				return NP1;
			}
			
			@Override
			public FluxOrOpacityFunction getFluxOrOpacityFunction(double[] parameters) {
				return null;
			}
		};
	}
}
