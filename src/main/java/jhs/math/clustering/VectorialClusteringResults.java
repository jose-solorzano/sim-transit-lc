package jhs.math.clustering;

import jhs.math.common.VectorialItem;

public class VectorialClusteringResults<T extends VectorialItem> {
	private final VectorialCluster<T>[] clusters;
	private final int numVars;

	public VectorialClusteringResults(VectorialCluster<T>[] clusters, int numVars) {
		this.clusters = clusters;
		this.numVars = numVars;
	}

	public final int getNumVars() {
		return numVars;
	}

	public final VectorialCluster<T>[] getClusters() {
		return clusters;
	} 
}
