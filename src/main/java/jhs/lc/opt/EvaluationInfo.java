package jhs.lc.opt;

public class EvaluationInfo {
	private final double rmse;
	private final double loss;
	private final double fluxLoss;
	private final double trendLoss;
	private final double trendChangeLoss;
	
	public EvaluationInfo(double rmse, double loss, double fluxLoss, double trendLoss, double trendChangeLoss) {
		super();
		this.rmse = rmse;
		this.loss = loss;
		this.fluxLoss = fluxLoss;
		this.trendLoss = trendLoss;
		this.trendChangeLoss = trendChangeLoss;
	}

	public final double getRmse() {
		return rmse;
	}

	public final double getLoss() {
		return loss;
	}

	public final double getFluxLoss() {
		return fluxLoss;
	}

	public final double getTrendChangeLoss() {
		return trendChangeLoss;
	}

	public final double getTrendLoss() {
		return trendLoss;
	}
}
