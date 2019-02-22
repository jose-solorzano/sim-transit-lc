package jhs.lc.opt;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import jhs.lc.geom.TransitFunction;
import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.sims.ImageElementInfo;
import jhs.lc.sims.SimulatedFlux;
import jhs.lc.sims.SimulatedFluxSource;

import org.junit.Test;

public class TestSolutionSampler {
	private static final int NP1 = 7;
	
	@Test
	public void testParametersVsSolution() {
		this.testParametersVsSolutionImpl(11, 90, 110);
		this.testParametersVsSolutionImpl(13, 150, 150);
	}
	
	private void testParametersVsSolutionImpl(int seed, double minRadius, double maxRadius) {		
		Random random = new Random(seed);
		ParametricTransitFunctionSource opacitySource = this.getOpacitySource();	
		SimulatedFluxSource fluxSource = new SimulatedFluxSource() {			
			@Override
			public SimulatedFlux produceModeledFlux(double peakFraction,
					TransitFunction brightnessFunction, double orbitRadius) {
				return null;
			}
			
			@Override
			public double numPixelsInTimeSpanArc(
					TransitFunction brightnessFunction, double orbitRadius) {
				return 0;
			}
			
			@Override
			public ImageElementInfo createImageElementInfo(
					TransitFunction brightnessFunction, double orbitRadius) {
				return null;
			}
		};
		SolutionSampler sampler = new SolutionSampler(random, fluxSource, opacitySource, minRadius, maxRadius);
		int np = sampler.getNumParameters();
		int expectedExtraParams = minRadius == maxRadius ? 0 : 1;
		assertEquals(opacitySource.getNumParameters() + expectedExtraParams, np);
		assertEquals(NP1 + expectedExtraParams, np);
		double[] parameters = new double[np];
		for(int i = 0; i < np; i++) {
			parameters[i] = minRadius == maxRadius && i == np - 1 ? 0 : random.nextGaussian();
		}
		Solution solution = sampler.parametersAsSolution(parameters);
		assertTrue(solution.getOrbitRadius() <= maxRadius);
		assertTrue(solution.getOrbitRadius() >= minRadius);
		double[] sp = sampler.solutionAsParameters(solution);
		assertArrayEquals(parameters, sp, 0.0001);
		if(minRadius == maxRadius) {
			assertEquals(0, sampler.getExtraParamError(parameters), 0.00001);
		}
		else {
			parameters[np - 1] = 2.0;
			assertEquals(0, sampler.getExtraParamError(parameters), 0.00001);
			parameters[np - 1] = -2.0;
			assertEquals(0, sampler.getExtraParamError(parameters), 0.00001);
			parameters[np - 1] = 0;
			assertEquals(0, sampler.getExtraParamError(parameters), 0.00001);			
		}
	}
	
	private ParametricTransitFunctionSource getOpacitySource() {
		return new ParametricTransitFunctionSource() {			
			@Override
			public double getParameterScale(int paramIndex) {
				return 3.0 + paramIndex;
			}
			
			@Override
			public int getNumParameters() {
				return NP1;
			}
			
			@Override
			public TransitFunction getTransitFunction(double[] parameters) {
				return null;
			}
		};
	}
}
