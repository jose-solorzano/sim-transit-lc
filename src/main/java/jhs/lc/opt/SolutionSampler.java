package jhs.lc.opt;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math.MathException;

import jhs.lc.data.LightCurve;
import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.sims.AngularSimulation;
import jhs.lc.sims.SimulatedFluxSource;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

public class SolutionSampler {
	private static final int NUM_EXTRA_PARAMS = 1;
	private static final double WF = 3.0;
	private static final int WL = 11;
	
	private final Random random;
	private final double baseRadius;
	private final double logRadiusSD;
	private final SimulatedFluxSource fluxSource;
	private final ParametricFluxFunctionSource opacitySource;
	
	private double numMutateParamsFraction = 0.20;
	private double mutateSD = 0.03;

	public SolutionSampler(Random random, double baseRadius, double logRadiusSD,
			SimulatedFluxSource fluxSource, ParametricFluxFunctionSource opacitySource) {
		if(baseRadius <= 1.0) {
			throw new IllegalArgumentException("Invalid baseRadius: " + baseRadius);
		}
		this.random = random;
		this.baseRadius = baseRadius;
		this.logRadiusSD = logRadiusSD;
		this.fluxSource = fluxSource;
		this.opacitySource = opacitySource;
	}

	public final double getNumMutateParamsFraction() {
		return numMutateParamsFraction;
	}

	public final void setNumMutateParamsFraction(double numMutateParamsFraction) {
		this.numMutateParamsFraction = numMutateParamsFraction;
	}

	public final double getMutateSD() {
		return mutateSD;
	}

	public final void setMutateSD(double mutateSD) {
		this.mutateSD = mutateSD;
	}

	public final double getRadius() {
		return baseRadius;
	}

	public Random getRandom() {
		return random;
	}
	
	public double[] createFluxWeights(double[] fluxArray) {
		if(fluxArray.length < 2) {
			throw new IllegalArgumentException("Length of flux array must be at least 2.");
		}
		double[] trendChangeProfile = LightCurve.trendChangeProfile(fluxArray, WL);
		double[] weights = MathUtil.abs(trendChangeProfile);
		double mean = MathUtil.mean(weights);
		MathUtil.addInPlace(weights, mean * WF);
		return weights;
	}

	public final Solution sample() {
		double[] osParameters = this.sampleOpacityFunctionParameters();
		FluxOrOpacityFunction of = this.opacitySource.getFluxOrOpacityFunction(osParameters);
		double radius = this.baseRadius * Math.exp(this.random.nextGaussian() * this.logRadiusSD);
		return new Solution(this.fluxSource, of, radius, osParameters);
	}
	
	public final int getNumParameters() {
		return this.opacitySource.getNumParameters() + NUM_EXTRA_PARAMS;
	}
	
	public final double[] solutionAsParameters(Solution solution) {
		ParametricFluxFunctionSource source = this.opacitySource;
		int np = this.getNumParameters();
		int snp = source.getNumParameters();
		double[] solParameters = solution.getOpacityFunctionParameters();
		if(solParameters.length != snp) {
			throw new IllegalStateException();
		}
		double[] parameters = new double[np];
		for(int i = 0; i < snp; i++) {
			double scale = source.getParameterScale(i);
			parameters[i] = solParameters[i] / (scale == 0 ? 1 : scale);
		}
		int extraIndex = solParameters.length;
		parameters[extraIndex++] = this.getOrbitRadiusChangeParameter(solution);
		return parameters;
	}
	
	private double getOrbitRadiusChangeParameter(Solution solution) {
		double orbitRadius = solution.getOrbitRadius();
		if(orbitRadius == 0) {
			throw new IllegalStateException("Simulation orbit radius is zero.");
		}
		double logDiff = Math.log(orbitRadius / this.baseRadius);
		return logDiff / this.logRadiusSD;
	}
	
	public final Solution parametersAsSolution(double[] parameters) {
		if(parameters.length != this.getNumParameters()) {
			throw new IllegalStateException();
		}
		double[] osParameters = this.opacitySourceParameters(parameters);
		FluxOrOpacityFunction of = this.opacitySource.getFluxOrOpacityFunction(osParameters);
		double radius = this.getOrbitRadius(parameters);
		return new Solution(this.fluxSource, of, radius, osParameters);
	}
	
	private double getOrbitRadius(double[] parameters) {
		int orcIndex = this.opacitySource.getNumParameters();
		double changeParam = parameters[orcIndex];
		double logDiff = changeParam * this.logRadiusSD;
		return this.baseRadius * Math.exp(logDiff);
	}

	public final double sampleParameter(int paramIndex) {
		return this.random.nextGaussian();
	}
	
	private double[] opacitySourceParameters(double[] optimizerParameters) {
		ParametricFluxFunctionSource source = this.opacitySource;
		int np = source.getNumParameters();
		double[] osParameters = new double[np];
		for(int i = 0; i < np; i++) {
			double scale = source.getParameterScale(i);
			osParameters[i] = optimizerParameters[i] * (scale == 0 ? 1 : scale);
		}
		return osParameters;
	}
	
	private double[] sampleOpacityFunctionParameters() {
		ParametricFluxFunctionSource source = this.opacitySource;
		Random r = this.random;
		int n = source.getNumParameters();
		double[] parameters = new double[n];
		for(int i = 0; i < n; i++) {
			double scale = source.getParameterScale(i);
			parameters[i] = r.nextGaussian() * scale;
		}
		return parameters;
	}

	public static double meanSquaredError(double[] groundTruthFlux, double[] weights, Solution solution) {
		double[] testFluxArray = solution.produceModeledFlux();
		int length = groundTruthFlux.length;
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < length; i++) {
			double weight = weights[i];
			double diff = testFluxArray[i] - groundTruthFlux[i];
			sum += (diff * diff) * weight;
			weightSum += weight;
		}
		return weightSum == 0 ? 0 : sum / weightSum;
	}
}
