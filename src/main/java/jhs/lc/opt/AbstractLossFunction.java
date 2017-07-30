package jhs.lc.opt;

import java.util.Arrays;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;

import jhs.lc.sims.SimulatedFlux;

public abstract class AbstractLossFunction implements MultivariateRealFunction, ClusteredEvaluator {
	private final SolutionSampler sampler;
	private final double extraErrorFactor;

	public AbstractLossFunction(SolutionSampler sampler, double extraErrorFactor) {
		this.sampler = sampler;
		this.extraErrorFactor = extraErrorFactor;
	}
	
	protected abstract double baseLoss(double[] testFluxArray);
	
	@Override
	public ClusteredParamEvaluation evaluate(double[] params) throws FunctionEvaluationException, IllegalArgumentException {
		Solution solution = this.sampler.parametersAsSolution(params);
		SimulatedFlux sf = solution.produceModeledFlux();
		double[] modeledFlux = sf.getFluxArray();
		double baseError = this.baseLoss(modeledFlux);
		double extraError = this.sampler.getExtraParamError(params) + solution.getExtraOptimizerError();
		double error = baseError + extraError * this.extraErrorFactor;
		if(Double.isNaN(error)) {
			throw new IllegalStateException("Loss is " + error + "; baseError=" + baseError + "; extraError=" + extraError + "; params=" + Arrays.toString(params) + "; modeledFlux=" + Arrays.toString(modeledFlux));
		}
		double[] clusteringPosition = sf.getClusteringPosition();
		return new ClusteredParamEvaluation(error, clusteringPosition);
	}

	@Override
	public final double value(double[] parameters) throws FunctionEvaluationException, IllegalArgumentException {
		return this.evaluate(parameters).getError();
	}

	/*
	private double[] getClusteringPosition(double[] modeledFlux) {
		double[] trendChangeArray = LightCurveMatcher.trendChangeProfile(modeledFlux);
		return SeriesUtil.skewSeries(trendChangeArray, 0, this.tcCosd, this.tcWidth);
	}
	*/

	@Override
	public final double[] recommendEpsilon(double[] params) {
		return this.sampler.minimalChangeThreshold(params, 1.0);
	}		
}