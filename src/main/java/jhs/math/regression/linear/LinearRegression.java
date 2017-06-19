package jhs.math.regression.linear;

import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;

import jhs.math.common.ModelFactory;
import jhs.math.regression.VectorialRegressionItem;

public class LinearRegression<T extends VectorialRegressionItem> implements ModelFactory<T> {	
	private final double lambda;
	
	public LinearRegression(double lambda) {
		super();
		this.lambda = lambda;
	}

	public LinearRegression() {
		this.lambda = 0;
	}

	public final LinearParametricModel<T> createModel(List<? extends T> items) {
		return this.createModel(items, true);
	}

	public final LinearParametricModel<T> createModel(List<? extends T> items, boolean hasIntercept) {
		if(items.size() == 0) {
			throw new IllegalStateException("Item collection is empty!");
		}
		double[] params = this.produceParameters(items, hasIntercept);
		return new LinearParametricModel<T>(params, hasIntercept);		
	}

	public final double[] produceParameters(List<? extends T> items, boolean hasIntercept) {
		int size = items.size();
		double[] y = new double[size];
		double[][] x = new double[size][];
		for(int i = 0; i < size; i++) {
			VectorialRegressionItem item = items.get(i);
			y[i] = item.getResponse();
			if(hasIntercept) {
				double[] position = item.getPosition();
				double[] vector = new double[position.length+1];
				System.arraycopy(position, 0, vector, 0, position.length);
				vector[position.length] = 1.0;
				x[i] = vector;
			}
			else {
				x[i] = item.getPosition();
			}
		}
		return this.produceParameters(y, x, hasIntercept, this.lambda);
	}
	
	private final double[] produceParameters(double[] response, double[][] data, boolean containsIntercept, double lambda) {
		RealMatrix x = new Array2DRowRealMatrix(data);
		RealMatrix xT = x.transpose();
		RealMatrix product = xT.multiply(x);
		
		int pcd = product.getColumnDimension();
		RealMatrix identity = MatrixUtils.createRealIdentityMatrix(pcd);
		if(containsIntercept) {
			identity.setEntry(pcd - 1, pcd - 1, 0);
		}
		
		RealMatrix sum = product.add(identity.scalarMultiply(lambda));
		
		RealMatrix inverse = new LUDecompositionImpl(sum).getSolver().getInverse();
		RealMatrix partial = inverse.multiply(xT);
		RealMatrix resultMatrix = partial.multiply(new Array2DRowRealMatrix(response));
		return resultMatrix.getColumnVector(0).getData();		
	}

	/*
	private static final double[] equationResults(double[] response, double[][] data, int numVars) {
		double[] eqResults = new double[numVars];
		for(int i = 0; i < numVars; i++) {
			double sum = responseProductSum(response, data, i);
			eqResults[i] = sum;
		}
		return eqResults;
	}
	*/

	/*
	private static final double responseProductSum(double[] response, double[][] data, int i) {
		int numDP = data.length;
		double sum = 0;
		for(int row = 0; row < numDP; row++) {
			sum += response[row] * data[row][i];
		}
		return sum;
	}
	*/

	/*
	private static final double[][] productMatrix(double[][] data, int numVars) {
		double[][] matrix = new double[numVars][numVars];
		for(int i = 0; i < numVars; i++) {
			for(int j = i; j < numVars; j++) {
				double sum = productSum(data, i, j);
				matrix[i][j] = sum;
				matrix[j][i] = sum;
			}
		}
		return matrix;
	}
	*/

	/*
	private static final double productSum(double[][] data, int i, int j) {
		double sum = 0;
		for(double[] row : data) {
			sum += row[i] * row[j];
		}
		return sum;
	}
	*/
}
