package jhs.lc.opt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.stat.clustering.Cluster;

import jhs.math.clustering.KMeansClusteringProducer;
import jhs.math.clustering.VectorialCluster;
import jhs.math.clustering.VectorialClusteringResults;
import jhs.math.common.ItemUtil;
import jhs.math.common.SimpleVectorialItem;
import jhs.math.common.VectorialItem;
import jhs.math.optimization.GradientReductionConvergenceChecker;
import jhs.math.regression.linear.WeightedLinearRegression;
import jhs.math.util.ComparableValueHolder;
import jhs.math.util.ListUtil;
import jhs.math.util.MathUtil;

public class CircuitSearchOptimizer {
	private final Random random;
	private final int populationSize;
	
	private int maxTotalIterations = 2000;
	private int maxIterationsWithClustering = 100;
	private int maxClusterAlgoSteps = 3;
	
	private double expansionFactor = 3.0;
	private double displacementFactor = 0.03;
	private double convergeDistance = 0.0001;
	private double circuitShuffliness = 0.5;
	
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

	public final int getMaxTotalIterations() {
		return maxTotalIterations;
	}

	public final void setMaxTotalIterations(int maxTotalIterations) {
		this.maxTotalIterations = maxTotalIterations;
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

	@SuppressWarnings("unchecked")
	public RealPointValuePair optimize(MultivariateRealFunction errorFunction, int vectorLength) throws MathException {
		int n = this.populationSize;
		int maxIterations = this.maxTotalIterations;
		int maxIterationsWithClustering = this.maxIterationsWithClustering;
		List<Particle> workingSet = this.createInitialWorkingSet(n, vectorLength);
		RealPointValuePair rpvp = null;
		for(int i = 0; i < maxIterations; i++) {
			boolean clusteringPhase = i < maxIterationsWithClustering;
			List<Particle> newParticles = this.circuitSearch(workingSet, errorFunction, clusteringPhase);
			if(i == 0) {
				workingSet = newParticles;
			} else if(clusteringPhase) {
				workingSet = this.extractBestWithClustering(n, ListUtil.concat(workingSet, newParticles));
			}
			else {
				workingSet = this.extractBest(n, ListUtil.concat(workingSet, newParticles));
			}
			if(workingSet.size() != n) {
				throw new IllegalStateException();
			}
			rpvp = this.getBestPoint(workingSet);
			this.informProgress(i, rpvp);
			if(this.converged(workingSet)) {
				break;
			}
		}
		return rpvp;
	}
	
	private boolean converged(List<Particle> workingSet) {
		double[] meanPosition = ItemUtil.meanPosition(workingSet);
		double maxDiffSq = Double.NEGATIVE_INFINITY;
		for(Particle particle : workingSet) {
			double pmds = MathUtil.maxSquaredDiff(particle.position, meanPosition);
			if(pmds > maxDiffSq) {
				maxDiffSq = pmds;
			}
		}
		return Math.sqrt(maxDiffSq) <= this.convergeDistance;
	}
	
	private RealPointValuePair getBestPoint(List<Particle> workingSet) {
		Particle bestParticle = this.getBestParticle(workingSet);
		return bestParticle == null ? null : new RealPointValuePair(bestParticle.position, bestParticle.evaluation);
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
		return sortedParticles.subList(0, n);
	}
	
	private List<Particle> extractBestWithClustering(int n, List<Particle> particles) {
		KMeansClusteringProducer<Particle> clusterEngine = new KMeansClusteringProducer<>(this.random, n, this.maxClusterAlgoSteps);
		VectorialClusteringResults<Particle> clusterResults = clusterEngine.produceClustering(particles);		
		List<Particle> result = new ArrayList<>();
		int missingCount = 0;
		for(VectorialCluster<Particle> cluster : clusterResults.getClusters()) {
			Particle particle = this.getBestParticle(cluster.getItems());
			if(particle != null) {
				result.add(particle);
			} 
			else {
				missingCount++;
			}
		}
		if(missingCount > 0) {
			Set<Particle> resultSet = new HashSet<>(result);
			for(Particle particle : ListUtil.sorted(particles)) {
				if(missingCount == 0) {
					break;
				}
				if(!resultSet.contains(particle)) {
					result.add(particle);
					missingCount--;
				}
			}
		}
		return result;
	}
	 
	private List<Particle> circuitSearch(List<Particle> workingSet, MultivariateRealFunction errorFunction, boolean clusteringPhase) throws MathException {
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

	private Particle lineSearch(Particle particle1, Particle particle2, MultivariateRealFunction errorFunction) throws MathException {
		double error1 = particle1.evaluation;
		double error2 = particle2.evaluation;
		double[] position1 = particle1.getPosition();
		double[] position2 = particle2.getPosition();
		double[] difference = MathUtil.subtract(position2, position1);
		double diffSd = MathUtil.standardDev(difference, 0);
		double moveSd = diffSd * this.displacementFactor;
		position1 = this.movePoint(position1, moveSd);
		position2 = this.movePoint(position2, moveSd);
		difference = MathUtil.subtract(position2, position1);

		Particle candidate = this.newParticleFromBaseline(errorFunction, position1, difference, 0, 1.0);
		double candidateError = candidate.evaluation;
		if(candidateError <= error1 && candidateError <= error2) {
			return candidate;
		}
		double ef = this.expansionFactor;
		if(candidateError <= error1) {
			Particle secondCandidate = this.newParticleFromBaseline(errorFunction, position1, difference, 1.0, 1.0 + ef);
			return secondCandidate.evaluation < candidateError ? secondCandidate : candidate;
		} else if(candidateError <= error2) {
			Particle secondCandidate = this.newParticleFromBaseline(errorFunction, position1, difference, -ef, 0);
			return secondCandidate.evaluation < candidateError ? secondCandidate : candidate;
		}
		else {
			Particle secondCandidate = this.newParticleFromBaseline(errorFunction, position1, difference, -ef, 1.0 + ef);
			return secondCandidate.evaluation < candidateError ? secondCandidate : candidate;			
		}
	}

	private Particle newParticleFromBaseline(MultivariateRealFunction errorFunction, double[] baselinePosition, double[] directionVector, double fromFraction, double toFraction) throws MathException {
		double p = fromFraction + this.random.nextDouble() * (toFraction - fromFraction);
		double[] newPosition = MathUtil.add(baselinePosition, MathUtil.multiply(directionVector, p));
		double newError = errorFunction.value(newPosition);
		return new Particle(newPosition, newError);
	}
	
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
		double[] position = particle.position;
		double distanceToHead = MathUtil.euclideanDistance(position, head.particle.position);
		double distanceToPrior = distanceToHead;
		double minDistanceChange = distanceToHead;
		Node insertAfter = null;
		Node pointer = head;
		Node next;
		while((next = pointer.next) != null) {
			double distancePointerNext = MathUtil.euclideanDistance(pointer.particle.position, next.particle.position);
			double distanceParticleNext =  MathUtil.euclideanDistance(position, next.particle.position);
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

	private List<Particle> createInitialWorkingSet(int n, int vectorLength) {
		Random r = this.random;
		List<Particle> result = new ArrayList<>();		
		for(int i = 0; i < n; i++) {
			double[] position = MathUtil.sampleGaussian(r, 1.0, vectorLength);
			result.add(new Particle(position, Double.NEGATIVE_INFINITY));
		}
		return result;
	}
	
	protected void informProgress(int iteration, RealPointValuePair pointValue) {		
	}
	
	private static class Particle extends ComparableValueHolder<Particle> implements VectorialItem {
		private final double[] position;
		private final double evaluation;
		
		public Particle(double[] position, double evaluation) {
			super();
			this.position = position;
			this.evaluation = evaluation;
		}

		@Override
		public final double[] getPosition() {
			return this.position;
		}

		@Override
		protected final double getValue() {
			return this.evaluation;
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
}
