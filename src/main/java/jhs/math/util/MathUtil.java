package jhs.math.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import jhs.math.regression.linear.WeightedLinearRegression;

public class MathUtil {
	private MathUtil() {}
	
	public static final int bounded(int value, int min, int max) {
		if(value < min) {
			return min;
		}
		if(value > max) {
			return max;
		}
		return value;
	}
	
	public static final double[] seq(double fromValue, double toValue, int numEntries) {
		double[] sequence = new double[numEntries];
		double range = toValue - fromValue;
		for(int i = 0; i < numEntries; i++) {
			sequence[i] = fromValue + range * i / (numEntries - 1);
		}
		return sequence;
	}
		
	public static final double expAverage(double[] series, double alpha) {
		double ra = Double.NaN;
		for(double value : series) {
			ra = Double.isNaN(ra) ? value : value * alpha + ra * (1 - alpha);
		}
		return ra;
	}
	
	public static final double[] unitVector(double[] vector) {
		double magnitude = vectorMagnitude(vector);
		return magnitude == 0 ? vector : MathUtil.divide(vector, magnitude);
	}
	
	public static final double vectorMagnitude(double[] vector) {
		double sumSq = sumOfSquares(vector);
		return StrictMath.sqrt(sumSq);
	}
	
	public static final double sumOfSquares(double[] vector) {
		int len = vector.length;
		double sum = 0;
		for(int i = 0; i < len; i++) {
			double v = vector[i];
			sum += v * v;
		}
		return sum;
	}

	public static final double sumOfSquares(float[] vector) {
		int len = vector.length;
		double sum = 0;
		for(int i = 0; i < len; i++) {
			double v = vector[i];
			sum += v * v;
		}
		return sum;
	}

	public static double[] gaussianFilter(int filterSize, double sd) {
		double varianceT2 = sd * sd * 2;
		double[] filter = new double[filterSize];
		double midPoint = (filterSize - 1) / 2.0;
		for(int i = 0; i < filterSize; i++) {
			double diff = i - midPoint;
			filter[i] = Math.exp(-((diff * diff)/varianceT2));
		}
		return filter;
	}
	
	public static double[] applyFilter(double[] series, double[] filter, boolean circular) {
		int fl = filter.length;
		int sl = series.length;
		int startOffset = fl / 2;
		double[] newSeries = new double[sl];
		for(int i = 0; i < sl; i++) {
			double sumWeight = 0;
			double sum = 0;
			for(int f = 0; f < fl; f++) {
				int j = i + f - startOffset;
				if(circular) {
					if(j < 0) {
						j = sl + j;
					}
					else if(j >= sl) {
						j = j - sl;
					}
				}
				if(j < 0) {
					j = 0;
				}
				else if(j >= sl) {
					j = sl - 1;
				}
				double weight = filter[f];
				sum += series[j] * weight;
				sumWeight += weight;
			}
			newSeries[i] = sum / sumWeight;
		}
		return newSeries;
	}
	
	public static final void replaceNaNWithZero(double[] array) {
		int len = array.length;
		for(int i = 0; i < len; i++) {
			if(Double.isNaN(array[i])) {
				array[i] = 0;
			}
		}
	}

	public static final void replaceNaNAndInfinityWithZero(double[] array) {
		int len = array.length;
		for(int i = 0; i < len; i++) {
			double v = array[i];
			if(Double.isNaN(v) || Double.isInfinite(v)) {
				array[i] = 0;
			}
		}
	}

	public static final double[] rotate(double[] array, int shift) {
		int len = array.length;
		int lms = len - shift;
		double[] newArray = new double[len];
		for(int i = 0; i < lms; i++) {
			newArray[i] = array[i + shift];
		}
		for(int i = lms; i < len; i++) {
			newArray[i] = array[i - lms];
		}
		return newArray;
	}
	
	public static final double dotProduct(double[] data1, double[] data2) {
		int len = data1.length;
		double sum = 0;
		for(int i = 0; i < len; i++) {
			sum += data1[i] * data2[i];
		}
		return sum;
	}
	
	public static int log2floor(int n)
	{
	    if(n <= 0) {
	    	throw new IllegalArgumentException("bits=" + n);
	    }
	    return 31 - Integer.numberOfLeadingZeros(n);
	}
	
	public static double randomAngle(Random random) {
		return random.nextDouble() * Math.PI * 2;
	}
	
	public static double[] addWhiteNoise(Random random, double[] vector, double noiseSD) {
		int length = vector.length;
		double[] newVector = new double[length];
		for(int i = 0; i < length; i++) {
			newVector[i] = vector[i] + random.nextGaussian() * noiseSD;
		}
		return newVector;
	}
	
	public static float[] negative(float[] array) {
		int length = array.length;
		float[] result = new float[length];
		for(int i = 0; i < length; i++) {
			result[i] = -array[i];
		}
		return result;
	}
	
	public static double[][] covarianceMatrix(double[][] data, double[] varMeans) {
		return covarianceMatrix(data, varMeans, true);
	}

	public static double[][] covarianceMatrix(double[][] data, double[] varMeans, boolean setIdentityDiagonal) {
		int numVars = data.length;
		if(numVars != varMeans.length) {
			throw new IllegalArgumentException("Mismatch in number of vars.");
		}
		double[][] matrix = new double[numVars][numVars];
		for(int i = 0; i < numVars; i++) {
			for(int j = i; j < numVars; j++) {
				double covariance = i == j && !setIdentityDiagonal ? Double.NaN : covariance(data[i], data[j], varMeans[i], varMeans[j]);
				matrix[i][j] = covariance;
				matrix[j][i] = covariance;
			}
		}
		return matrix;
	}

	public static double maxMeanAbsCovariance(double[] varVariances) {
		int numVars = varVariances.length;
		double sum = 0;
		int count = 0;
		for(int i = 0; i < numVars; i++) {
			for(int j = i + 1; j < numVars; j++) {
				double v1 = varVariances[i];
				double v2 = varVariances[j];
				double maxAbsCovariance = Math.sqrt(v1 * v2);
				sum += maxAbsCovariance;
				count++;
			}
		}
		return sum / count;
	}

	public static double meanAbsCovariance(double[][] data, double[] varMeans) {
		int numVars = data.length;
		if(numVars != varMeans.length) {
			throw new IllegalArgumentException("Mismatch in number of vars.");
		}
		double sum = 0;
		int count = 0;
		for(int i = 0; i < numVars; i++) {
			for(int j = i + 1; j < numVars; j++) {
				double covariance = covariance(data[i], data[j], varMeans[i], varMeans[j]);
				sum += Math.abs(covariance);
				count++;
			}
		}
		return sum / count;
	}

	public static double maxRelativeAbsCovariance(double[][] data, double[] varMeans, double[] varVariances) {
		int numVars = data.length;
		if(numVars != varMeans.length) {
			throw new IllegalArgumentException("Mismatch in number of vars.");
		}
		double max = 0;
		for(int i = 0; i < numVars; i++) {
			for(int j = i + 1; j < numVars; j++) {
				double absCov = Math.abs(covariance(data[i], data[j], varMeans[i], varMeans[j]));
				double v1 = varVariances[i];
				double v2 = varVariances[j];
				double maxAbsCov = Math.sqrt(v1 * v2);
				double relAbsCov = absCov / maxAbsCov;
				if(relAbsCov > max) {
					max = relAbsCov;
				}
			}
		}
		return max;
	}

	public static double covariance(double[] varData1, double[] varData2) {
		double mean1 = mean(varData1);
		double mean2 = mean(varData2);
		return covariance(varData1, varData2, mean1, mean2);
	}

	public static double covariance(double[] varData1, double[] varData2, double mean1, double mean2) {
		double sum = 0;
		int length = varData1.length;
		for(int i = 0; i < length; i++) {
			double value1 = varData1[i];
			double value2 = varData2[i];
			sum += (value1 - mean1) * (value2 - mean2);
		}
		return sum / length;
	}

	public static double productMoment(double[] varData1, double[] varData2) {
		double mean1 = MathUtil.mean(varData1);
		double mean2 = MathUtil.mean(varData2);
		return productMoment(varData1, varData2, mean1, mean2);
	}
		
	public static final double productMoment(double[] varData1, double[] varData2, double mean1, double mean2) {
		double covariance = covariance(varData1, varData2, mean1, mean2);
		double variance1 = variance(varData1, mean1);
		double variance2 = variance(varData2, mean2);
		return covariance / Math.sqrt(variance1 * variance2);
	}

	public static double[] varMeans(double[][] data) {
		int numVars = data.length;
		double[] means = new double[numVars];
		for(int v = 0; v < numVars; v++) {
			means[v] = mean(data[v]);
		}
		return means;
	}

	public static double[] varVariances(double[][] data, double[] varMeans) {
		int numVars = varMeans.length;
		double[] variances = new double[numVars];
		for(int v = 0; v < numVars; v++) {
			variances[v] = variance(data[v], varMeans[v]);
		}
		return variances;
	}

	public static double averageIndex(double[] weights) {
		int length = weights.length;
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < length; i++) {
			double weight = weights[i];
			weightSum += weight;			
			sum += weight * i;
		}
		return sum / weightSum;
	}

	public static double indexVariance(double[] weights) {
		double averageIndex = averageIndex(weights);
		return indexVariance(weights, averageIndex);
	}

	public static double indexVariance(double[] weights, double averageIndex) {
		int length = weights.length;
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < length; i++) {
			double weight = weights[i];
			weightSum += weight;			
			double diff = i - averageIndex;
			sum += weight * diff * diff;
		}
		return sum / weightSum;
	}

	public static double[] standardize(double[] values, double mean, double sd) {
		int length = values.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = (values[i] - mean) / sd;
		}
		return result;
	}

	public static double[] standardize(double[] values, double[] means, double[] sd) {
		int length = values.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			double sdi = sd[i];
			result[i] = sdi == 0 ? 0 : (values[i] - means[i]) / sdi;
		}
		return result;
	}

	public static double powPosInt(double a, int b) {
		if(b < 0) {
			throw new IllegalArgumentException("Exponent must be positive.");
		}
		double result = 1.0;
		for(int i = 0; i < b; i++) {
			result *= a;
		}
		return result;
	}
	
	public static boolean sameSign(double a, double b) {
		return !((a >= 0) ^ (b >= 0));
	}
	
	public static Iterable<Integer[]> permutations(Set<Integer> set) {
		final Integer[] base = set.toArray(new Integer[set.size()]);
		final int n = base.length;
		final int[] permutation = new int[n];
		for(int i = 0; i < n; i++) {
			permutation[i] = i;
		}		
		return new Iterable<Integer[]>() {			
			public Iterator<Integer[]> iterator() {
				return new Iterator<Integer[]>() {
					private boolean hn = true;
					
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					public Integer[] next() {
						Integer[] result = new Integer[n];
						for(int i = 0; i < n; i++) { 
							result[i] = base[permutation[i]];
						}
						int k = -1;
						boolean foundK = false;
						for(int i = n - 1; --i >= 0;) {
							if(permutation[i] < permutation[i+1]) {
								k = i;
								foundK = true;
								break;
							}
						}
						if(!foundK) {
							this.hn = false;						
						}
						else {
							int l = -1;
							for(int i = n; --i > k;) {
								if(permutation[k] < permutation[i]) {
									l = i;
									break;
								}
							}
							if(l == -1) {
								throw new IllegalStateException();
							}
							int helper = permutation[k];
							permutation[k] = permutation[l];
							permutation[l] = helper;
							
							for(int ctr = 0; ; ctr++) {
								int i = k + ctr + 1;
								int j = n - ctr - 1;
								if(i >= j) {
									break;
								}
								int helper2 = permutation[i];
								permutation[i] = permutation[j];
								permutation[j] = helper2;
							}
							
						}					
						return result;
					}
					
					public boolean hasNext() {
						return this.hn;
					}
				};
			}
		};		
	}
	
	public static double[] sampleGaussian(Random random, double sd, int length) {
		double[] values = new double[length];
		for(int i = 0; i < length; i++) {
			values[i] = random.nextGaussian() * sd;
		}
		return values;
	}

	public static double[] sampleUniform(Random random, int length) {
		double[] values = new double[length];
		for(int i = 0; i < length; i++) {
			values[i] = random.nextDouble();
		}
		return values;
	}

	public static double[] sampleBinary(Random random, int length) {
		double[] values = new double[length];
		for(int i = 0; i < length; i++) {
			values[i] = random.nextBoolean() ? 1.0 : 0.0;
		}
		return values;
	}

	public static double[] sampleExpSymmetric(Random random, double coeff, int length) {
		RandomExt re = new RandomExt(random);
		double[] values = new double[length];
		for(int i = 0; i < length; i++) {
			values[i] = re.nextExponentialSymmetric(coeff);
		}
		return values;
	}

	public static double sampleExponential(Random random, double meanValue) {
		double coefficient = 1.0 / meanValue;
		double totalArea = meanValue;
		double area = random.nextDouble() * totalArea;
		return -Math.log(1 - coefficient * area) / coefficient;
	}
	
	public static void fillNaNWithMean(double[] values) {
		int length = values.length;
		double sum = 0;
		int countNonNaN = 0;
		for(int i = 0; i < length; i++) {
			double value = values[i];
			if(!Double.isNaN(value)) {
				sum += value;
				countNonNaN++;
			}			
		}
		double mean = countNonNaN == 0 ? 0 : sum / countNonNaN;
		for(int i = 0; i < length; i++) {
			double value = values[i];
			if(Double.isNaN(value)) {
				values[i] = mean;
			}			
		}		
	}
	
	public static double radiansToDegrees(double radians) {
		return radians * 180 / Math.PI;
	}

	public static final double maxSquaredDiff(double[] point1, double[] point2) {
		int length = point1.length;
		double maxDiffSq = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < length; i++) {
			double value1 = point1[i];
			double value2 = point2[i];
			double diff = value1 - value2;
			double diffSq = diff * diff;
			if(diffSq > maxDiffSq) {
				maxDiffSq = diffSq;
			}
		}
		return maxDiffSq;
	}

	public static double euclideanDistance(double[] point1, double[] point2) {
		return Math.sqrt(euclideanDistanceSquared(point1, point2));
	}

	public static double euclideanDistance(float[] point1, float[] point2) {
		return Math.sqrt(euclideanDistanceSquared(point1, point2));
	}

	public static double[] euclideanDistanceSquaredArray(double[][] pointArray, double[] point2) {
		int numPoints = pointArray.length;
		double[] dSq = new double[numPoints];
		for(int p = 0; p < numPoints; p++) {
			dSq[p] = euclideanDistanceSquared(pointArray[p], point2);
		}
		return dSq;
	}

	public static final double euclideanDistanceSquared(double[] point1, double[] point2) {
		return euclideanDistanceSquared(point1, point2, 0);
	}

	public static final double euclideanDistanceSquared(double[] point1, double[] point2Container, int point2Index) {
		int length = point1.length;
		double sumDiff2 = 0;
		for(int i = 0; i < length; i++) {
			double value1 = point1[i];
			double value2 = point2Container[i + point2Index];
			double diff = value1 - value2;
			sumDiff2 += (diff * diff);
		}
		return sumDiff2;
	}

	public static double standardEuclideanDistanceSquared(double[] point1, double[] point2, double[] variances) {
		int length = point1.length;
		double sumDiff2 = 0;
		for(int i = 0; i < length; i++) {
			double value1 = point1[i];
			double value2 = point2[i];
			double variance = variances[i];
			double diff = value1 - value2;
			sumDiff2 += (diff * diff) / variance;
		}
		return sumDiff2;
	}

	public static double euclideanDistanceSquared(float[] point1, float[] point2) {
		int length = point1.length;
		double sumDiff2 = 0;
		for(int i = 0; i < length; i++) {
			double value1 = point1[i];
			double value2 = point2[i];
			double diff = value1 - value2;
			sumDiff2 += (diff * diff);
		}
		return sumDiff2;
	}
	
	public static final double manhattanDistance(double[] point1, double[] point2Container, int point2Index) {
		int length = point1.length;
		double sumAbsDiff = 0;
		for(int i = 0; i < length; i++) {
			double value1 = point1[i];
			double value2 = point2Container[i + point2Index];
			double diff = value1 - value2;
			sumAbsDiff += Math.abs(diff);
		}
		return sumAbsDiff;		
	}

	public static double[] round(double[] numbers, int decimals) {
		double factor = Math.pow(10, decimals);
		int length = numbers.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = Math.floor(numbers[i] * factor + 0.5) / factor;
		}
		return result;
	}

	public static double round(double number, int decimals) {
		double factor = Math.pow(10, decimals);
		return Math.floor(number * factor + 0.5) / factor;
	}

	public static double[] abs(double[] values) {
		int length = values.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = Math.abs(values[i]);
		}
		return result;
	}

	public static double[] log(double[] values) {
		int length = values.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = Math.log(values[i]);
		}
		return result;
	}

	public static double[] log(double[] values, double plusTerm) {
		int length = values.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = Math.log(values[i] + plusTerm);
		}
		return result;
	}

	public static String formatDouble(double number, int significantDigits) {
		double abs = Math.abs(number);
		if(abs == 0.0) {
			return String.valueOf(number);
		}
		else if(abs < 100.0 && abs >= 0.1) {
			int zone = (int) Math.floor(Math.log10(number));
			double multiplier = Math.pow(10, significantDigits - zone - 1); 
			return String.valueOf(Math.round(number * multiplier) / multiplier);
		}
		else {
			char[] chars = new char[significantDigits-1];
			Arrays.fill(chars, '#');
			String decimalChars = new String(chars);
			DecimalFormat df = new DecimalFormat("0." + decimalChars + "E0");
			return df.format(number);
		}
	}
	
	public static String negativeInParenthesis(double number, int significantDigits) {
		if(number < 0) {
			return "(" + formatDouble(number, significantDigits) + ")";
		}
		else {
			return formatDouble(number, significantDigits);
		}
	}
	
	public static double binomialDensity(double p, int n, int k) {
		return binomialCoefficient(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);		
	}

	public static double logBinomialDensity(double p, int n, int k) {
		return logBinomialCoefficient(n, k) + k * Math.log(p) + (n - k) * Math.log(1 - p);
	}

	public static double logBinomialDensityNoCoeff(double p, int n, int k) {
		return k * Math.log(p) + (n - k) * Math.log(1 - p);
	}

	public static double binomialDensityUnbiased(int n, int k) {
		return binomialCoefficient(n, k) * Math.pow(0.5, n);		
	}

	public static double binomialCoefficient(int n, int k) {
		double product = 1;
		int top = Math.min(k, n - k);
		for(int i = 0; i < top; i++) {
			product *= (double) (n - i) / (i + 1);
		}
		return product;
	}

	public static double logBinomialCoefficient(int n, int k) {
		int top = Math.min(k, n - k);
		double sum = 0;
		for(int i = 0; i < top;) {
			double product = 1.0;
			int topJ = Math.min(top, i + 25);
			for(int j = i; j < topJ; j++) {
				product *= (double) (n - j) / (j + 1);
			}
			sum += StrictMath.log(product);
			i = topJ;
		}
		return sum;
	}

	public static final double sum(double[] data) {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		return sum;
	}

	public static final double sum(double[] data, int offset, int length) {
		double sum = 0;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			sum += data[i];
		}
		return sum;
	}

	public static final double sum(float[] data) {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		return sum;
	}

	public static final int sum(int[] data) {
		int sum = 0;
		for(int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		return sum;
	}

	public static final int sum(short[] data) {
		int sum = 0;
		for(int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		return sum;
	}

	public static double mean(double ... data) {
		double sum = 0;
		double prevValue = Double.NaN;
		boolean allEqual = true;
		int length = data.length;
		for(int i = 0; i < length; i++) {
			double value = data[i];			
			sum += value;
			if(allEqual && i > 0 && value != prevValue) {
				allEqual = false;
			}
			prevValue = value;
		}
		// If all are equal, there can be imprecisions introduced
		// by summing all values and dividing by length.
		return allEqual ? data[0] : sum / length;
	}

	public static double weightedMean(double[] data, double[] weights) {
		double sum = 0;
		double weightSum = 0;
		double prevValue = Double.NaN;
		boolean allEqual = true;
		int length = data.length;
		for(int i = 0; i < length; i++) {
			double value = data[i];
			double weight = weights[i];
			sum += value * weight;
			weightSum += weight;
			if(allEqual && i > 0 && value != prevValue) {
				allEqual = false;
			}
			prevValue = value;
		}
		// If all are equal, there can be imprecisions introduced
		// by summing all values and dividing by length.
		return allEqual ? data[0] : sum / weightSum;
	}

	/*
	public static double mean(int ... data) {
		int sum = 0;
		double prevValue = Double.NaN;
		boolean allEqual = true;
		int length = data.length;
		for(int i = 0; i < length; i++) {
			double value = data[i];			
			sum += value;
			if(allEqual && i > 0 && value != prevValue) {
				allEqual = false;
			}
			prevValue = value;
		}
		// If all are equal, there can be imprecisions introduced
		// by summing all values and dividing by length.
		return allEqual ? data[0] : (double) sum / length;
	}
	*/

	public static double mean(double[] data, int offset, int length) {
		double sum = 0;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			sum += data[i];
		}
		return sum / length;
	}

	public static double mean(int[] data, int offset, int length) {
		double sum = 0;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			sum += data[i];
		}
		return sum / length;
	}

	public static double mean(float[] data) {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		return sum / data.length;
	}

	public static double mean(float[] data, int offset, int length) {
		double sum = 0;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			sum += data[i];
		}
		return sum / length;
	}

	public static double geometricMean(double[] data) {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {			
			sum += Math.log(data[i]);
		}
		return Math.exp(sum / data.length);
	}

	public static double variance(double[] data, double mean) {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			double diff = data[i] - mean;
			sum += (diff * diff);
		}
		return sum / data.length;
	}

	public static double variance(float[] data, double mean) {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			double diff = data[i] - mean;
			sum += (diff * diff);
		}
		return sum / data.length;
	}

	public static double variance(double[] data, double mean, int offset, int length) {
		double sum = 0;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			double diff = data[i] - mean;
			sum += (diff * diff);
		}
		return sum / length;
	}

	public static double variance(float[] data, double mean, int offset, int length) {
		double sum = 0;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			double diff = data[i] - mean;
			sum += (diff * diff);
		}
		return sum / length;
	}

	public static double variance(double[] data) {
		double mean = mean(data);
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			double diff = data[i] - mean;
			sum += (diff * diff);
		}
		return sum / data.length;
	}

	public static double variance(float[] data) {
		double mean = mean(data);
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			double diff = data[i] - mean;
			sum += (diff * diff);
		}
		return sum / data.length;
	}

	public static double variance(float[] data, int offset, int length) {
		double mean = mean(data, offset, length);
		double sum = 0;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			double diff = data[i] - mean;
			sum += (diff * diff);
		}
		return sum / length;
	}

	public static double weightedVariance(double[] data, double[] weights) {
		double mean = weightedMean(data, weights);
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < data.length; i++) {
			double diff = data[i] - mean;
			double weight = weights[i];
			sum += (diff * diff) * weight;
			weightSum += weight;
		}
		return sum / weightSum;		
	}

	public static double weightedVariance(double[] data, double mean, double[] weights) {
		double sum = 0;
		double weightSum = 0;
		for(int i = 0; i < data.length; i++) {
			double diff = data[i] - mean;
			double weight = weights[i];
			sum += (diff * diff) * weight;
			weightSum += weight;
		}
		return sum / weightSum;		
	}

	public static double standardDev(double[] data, double mean) {
		double variance = variance(data, mean);
		return Math.sqrt(variance);
	}

	public static double standardDev(float[] data, double mean) {
		double variance = variance(data, mean);
		return Math.sqrt(variance);
	}

	public static double standardDev(double[] data) {
		double variance = variance(data);
		return Math.sqrt(variance);
	}

	public static double standardDev(double[] data, int offset, int length) {
		double mean = mean(data, offset, length);
		double variance = variance(data, mean, offset, length);
		return Math.sqrt(variance);
	}

	public static double standardDev(double[] data, double mean, int offset, int length) {
		double variance = variance(data, mean, offset, length);
		return Math.sqrt(variance);
	}

	public static double standardDev(float[] data, double mean, int offset, int length) {
		double variance = variance(data, mean, offset, length);
		return Math.sqrt(variance);
	}
	
	public static double averageAbsDev(double[] data, double mean) {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			double diff = data[i] - mean;
			sum += Math.abs(diff);
		}
		return sum / data.length;		
	}

	public static int max(int[] data) {
		int max = 0;
		for(int i = 0; i < data.length; i++) {
			if(i == 0) {
				max = data[i];
			}
			else {
				if(data[i] > max) {
					max = data[i];
				}
			}
		}
		return max;		
	}

	public static int maxIndex(int[] data) {
		int maxIndex = -1;
		int max = 0;
		for(int i = 0; i < data.length; i++) {
			if(i == 0) {
				max = data[i];
				maxIndex = i;
			}
			else {
				if(data[i] > max) {
					max = data[i];
					maxIndex = i;
				}
			}
		}
		return maxIndex;		
	}

	public static int maxIndex(double[] data) {
		int maxIndex = -1;
		double max = 0;
		for(int i = 0; i < data.length; i++) {
			if(i == 0) {
				max = data[i];
				maxIndex = i;
			}
			else {
				if(data[i] > max) {
					max = data[i];
					maxIndex = i;
				}
			}
		}
		return maxIndex;		
	}

	public static int minIndex(double[] data) {
		int minIndex = -1;
		double min = 0;
		for(int i = 0; i < data.length; i++) {
			if(i == 0) {
				min = data[i];
				minIndex = i;
			}
			else {
				if(data[i] < min) {
					min = data[i];
					minIndex = i;
				}
			}
		}
		return minIndex;		
	}

	public static int minIndex(int[] data) {
		int minIndex = -1;
		int min = 0;
		for(int i = 0; i < data.length; i++) {
			if(i == 0) {
				min = data[i];
				minIndex = i;
			}
			else {
				if(data[i] < min) {
					min = data[i];
					minIndex = i;
				}
			}
		}
		return minIndex;		
	}

	public static double max(double ... data) {
		double max = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < data.length; i++) {
			if(data[i] > max) {
				max = data[i];
			}
		}
		return max;		
	}

	public static double max(double[] data, int offset, int length) {
		double max = Double.NEGATIVE_INFINITY;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			if(data[i] > max) {
				max = data[i];
			}
		}
		return max;		
	}

	public static double max(float[] data, int offset, int length) {
		double max = Double.NEGATIVE_INFINITY;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			if(data[i] > max) {
				max = data[i];
			}
		}
		return max;		
	}

	public static double min(double ... data) {
		double min = Double.POSITIVE_INFINITY;
		for(int i = 0; i < data.length; i++) {
			if(data[i] < min) {
				min = data[i];
			}
		}
		return min;		
	}

	public static double min(double[] data, int offset, int length) {
		double min = Double.POSITIVE_INFINITY;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			if(data[i] < min) {
				min = data[i];
			}
		}
		return min;		
	}

	public static double min(float[] data, int offset, int length) {
		double min = Double.POSITIVE_INFINITY;
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			if(data[i] < min) {
				min = data[i];
			}
		}
		return min;		
	}

	public static int min(int[] data) {
		int min = 0;
		for(int i = 0; i < data.length; i++) {
			if(i == 0) {
				min = data[i];
			}
			else {
				if(data[i] < min) {
					min = data[i];
				}
			}
		}
		return min;		
	}

	public static long min(long[] data) {
		long min = 0;
		for(int i = 0; i < data.length; i++) {
			if(i == 0) {
				min = data[i];
			}
			else {
				if(data[i] < min) {
					min = data[i];
				}
			}
		}
		return min;		
	}

	public static double[] add(double[] data1, double[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException();
		}
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] + data2[i];
		}
		return result;
	}

	public static short[] add(short[] data1, short[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException();
		}
		short[] result = new short[length];
		for(int i = 0; i < length; i++) {
			result[i] = (short) (data1[i] + data2[i]);
		}
		return result;
	}

	public static int[] add(int[] data1, int[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException();
		}
		int[] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] + data2[i];
		}
		return result;
	}

	public static int[] add(int[] data1, int scalar) {
		int length = data1.length;
		int[] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] + scalar;
		}
		return result;
	}

	public static float[] add(float[] data1, float[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException();
		}
		float[] result = new float[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] + data2[i];
		}
		return result;
	}

	public static double[] add(double[] data1, double value) {
		int length = data1.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] + value;
		}
		return result;
	}

	public static double[] subtract(double[] data1, double[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException("Length1: " + length + ", Length2: " + data2.length);
		}
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] - data2[i];
		}
		return result;
	}

	public static float[] subtract(float[] data1, float[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException();
		}
		float[] result = new float[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] - data2[i];
		}
		return result;
	}

	public static double[] subtract(double[] data1, double number) {
		int length = data1.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] - number;
		}
		return result;
	}

	public static double[] subtractMean(double[] data1) {
		return subtract(data1, MathUtil.mean(data1));
	}

	public static double[] multiply(double[] data1, double factor) {
		int length = data1.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] * factor;
		}
		return result;
	}

	public static float[] multiply(float[] data1, float factor) {
		int length = data1.length;
		float[] result = new float[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] * factor;
		}
		return result;
	}

	public static double[] divide(double[] data1, double factor) {
		int length = data1.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] / factor;
		}
		return result;
	}

	public static double[] divide(float[] data1, double factor) {
		int length = data1.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] / factor;
		}
		return result;
	}

	public static double[] divide(int[] data1, double factor) {
		int length = data1.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] / factor;
		}
		return result;
	}

	public static int[] divideInt(int[] data1, int factor) {
		int length = data1.length;
		int[] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] / factor;
		}
		return result;
	}

	public static double[] multiply(double[] data1, double[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException();
		}
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] * data2[i];
		}
		return result;
	}

	public static double[] divide(double[] data1, double[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException();
		}
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] / data2[i];
		}
		return result;
	}

	public static double[] divide(double[] data1, int[] data2) {
		int length = data1.length;
		if(length != data2.length) {
			throw new IllegalArgumentException();
		}
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = data1[i] / data2[i];
		}
		return result;
	}

	public static void divideInPlace(double[] numbers, double denominator) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] = numbers[i] / denominator;
		}
	}

	public static void sqrtInPlace(double[] numbers) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] = Math.sqrt(numbers[i]);
		}
	}

	public static void divideInPlace(double[] numbers, int[] denominators) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] = numbers[i] / denominators[i];
		}
	}

	public static void divideInPlace(double[] numbers, double[] denominators) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] = numbers[i] / denominators[i];
		}
	}

	public static void divideInPlace(int[] numbers, int denominator) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] = numbers[i] / denominator;
		}
	}

	public static void multiplyInPlace(double[] numbers, double[] timesThese) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] *= timesThese[i];
		}
	}

	public static void multiplyInPlace(double[] numbers, double factor) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] *= factor;
		}
	}

	public static void addInPlace(double[] numbers, double plusNumber) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] += plusNumber;
		}
	}

	public static void addInPlace(double[] numbers, double[] plusThese) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] += plusThese[i];
		}
	}

	public static void addInPlace(double[] numbers, double[] plusThese, double factor) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] += (plusThese[i] * factor);
		}
	}

	public static void addInPlace(float[] numbers, float[] plusThese) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] += plusThese[i];
		}
	}

	public static void addInPlace(float[] numbers, float plusThis) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] += plusThis;
		}
	}

	public static void addInPlace(double[] numbers, int[] plusThese) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] += plusThese[i];
		}
	}

	public static void addInPlace(int[] numbers, int[] plusThese) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] += plusThese[i];
		}
	}

	public static void subtractInPlace(double[] numbers, double[] minusThese) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] -= minusThese[i];
		}
	}

	public static void subtractInPlace(double[] numbers, double minusThis) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] -= minusThis;
		}
	}

	public static void inverseInPlace(double[] numbers) {
		int length = numbers.length;
		for(int i = 0; i < length; i++) {
			numbers[i] = 1 / numbers[i];
		}
	}

	public static final double square(double number) {
		return number*number;
	}

	public static double[] square(double[] numbers) {
		int length = numbers.length;
		double[] results = new double[length];
		for(int i = 0; i < length; i++) {
			double number = numbers[i];
			results[i] = number * number;
		}
		return results;
	}

	public static double[] sqrt(double[] numbers) {
		int length = numbers.length;
		double[] results = new double[length];
		for(int i = 0; i < length; i++) {
			results[i] = StrictMath.sqrt(numbers[i]);
		}
		return results;
	}

	public static double[] exp(double[] numbers) {
		int length = numbers.length;
		double[] results = new double[length];
		for(int i = 0; i < length; i++) {
			results[i] = StrictMath.exp(numbers[i]);
		}
		return results;
	}

	public static double[] pow(double[] numbers, double exp) {
		int length = numbers.length;
		double[] results = new double[length];
		for(int i = 0; i < length; i++) {
			results[i] = StrictMath.pow(numbers[i], exp);
		}
		return results;
	}

	public static boolean contains(double[] data, double value) {
		int length = data.length;
		if(Double.isNaN(value)) {
			for(int i = 0; i < length; i++) {
				if(Double.isNaN(data[i])) {
					return true;
				}
			}			
		}
		else {
			for(int i = 0; i < length; i++) {
				if(data[i] == value) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static double median(double[] values, int from, int to) {
		double[] sortedArray = Arrays.copyOfRange(values, from, to);
		Arrays.sort(sortedArray);
		return sortedArray[sortedArray.length / 2];
	}

	public static double median(double[] values, boolean okToSortInPlace) {
		int length = values.length;
		double[] sortedArray;
		if(okToSortInPlace) {
			sortedArray = values;
		}
		else {
			sortedArray = new double[length];
			System.arraycopy(values, 0, sortedArray, 0, length);
		}
		Arrays.sort(sortedArray);
		return sortedArray[length / 2];
	}

	public static int median(int[] values, boolean okToSortInPlace) {
		int length = values.length;
		int[] sortedArray;
		if(okToSortInPlace) {
			sortedArray = values;
		}
		else {
			sortedArray = new int[length];
			System.arraycopy(values, 0, sortedArray, 0, length);
		}
		Arrays.sort(sortedArray);
		return sortedArray[length / 2];
	}
	
	private static final double LN075 = Math.log(0.75);
	
	public static double adjustedProportion(int predictors, int sampleSize, double biasProbability) {
		if(sampleSize == 0) {
			return biasProbability;
		}
		if(predictors == 0) {
			double rawProb = 1.0 - Math.exp(LN075 / sampleSize);
			if(rawProb == 0) {
				return 0;
			}
			double biasOR = biasProbability / (1 - biasProbability);
			double probOR = rawProb / (1 - rawProb);
			double finalOR = probOR * biasOR;
			return finalOR / (1 + finalOR);			
		}
		else if(predictors == sampleSize) {
			double rawProb = Math.exp(LN075 / sampleSize);
			if(rawProb == 1) {
				return 1;
			}
			double biasOR = biasProbability / (1 - biasProbability);
			double probOR = rawProb / (1 - rawProb);
			double finalOR = probOR * biasOR;
			return finalOR / (1 + finalOR);			
		}
		else {
			return (double) predictors / sampleSize;
		}				
	}
	
	public static double slope(double[] series) {
		WeightedLinearRegression regression = new WeightedLinearRegression();
		int length = series.length;
		for(int x = 0; x < length; x++) {
			double y = series[x];
			if(!Double.isNaN(y)) {
				regression.addData(1.0, x, y);
			}
		}
		return regression.getSlope();
	}

	public static double rSquared(double[] series) {
		return rSquared(series, 0, series.length);
	}

	public static double rSquared(double[] series, int offset, int length) {
		WeightedLinearRegression regression = new WeightedLinearRegression();
		int top = offset + length;
		for(int x = offset; x < top; x++) {
			double y = series[x];
			if(!Double.isNaN(y)) {
				regression.addData(1.0, x, y);
			}
		}
		return regression.getRSquared();
	}
	
	public static double rSquared(double[] x, double[] y) {
		int length = x.length;
		WeightedLinearRegression regression = new WeightedLinearRegression();
		for(int i = 0; i < length; i++) {
			double xi = x[i];
			double yi = y[i];
			regression.addData(1.0, xi, yi);
		}
		return regression.getRSquared();		
	}

	public static double slope(double[] x, double[] y, double intercept) {
		int length = x.length;
		WeightedLinearRegression regression = new WeightedLinearRegression();
		for(int i = 0; i < length; i++) {
			double xi = x[i];
			double yi = y[i];
			regression.addData(1.0, xi, yi);
		}
		return regression.getSlope(intercept);
	}
	
	public static double slope(double[] xArray, double[] yArray, int fromIndex, int toIndex) {
		double sumXY = 0.0;
		double sumX = 0.0;
		double sumY = 0.0;
		double sumX2 = 0.0;
		for(int i = fromIndex; i < toIndex; i++) {
			double x = xArray[i];
			double y = yArray[i];
			sumXY += (x * y);
			sumX += x;
			sumY += y;
			sumX2 += (x * x);
		}
		int n = toIndex - fromIndex;
		return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX); 
	}

	public static double residualVariance(double[] series) {
		WeightedLinearRegression regression = new WeightedLinearRegression();
		int length = series.length;
		for(int x = 0; x < length; x++) {
			double y = series[x];
			if(!Double.isNaN(y)) {
				regression.addData(1.0, x, y);
			}
		}
		return regression.getResidualVariance();
	}

	public static double[] detrend(double[] series) {
		WeightedLinearRegression regression = new WeightedLinearRegression();
		int length = series.length;
		for(int x = 0; x < length; x++) {
			double y = series[x];
			if(!Double.isNaN(y)) {
				regression.addData(1.0, x, y);
			}
		}
		double slope = regression.getSlope();
		double intercept = regression.getIntercept();
		double[] detrended = new double[length];
		for(int x = 0; x < length; x++) {
			double origY = series[x];
			if(Double.isNaN(origY)) {
				detrended[x] = Double.NaN;
			}
			else {
				double lineY = x * slope + intercept;
				detrended[x] = origY - lineY;
			}
		}
		return detrended;
	}

	public static double[] firstDifference(double[] series) {
		int length = series.length - 1;
		double[] fdSeries = new double[length];
		for(int x = 0; x < length; x++) {
			fdSeries[x] = series[x+1] - series[x];
		}
		return fdSeries;
	}
	
	public static double autoCorrelation(double[] series, int offset) {
		WeightedLinearRegression regression = new WeightedLinearRegression();
		int length = series.length;
		int top = offset > 0 ? length - offset : length;
		for(int i = offset > 0 ? 0 : -offset; i < top; i++) {
			double x = series[i];
			double y = series[i+offset];
			regression.addData(1.0, x, y);
		}
		return regression.getSlope(0);
	}
	
	public static double[] averagePoint(double[] point1, double[] point2) {
		int length = point1.length;
		double[] avgPoint = new double[length];
		for(int i = 0; i < length; i++) {
			avgPoint[i] = (point1[i] + point2[i]) / 2;
		}
		return avgPoint;
	}
}
