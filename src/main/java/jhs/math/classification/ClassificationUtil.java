package jhs.math.classification;

import jhs.math.util.MathUtil;

public class ClassificationUtil {
	public static final double probabilityToLogit(double probability) {
		if(probability >= 1.0) {
			return Double.POSITIVE_INFINITY;
		}
		else if(probability <= 0.0) {
			return Double.NEGATIVE_INFINITY;
		}
		else {
			return Math.log(probability / (1-probability));
		}
	}
	
	public static final double[] probabilityToLogit(double[] probability) {
		double[] result = new double[probability.length];
		for(int i = 0; i < probability.length; i++) {
			result[i] = probabilityToLogit(probability[i]);
		}
		return result;
	}
	
	public static final double logitToProbability(double logit) {
		if(Double.POSITIVE_INFINITY == logit) {
			return 1.0;
		}
		else if(Double.NEGATIVE_INFINITY == logit) {
			return 0.0;
		}
		else {
			return 1.0 / (1.0 + Math.exp(-logit));
		}
	}

	public static final double[] logitToProbability(double[] logits) {
		double[] result = new double[logits.length];
		for(int i = 0; i < logits.length; i++) {
			result[i] = logitToProbability(logits[i]);
		}
		return result;		
	}

	public static double[] softmaxProbabilities(double[] multinomialLogits) {
		int len = multinomialLogits.length;
		double[] calcBuffer = new double[len];
		double expSum = 0;
		for(int i = 0; i < len; i++) {
			double e = StrictMath.exp(multinomialLogits[i]);
			calcBuffer[i] = e;
			expSum += e;
		}
		if(expSum != 0) {
			if(Double.isInfinite(expSum)) {
				int idxOfMax = MathUtil.maxIndex(calcBuffer);
				for(int i = 0; i < len; i++) {
					calcBuffer[i] = i == idxOfMax ? 1.0 : 0.0;
				}
			}
			else {
				for(int i = 0; i < len; i++) {
					calcBuffer[i] /= expSum;
				}
			}
		}
		return calcBuffer;				
	}	

	public static double[] normalSoftmaxLogits(double[] probabilities) {
		return MathUtil.log(probabilities);
	}

	public static final double[] logitToProbability(double[] logits, boolean softmax) {
		return softmax ? softmaxProbabilities(logits) : logitToProbability(logits);
	}
	
	public static final double[] probabilityToLogit(double[] probability, boolean softmax) {
		return softmax ? normalSoftmaxLogits(probability) : probabilityToLogit(probability);
	}
	
	public static final double logPFromLogit(double logit) {
		//TODO: Could be more efficient by checking value of logit.
		double denom = StrictMath.log(1.0 + StrictMath.exp(-logit));
		return Double.isInfinite(denom) ? logit : -denom;
	}

	public static final double log1MinusPFromLogit(double logit) {
		//TODO: Could be more efficient by checking value of logit.
		double denom = StrictMath.log(1.0 + StrictMath.exp(-logit));
		return -logit + (Double.isInfinite(denom) ? logit : -denom);
	}	

	public static final double[] logPFromLogit(double[] logit) {
		int len = logit.length;
		double[] result = new double[len];
		for(int i = 0; i < len; i++) {
			result[i] = logPFromLogit(logit[i]);
		}
		return result;
	}
	
	public static final double logitFromLogP(double logP) {
		if(logP < -40) {
			return logP;
		}
		double p = Math.exp(logP);
		return logP - Math.log(1 - p);
	}	
}
