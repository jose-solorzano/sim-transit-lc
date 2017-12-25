package jhs.lc.opt;

import java.util.List;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.RealPointValuePair;

import jhs.lc.data.LightCurve;
import jhs.lc.data.LightCurvePoint;
import jhs.lc.opt.ClusteredGridSearchOptimizer.Phase;
import jhs.math.util.MathUtil;

public class LightCurveFitter {	
	//private static final Logger logger = Logger.getLogger(CSLightCurveFitter.class.getName());
	private final SolutionSampler sampler;
	private final int numClusters;
	private final int numParticlesPerCluster;
		
	private int maxClusteringIterations = 300;
	private int maxGradientDescentIterations = 200;
	
	private double epsilonFactor = 3.0;

	public LightCurveFitter(SolutionSampler sampler, int numClusters, int numParticlesPerCluster) {
		this.sampler = sampler;
		this.numClusters = numClusters;
		this.numParticlesPerCluster = numParticlesPerCluster;
	}

	public final double getEpsilonFactor() {
		return epsilonFactor;
	}

	public final void setEpsilonFactor(double epsionFactor) {
		this.epsilonFactor = epsionFactor;
	}

	public final int getMaxGradientDescentIterations() {
		return maxGradientDescentIterations;
	}

	public final void setMaxGradientDescentIterations(int maxGradientDescentIterations) {
		this.maxGradientDescentIterations = maxGradientDescentIterations;
	}

	public final int getMaxClusteringIterations() {
		return maxClusteringIterations;
	}

	public final void setMaxClusteringIterations(int maxClusteringIterations) {
		this.maxClusteringIterations = maxClusteringIterations;
	}

	public final int getNumClusters() {
		return numClusters;
	}

	public final int getNumParticlesPerCluster() {
		return numParticlesPerCluster;
	}

	public Solution optimize(LightCurvePoint[] lightCurve) throws MathException {
		double[] fluxArray = LightCurvePoint.fluxArray(lightCurve);
		double comf = LightCurve.centerOfMassAsFraction(fluxArray);
		this.sampler.setPeakFraction(comf);
		Solution solutionStage1 = this.optimizeStandardErrorCS(fluxArray);
		Solution solutionStage3 = this.optimizeStandardErrorAGD(fluxArray, solutionStage1, this.maxGradientDescentIterations);
		return solutionStage3;
	}

	public Solution optimizeStandardErrorCS(double[] fluxArray) throws MathException {
		ClusteredEvaluator lfFinal = new FlexibleLossFunction(sampler, fluxArray, 0.50, 0.50);
		return this.optimizeCGSO(lfFinal);
	}
	
	public Solution optimizeStandardErrorAGD(double[] fluxArray, Solution initialSolution, int maxIterations) throws MathException {
		MultivariateRealFunction errorFunction = new FlexibleLossFunction(sampler, fluxArray, 0.10, 0.10);
		return this.optimizeAGD(fluxArray, initialSolution, errorFunction, maxIterations);
	}

	public Solution optimizeAGD(double[] fluxArray, Solution initialSolution, MultivariateRealFunction errorFunction, int maxIterations) throws MathException {
		SolutionSampler sampler = this.sampler;
		ApproximateGradientDescentOptimizer optimizer = new ApproximateGradientDescentOptimizer(sampler.getRandom()) {
			@Override
			protected void informProgress(int iteration, RealPointValuePair pointValue) {
				LightCurveFitter.this.informProgress("agd", iteration, pointValue.getValue());
			}			
		};
		optimizer.setMaxIterations(maxIterations);
		double[] initialPoint = sampler.solutionAsParameters(initialSolution);
		double[] minChangeShift = sampler.minimalChangeThreshold(initialPoint, 0.003);
		double[] epsilon = MathUtil.multiply(minChangeShift, this.epsilonFactor);
		RealPointValuePair optPoint = optimizer.optimize(errorFunction, initialPoint, epsilon);
		return sampler.parametersAsSolution(optPoint.getPointRef());
	}

	public Solution optimizeCGSO(ClusteredEvaluator finalErrorFunction, ClusteredEvaluator ... alternatingErrorFunctions) throws MathException {
		SolutionSampler sampler = this.sampler;
		Random random = sampler.getRandom();
		ClusteredGridSearchOptimizer optimizer = new ClusteredGridSearchOptimizer(random, numClusters, numParticlesPerCluster) {
			@Override
			protected void informProgress(Phase phase, int iteration, RealPointValuePair pointValue) {
				LightCurveFitter.this.informProgress("cgso-" + phase.name().toLowerCase(), iteration, pointValue.getValue());
			}			
		};
		
		optimizer.setMaxIterations(this.maxClusteringIterations);
		
		int vectorLength = sampler.getNumParameters();
		RealPointValuePair result = optimizer.optimize(vectorLength, finalErrorFunction);

		Solution solution = sampler.parametersAsSolution(result.getPointRef());

		return solution;
	}

	protected void informProgress(String stage, int iteration, double error) {		
	}
	
	protected void informEndOfWarmUpPhase(SolutionSampler sampler, List<RealPointValuePair> pointValues) {		
	}

	protected void informEndOfClusteringPhase(SolutionSampler sampler, List<RealPointValuePair> pointValues) {		
	}

	public static double meanSquaredError(LightCurvePoint[] lightCurve, double[] weights, Solution solution) {
		double[] fluxArray = LightCurvePoint.fluxArray(lightCurve);
		return meanSquaredError(fluxArray, weights, solution);
	}

	public static double meanSquaredError(double[] fluxArray, double[] weights, Solution solution) {
		double[] testFluxArray = solution.produceModeledFlux().getFluxArray();
		return meanSquaredError(fluxArray, weights, testFluxArray);
	}

	public static double meanSquaredError(double[] fluxArray, double[] weights, double[] testFluxArray) {
		int length = fluxArray.length;
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < length; i++) {
			double weight = weights[i];
			double diff = testFluxArray[i] - fluxArray[i];
			sum += (diff * diff) * weight;
			weightSum += weight;
		}
		return weightSum == 0 ? 0 : sum / weightSum;
	}
}
