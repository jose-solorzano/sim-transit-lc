package jhs.lc.opt;

public class ClusteredParamEvaluation {
	private final double error;
	private final double[] clusteringPosition;
	
	public ClusteredParamEvaluation(double error, double[] clusteringPosition) {
		super();
		this.error = error;
		this.clusteringPosition = clusteringPosition;
	}

	public final double getError() {
		return error;
	}

	public final double[] getClusteringPosition() {
		return clusteringPosition;
	}
}
