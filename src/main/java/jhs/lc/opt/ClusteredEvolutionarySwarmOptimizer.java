package jhs.lc.opt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TreeSelectionEvent;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.optimization.RealPointValuePair;

import jhs.math.clustering.KMeansClusteringProducer;
import jhs.math.clustering.VectorialCluster;
import jhs.math.clustering.VectorialClusteringResults;
import jhs.math.common.ItemUtil;
import jhs.math.common.VectorialItem;
import jhs.math.util.ArrayUtil;
import jhs.math.util.ComparableValueHolder;
import jhs.math.util.ListUtil;
import jhs.math.util.MathUtil;

public class ClusteredEvolutionarySwarmOptimizer {
	private static final Logger logger = Logger.getLogger(ClusteredEvolutionarySwarmOptimizer.class.getName());
	private final Random random;	
	private final int populationSize;
	
	private int initialPoolSize = 1000;

	private int maxStartIterations = 0;	
	private int maxIterationsWithClustering = 200;
	private int maxConsolidationIterations = 50;

	private int maxClusterAlgoSteps = 3;
	private int numParticlesPerCluster = 1;

	private double phi = 2.01;
	private double omega = 0.1;

	private int maxSubspaceSize = 6;
	
	private double convergeDistance = 0.0001;
	private double outlierErrorSDFactor = 1.0;
	
	private double fitnessWeightDecayHalfFraction = 0.25;
	private double distanceWeightDecayHalfFraction = 0.10;
	
	private double startSD = 1.5;
	private double phiWarmup = -0.01;
	private double omegaWarmup = 0.05;
	private double warmUpDistanceWeightDecayHalfFraction = 0.03;
	
	public ClusteredEvolutionarySwarmOptimizer(Random random, int populationSize) {
		this.random = random;
		this.populationSize = populationSize;
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

	public final int getMaxConsolidationIterations() {
		return maxConsolidationIterations;
	}

	public final void setMaxConsolidationIterations(int maxConsolidationIterations) {
		this.maxConsolidationIterations = maxConsolidationIterations;
	}

	public final int getMaxStartIterations() {
		return maxStartIterations;
	}

	public final void setMaxStartIterations(int maxStartIterations) {
		this.maxStartIterations = maxStartIterations;
	}

	public final double getPhi() {
		return phi;
	}

	public final void setPhi(double phi) {
		this.phi = phi;
	}

	public final double getOmega() {
		return omega;
	}

	public final void setOmega(double omega) {
		this.omega = omega;
	}

	public final int getMaxSubspaceSize() {
		return maxSubspaceSize;
	}

	public final void setMaxSubspaceSize(int maxSubspaceSize) {
		this.maxSubspaceSize = maxSubspaceSize;
	}

	public final double getWeightDecayHalfFraction() {
		return fitnessWeightDecayHalfFraction;
	}

	public final void setWeightDecayHalfFraction(double weightDecayHalfFraction) {
		this.fitnessWeightDecayHalfFraction = weightDecayHalfFraction;
	}

	public final int getMaxClusterAlgoSteps() {
		return maxClusterAlgoSteps;
	}

	public final void setMaxClusterAlgoSteps(int maxClusterAlgoSteps) {
		this.maxClusterAlgoSteps = maxClusterAlgoSteps;
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
	public RealPointValuePair optimize(int vectorLength, ClusteredEvaluator finalErrorFunction, ClusteredEvaluator ...  alternatingErrorFunctions) throws MathException {
		int n = this.populationSize;
		double[] fitnessWeights = this.createGlobalWeights(n, this.fitnessWeightDecayHalfFraction);
		double[] distanceWeights = this.createGlobalWeights(n, this.distanceWeightDecayHalfFraction);
		double[] warmUpDistanceWeights = this.createGlobalWeights(n, this.warmUpDistanceWeightDecayHalfFraction);
		ClusteredEvaluator errorFunction = alternatingErrorFunctions.length > 0 ? alternatingErrorFunctions[0] : finalErrorFunction;
		List<Particle> workingSet = this.createInitialWorkingSet(n, vectorLength, errorFunction, warmUpDistanceWeights);
		this.informEndOfWarmUpPhase(ListUtil.map(workingSet, p -> p.getPointValuePair()));
		Phase phase = Phase.CLUSTERING;
		int maxIterationsWithClustering = this.maxIterationsWithClustering;
		int maxConsolidationIterations = this.maxConsolidationIterations;
		int currentAltFunctionIndex = -1;
		int iterationCount = 0;
		OUTER:
		for(;;) {
			switch(phase) {
			case CLUSTERING: 
				if(iterationCount >= maxIterationsWithClustering) {
					phase = Phase.CONSOLIDATION;
					iterationCount = 0;
					this.informEndOfClusteringPhase(ListUtil.map(workingSet, p -> p.getPointValuePair()));
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
				if(iterationCount >= maxConsolidationIterations) {
					break OUTER;
				}
				break;
			default:
				throw new IllegalStateException();
			}
			Collections.sort(workingSet);
			boolean consolidationPhase = phase == Phase.CONSOLIDATION;
			List<Particle> newParticles = this.produceNewParticles(workingSet.toArray(new Particle[workingSet.size()]), errorFunction, fitnessWeights, distanceWeights);
			if(!consolidationPhase) {
				List<Particle> wsWithoutOutliers = this.removeErrorOutliers(workingSet);
				//List<Particle> wsWithoutOutliers = this.extractBestWithClustering(n * 3 / 4, workingSet, 1);
				List<Particle> clusteringSet = ListUtil.concat(wsWithoutOutliers, newParticles);
				workingSet = this.extractBestWithClusteringDistance(n, clusteringSet);
				//workingSet = this.extractBestWithClustering(n, clusteringSet, this.numParticlesPerCluster);
			}
			else {
				workingSet = this.extractBest(n, ListUtil.concat(workingSet, newParticles));
			}
			if(workingSet.size() != n) {
				throw new IllegalStateException();
			}
			RealPointValuePair rpvp = this.getBestPoint(workingSet);
			this.informProgress(phase, iterationCount, rpvp);
			if(this.converged(workingSet)) {
				break;
			}
			if(iterationCount != 0 && (iterationCount % 20 == 0) && phase == Phase.CLUSTERING) {
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
			iterationCount++;
		}
		if(errorFunction != finalErrorFunction) {
			if(logger.isLoggable(Level.INFO)) {
				logger.info("---- Switching evaluation function ----");
			}
			errorFunction = finalErrorFunction;
			workingSet = this.revalidateWorkingSet(workingSet, errorFunction);													
		}
		return this.getBestPoint(workingSet);
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

	private RealPointValuePair getBestPointFromPS(List<ParticleStart> workingSet) {
		Particle bestParticle = this.getBestParticle(ListUtil.map(workingSet, ParticleStart::getParticle));
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

	private List<Particle> extractBestWithClusteringDistance(int n, List<Particle> particles) {
		int size = particles.size();
		if(n > size) {
			throw new IllegalArgumentException("Cannot extract " + n + " particles out of " + particles.size());
		}
		int numEliminatePerPass = (int) Math.ceil((double) size / n) - 1;
		TreeSet<Particle> sourcePool = new TreeSet<>(particles);
		List<Particle> discarded = new ArrayList<>();
		List<Particle> result = new ArrayList<>();
		while(result.size() < n) {
			if(sourcePool.isEmpty()) {
				sourcePool.addAll(discarded);
				discarded.clear();
				if(sourcePool.isEmpty()) {
					throw new IllegalStateException();
				}
			}
			Particle first = sourcePool.first();
			if(!sourcePool.remove(first)) {
				throw new IllegalStateException();
			}
			result.add(first);
			INNER:
			for(int i = 0; i < numEliminatePerPass; i++) {
				if(sourcePool.isEmpty()) {
					break INNER;
				}
				Particle closest = this.closestClusteringPosition(first, sourcePool);
				if(!sourcePool.remove(closest)) {
					throw new IllegalStateException();				
				}
				discarded.add(closest);
			}
		}
		return result;
	}
	
	private Particle closestClusteringPosition(Particle pivot, Collection<Particle> others) {
		Particle closest = null;
		double minDistance = Double.POSITIVE_INFINITY;
		double[] pivotPosition = pivot.clusteringPosition;
		for(Particle p : others) {
			double d = MathUtil.euclideanDistanceSquared(pivotPosition, p.clusteringPosition);
			if(d < minDistance) {
				minDistance = d;
				closest = p;
			}
		}
		if(closest == null) {
			throw new IllegalArgumentException();
		}
		return closest;
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
	 
	private List<Particle> produceNewParticles(Particle[] sortedWorkingSet, ClusteredEvaluator errorFunction, double[] fitnessWeights, double[] distanceWeights) throws MathException {
		List<Particle> result = new ArrayList<>();
		for(Particle particle1 : sortedWorkingSet) {
			Particle p = this.newParticle(particle1, sortedWorkingSet, fitnessWeights, distanceWeights, errorFunction);
			result.add(p);
		}
		return result;
	}

	private Particle newParticle(Particle particle1, Particle[] sortedParticles, double[] fitnessWeights, double[] distanceWeights, ClusteredEvaluator errorFunction) throws MathException {
		int[] subspace = this.createSubspace(particle1.parameters.length);
		double[] weights = this.getParticleWeights(particle1, sortedParticles, fitnessWeights, distanceWeights, subspace);
		if(sortedParticles.length != weights.length) {
			throw new IllegalStateException();
		}
		int index2 = ArrayUtil.randomIndex(weights, random);
		int index3 = ArrayUtil.randomIndex(weights, random);
		Particle particle2 = sortedParticles[index2];
		Particle particle3 = sortedParticles[index3];
		return this.newParticle(errorFunction, subspace, particle1, particle2, particle3);		
	}
	
	private double[] getParticleWeights(Particle particle, Particle[] particles, double[] fitnessWeights, double[] distanceWeights, int[] subspace) {
		double[] pivotPoint = particle.parameters;
		int[] ranks = ArrayUtil.ranks(particles, p -> distanceMetric(p.parameters, pivotPoint, subspace));
		int length = fitnessWeights.length;
		double[] weights = new double[length];
		for(int i = 0; i < length; i++) {
			Particle p = particles[i];
			if(p != particle) {
				weights[i] = fitnessWeights[i] * distanceWeights[ranks[i]];
			}
		}
		return weights;
	}

	private double distanceMetric(double[] point1, double[] point2, int[] subspace) {
		double sum = 0;
		for(int si = 0; si < subspace.length; si++) {
			int i = subspace[si];
			double diff = point1[i] - point2[i];
			sum += diff * diff;
		}
		return sum;
	}

	private Particle newParticle(ClusteredEvaluator errorFunction, int[] subspace, Particle particle1, Particle particle2, Particle particle3) throws MathException {
		Random r = this.random;
		double phi = this.phi;
		double omega = this.omega;
		double[] point1 = particle1.parameters;
		double[] point2 = particle2.parameters;
		double[] point3 = particle3.parameters;	
		double direction = particle2.getValue() < particle1.getValue() ? +1.0 : -1.0;
		double[] newParams = Arrays.copyOf(point1, point1.length);
		for(int si = 0; si < subspace.length; si++) {
			int i = subspace[si];
			double g = r.nextGaussian();
			double x = point1[i];
			x += (point3[i] - x) * g * omega;
			double f = r.nextDouble();
			newParams[i] =  x + direction * (point2[i] - x) * f * phi;
		}
		ClusteredParamEvaluation eval = errorFunction.evaluate(newParams);
		return new Particle(newParams, eval.getClusteringPosition(), eval.getError());
	}

	private double[] createGlobalWeights(int n, double decayHalfFraction) {
		double[] weights = new double[n];
		double k = 1.0 / (decayHalfFraction * n);
		for(int i = 0; i < n; i++) {
			weights[i] = Math.exp(-i * k);
		}
		return weights;
	}

	private int[] createSubspace(int vectorLength) {
		int[] vars = ArrayUtil.indexIdentity(vectorLength);
		int maxSS = this.maxSubspaceSize;
		if(maxSS >= vectorLength) {
			return vars;
		}
		ArrayUtil.shuffle(vars, random);
		return Arrays.copyOf(vars, maxSS);
	}

	private List<Particle> createInitialWorkingSet(int n, int vectorLength, ClusteredEvaluator errorFunction, double[] distanceWeights) throws MathException {
		List<Particle> startList = this.createRandomStarts(n, vectorLength, errorFunction);
		List<ParticleStart> workingSet = new ArrayList<ParticleStart>(ListUtil.map(startList, s -> createStartParticle2(s, s, startList)));
		int ni = this.maxStartIterations;
		for(int i = 0; i < ni; i++) {
			workingSet = this.updateInitialWorkingSet(n, vectorLength, workingSet, errorFunction, distanceWeights);
			RealPointValuePair pointValue = this.getBestPointFromPS(workingSet);
			this.informProgress(Phase.WARMUP, i, pointValue);
		}
		return ListUtil.map(workingSet, ParticleStart::getParticle);
	}

	private List<ParticleStart> updateInitialWorkingSet(int n, int vectorLength, List<ParticleStart> workingSet, ClusteredEvaluator errorFunction, double[] distanceWeights) throws FunctionEvaluationException {
		ParticleStart[] workingSetArray = workingSet.toArray(new ParticleStart[workingSet.size()]);
		for(int j = 0; j < n; j++) {
			ParticleStart currentStart = workingSet.get(j);
			ParticleStart candidateStart  = this.newCandidateStart(vectorLength, currentStart, j, workingSetArray, errorFunction, distanceWeights);
			if(candidateStart.getValue() < currentStart.getValue()) {
				workingSet.set(j, candidateStart);
			}
		}
		return workingSet;		
	}
	
	private ParticleStart newCandidateStart(int vectorLength, ParticleStart oldStart, int oldStartIndex, ParticleStart[] workingSet, ClusteredEvaluator errorFunction, double[] distanceWeights) throws FunctionEvaluationException {
		int[] subspace = this.createSubspace(vectorLength);
		Particle particle1 = oldStart.particle;
		double[] directionVector = this.startDirectionVector(vectorLength, oldStart, workingSet, subspace, distanceWeights);
		//Particle particle3 = this.randomParticle(workingSet, oldStartIndex);
		Random r = this.random;
		double phi = this.phiWarmup;
		double omega = this.omegaWarmup;
		double[] point1 = particle1.parameters;
		//double[] point3 = particle3.parameters;
		double[] newParams = Arrays.copyOf(point1, point1.length);
		for(int si = 0; si < subspace.length; si++) {
			int i = subspace[si];
			double g = r.nextGaussian();
			//double x = point1[i];
			//x += (point3[i] - x) * g * omega;
			double f = r.nextDouble();
			double dv = directionVector[i];
			newParams[i] =  point1[i] + dv * (g * omega + f * phi);
		}
		ClusteredParamEvaluation eval = errorFunction.evaluate(newParams);
		Particle newParticle = new Particle(newParams, eval.getClusteringPosition(), eval.getError());
		return this.createStartParticle(newParticle, newParticle, Arrays.asList(workingSet));
	}
	
	private double[] startDirectionVector(int vectorLength, ParticleStart oldStart, ParticleStart[] workingSet, int[] subspace, double[] distanceWeights) {
		Particle oldStartParticle = oldStart.particle;
		//TODO: Try clustering position
		double[] pivotPoint = oldStartParticle.parameters;
		int[] ranks = ArrayUtil.ranks(workingSet, p -> distanceMetric(p.particle.parameters, pivotPoint, subspace));
		double[] weights = new double[workingSet.length];
		for(int i = 0; i < weights.length; i++) {
			Particle p = workingSet[i].particle;
			if(p != oldStartParticle) {
				weights[i] = distanceWeights[ranks[i]];
			}
		}
		int index = ArrayUtil.randomIndex(weights, random);
		ParticleStart targetStart = workingSet[index];
		double[] oldStartParams = oldStartParticle.parameters;
		return MathUtil.subtract(targetStart.particle.parameters, oldStartParams);
	}
	
	private Particle randomParticle(List<ParticleStart> workingSet, int exceptIndex) {
		int n = workingSet.size();
		Random r = this.random;
		int refIndex;
		do {
			refIndex = Math.abs(r.nextInt() % n);
		} while(refIndex == exceptIndex);
		return workingSet.get(refIndex).particle;
	}

	/*
	private List<ParticleStart> updateInitialWorkingSet(int n, List<ParticleStart> workingSet, ClusteredEvaluator errorFunction) throws FunctionEvaluationException {
		Random r = this.random;
		for(int j = 0; j < n; j++) {
			ParticleStart currentStart = workingSet.get(j);
			int refIndex;
			do {
				refIndex = Math.abs(r.nextInt() % n);
			} while(refIndex == j);
			ParticleStart refStart = workingSet.get(refIndex);
			ParticleStart newStart = this.newStart(currentStart, refStart, workingSet, errorFunction);
			if(newStart.minDistanceToOthers > currentStart.minDistanceToOthers && newStart.getValue() < currentStart.getValue()) {
				workingSet.set(j, newStart);
			}
		}
		return workingSet;
	}
	*/
	
	private ParticleStart newStart(ParticleStart priorStart, ParticleStart refStart, List<ParticleStart> startWorkingSet, ClusteredEvaluator errorFunction) throws FunctionEvaluationException {
		double[] priorParams = priorStart.particle.parameters;
		double[] refParams = refStart.particle.parameters;
		double[] newParams = new double[priorParams.length];
		double omega = this.omega;
		Random r = this.random;
		for(int i = 0; i < newParams.length; i++) {
			double x = priorParams[i];
			newParams[i] = x + (refParams[i] - x) * r.nextGaussian() * omega;			
		}
		ClusteredParamEvaluation eval = errorFunction.evaluate(newParams);
		Particle newParticle = new Particle(newParams, eval.getClusteringPosition(), eval.getError());
		return this.createStartParticle(newParticle, priorStart.particle, startWorkingSet);
	}
	
	private ParticleStart createStartParticle(Particle particle, Particle except, List<ParticleStart> others) {
		double[] pivotPos = particle.clusteringPosition;
		double minDistance = Double.POSITIVE_INFINITY;
		for(ParticleStart op : others) {
			Particle opp = op.particle;
			if(opp != except) {
				double d = MathUtil.euclideanDistanceSquared(opp.clusteringPosition, pivotPos);
				if(d < minDistance) {
					minDistance = d;
				}
			}
		}
		return new ParticleStart(minDistance, particle);
	}

	private ParticleStart createStartParticle2(Particle particle, Particle except, List<Particle> others) {
		double[] pivotPos = particle.clusteringPosition;
		double minDistance = Double.POSITIVE_INFINITY;
		for(Particle opp : others) {
			if(opp != except) {
				double d = MathUtil.euclideanDistanceSquared(opp.clusteringPosition, pivotPos);
				if(d < minDistance) {
					minDistance = d;
				}
			}
		}
		return new ParticleStart(minDistance, particle);
	}

	private List<Particle> createRandomStarts(int n, int vectorLength, ClusteredEvaluator errorFunction) throws MathException {
		Random r = this.random;
		int poolSize = this.initialPoolSize;
		if(poolSize < n) {
			poolSize = n;
		}
		if(logger.isLoggable(Level.INFO)) {
			logger.info("Creating initial pool of " + poolSize + " particles.");
		}
		double ssd = this.startSD;
		List<Particle> pool = new ArrayList<>();		
		for(int i = 0; i < poolSize; i++) {
			double[] params = MathUtil.sampleUniformSymmetric(r, ssd, vectorLength);
			ClusteredParamEvaluation eval = errorFunction.evaluate(params);
			pool.add(new Particle(params, eval.getClusteringPosition(), eval.getError()));
		}
		return this.extractBestWithClustering(n, pool, 1);
	}
	
	protected void informProgress(Phase phase, int iteration, RealPointValuePair pointValue) {		
	}
	
	private static class ParticleStart extends ComparableValueHolder<ParticleStart> {
		private final double minDistanceToOthers;
		private final Particle particle;
		
		public ParticleStart(double minDistanceToOthers, Particle particle) {
			super();
			this.minDistanceToOthers = minDistanceToOthers;
			this.particle = particle;
		}

		public final Particle getParticle() {
			return particle;
		}

		@Override
		protected final double getValue() {
			return this.particle.getValue();
		}
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
		
	protected enum Phase {
		WARMUP, CLUSTERING, CONSOLIDATION;
	}
}
