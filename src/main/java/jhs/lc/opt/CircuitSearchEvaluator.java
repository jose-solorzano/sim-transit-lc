package jhs.lc.opt;

import org.apache.commons.math.FunctionEvaluationException;

public interface CircuitSearchEvaluator {
	public CircuitSearchParamEvaluation evaluate(double[] params) throws FunctionEvaluationException, IllegalArgumentException;
	public double[] recommendEpsilon(double[] params);
}
