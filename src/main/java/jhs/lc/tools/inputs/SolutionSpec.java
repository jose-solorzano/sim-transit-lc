package jhs.lc.tools.inputs;

public class SolutionSpec {
	private double rmse;
	private double[] lossFeatures;
	private double[] parameters;
	private double orbitRadius;
	private String userLabel = "";

	public SolutionSpec(double rmse, double[] lossFeatures, double[] parameters,
			double orbitRadius) {
		super();
		this.rmse = rmse;
		this.lossFeatures = lossFeatures;
		this.parameters = parameters;
		this.orbitRadius = orbitRadius;
	}

	public final double getRmse() {
		return rmse;
	}

	public final void setRmse(double rmse) {
		this.rmse = rmse;
	}

	public final double[] getLossFeatures() {
		return lossFeatures;
	}

	public final void setLossFeatures(double[] lossFeatures) {
		this.lossFeatures = lossFeatures;
	}

	public final double[] getParameters() {
		return parameters;
	}

	public final void setParameters(double[] parameters) {
		this.parameters = parameters;
	}

	public final double getOrbitRadius() {
		return orbitRadius;
	}

	public final void setOrbitRadius(double orbitRadius) {
		this.orbitRadius = orbitRadius;
	}

	public final String getUserLabel() {
		return userLabel;
	}

	public final void setUserLabel(String userLabel) {
		this.userLabel = userLabel;
	}
}
