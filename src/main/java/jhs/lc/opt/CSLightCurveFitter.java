package jhs.lc.opt;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jhs.lc.data.LightCurve;
import jhs.lc.data.LightCurvePoint;
import jhs.math.nn.PlainNeuralNetwork;
import jhs.math.util.ListUtil;
import jhs.math.util.MathUtil;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.RealPointValuePair;

public class CSLightCurveFitter {	
	//private static final Logger logger = Logger.getLogger(CSLightCurveFitter.class.getName());
	private final SolutionSampler sampler;
	private final int populationSize;
	
	private int initialPoolSize = 1000;
	
	private int maxCSWarmUpIterations = 50;
	private int maxCSIterationsWithClustering = 100;
	private int maxExtraCSIterations = 10;
	private int maxEliminationIterations = 0;
	private int maxGradientDescentIterations = 10;

	private double expansionFactor = 3.0;
	private double displacementFactor = 0.03;
	private double convergeDistance = 0.0001;
	private double circuitShuffliness = 0.5;
	
	private double epsilonFactor = 3.0;

	public CSLightCurveFitter(SolutionSampler sampler, int populationSize) {
		this.sampler = sampler;
		this.populationSize = populationSize;
	}

	public final int getInitialPoolSize() {
		return initialPoolSize;
	}

	public final void setInitialPoolSize(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	public final int getMaxCSWarmUpIterations() {
		return maxCSWarmUpIterations;
	}

	public final void setMaxCSWarmUpIterations(int maxCSWarmUpIterations) {
		this.maxCSWarmUpIterations = maxCSWarmUpIterations;
	}

	public final int getPopulationSize() {
		return populationSize;
	}

	public final double getEpsilonFactor() {
		return epsilonFactor;
	}

	public final void setEpsilonFactor(double epsionFactor) {
		this.epsilonFactor = epsionFactor;
	}

	public final int getMaxCSIterationsWithClustering() {
		return maxCSIterationsWithClustering;
	}

	public final void setMaxCSIterationsWithClustering(int maxCSIterationsWithClustering) {
		this.maxCSIterationsWithClustering = maxCSIterationsWithClustering;
	}

	public final int getMaxExtraCSIterations() {
		return maxExtraCSIterations;
	}

	public final void setMaxExtraCSIterations(int maxExtraCSIterations) {
		this.maxExtraCSIterations = maxExtraCSIterations;
	}

	public final int getMaxGradientDescentIterations() {
		return maxGradientDescentIterations;
	}

	public final void setMaxGradientDescentIterations(int maxGradientDescentIterations) {
		this.maxGradientDescentIterations = maxGradientDescentIterations;
	}

	public int getMaxEliminationIterations() {
		return maxEliminationIterations;
	}

	public void setMaxEliminationIterations(int maxEliminationIterations) {
		this.maxEliminationIterations = maxEliminationIterations;
	}

	public final double getExpansionFactor() {
		return expansionFactor;
	}

	public final void setExpansionFactor(double expansionFactor) {
		this.expansionFactor = expansionFactor;
	}

	public final double getDisplacementFactor() {
		return displacementFactor;
	}

	public final void setDisplacementFactor(double displacementFactor) {
		this.displacementFactor = displacementFactor;
	}

	public final double getConvergeDistance() {
		return convergeDistance;
	}

	public final void setConvergeDistance(double convergeDistance) {
		this.convergeDistance = convergeDistance;
	}

	public final double getCircuitShuffliness() {
		return circuitShuffliness;
	}

	public final void setCircuitShuffliness(double circuitShuffliness) {
		this.circuitShuffliness = circuitShuffliness;
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
		CircuitSearchEvaluator lfWarmUp = new SizingLossFunction(sampler, fluxArray);
		//CircuitSearchEvaluator lfWarmUp = new PrimaryLossFunction(sampler, fluxArray, 1, 0, 1);
		CircuitSearchEvaluator lfFinal = this.getDefaultLossFunction(fluxArray);
		//CircuitSearchEvaluator lf1 = new PrimaryLossFunction(sampler, fluxArray, 1, 0, 0);
		CircuitSearchEvaluator lf2 = new PrimaryLossFunction(sampler, fluxArray, 1, 0, 1);
		CircuitSearchEvaluator lf3 = new PrimaryLossFunction(sampler, fluxArray, 3, 0, 1);
		return this.optimizeCircuitSearch(lfWarmUp, lfFinal, lf2, lf3);
	}
	
	private AbstractLossFunction getDefaultLossFunction(double[] fluxArray) {
		return new PrimaryLossFunction(sampler, fluxArray, 3, 0, 1);
	}

	public Solution optimizeStandardErrorAGD(double[] fluxArray, Solution initialSolution, int maxIterations) throws MathException {
		MultivariateRealFunction errorFunction = this.getDefaultLossFunction(fluxArray);
		return this.optimizeAGD(fluxArray, initialSolution, errorFunction, maxIterations);
	}

	public Solution optimizeAGD(double[] fluxArray, Solution initialSolution, MultivariateRealFunction errorFunction, int maxIterations) throws MathException {
		SolutionSampler sampler = this.sampler;
		ApproximateGradientDescentOptimizer optimizer = new ApproximateGradientDescentOptimizer(sampler.getRandom()) {
			@Override
			protected void informProgress(int iteration, RealPointValuePair pointValue) {
				CSLightCurveFitter.this.informProgress("agd", iteration, pointValue.getValue());
			}			
		};
		optimizer.setMaxIterations(maxIterations);
		double[] initialPoint = sampler.solutionAsParameters(initialSolution);
		double[] minChangeShift = sampler.minimalChangeThreshold(initialPoint, 0.003);
		double[] epsilon = MathUtil.multiply(minChangeShift, this.epsilonFactor);
		RealPointValuePair optPoint = optimizer.optimize(errorFunction, initialPoint, epsilon);
		return sampler.parametersAsSolution(optPoint.getPointRef());
	}

	public Solution optimizeCircuitSearch(CircuitSearchEvaluator warmUpErrorFunction, CircuitSearchEvaluator finalErrorFunction, CircuitSearchEvaluator ... alternatingErrorFunctions) throws MathException {
		SolutionSampler sampler = this.sampler;
		Random random = sampler.getRandom();
		CircuitSearchOptimizer optimizer = new CircuitSearchOptimizer(random, this.populationSize) {
			@Override
			protected void informProgress(int iteration, RealPointValuePair pointValue) {
				CSLightCurveFitter.this.informProgress("circuit search", iteration, pointValue.getValue());
			}

			@Override
			protected void informEndOfWarmUpPhase(List<RealPointValuePair> pointValues) {
				CSLightCurveFitter.this.informEndOfWarmUpPhase(sampler, pointValues);
			}						
		};
		optimizer.setInitialPoolSize(this.initialPoolSize);
		optimizer.setCircuitShuffliness(this.circuitShuffliness);
		optimizer.setConvergeDistance(this.convergeDistance);
		optimizer.setDisplacementFactor(this.displacementFactor);
		optimizer.setExpansionFactor(this.expansionFactor);
		optimizer.setMaxWarmUpIterations(this.maxCSWarmUpIterations);
		optimizer.setMaxConsolidationIterations(this.maxExtraCSIterations);
		optimizer.setMaxIterationsWithClustering(this.maxCSIterationsWithClustering);
		optimizer.setMaxEliminationIterations(this.maxEliminationIterations);
		int vectorLength = sampler.getNumParameters();
		RealPointValuePair result = optimizer.optimize(vectorLength, warmUpErrorFunction, finalErrorFunction, alternatingErrorFunctions);

		Solution solution = sampler.parametersAsSolution(result.getPointRef());

		return solution;
	}

	protected void informProgress(String stage, int iteration, double error) {		
	}
	
	protected void informEndOfWarmUpPhase(SolutionSampler sampler, List<RealPointValuePair> pointValues) {		
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
