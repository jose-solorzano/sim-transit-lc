package jhs.lc.opt;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.sims.SimulatedFluxSource;

public class TestSolutionSampler {
	private static final int NP1 = 7;
	
	@Test
	public void testParametersVsSolution() {
		Random random = new Random(11);
		double baseRadius = 50;
		double logRadiusSD = 0.01;
		ParametricFluxFunctionSource opacitySource = this.getOpacitySource();	
		SimulatedFluxSource fluxSource = null;
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
