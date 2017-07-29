package jhs.lc.opt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
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

public class DiversifiedParticleSwarmOptimizer {
	private static final Logger logger = Logger.getLogger(DiversifiedParticleSwarmOptimizer.class.getName());
	private final Random random;	
	private final int populationSize;
	
	private int maxIterations = 200;
	private double convergeDistance = 0.0001;
	
	private double omega = 0.2;
	private double phi = +2.0;
	
	private double weightDecayHalfFraction = 0.13; //TODO testing 0.09491;
 	private double initialVelocitySd = 2.0; //TODO testing 1.0
	
	public DiversifiedParticleSwarmOptimizer(Random random, int populationSize) {
		this.random = random;
		this.populationSize = populationSize;
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

	public final int getMaxIterations() {
		return maxIterations;
	}

	public final void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public final double getOmega() {
		return omega;
	}

	public final void setOmega(double omega) {
		this.omega = omega;
	}

	public RealPointValuePair optimize(int vectorLength, ClusteredEvaluator errorFunction) throws MathException {
		int n = this.populationSize;
		double[] weights = this.createWeights(n);
		List<Particle> workingSet = this.createSwarm(n, vectorLength, errorFunction);
		int maxIterations = this.maxIterations;
		Position bestPosition = null;
		int i;
		for(i = 0; i < maxIterations; i++) {
			Collections.sort(workingSet, (p1, p2) -> Double.compare(p1.bestPosition.evaluation, p2.bestPosition.evaluation));
			this.updateVelocities(workingSet, weights);
			this.updatePositions(workingSet, errorFunction);
			bestPosition = this.getBestPositionInSwarm(workingSet);			
			
			this.informProgress(i, bestPosition.getPointValuePair());
			if(this.convergedSwarm(workingSet)) {
				if(logger.isLoggable(Level.INFO)) {
					logger.info("Converged at iteration " + i);
				}
				break;
			}
		}		
		if(bestPosition == null) {
			throw new IllegalStateException("No iterations.");
		}
		return bestPosition.getPointValuePair();
	}	

	private double[] createWeights(int n) {
		double[] weights = new double[n];
		double halfFraction = this.weightDecayHalfFraction;
		double k = 1.0 / (halfFraction * n);
		for(int i = 0; i < n; i++) {
			weights[i] = Math.exp(-i * k);
		}
		return weights;
	}

	private void updatePositions(List<Particle> particles, ClusteredEvaluator errorFunction) throws FunctionEvaluationException {
		for(Particle particle : particles) {
			double[] newParams = MathUtil.add(particle.currentPosition.parameters, particle.velocity);
			ClusteredParamEvaluation eval = errorFunction.evaluate(newParams);
			double error = eval.getError();
			particle.currentPosition = new Position(newParams, eval.getClusteringPosition(), error);
			if(error < particle.bestPosition.evaluation) {
				particle.bestPosition = particle.currentPosition;
			}
		}
	}
	
	private void updateVelocities(List<Particle> particles, double[] weights) {
		for(Particle particle : particles) {
			this.updateVelocity(particle, particles, weights);
		}
	}
	
	private void updateVelocity(Particle particle, List<Particle> particles, double[] weights) {
		Random r = this.random;
		double omega = this.omega;
		double phi = this.phi;
		Particle suitablePeer = this.pickParticle(particles, weights);
		double[] suitableDirection = MathUtil.subtract(suitablePeer.bestPosition.parameters, particle.currentPosition.parameters);
		double[] oldVelocity = particle.velocity;
		double[] newVelocity = new double[oldVelocity.length];
		for(int d = 0; d < newVelocity.length; d++) {
			double rp = r.nextDouble();
			newVelocity[d] = omega * oldVelocity[d] + phi * rp * suitableDirection[d];
		}
		particle.velocity = newVelocity;
	}
	
	private Particle pickParticle(List<Particle> particles, double[] weights) {
		int index = ArrayUtil.randomIndex(weights, random);
		return particles.get(index);
	}
	
	private boolean convergedSwarm(List<Particle> workingSet) {
		List<Position> positions = ListUtil.map(workingSet, Particle::getBestPosition);
		return this.converged(positions);
	}

	private boolean converged(List<Position> workingSet) {
		double[] meanPosition = ItemUtil.meanPosition(workingSet, Position::getParameters);
		double maxDiffSq = Double.NEGATIVE_INFINITY;
		for(Position particle : workingSet) {
			double pmds = MathUtil.maxSquaredDiff(particle.parameters, meanPosition);
			if(pmds > maxDiffSq) {
				maxDiffSq = pmds;
			}
		}
		return Math.sqrt(maxDiffSq) <= this.convergeDistance;
	}
	
	private Position getBestPositionInSwarm(List<Particle> workingSet) {	
		return this.getBestPosition(ListUtil.map(workingSet, Particle::getBestPosition));
	}

	private Position getBestPosition(List<Position> workingSet) {	
		double minError = Double.POSITIVE_INFINITY;
		Position bestParticle = null;
		for(Position particle : workingSet) {
			if(particle.evaluation < minError) {
				minError = particle.evaluation;
				bestParticle = particle;
			}
		}
		return bestParticle;
	}

	private List<Particle> createSwarm(int n, int vectorLength, ClusteredEvaluator errorFunction) throws MathException {
		Random r = this.random;
		if(logger.isLoggable(Level.INFO)) {
			logger.info("Creating initial pool of " + n + " particles.");
		}
		double ivsd = this.initialVelocitySd;
		List<Particle> pool = new ArrayList<>();		
		for(int i = 0; i < n; i++) {
			double[] params = MathUtil.sampleUniformSymmetric(r, ivsd / 2.0, vectorLength);
			ClusteredParamEvaluation eval = errorFunction.evaluate(params);
			Position position = new Position(params, eval.getClusteringPosition(), eval.getError());
			Particle particle = new Particle();
			particle.bestPosition = position;
			particle.currentPosition = position;
			particle.velocity = MathUtil.sampleUniformSymmetric(r, ivsd, vectorLength);
			pool.add(particle);
		}
		return pool;
	}
	
	protected void informProgress(int iteration, RealPointValuePair pointValue) {		
	}
	
	private static class Particle {
		private Position bestPosition;
		private Position currentPosition;
		private double[] velocity;

		public final Position getBestPosition() {
			return bestPosition;
		}

		public final Position getCurrentPosition() {
			return currentPosition;
		}
	}
	
	private static class Position extends ComparableValueHolder<Position> implements VectorialItem {
		private final double[] parameters;
		private final double[] clusteringPosition;
		private final double evaluation;
		
		public Position(double[] parameters, double[] clusteringPosition, double evaluation) {
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
}
