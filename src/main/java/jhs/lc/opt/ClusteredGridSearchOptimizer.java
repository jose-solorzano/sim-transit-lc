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
import org.apache.commons.math.analysis.MultivariateRealFunction;
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

public class ClusteredGridSearchOptimizer {
	//private static final Logger logger = Logger.getLogger(ClusteredGridSearchOptimizer.class.getName());
	private final Random random;	
	private final int numClusters, numParticlesPerCluster;
	
	private int maxClusterAlgoSteps = 3;
	private int maxIterations = 200;

	private double convergeDistance = 0.0001;
		
	private double startSD = 0.75;
	private double boundsIterativeFactor = 0.993;
	private int maxSubspaceSize = 4;
	
	private int maxAgdIterations = 3;
	
	public ClusteredGridSearchOptimizer(Random random, int numClusters, int numParticlesPerCluster) {
		this.random = random;
		this.numClusters = numClusters;
		this.numParticlesPerCluster = numParticlesPerCluster;
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

	public final int getMaxIterations() {
		return maxIterations;
	}

	public final void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public RealPointValuePair optimize(int vectorLength, ClusteredEvaluator errorFunction) throws MathException {
		int nc = this.numClusters;
		int nppc = this.numParticlesPerCluster;
		int n = nc * nppc;
		int maxI = this.maxIterations;
		double factor = this.boundsIterativeFactor;
		List<Particle> clusterParticles = this.createInitialClusterParticles(nc, vectorLength, errorFunction);
		double boundsSd = this.startSD;
		for(int i = 0; i < maxI; i++) {
			List<Particle> particles = this.populateParticlesAroundClusters(clusterParticles, nppc - 1, boundsSd, vectorLength, errorFunction);
			if(particles.size() != n) {
				throw new IllegalStateException("Expected " + n + " particles, but got " + particles.size() + ".");
			}
			clusterParticles = this.extractBestWithClustering(clusterParticles, particles);
			if(clusterParticles.size() != nc) {
				throw new IllegalStateException();
			}
			RealPointValuePair bestPv = this.getBestPoint(clusterParticles);
			this.informProgress(Phase.CLUSTERING, i, bestPv);
			boundsSd *= factor;
		}		
		return this.selectBestResult(clusterParticles, errorFunction, vectorLength);
	}	
	
	private RealPointValuePair selectBestResult(List<Particle> particles, ClusteredEvaluator errorFunction, int vectorLength) throws FunctionEvaluationException {
		double minError = Double.POSITIVE_INFINITY;
		RealPointValuePair bestPv = null;
		for(Particle particle : particles) {
			RealPointValuePair rpvp = this.advanceWithAgd(particle, errorFunction, vectorLength);
			if(rpvp.getValue() < minError) {
				minError = rpvp.getValue();
				bestPv = rpvp;
			}
		}
		if(bestPv == null) {
			throw new IllegalStateException();
		}
		return bestPv;
	}
	
	private RealPointValuePair advanceWithAgd(Particle particle, ClusteredEvaluator errorFunction, int vectorLength) throws FunctionEvaluationException {
		ApproximateGradientDescentOptimizer optimizer = new ApproximateGradientDescentOptimizer(this.random);
		optimizer.setMaxIterations(this.maxAgdIterations);
		double[] epsilon = ArrayUtil.repeat(0.03, vectorLength);
		MultivariateRealFunction ef = new MultivariateRealFunction() {			
			@Override
			public double value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
				return errorFunction.evaluate(point).getError();
			}
		};
		return optimizer.optimize(ef, particle.getParameters(), epsilon);
	}
	
	private List<Particle> extractBestWithClustering(List<Particle> clusterParticles, List<Particle> allParticles) {
		int numClusters = clusterParticles.size();
		KMeansClusteringProducer<Particle> clusterEngine = new KMeansClusteringProducer<>(this.random, numClusters, this.maxClusterAlgoSteps);
		VectorialClusteringResults<Particle> clusterResults = clusterEngine.produceClustering(allParticles, clusterParticles);
		VectorialCluster<Particle>[] clusters = clusterResults.getClusters();
		List<VectorialCluster<Particle>> clusterList = this.ensureClusterCount(clusters, numClusters);
		if(clusterList.size() != numClusters) {
			throw new IllegalStateException();
		}
		List<Particle> result = new ArrayList<>();
		for(VectorialCluster<Particle> cluster : clusterList) {
			List<Particle> cp = this.extractBest(1, cluster.getItems());
			result.addAll(cp);
		}
		return result;
	}
	
	private List<VectorialCluster<Particle>> ensureClusterCount(VectorialCluster<Particle>[] clusters, int numClusters) {
		List<VectorialCluster<Particle>> result = new ArrayList<>(Arrays.asList(clusters));
		while(result.size() < numClusters) {
			int maxIdx = ListUtil.maxIndex(result, c -> (double) c.size());
			VectorialCluster<Particle> biggestCluster = result.remove(maxIdx);
			List<Particle> items = biggestCluster.getItems();
			int size = items.size();
			if(size < 2) {
				throw new IllegalStateException();
			}
			List<Particle> items1 = items.subList(0, size / 2);
			List<Particle> items2 = items.subList(size / 2, size);
			result.add(VectorialCluster.build(items1));
			result.add(VectorialCluster.build(items2));
		}
		return result;
	}
	
	private List<Particle> populateParticlesAroundClusters(List<Particle> clusterParticles, int numExtraPerCluster, double sd, int vectorLength, ClusteredEvaluator errorFunction) throws FunctionEvaluationException {
		List<Particle> pool = new ArrayList<>();		
		for(Particle clusterParticle : clusterParticles) {
			pool.add(clusterParticle);
			this.populateRandomParticles(pool, clusterParticle, sd, numExtraPerCluster, vectorLength, errorFunction);
		}
		return pool;		
	}
	
	private List<Particle> createInitialClusterParticles(int numClusters, int vectorLength, ClusteredEvaluator errorFunction) throws FunctionEvaluationException {
		Random r = this.random;
		double ssd = this.startSD;
		List<Particle> pool = new ArrayList<>();		
		for(int i = 0; i < numClusters; i++) {
			double[] params = MathUtil.sampleUniformSymmetric(r, ssd, vectorLength);
			ClusteredParamEvaluation eval = errorFunction.evaluate(params);
			pool.add(new Particle(params, eval.getClusteringPosition(), eval.getError()));
		}
		return pool;
	}
	
	private void populateRandomParticles(List<Particle> pool, Particle refParticle, double sd, int n, int vectorLength, ClusteredEvaluator errorFunction) throws FunctionEvaluationException {
		double[] refParams = refParticle.parameters;
		for(int i = 0; i < n; i++) {
			double[] params = this.newRandomParams(refParams, sd, vectorLength);
			ClusteredParamEvaluation eval = errorFunction.evaluate(params);
			pool.add(new Particle(params, eval.getClusteringPosition(), eval.getError()));
		}		
	}
	
	private double[] newRandomParams(double[] refParams, double sd, int vectorLength) {
		Random r = this.random;
		int[] subspace = this.createSubspace(vectorLength);
		double[] newParams = Arrays.copyOf(refParams, vectorLength);
		for(int si = 0; si < subspace.length; si++) {
			newParams[subspace[si]] += r.nextGaussian() * sd;
		}
		return newParams;
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
	
	protected void informProgress(Phase phase, int iteration, RealPointValuePair pointValue) {		
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
		CLUSTERING;
	}
}
