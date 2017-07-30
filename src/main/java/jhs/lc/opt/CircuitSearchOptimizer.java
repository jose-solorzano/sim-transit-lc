package jhs.lc.opt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jhs.math.clustering.KMeansClusteringProducer;
import jhs.math.clustering.VectorialCluster;
import jhs.math.clustering.VectorialClusteringResults;
import jhs.math.common.ItemUtil;
import jhs.math.common.VectorialItem;
import jhs.math.util.ArrayUtil;
import jhs.math.util.ComparableValueHolder;
import jhs.math.util.ListUtil;
import jhs.math.util.MathUtil;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.RealPointValuePair;

public class CircuitSearchOptimizer {
	private static final Logger logger = Logger.getLogger(CircuitSearchOptimizer.class.getName());
	private final Random random;	
	private final int populationSize;
	
	private int initialPoolSize = 1000;

	private int maxWarmUpIterations = 50;	
	private int maxIterationsWithClustering = 200;
	private int maxConsolidationIterations = 50;

	private int maxClusterAlgoSteps = 3;
	private int maxEliminationIterations = 0;
	private int numParticlesPerCluster = 2;
	
	private double expansionFactor = 3.0;
	private double displacementFactor = 0.03;
	private double convergeDistance = 0.0001;
	private double circuitShuffliness = 0.5;
	private double outlierErrorSDFactor = 0.5;
	
	private double agdGradientFactor = 0.3;
		
	public CircuitSearchOptimizer(Random random, int populationSize) {
		this.random = random;
		this.populationSize = populationSize;
	}
	
	public final double getCircuitShuffliness() {
		return circuitShuffliness;
	}

	public final void setCircuitShuffliness(double circuitShuffliness) {
		this.circuitShuffliness = circuitShuffliness;
	}

	
	public final int getMaxWarmUpIterations() {
		return maxWarmUpIterations;
	}

	public final void setMaxWarmUpIterations(int maxWarmUpIterations) {
		this.maxWarmUpIterations = maxWarmUpIterations;
	}

	public final int getMaxConsolidationIterations() {
		return maxConsolidationIterations;
	}

	public final void setMaxConsolidationIterations(int maxConsolidationIterations) {
		this.maxConsolidationIterations = maxConsolidationIterations;
	}

	public final int getNumParticlesPerCluster() {
		return numParticlesPerCluster;
	}

	public final void setNumParticlesPerCluster(int numParticlesPerCluster) {
		this.numParticlesPerCluster = numParticlesPerCluster;
	}

	public final double getOutlierErrorSDFactor() {
		return outlierErrorSDFactor;
	}

	public final void setOutlierErrorSDFactor(double outlierErrorSDFactor) {
		this.outlierErrorSDFactor = outlierErrorSDFactor;
	}

	public final int getMaxIterationsWithClustering() {
		return maxIterationsWithClustering;
	}

	public final void setMaxIterationsWithClustering(int maxIterationsWithClustering) {
		this.maxIterationsWithClustering = maxIterationsWithClustering;
	}

	public final int getMaxClusterAlgoSteps() {
		return maxClusterAlgoSteps;
	}

	public final void setMaxClusterAlgoSteps(int maxClusterAlgoSteps) {
		this.maxClusterAlgoSteps = maxClusterAlgoSteps;
	}

	public int getMaxEliminationIterations() {
		return maxEliminationIterations;
	}

	public void setMaxEliminationIterations(int maxEliminationIterations) {
		this.maxEliminationIterations = maxEliminationIterations;
	}

	public double getAgdGradientFactor() {
		return agdGradientFactor;
	}

	public void setAgdGradientFactor(double agdGradientFactor) {
		this.agdGradientFactor = agdGradientFactor;
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

	public final int getPopulationSize() {
		return populationSize;
	}

	public final int getInitialPoolSize() {
		return initialPoolSize;
	}

	public final void setInitialPoolSize(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	@SuppressWarnings("unchecked")
	public RealPointValuePair optimize(int vectorLength, ClusteredEvaluator warmUpErrorFunction, ClusteredEvaluator finalErrorFunction, ClusteredEvaluator ...  alternatingErrorFunctions) throws MathException {
		int n = this.populationSize;
		Phase phase = Phase.WARMUP;
		int maxWarmUpIterations = this.maxWarmUpIterations;
		ClusteredEvaluator errorFunction = maxWarmUpIterations <= 0 && alternatingErrorFunctions.length > 0 ? alternatingErrorFunctions[0] : (maxWarmUpIterations <= 0 ? finalErrorFunction : warmUpErrorFunction);
		List<Particle> workingSet = this.createInitialWorkingSet(n, vectorLength, errorFunction);
		int maxIterationsWithClustering = this.maxIterationsWithClustering;
		int maxConsolidationIterations = this.maxConsolidationIterations;
		int maxWarmUpPlusClusteringIterations = maxWarmUpIterations + maxIterationsWithClustering;
		int maxIterations = maxWarmUpIterations + maxIterationsWithClustering + maxConsolidationIterations;
		int currentAltFunctionIndex = -1;
		int i;
		for(i = 0; i < maxIterations; i++) {
			switch(phase) {
			case WARMUP:
				if(i >= maxWarmUpIterations) {
					this.informEndOfWarmUpPhase(ListUtil.map(workingSet, p -> p.getPointValuePair()));
					phase = Phase.CLUSTERING;
					ClusteredEvaluator newErrorFunction;
					if(alternatingErrorFunctions.length != 0) {
						currentAltFunctionIndex = 0;
						newErrorFunction = alternatingErrorFunctions[currentAltFunctionIndex];
					}
					else {
						newErrorFunction = finalErrorFunction;
					}
					if(newErrorFunction != errorFunction) {
						if(logger.isLoggable(Level.INFO)) {
							logger.info("---- Switching evaluation function ----");
						}
						errorFunction = newErrorFunction;
						workingSet = this.revalidateWorkingSet(workingSet, errorFunction);							
					}
				}
				break;
			case CLUSTERING: 
				if(i >= maxWarmUpPlusClusteringIterations) {
					this.informEndOfClusteringPhase(ListUtil.map(ListUtil.sorted(workingSet), p -> p.getPointValuePair()));
					phase = Phase.CONSOLIDATION;
					if(errorFunction != finalErrorFunction) {
						if(logger.isLoggable(Level.INFO)) {
							logger.info("---- Switching evaluation function ----");
						}
						errorFunction = finalErrorFunction;
						workingSet = this.revalidateWorkingSet(workingSet, errorFunction);													
					}
				}
				break;
			case CONSOLIDATION:
				break;
			}
			boolean consolidationPhase = phase == Phase.CONSOLIDATION;
			List<Particle> newParticles = this.circuitSearch(workingSet, errorFunction, !consolidationPhase);
			if(i == 0) {
				workingSet = newParticles;
			} else if(!consolidationPhase) {
				//List<Particle> wsWithoutOutliers = this.extractBestWithClustering(n * 2 / 3, workingSet, 1);
				List<Particle> wsWithoutOutliers = this.removeErrorOutliers(workingSet);
				List<Particle> clusteringSet = ListUtil.concat(wsWithoutOutliers, newParticles);
				workingSet = this.extractBestWithClustering(n, clusteringSet, this.numParticlesPerCluster);
			}
			else {
				workingSet = this.extractBest(n, ListUtil.concat(workingSet, newParticles));
			}
			if(workingSet.size() != n) {
				throw new IllegalStateException();
			}
			RealPointValuePair rpvp = this.getBestPoint(workingSet);
			this.informProgress(i, rpvp);
			if(this.converged(workingSet)) {
				break;
			}
			if(i != 0 && i != maxWarmUpIterations && (i % 20 == 0) && phase == Phase.CLUSTERING) {
				if(currentAltFunctionIndex != -1) {
					currentAltFunctionIndex++;
					if(currentAltFunctionIndex >= alternatingErrorFunctions.length) {
						currentAltFunctionIndex = 0;
					}
					ClusteredEvaluator newErrorFunction = alternatingErrorFunctions[currentAltFunctionIndex];
					if(newErrorFunction != errorFunction) {
						if(logger.isLoggable(Level.INFO)) {
							logger.info("---- Switching evaluation function ----");
						}
						errorFunction = newErrorFunction;
						workingSet = this.revalidateWorkingSet(workingSet, errorFunction);							
					}
				}				
			}
		}
		if(errorFunction != finalErrorFunction) {
			if(logger.isLoggable(Level.INFO)) {
				logger.info("---- Switching evaluation function ----");
			}
			errorFunction = finalErrorFunction;
			workingSet = this.revalidateWorkingSet(workingSet, errorFunction);													
		}
		List<RealPointValuePair> pointValues = ListUtil.map(workingSet, p -> p.getPointValuePair());		
		return this.eliminationViaAGD(pointValues, errorFunction, i);
	}	
	
	protected void informEndOfWarmUpPhase(List<RealPointValuePair> pointValues) {		
	}

	protected void informEndOfClusteringPhase(List<RealPointValuePair> pointValues) {
	}

	private List<Particle> revalidateWorkingSet(List<Particle> particles, ClusteredEvaluator errorFunction) throws FunctionEvaluationException {
		List<Particle> result = new ArrayList<>();
		for(Particle p : particles) {
			double[] parameters = p.parameters;
			ClusteredParamEvaluation eval = errorFunction.evaluate(parameters);
			result.add(new Particle(parameters, eval.getClusteringPosition(), eval.getError()));
		}
		return result;
	}
	
	private RealPointValuePair eliminationViaAGD(List<RealPointValuePair> pointValues, final ClusteredEvaluator errorFunction, int baseIteration) throws MathException {
		MultivariateRealFunction gdErrorFunction = new MultivariateRealFunction() {			
			@Override
			public final double value(double[] params) throws FunctionEvaluationException, IllegalArgumentException {
				ClusteredParamEvaluation pe = errorFunction.evaluate(params);
				return pe.getError();
			}
		};
		ApproximateGradientDescentOptimizer agdOptimizer = new ApproximateGradientDescentOptimizer(this.random);
		int maxAgdIterations = this.maxEliminationIterations;
		List<RealPointValuePair> currentPointValues = pointValues;
		double agdgf = this.agdGradientFactor;
		for(int i = 0; i < maxAgdIterations && currentPointValues.size() > 1; i++) {
			List<RealPointValuePair> newPointValues = new ArrayList<>();
			for(RealPointValuePair pointValue : currentPointValues) {
				double[] epsilon = new double[pointValue.getPointRef().length];
				Arrays.fill(epsilon, 0.01);
				RealPointValuePair newPointValue = agdOptimizer.doOneStep(pointValue, gdErrorFunction, agdgf, epsilon);
				newPointValues.add(newPointValue);
			}
			Collections.sort(newPointValues, (pv1, pv2) -> Double.compare(pv1.getValue(), pv2.getValue()));
			currentPointValues = newPointValues.subList(0, newPointValues.size() - 1);
			this.informProgress(baseIteration + i, currentPointValues.get(0));
		}
		return ListUtil.min(currentPointValues, RealPointValuePair::getValue);
	}
	
	private boolean converged(List<Particle> workingSet) {
		double[] meanPosition = ItemUtil.meanPosition(workingSet, Particle::getParameters);
		double maxDiffSq = Double.NEGATIVE_INFINITY;
		for(Particle particle : workingSet) {
			double pmds = MathUtil.maxSquaredDiff(particle.parameters, meanPosition);
			if(pmds > maxDiffSq) {
				maxDiffSq = pmds;
			}
		}
		return Math.sqrt(maxDiffSq) <= this.convergeDistance;
	}
	
	private RealPointValuePair getBestPoint(List<Particle> workingSet) {
		Particle bestParticle = this.getBestParticle(workingSet);
		return bestParticle == null ? null : new RealPointValuePair(bestParticle.parameters, bestParticle.evaluation);
	}
		
	private Particle getBestParticle(List<Particle> workingSet) {	
		double minError = Double.POSITIVE_INFINITY;
		Particle bestParticle = null;
		for(Particle particle : workingSet) {
			if(particle.evaluation < minError) {
				minError = particle.evaluation;
				bestParticle = particle;
			}
		}
		return bestParticle;
	}

	private List<Particle> extractBest(int n, List<Particle> particles) {
		List<Particle> sortedParticles = new ArrayList<>(particles);
		Collections.sort(sortedParticles);
		int top = Math.min(n, sortedParticles.size());
		return sortedParticles.subList(0, top);
	}
	
	private List<Particle> extractBestWithClustering(int n, List<Particle> particles, int numParticlesPerCluster) {
		int nppc = numParticlesPerCluster;
		if(nppc < 1) {
			nppc = 1;
		}
		int numClusters = n / nppc;
		if(numClusters < 1) {
			numClusters = 1;
		}
		KMeansClusteringProducer<Particle> clusterEngine = new KMeansClusteringProducer<>(this.random, numClusters, this.maxClusterAlgoSteps);
		VectorialClusteringResults<Particle> clusterResults = clusterEngine.produceClustering(particles);		
		List<Particle> result = new ArrayList<>();
		for(VectorialCluster<Particle> cluster : clusterResults.getClusters()) {
			List<Particle> cp = this.extractBest(nppc, cluster.getItems());
			result.addAll(cp);
		}
		Set<Particle> origResultSet = new HashSet<>(result);
		if(result.size() < n) {
			for(Particle particle : ListUtil.sorted(particles)) {
				if(!origResultSet.contains(particle)) {
					result.add(particle);
					if(result.size() >= n) {
						break;
					}
				}
			}
		}
		if(result.size() != n) {
			throw new IllegalStateException("result.size()=" + result.size() + ", particles.size()=" + particles.size());
		}
		return result;
	}
	
	private List<Particle> removeErrorOutliers(List<Particle> particles) {
		double[] errors = ArrayUtil.doubleValueVector(particles, Particle::getValue);
		double errorMedian = MathUtil.median(errors, true);
		double errorSD = MathUtil.standardDev(errors, errorMedian);
		double maxError = errorMedian + errorSD * this.outlierErrorSDFactor;
		List<Particle> result = new ArrayList<Particle>(particles);
		result.removeIf(p -> p.getValue() > maxError);
		return result;
	}
	 
	private List<Particle> circuitSearch(List<Particle> workingSet, ClusteredEvaluator errorFunction, boolean clusteringPhase) throws MathException {
		List<Particle> circuit = this.produceCircuit(workingSet, clusteringPhase);
		int size = circuit.size();
		List<Particle> result = new ArrayList<>();
		for(int i = 0; i < size; i++) {
			int j = i == size -1 ? 0 : i + 1;
			Particle point1 = circuit.get(i);
			Particle point2 = circuit.get(j);
			Particle p = this.lineSearch(point1, point2, errorFunction);
			result.add(p);
		}
		return result;
	}

	private Particle lineSearch(Particle particle1, Particle particle2, ClusteredEvaluator errorFunction) throws MathException {
		double error1 = particle1.evaluation;
		double error2 = particle2.evaluation;
		double[] params1 = particle1.getParameters();
		double[] params2 = particle2.getParameters();
		double[] difference = MathUtil.subtract(params2, params1);
		double diffSd = MathUtil.standardDev(difference, 0);
		double moveSd = diffSd * this.displacementFactor;
		params1 = this.movePoint(params1, moveSd);
		params2 = this.movePoint(params2, moveSd);
		//difference = MathUtil.subtract(params2, params1);

		Particle candidate = this.newParticleInHyperrectangle(errorFunction, params1, params2, 0, 1.0);

		double candidateError = candidate.evaluation;
		if(candidateError <= error1 && candidateError <= error2) {
			return candidate;
		}
		double ef = this.expansionFactor;
		if(candidateError <= error1) {
			Particle secondCandidate = this.newParticleInHyperrectangle(errorFunction, params1, params2, 1.0, 1.0 + ef);
			return secondCandidate.evaluation < candidateError ? secondCandidate : candidate;
		} else if(candidateError <= error2) {
			Particle secondCandidate = this.newParticleInHyperrectangle(errorFunction, params1, params2, -ef, 0);
			return secondCandidate.evaluation < candidateError ? secondCandidate : candidate;
		}
		else {
			Particle secondCandidate = this.newParticleInHyperrectangle(errorFunction, params1, params2, -ef, 1.0 + ef);
			return secondCandidate.evaluation < candidateError ? secondCandidate : candidate;			
		}
	}

	private Particle newParticleInHyperrectangle(ClusteredEvaluator errorFunction, double[] point1, double[] point2, double fromFraction, double toFraction) throws MathException {
		Random r = this.random;
		int length = point1.length;
		double[] newParams = new double[length];
		for(int i = 0; i < length; i++) {
			double f = r.nextDouble();
			double p = fromFraction + f * (toFraction - fromFraction);
			newParams[i] = point1[i] * (1 - p) + point2[i] * p;
		}
		ClusteredParamEvaluation eval = errorFunction.evaluate(newParams);
		return new Particle(newParams, eval.getClusteringPosition(), eval.getError());
	}

	/*
	private Particle crossover(CircuitSearchEvaluator errorFunction, double[] params1, double[] params2) throws FunctionEvaluationException {
		Random r = this.random;
		if(params1.length <= 1) {
			throw new IllegalArgumentException("Only " + params1.length + " parameters.");
		}
		int pivot1 = Math.abs(r.nextInt() % (params1.length + 1));
		int pivot2;
		do {
			pivot2 = Math.abs(r.nextInt() % (params1.length + 1));
		} while(pivot2 == pivot1);
		int pivotMin = Math.min(pivot1, pivot2);
		int pivotMax = Math.max(pivot1, pivot2);
		double[] newParams = ArrayUtil.concat(Arrays.copyOfRange(params1, 0, pivotMin), Arrays.copyOfRange(params2, pivotMin, pivotMax), Arrays.copyOfRange(params1, pivotMax, params1.length));
		CircuitSearchParamEvaluation evaluation = errorFunction.evaluate(newParams);
		return new Particle(newParams, evaluation.getClusteringPosition(), evaluation.getError());
	}
	*/

	/*
	private Particle newParticleFromBaseline(CircuitSearchEvaluator errorFunction, double[] baselineParams, double[] directionVector, double fromFraction, double toFraction) throws MathException {
		double p = fromFraction + this.random.nextDouble() * (toFraction - fromFraction);
		double[] newParams = MathUtil.add(baselineParams, MathUtil.multiply(directionVector, p));
		CircuitSearchParamEvaluation eval = errorFunction.evaluate(newParams);
		return new Particle(newParams, eval.getClusteringPosition(), eval.getError());
	}
	*/

	/*
	private double[] alterVector(double[] vector) {
		Random r = this.random;
		double d = this.displacementFactor;
		double[] newVector = new double[vector.length];
		for(int i = 0; i < vector.length; i++) {
			newVector[i] = vector[i] * Math.exp(r.nextGaussian() * d);
		}
		return newVector;		
	}
	*/

	private double[] movePoint(double[] vector, double sd) {
		Random r = this.random;
		double[] newVector = new double[vector.length];
		for(int i = 0; i < vector.length; i++) {
			newVector[i] = vector[i] + r.nextGaussian() * sd;
		}
		return newVector;		
	}
	
	private List<Particle> produceCircuit(List<Particle> workingSet, boolean clusteringPhase) {
		Random r = this.random;
		double cs = this.circuitShuffliness;
		List<Particle> shuffledWS = ListUtil.shuffledList(workingSet, r);
		Node head = null;
		for(Particle particle : shuffledWS) {
			if(head == null || head.next == null || r.nextDouble() < cs) {
				head = this.insertHead(head, particle);
			}
			else {
				head = clusteringPhase ? this.insertAtDistanceBasedLocation(head, particle) : this.insertAtErrorBasedLocation(head, particle);
			}
		}
		List<Particle> result = new ArrayList<>();
		while(head != null) {
			result.add(head.particle);
			head = head.next;
		}
		if(result.size() != shuffledWS.size()) {
			throw new IllegalStateException();
		}
		return result;
	}
	
	private Node insertHead(Node head, Particle particle) {
		return new Node(particle, head);
	}

	private void insertAfter(Node prior, Particle particle) {
		prior.next = new Node(particle, prior.next);
	}
	
	private Node insertAtDistanceBasedLocation(Node head, Particle particle) {
		double[] point = particle.parameters;
		double distanceToHead = MathUtil.euclideanDistance(point, head.particle.parameters);
		double distanceToPrior = distanceToHead;
		double minDistanceChange = distanceToHead;
		Node insertAfter = null;
		Node pointer = head;
		Node next;
		while((next = pointer.next) != null) {
			double distancePointerNext = MathUtil.euclideanDistance(pointer.particle.parameters, next.particle.parameters);
			double distanceParticleNext =  MathUtil.euclideanDistance(point, next.particle.parameters);
			double distanceChange = distanceToPrior + distanceParticleNext - distancePointerNext;
			if(distanceChange < minDistanceChange) {
				minDistanceChange = distanceChange;
				insertAfter = pointer;
			}
			distanceToPrior = distanceParticleNext;
			pointer = pointer.next;
		}
		if(distanceToPrior < minDistanceChange) {
			insertAfter = pointer;
		}
		if(insertAfter == null) {
			return this.insertHead(head, particle);
		}
		else {
			this.insertAfter(insertAfter, particle);
			return head;
		}
	}

	private Node insertAtErrorBasedLocation(Node head, Particle particle) {
		double particleError = particle.evaluation;
		double errorDiffWithHead = Math.abs(particleError - head.particle.evaluation);
		double errorDiffWithPrior = errorDiffWithHead;
		double maxErrorDiffChange = errorDiffWithPrior;
		Node insertAfter = null;
		Node pointer = head;
		Node next;
		while((next = pointer.next) != null) {
			double errorDiffPointerNext = Math.abs(pointer.particle.evaluation - next.particle.evaluation);
			double errorDiffParticleNext =  Math.abs(particleError - next.particle.evaluation);
			double errorDiffChange = errorDiffWithPrior + errorDiffParticleNext - errorDiffPointerNext;
			if(errorDiffChange > maxErrorDiffChange) {
				maxErrorDiffChange = errorDiffChange;
				insertAfter = pointer;
			}
			errorDiffWithPrior = errorDiffParticleNext;
			pointer = pointer.next;
		}
		if(errorDiffWithPrior > maxErrorDiffChange) {
			insertAfter = pointer;
		}
		if(insertAfter == null) {
			return this.insertHead(head, particle);
		}
		else {
			this.insertAfter(insertAfter, particle);
			return head;
		}
	}

	private List<Particle> createInitialWorkingSet(int n, int vectorLength, ClusteredEvaluator errorFunction) throws MathException {
		Random r = this.random;
		int poolSize = this.initialPoolSize;
		if(poolSize < n) {
			poolSize = n;
		}
		if(logger.isLoggable(Level.INFO)) {
			logger.info("Creating initial pool of " + poolSize + " particles.");
		}
		List<Particle> pool = new ArrayList<>();		
		for(int i = 0; i < poolSize; i++) {
			double[] params = MathUtil.sampleUniformSymmetric(r, 1.0, vectorLength);
			ClusteredParamEvaluation eval = errorFunction.evaluate(params);
			pool.add(new Particle(params, eval.getClusteringPosition(), eval.getError()));
		}
		return this.extractBestWithClustering(n, pool, 1);
	}
	
	protected void informProgress(int iteration, RealPointValuePair pointValue) {		
	}
	
	private static class Particle extends ComparableValueHolder<Particle> implements VectorialItem {
		private final double[] parameters;
		private final double[] clusteringPosition;
		private final double evaluation;
		
		public Particle(double[] parameters, double[] clusteringPosition, double evaluation) {
			super();
			this.parameters = parameters;
			this.clusteringPosition = clusteringPosition;
			this.evaluation = evaluation;
		}

		public final double[] getParameters() {
			return parameters;
		}

		@Override
		public final double[] getPosition() {
			return this.clusteringPosition;
		}

		@Override
		protected final double getValue() {
			return this.evaluation;
		}
		
		public RealPointValuePair getPointValuePair() {
			return new RealPointValuePair(this.parameters, this.evaluation);
		}
	}
	
	private static class Node {
		private final Particle particle;
		private Node next;
		
		public Node(Particle particle, Node next) {
			this.particle = particle;
			this.next = next;
		}
	}
	
	private enum Phase {
		WARMUP, CLUSTERING, CONSOLIDATION;
	}
}
