package jhs.math.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public class MatrixUtil {
	public static double min(double[][] matrix) {
		double min = Double.POSITIVE_INFINITY;
		int length = matrix.length;
		for(int x = 0; x < length; x++) {
			double[] column = matrix[x];
			double colMin = MathUtil.min(column);
			if(colMin < min) {
				min = colMin;
			}
		}
		return min;
	}

	public static double max(double[][] matrix) {
		double max = Double.NEGATIVE_INFINITY;
		int length = matrix.length;
		for(int x = 0; x < length; x++) {
			double[] column = matrix[x];
			double colMax = MathUtil.max(column);
			if(colMax > max) {
				max = colMax;
			}
		}
		return max;
	}

	public static double[] multiplyBySparseVector(double[][] matrixTranspose, int vectorLength, int[] nonZeroIndexes, double[] nonZeroValues) {		
		if(nonZeroIndexes.length != nonZeroValues.length) {
			throw new IllegalArgumentException("nonZeroIndexes and nonZeroValues must have the same length.");
		}
		int numRows = matrixTranspose.length;
		if(numRows == 0) {
			return new double[0];
		}
		int nnz = nonZeroIndexes.length;
		int numColumns = matrixTranspose[0].length;
		if(numColumns != vectorLength) {
			throw new IllegalArgumentException("Expecting a vector of length " + numColumns + " but got " + vectorLength + ".");
		}
		double[] result = new double[numRows];
		for(int r = 0; r < numRows; r++) {
			double[] row = matrixTranspose[r];
			double sum = 0;
			for(int si = 0; si < nnz; si++) {
				sum += nonZeroValues[si] * row[nonZeroIndexes[si]];
			}
			result[r] = sum;
		}
		return result;
	}
	
	public static double[][] sampleGaussian(Random random, double sd, int numCols, int numRows) {
		double[][] matrix = new double[numCols][numRows];
		for(int x = 0; x < numCols; x++) {
			for(int y = 0; y < numRows; y++) {
				matrix[x][y] = random.nextGaussian() * sd;
			}
		}
		return matrix;
	}
	
	public static double[] columnMeans(double[][] matrix) {
		int nc = matrix.length;
		double[] means = new double[nc];
		for(int j = 0; j < nc; j++) {
			means[j] = MathUtil.mean(matrix[j]);
		}
		return means;
	}
	
	public static double[][] squareLog(double[][] matrix1, double a) {
		int numCols = matrix1.length;
		double[][] result = new double[numCols][];
		for(int x = 0; x < numCols; x++) {
			double[] column = matrix1[x];
			int numRows = column.length;
			double[] resultColumn = new double[numRows];
			for(int y = 0; y < numRows; y++) {
				double value = column[y];
				resultColumn[y] = Math.log(value * value + a);
			}
			result[x] = resultColumn;
		}
		return result;
	}
	
	public static double[] meanColumn(double[][] matrix) {
		if(matrix.length == 0) {
			return new double[0];
		}
		int numCols = matrix.length;
		int numRows = matrix[0].length;
		double[] sum = new double[numRows];
		for(int c = 0; c < numCols; c++) {
			MathUtil.addInPlace(sum, matrix[c]);
		}
		MathUtil.divideInPlace(sum, numCols);
		return sum;
	}

	public static double[] standardDevColumn(double[][] matrix, double[] meanColumn) {
		return MathUtil.sqrt(varianceColumn(matrix, meanColumn));
	}

	public static double[] varianceColumn(double[][] matrix, double[] meanColumn) {
		if(matrix.length == 0) {
			return new double[0];
		}
		int numCols = matrix.length;
		int numRows = matrix[0].length;
		double[] sum = new double[numRows];
		for(int c = 0; c < numCols; c++) {
			double[] column = matrix[c];
			for(int r = 0; r < numRows; r++) {
				double diff = column[r] - meanColumn[r];
				sum[r] += (diff * diff);
			}
		}
		MathUtil.divideInPlace(sum, numCols);
		return sum;
	}

	public static double[][] transpose(double[][] matrix) {
		if(matrix.length == 0) {
			return new double[0][0];
		}
		int numCols = matrix.length;
		int numRows = matrix[0].length;
		double[][] result = new double[numRows][numCols];
		for(int r = 0; r < numRows; r++) {
			for(int c = 0; c < numCols; c++) {
				result[r][c] = matrix[c][r];
			}
		}
		return result;
	}

	public static float[][] transpose(float[][] matrix) {
		if(matrix.length == 0) {
			return new float[0][0];
		}
		int numCols = matrix.length;
		int numRows = matrix[0].length;
		float[][] result = new float[numRows][numCols];
		for(int r = 0; r < numRows; r++) {
			for(int c = 0; c < numCols; c++) {
				result[r][c] = matrix[c][r];
			}
		}
		return result;
	}
	
	public static void fill(double[][] matrix, double value) {
		int length = matrix.length;
		for(int x = 0; x < length; x++) {
			double[] column = matrix[x];
			int numRows = column.length;
			for(int y = 0; y < numRows; y++) {
				column[y] = value;
			}
		}		
	}

	public static void fill(float[][] matrix, float value) {
		int length = matrix.length;
		for(int x = 0; x < length; x++) {
			float[] column = matrix[x];
			int numRows = column.length;
			for(int y = 0; y < numRows; y++) {
				column[y] = value;
			}
		}		
	}

	public static void fill(int[][] matrix, int value) {
		int length = matrix.length;
		for(int x = 0; x < length; x++) {
			int[] column = matrix[x];
			int numRows = column.length;
			for(int y = 0; y < numRows; y++) {
				column[y] = value;
			}
		}		
	}

	public static int countTrue(boolean[][] matrix) {
		int count = 0;
		for(boolean[] column : matrix) {
			for(boolean cell : column) {
				if(cell) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Flattening proceeds by getting the values from the first column first, then all from 
	 * the second column, and so on.
	 */
	public static double[] flatten(double[][] matrix, int fromColumn, int toColumn, int fromRow, int toRow) {
		int length = (toRow - fromRow) * (toColumn - fromColumn);
		double[] values = new double[length];
		int index = 0;
		for(int col = fromColumn; col < toColumn; col++) {
			for(int row = fromRow; row < toRow; row++) {
				values[index++] = matrix[col][row];
			}
		}
		return values;
	}

	public static double[] flatten(double[][] matrix) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return new double[0];
		}
		int numRows = matrix[0].length;
		return flatten(matrix, 0, numCols, 0, numRows);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] flatten(T[][] matrix) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return (T[]) new Object[0];
		}
		int numRows = matrix[0].length;
		return flatten(matrix, 0, numCols, 0, numRows);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] flatten(T[][] matrix, int fromColumn, int toColumn, int fromRow, int toRow) {
		int length = (toRow - fromRow) * (toColumn - fromColumn);
		if(length == 0) {
			return (T[]) new Object[0];
		}
		@SuppressWarnings("rawtypes")
		Class elementType = matrix[0][0].getClass();
		T[] values = (T[]) Array.newInstance(elementType, length);
		int index = 0;
		for(int col = fromColumn; col < toColumn; col++) {
			for(int row = fromRow; row < toRow; row++) {
				values[index++] = matrix[col][row];
			}
		}
		return values;
	}

	public static double[][] unflatten(double[] values, int width, int height) {
		int index = 0;
		double[][] matrix = new double[width][height];
		for(int column = 0; column < width; column++) {
			for(int row = 0; row < height; row++) {
				matrix[column][row] = values[index++];
			}
		}
		return matrix;
	}

	public static short[][] unflatten(short[] values, int width, int height) {
		int index = 0;
		short[][] matrix = new short[width][height];
		for(int column = 0; column < width; column++) {
			for(int row = 0; row < height; row++) {
				matrix[column][row] = values[index++];
			}
		}
		return matrix;
	}

	public static int flatIndex(int width, int height, int x, int y) {
		return x * height + y;
	}
	
	public static void populateFlatMatrixRegion(double[] flatMatrix, int width, int height, int fromX, int toX, int fromY, int toY, double[] boxBuffer) {
		int index = 0;
		for(int col = fromX; col < toX; col++) {
			int flatIndexColStart = col * height;
			for(int row = fromY; row < toY; row++) {
				int flatIndex = flatIndexColStart + row;
				boxBuffer[index++] = flatMatrix[flatIndex];
			}
		}		
	}
	
	public static double[][] subMatrix(double[][] matrix, int fromX, int toX, int fromY, int toY) {
		int newWidth = toX - fromX;
		int newHeight = toY - fromY;
		double[][] newMatrix = new double[newWidth][newHeight];
		for(int col = fromX; col < toX; col++) {
			for(int row = fromY; row < toY; row++) {
				newMatrix[col-fromX][row-fromY] = matrix[col][row];
			}
		}		
		return newMatrix;
	}
	
	public static double[][] appendRow(double[][] matrix, double[] row) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return matrix;
		}
		if(row.length != numCols) {
			throw new IllegalArgumentException("New row doesn't have the right length.");
		}
		double[][] newMatrix = new double[numCols][];
		for(int x = 0; x < numCols; x++) {
			newMatrix[x] = ArrayUtil.concat(matrix[x], row[x]);
		}
		return newMatrix;
	}

	public static RealMatrix mean(Collection<RealMatrix> matrixList) {
		double[][] sum = null;
		for(RealMatrix matrix : matrixList) {
			double[][] mdata = matrix.getData();
			if(sum == null) {
				sum = copyOf(mdata);
			}
			else {
				addInPlace(sum, mdata);
			}
		}
		divideInPlace(sum, matrixList.size());
		return new Array2DRowRealMatrix(sum);
	}

	public static RealMatrix variance(Collection<RealMatrix> matrixList, RealMatrix mean) {
		double[][] sum = null;
		for(RealMatrix matrix : matrixList) {
			RealMatrix diff = matrix.subtract(mean);
			RealMatrix diffSq = hadamardSquare(diff);
			if(sum == null) {
				sum = copyOf(diffSq.getData());
			}
			else {
				addInPlace(sum, diffSq.getData());
			}			
		}
		divideInPlace(sum, matrixList.size());
		return new Array2DRowRealMatrix(sum);
	}

	public static RealMatrix standardDev(Collection<RealMatrix> matrixList, RealMatrix mean) {
		RealMatrix variance = variance(matrixList, mean); 
		return hadamardSquareRoot(variance);
	}
	
	public static RealMatrix hadamardSquare(RealMatrix matrix) {
		return new Array2DRowRealMatrix(hadamardSquare(matrix.getData()));
	}

	public static double[][] hadamardSquare(double[][] matrix) {
		int numCols = matrix.length;
		double[][] result = new double[numCols][];
		for(int x = 0; x < numCols; x++) {
			double[] column = matrix[x];
			int numRows = column.length;
			double[] resultColumn = new double[numRows];
			for(int y = 0; y < numRows; y++) {
				double value = column[y];
				resultColumn[y] = value * value;
			}
			result[x] = resultColumn;
		}
		return result;
	}

	public static RealMatrix hadamardSquareRoot(RealMatrix matrix) {
		return new Array2DRowRealMatrix(hadamardSquareRoot(matrix.getData()));
	}

	public static double[][] hadamardSquareRoot(double[][] matrix) {
		int numCols = matrix.length;
		double[][] result = new double[numCols][];
		for(int x = 0; x < numCols; x++) {
			double[] column = matrix[x];
			int numRows = column.length;
			double[] resultColumn = new double[numRows];
			for(int y = 0; y < numRows; y++) {
				double value = column[y];
				resultColumn[y] = StrictMath.sqrt(value);
			}
			result[x] = resultColumn;
		}
		return result;
	}

	public static double[][] hadamardProduct(double[][] matrix1, double[][] matrix2) {
		int numCols = matrix1.length;
		double[][] result = new double[numCols][];
		for(int x = 0; x < numCols; x++) {
			double[] column = matrix1[x];
			int numRows = column.length;
			double[] resultColumn = new double[numRows];
			for(int y = 0; y < numRows; y++) {
				resultColumn[y] = column[y] * matrix2[x][y];
			}
			result[x] = resultColumn;
		}
		return result;
	}

	public static RealMatrix hadamardDivision(RealMatrix matrix1, RealMatrix matrix2) {
		return new Array2DRowRealMatrix(hadamardDivision(matrix1.getData(), matrix2.getData()));
	}

	public static double[][] hadamardDivision(double[][] matrix1, double[][] matrix2) {
		int numCols = matrix1.length;
		double[][] result = new double[numCols][];
		for(int x = 0; x < numCols; x++) {
			double[] column = matrix1[x];
			int numRows = column.length;
			double[] resultColumn = new double[numRows];
			for(int y = 0; y < numRows; y++) {
				resultColumn[y] = column[y] / matrix2[x][y];
			}
			result[x] = resultColumn;
		}
		return result;
	}

	public static double[][] cutColumns(double[][] matrix, int fromRow, int toRow) {
		int numCols = matrix.length;
		double[][] result = new double[numCols][];
		for(int c = 0; c < numCols; c++) {
			result[c] = Arrays.copyOfRange(matrix[c], fromRow, toRow);
		}
		return result;
	}

	public static double[][] region(double[][] matrix, int fromCol, int toCol, int fromRow, int toRow) {
		int resultNumCols = toCol - fromCol;
		double[][] result = new double[resultNumCols][];
		for(int c = fromCol; c < toCol; c++) {
			result[c - fromCol] = Arrays.copyOfRange(matrix[c], fromRow, toRow);
		}
		return result;
	}

	public static double[] row(double[][] matrix, int rowIndex) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return new double[0];
		}
		double[] row = new double[numCols];
		for(int c = 0; c < numCols; c++) {
			row[c] = matrix[c][rowIndex];
		}
		return row;
	}

	public static Complex[] row(Complex[][] matrix, int rowIndex) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return new Complex[0];
		}
		Complex[] row = new Complex[numCols];
		for(int c = 0; c < numCols; c++) {
			row[c] = matrix[c][rowIndex];
		}
		return row;
	}

	public static double[][] gaussianFilter(int filterSize, double variance) {
		if(filterSize % 2 == 0) {
			throw new IllegalArgumentException("filterSize must be odd.");
		}
		double[][] filter = new double[filterSize][filterSize];
		int halfSize = filterSize / 2;
		for(int x = -halfSize; x <= halfSize; x++) {
			for(int y = -halfSize; y <= halfSize; y++) {
				double distanceSq = x*x + y*y;
				filter[x+halfSize][y+halfSize] = Math.exp(-distanceSq / (2*variance));
			}
		}
		return filter;
	}

	public static double[][] applyFilter(double[][] matrix, double[][] filter) {
		int filterSize = filter.length;
		int fromF = -(filterSize / 2);
		int toF = fromF + filterSize;
		int width = matrix.length;
		if(width == 0) {
			return null;
		}
		int height = matrix[0].length;
		double[][] newMatrix = new double[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				double sum = 0;
				double weightSum = 0;
				int currentFromX = Math.max(0, x + fromF);
				int currentFromY = Math.max(0, y + fromF);
				int currentToX = Math.min(width, x + toF);
				int currentToY = Math.min(height, y + toF);
				for(int dx = currentFromX; dx < currentToX; dx++) {
					for(int dy = currentFromY; dy < currentToY; dy++) {
						int fx = dx - x - fromF;
						int fy = dy - y - fromF;
						double w = matrix[dx][dy];						
						double f = filter[fx][fy];
						sum += w * f;
						weightSum += f;
					}
				}
				newMatrix[x][y] = weightSum == 0 ? 0 : (sum / weightSum);
			}
		}
		return newMatrix;
	}

	public static double[][] copyOf(double[][] matrix) {
		int length = matrix.length;
		double[][] result = new double[length][];
		for(int i = 0; i < length; i++) {
			double[] column = matrix[i];
			double[] resultColumn = Arrays.copyOf(column, column.length);
			result[i] = resultColumn;
		}
		return result;
	}

	public static void addInPlace(double[][] matrix, double[][] plusMatrix) {
		int length = matrix.length;
		for(int x = 0; x < length; x++) {
			double[] column = matrix[x];
			int numRows = column.length;
			for(int y = 0; y < numRows; y++) {
				column[y] += plusMatrix[x][y];
			}
		}
	}

	public static void divideInPlace(double[][] matrix, double denominator) {
		int length = matrix.length;
		for(int x = 0; x < length; x++) {
			double[] column = matrix[x];
			int numRows = column.length;
			for(int y = 0; y < numRows; y++) {
				column[y] /= denominator;
			}
		}
	}

	public static boolean contains(RealMatrix matrix, double value) {
		return contains(matrix.getData(), value);
	}

	public static boolean contains(double[][] matrix, double value) {
		if(Double.isNaN(value)) {
			int length = matrix.length;
			for(int x = 0; x < length; x++) {
				double[] column = matrix[x];
				int numRows = column.length;
				for(int y = 0; y < numRows; y++) {
					if(Double.isNaN(column[y])) {
						return true;
					}
				}
			}			
		}
		else {
			int length = matrix.length;
			for(int x = 0; x < length; x++) {
				double[] column = matrix[x];
				int numRows = column.length;
				for(int y = 0; y < numRows; y++) {
					if(column[y] == value) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static double[][] log(double[][] matrix) {
		int length = matrix.length;
		double[][] result = new double[length][];
		for(int i = 0; i < length; i++) {
			result[i] = MathUtil.log(matrix[i]);
		}
		return result;
	}

	public static double mean(double[][] matrix) {
		int length = matrix.length;
		double sum = 0;
		for(int i = 0; i < length; i++) {
			sum += MathUtil.mean(matrix[i]);
		}
		return sum / length;
	}

	public static double variance(double[][] matrix, double mean) {
		int length = matrix.length;
		double sum = 0;
		for(int i = 0; i < length; i++) {
			sum += MathUtil.variance(matrix[i], mean);
		}
		return sum / length;
	}

	public static double standardDev(double[][] matrix, double mean) {
		return Math.sqrt(variance(matrix, mean));
	}
	
	public static double[][] standardize(double[][] matrix) {
		double mean = mean(matrix);
		double sd = standardDev(matrix, mean);
		int length = matrix.length;
		double[][] result = new double[length][];
		for(int i = 0; i < length; i++) {
			result[i] = MathUtil.standardize(matrix[i], mean, sd);
		}
		return result;		
	}

	public static double[][] standardizeByRow(double[][] matrix) {
		if(matrix.length == 0) {
			return new double[0][0];
		}
		int numCols = matrix.length;
		int numRows = matrix[0].length;
		double[] rowMeans = new double[numRows];
		double[] rowStdDevs = new double[numRows];
		for(int y = 0; y < numRows; y++) {
			double[] row = row(matrix, y);
			double mean = MathUtil.mean(row);
			double stdDev = MathUtil.standardDev(row, mean);
			rowMeans[y] = mean;
			rowStdDevs[y] = stdDev;
		}
		double[][] result = new double[numCols][];
		for(int i = 0; i < numCols; i++) {
			double[] column = matrix[i];
			result[i] = MathUtil.standardize(column, rowMeans, rowStdDevs);
		}
		return result;		
	}

	public static float[][] doubleToFloat(double[][] matrix) {
		int length = matrix.length;
		float[][] result = new float[length][];
		for(int r = 0; r < length; r++) {
			result[r] = ArrayUtil.doubleToFloat(matrix[r]);
		}
		return result;
	}
	
	public static double columnMean(double[][] matrix, int columnIndex) {
		double[] column = matrix[columnIndex];
		return MathUtil.mean(column);
	}
	
	public static double columnMean(float[][] matrix, int columnIndex) {
		float[] column = matrix[columnIndex];
		return MathUtil.mean(column);
	}

	public static double columnVariance(double[][] matrix, int columnIndex, double mean) {
		double[] column = matrix[columnIndex];
		return MathUtil.variance(column, mean);
	}


	public static double rowMean(double[][] matrix, int rowIndex) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return Double.NaN;
		}
		double sum = 0;
		for(int c = 0; c < numCols; c++) {
			double[] column = matrix[c];
			sum += column[rowIndex];
		}
		return sum / numCols;
	}

	public static double rowMean(float[][] matrix, int rowIndex) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return Double.NaN;
		}
		double sum = 0;
		for(int c = 0; c < numCols; c++) {
			float[] column = matrix[c];
			sum += column[rowIndex];
		}
		return sum / numCols;
	}
	
	public static double rowVariance(float[][] matrix, int rowIndex, double mean) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return Double.NaN;
		}
		double sum = 0;
		for(int c = 0; c < numCols; c++) {
			float[] column = matrix[c];
			double diff = column[rowIndex] - mean;
			sum += diff * diff;
		}
		return sum / numCols;
	}

	public static double meanRowVariance(double[][] matrix) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return Double.NaN;
		}
		int numRows = matrix[0].length;
		if(numRows == 0) {
			return Double.NaN;
		}
		double sumRowVar = 0;
		for(int r = 0; r < numRows; r++) {
			double sum = 0;
			for(int c = 0; c < numCols; c++) {
				sum += matrix[c][r];
			}
			double mean = sum / numCols;
			sum = 0;
			for(int c = 0; c < numCols; c++) {
				double diff = matrix[c][r] - mean;
				sum += (diff * diff);
			}
			double rowVar = sum / numCols;
			sumRowVar += rowVar;
		}
		return sumRowVar / numRows;
	}

	public static double[] rowVariances(double[][] matrix) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return new double[0];
		}
		int numRows = matrix[0].length;
		if(numRows == 0) {
			return new double[0];
		}
		double[] result = new double[numRows];
		for(int r = 0; r < numRows; r++) {
			double sum = 0;
			for(int c = 0; c < numCols; c++) {
				sum += matrix[c][r];
			}
			double mean = sum / numCols;
			sum = 0;
			for(int c = 0; c < numCols; c++) {
				double diff = matrix[c][r] - mean;
				sum += (diff * diff);
			}
			result[r] = sum / numCols;
		}
		return result;
	}

	public static double rowVariance(double[][] matrix, int rowIndex, double mean) {
		int numCols = matrix.length;
		if(numCols == 0) {
			return Double.NaN;
		}
		double sum = 0;
		for(int c = 0; c < numCols; c++) {
			double[] column = matrix[c];
			double diff = column[rowIndex] - mean;
			sum += diff * diff;
		}
		return sum / numCols;
	}

	public static double[] getDiagonalValues(double[][] transformation2d, int numValues) {
		int matrixWidth = transformation2d.length;
		int count = 0;
		double[] vars = new double[numValues];
		OUTER:
		for(int diag = 1; ; diag++) {
			for(int x = 0; x < diag; x++) {
				if(count >= numValues) {
					break OUTER;
				}
				int y = diag - x - 1;
				if(x >= matrixWidth || y < 0) {
					break OUTER;
				}
				vars[count++] = transformation2d[x][y];
			}
		}
		return vars;		
	}

	public static void copyMatrix(double[][] fromMatrix, double[][] toMatrix) {
		int length = fromMatrix.length;
		for(int c = 0; c < length; c++) {
			double[] column = fromMatrix[c];
			System.arraycopy(column, 0, toMatrix[c], 0, column.length);
		}
	}
}
