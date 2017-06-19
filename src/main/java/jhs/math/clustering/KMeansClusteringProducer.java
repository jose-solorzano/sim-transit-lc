package jhs.math.clustering;

import java.util.List;
import java.util.Random;

import jhs.math.common.ItemUtil;
import jhs.math.common.VectorialItem;

public class KMeansClusteringProducer<T extends VectorialItem> implements VectorialClusteringProducer<T> {
	private final Random random;
	private final int k;
	private final int maxIterations;

	public KMeansClusteringProducer(Random random, int k, int maxIterations) {
		super();
		this.random = random;
		this.k = k;
		this.maxIterations = maxIterations;
	}

	public KMeansClusteringProducer(Random random, int k) {
		this.random = random;
		this.k = k;
		this.maxIterations = Integer.MAX_VALUE;
	}

	public final Random getRandom() {
		return random;
	}

	public final int getK() {
		return k;
	}

	protected void informProgress(int iteration, double wcss, VectorialCluster<T>[] clusters) {		
	}
	
	public VectorialClusteringResults<T> produceClustering(List<? extends T> items) {
		int k = this.k;
		int numVars = ItemUtil.getNumContinuousVars(items);
		VectorialCluster<T>[] clusters = this.initialClusters(k, items);
		double initWcss = VectorialCluster.calculateWcss(clusters);
		double wcss = initWcss;
		int maxI = Math.min(items.size(), this.maxIterations);
		for(int iteration = 0; iteration < maxI; iteration++) {
			clusters = this.updateClusters(clusters, k, items);
			double prevWcss = wcss;
			wcss = VectorialCluster.calculateWcss(clusters);
			this.informProgress(iteration, wcss, clusters);
			double diff = Math.abs(wcss - prevWcss);
			if(diff <= 1E-21) {
				break;
			}
		}
		return new VectorialClusteringResults<T>(clusters, numVars);
	}	
	
	private VectorialCluster<T>[] initialClusters(int k, List<? extends T> items) {
		Random random = this.random;
		int size = items.size();
		@SuppressWarnings("unchecked")
		VectorialCluster<T>[] clusters = new VectorialCluster[k];
		for(int i = 0; i < k; i++) {
			int index = Math.abs(random.nextInt() % size);
			double[] clusterPosition = items.get(index).getPosition();
			clusters[i] = new VectorialCluster<T>(clusterPosition);
		}
		VectorialCluster.populateClusters(items, clusters);
		return clusters;
	}
	
	private VectorialCluster<T>[] updateClusters(VectorialCluster<T>[] priorClusters, int k, List<? extends T> items) {
		@SuppressWarnings("unchecked")
		VectorialCluster<T>[] clusters = new VectorialCluster[k];
		for(int i = 0; i < k; i++) {
			VectorialCluster<T> priorCluster = priorClusters[i];
			double[] clusterPosition = priorCluster.isEmpty() ? priorCluster.getClusterPosition() : priorCluster.calculateMeanPositionOfMembers();
			clusters[i] = new VectorialCluster<T>(clusterPosition);
		}
		VectorialCluster.populateClusters(items, clusters);
		return clusters;
	}	
}
