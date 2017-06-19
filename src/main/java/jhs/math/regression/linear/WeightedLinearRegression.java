package jhs.math.regression.linear;

import java.util.ArrayList;
import java.util.Iterator;

public class WeightedLinearRegression implements Iterable<UnivariateObservation> {
	private final ArrayList<UnivariateObservation> data = new ArrayList<UnivariateObservation>();

	public void clearData() {
		this.data.clear();
	}
	
	public void addData(double weight, double x, double y) {
		this.data.add(new UnivariateObservation(weight, x, y));
	}
	
	public Iterator<UnivariateObservation> iterator() {
		return this.data.iterator();
	}

	public int size() {
		return this.data.size();
	}
	
	public final double getXVariance() {
		ArrayList<UnivariateObservation> data = this.data;		
		double weightedxsum = 0;
		double weightSum = 0;
		for(UnivariateObservation point : data) {
			double weight = point.getWeight();
			weightedxsum += point.getX() * weight;
			weightSum += weight;
		}
		double meanX = weightedxsum / weightSum;
		double varianceSqSum = 0;
		for(UnivariateObservation point : data) {
			double variance = point.getX() - meanX;
			double weight = point.getWeight();
			varianceSqSum += (variance * variance * weight); 
		}
		return varianceSqSum / weightSum;		
	}
	
	public final double getSlope() {
		double sumXY = 0.0;
		double sumX = 0.0;
		double sumY = 0.0;
		double sumX2 = 0.0;
		ArrayList<UnivariateObservation> data = this.data;
		double weightSum = 0;
		for(UnivariateObservation point : data) {
			double x = point.getX();
			double y = point.getY();
			double weight = point.getWeight();
			sumXY += (x * y * weight);
			sumX += x * weight;
			sumY += y * weight;
			sumX2 += (x * x) * weight;
			weightSum += weight;
		}
		return (weightSum * sumXY - sumX * sumY) / (weightSum * sumX2 - sumX * sumX); 
	}
	
	public final double getIntercept() {
		double sumXY = 0.0;
		double sumX = 0.0;
		double sumY = 0.0;
		double sumX2 = 0.0;
		ArrayList<UnivariateObservation> data = this.data;		
		double weightSum = 0;
		for(UnivariateObservation point : data) {
			double x = point.getX();
			double y = point.getY();
			double weight = point.getWeight();
			sumXY += (x * y * weight);
			sumX += x * weight;
			sumY += y * weight;
			sumX2 += (x * x * weight);
			weightSum += weight;
		}
		return (sumY * sumX2 - sumX * sumXY) / (weightSum * sumX2 - sumX * sumX);
	}
	
	public final double getSlope(double intercept) {
		double sumX2 = 0.0;
		double sumXBMY = 0.0;
		ArrayList<UnivariateObservation> data = this.data;		
		for(UnivariateObservation point : data) {
			double x = point.getX();
			double y = point.getY();
			double weight = point.getWeight();
			sumX2 += (x * x * weight);
			sumXBMY += (x * (intercept - y) * weight);
		}
		return -sumXBMY / sumX2;	
	}

	public final double getRSquared(double intercept) {
		double slope = this.getSlope(intercept);
		return this.getRSquared(slope, intercept);
	}

	public final double getRSquared(double slope, double intercept) {
		ArrayList<UnivariateObservation> data = this.data;		
		double weightedysum = 0;
		double weightSum = 0;
		for(UnivariateObservation point : data) {
			double weight = point.getWeight();
			weightedysum += point.getY() * weight;
			weightSum += weight;
		}
		double meanY = weightedysum / weightSum;
		double residualErrorSqSum = 0;
		double varianceSqSum = 0;
		for(UnivariateObservation point : data) {
			double modelY = point.getX() * slope + intercept;
			double diff = point.getY() - modelY;
			double variance = point.getY() - meanY;
			double weight = point.getWeight();
			residualErrorSqSum += (diff * diff * weight);
			varianceSqSum += (variance * variance * weight); 
		}
		return 1 - residualErrorSqSum / varianceSqSum;
	}

	public final double getResidualVariance() {
		double intercept = this.getIntercept();
		double slope = this.getSlope(intercept);
		return this.getResidualVariance(slope, intercept);		
	}

	public final double getResidualVariance(double intercept) {
		double slope = this.getSlope(intercept);
		return this.getResidualVariance(slope, intercept);		
	}

	public final double getResidualVariance(double slope, double intercept) {
		ArrayList<UnivariateObservation> data = this.data;		
		double residualErrorSqSum = 0;
		double weightSum = 0;
		for(UnivariateObservation point : data) {
			double modelY = point.getX() * slope + intercept;
			double diff = point.getY() - modelY;
			double weight = point.getWeight();
			residualErrorSqSum += (diff * diff * weight);
			weightSum += weight;
		}
		return residualErrorSqSum / weightSum;
	}

	public final double getR(double intercept) {
		double slope = this.getSlope(intercept);
		return this.getR(slope, intercept);
	}
	
	public final double getR(double slope, double intercept) {
		double r2 = this.getRSquared(slope, intercept);
		return Math.signum(slope) * Math.sqrt(r2);
	}

	public final double getR() {
		double intercept = this.getIntercept();
		return this.getR(intercept);
	}
	
	public double getRSquared() {
		double slope = this.getSlope();
		double intercept = this.getIntercept();
		double r = this.getRSquared(slope, intercept);
		return r;
	}
	
	public final double getModelVariance(double intercept) {
		double slope = this.getSlope(intercept);
		double weightSum = 0;
		double sum = 0;
		ArrayList<UnivariateObservation> data = this.data;
		for(UnivariateObservation point : data) {
			double modelY = point.getX() * slope + intercept;
			double diff = point.getY() - modelY;
			double weight = point.getWeight();
			sum += weight * diff * diff;
			weightSum += weight;
		}
		return sum / weightSum;
	}
	
	public final double getStdError(double intercept) {
		double variance = this.getModelVariance(intercept);
		return Math.sqrt(variance);
	}
}
