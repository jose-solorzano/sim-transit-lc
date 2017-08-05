package jhs.lc.tools.inputs;

public class OptResultsSpec {
	private double orbitRadius;
	private double rmse;
	private double optElapsedSeconds;
	private double paramStandardDev;
	private double[] parameters;
	private AbstractOptMethod method;
	private String transitFunctionAsText;

	public final String getTransitFunctionAsText() {
		return transitFunctionAsText;
	}

	public final void setTransitFunctionAsText(String transitFunctionAsText) {
		this.transitFunctionAsText = transitFunctionAsText;
	}

	public final double getParamStandardDev() {
		return paramStandardDev;
	}

	public final void setParamStandardDev(double paramStandardDev) {
		this.paramStandardDev = paramStandardDev;
	}

	public final double getOrbitRadius() {
		return orbitRadius;
	}

	public final void setOrbitRadius(double orbitRadius) {
		this.orbitRadius = orbitRadius;
	}

	public final double getRmse() {
		return rmse;
	}

	public final void setRmse(double rmse) {
		this.rmse = rmse;
	}

	public final double getOptElapsedSeconds() {
		return optElapsedSeconds;
	}

	public final void setOptElapsedSeconds(double optElapsedSeconds) {
		this.optElapsedSeconds = optElapsedSeconds;
	}

	public final double[] getParameters() {
		return parameters;
	}

	public final void setParameters(double[] parameters) {
		this.parameters = parameters;
	}

	public final AbstractOptMethod getMethod() {
		return method;
	}

	public final void setMethod(AbstractOptMethod method) {
		this.method = method;
	}
}
