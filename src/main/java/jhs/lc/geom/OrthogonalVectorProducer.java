package jhs.lc.geom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jhs.math.regression.SimpleVectorialRegressionItem;
import jhs.math.regression.VectorialRegressionItem;
import jhs.math.regression.linear.LinearRegression;
import jhs.math.util.ListUtil;
import jhs.math.util.MathUtil;

public class OrthogonalVectorProducer {
	private final Random random;
	private final double lambda;
	
	public OrthogonalVectorProducer(Random random, double lambda) {
		super();
		this.random = random;
		this.lambda = lambda;
	}

	public final double[][] produceOrthogonalVectors(int n, int vectorLength) {
		double[] initVector = MathUtil.unitVector(MathUtil.sampleGaussian(this.random, 1.0, vectorLength));
		return this.produceOrthogonalVectors(n, vectorLength, initVector);
	}

	public final double[][] produceOrthogonalVectors(int n, int vectorLength, double[] initVector) {
		if(n == 0) {
			return new double[0][];
		}
		Random r = this.random;
		List<double[]> vectorList = new ArrayList<>();
		vectorList.add(MathUtil.unitVector(initVector));
		for(int i = 1; i < n; i++) {
			double[] baseline = MathUtil.sampleGaussian(r, 1.0, vectorLength);
			double[] vector = this.newVector(baseline, vectorList);
			vectorList.add(vector);
		}
		return ListUtil.asArray(vectorList, double[].class);
	}
	
	private double[] newVector(double[] baseline, List<double[]> vectorList) {
		List<VectorialRegressionItem> vitems = new ArrayList<>();
		for(double[] vector : vectorList) {
			double product = MathUtil.dotProduct(vector, baseline);
			vitems.add(new SimpleVectorialRegressionItem(vector, -product));
		}
		double[] offset = new LinearRegression<>(this.lambda).createModel(vitems, false).getParams();
		return MathUtil.unitVector(MathUtil.add(baseline, offset));
	}
}
