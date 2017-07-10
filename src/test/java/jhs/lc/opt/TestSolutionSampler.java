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
		this.testParametersVsSolutionImpl(11, 50, 0.1);
		this.testParametersVsSolutionImpl(13, 100, 0);
	}
	
	private void testParametersVsSolutionImpl(int seed, double baseRadius, double logRadiusSD) {		
		Random random = new Random(seed);
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
			parameters[i] = logRadiusSD == 0 && i == np - 1 ? 0 : random.nextGaussian();
		}
		Solution solution = sampler.parametersAsSolution(parameters);
		double maxRadius = baseRadius * Math.exp(logRadiusSD * 5);
		double minRadius = baseRadius * Math.exp(-logRadiusSD * 5);
		double maxDiff = Math.max(Math.abs(maxRadius - baseRadius), Math.abs(minRadius) - baseRadius);
		assertEquals(solution.getOrbitRadius(), baseRadius, maxDiff + 0.00001);
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
