package jhs.lc.sims;

public class SimulatedFlux {
	private final double[] fluxArray;
	private final double[] clusteringPosition;
	
	public SimulatedFlux(double[] fluxArray, double[] clusteringPosition) {
		super();
		this.fluxArray = fluxArray;
		this.clusteringPosition = clusteringPosition;
	}

	public double[] getFluxArray() {
		return fluxArray;
	}

	public double[] getClusteringPosition() {
		return clusteringPosition;
	}
}
