package jhs.math.common;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import jhs.math.regression.linear.WeightedLinearRegression;
import jhs.math.util.MathUtil;

public class ItemUtil {
	public static double[] subspacePosition(double[] position, int[] subspace) {
		double[] sp = new double[subspace.length];
		for(int f = 0; f < subspace.length; f++) {
			int feature = subspace[f];
			sp[f] = position[feature];
		}
		return sp;
	}
	
	public static <T extends VectorialItem> List<T> sort(List<T> items, final int feature) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) items.toArray(new VectorialItem[items.size()]);
		Arrays.sort(array, new Comparator<T>() {
			public int compare(T o1, T o2) {
				double value1 = o1.getPosition()[feature];
				double value2 = o2.getPosition()[feature];
				if(value1 == value2) {
					return 0;
				}
				else {
					return value1 > value2 ? 1 : -1;
				}
			}			
		});
		return Arrays.asList(array);
	}
	
	public static <T extends VectorialItem> double covariance(List<T> items, int feature1, int feature2) {
		double mean1 = mean(items, feature1);
		double mean2 = mean(items, feature2);
		double sum = 0;
		for(T item : items) {
			double value1 = item.getPosition()[feature1];
			double value2 = item.getPosition()[feature2];
			sum += (value1 - mean1) * (value2 - mean2);
		}
		return sum / items.size();
	}

	public static <T extends VectorialItem> double rSquared(List<T> items, int feature1, int feature2) {
		WeightedLinearRegression regression = new WeightedLinearRegression();
		for(T item : items) {
			double value1 = item.getPosition()[feature1];
			double value2 = item.getPosition()[feature2];
			regression.addData(1.0, value1, value2);
		}
		return regression.getRSquared();
	}

	public static <T extends VectorialItem> double mean(List<T> items, int feature) {
		double sum = 0;
		for(T item : items) {
			sum += item.getPosition()[feature];
		}
		return sum / items.size();
	}

	public static <T extends VectorialItem> double min(List<T> items, int varIndex) {
		double minValue = Double.POSITIVE_INFINITY;
		for(T item : items) {
			double x = item.getPosition()[varIndex];
			if(x < minValue) {
				minValue = x;
			}
		}
		return minValue;
	}

	public static <T extends VectorialItem> double max(List<T> items, int varIndex) {
		double maxValue = Double.NEGATIVE_INFINITY;
		for(T item : items) {
			double x = item.getPosition()[varIndex];
			if(x > maxValue) {
				maxValue = x;
			}
		}
		return maxValue;
	}

	public static <T extends VectorialItem> int getNumContinuousVars(List<T> items) {
		if(items.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		return items.get(0).getPosition().length;
	}

	public static <T extends VectorialItem> List<T>[] splitAtThreshold(List<? extends T> items, int feature, double threshold) {
		if(items.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		@SuppressWarnings("unchecked")
		List<T>[] listTuple = (List<T>[]) new List[2];
		listTuple[0] = new ArrayList<T>();
		listTuple[1] = new ArrayList<T>();
		for(T item : items) {
			double value = item.getPosition()[feature];
			if(value >= threshold) {
				listTuple[1].add(item);				
			}
			else {
				listTuple[0].add(item);
			}
		}
		return listTuple;
	}

	public static <T extends VectorialItem> double[] featureValues(List<? extends T> items, int feature) {
		int size = items.size();
		double[] values = new double[size];
		for(int i = 0; i < size; i++) {
			values[i] = items.get(i).getPosition()[feature];
		}
		return values;
	}

	public static <T> double[] meanPosition(List<T> allItems, Function<T,double[]> function) {
		if(allItems.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		double[] firstPosition = function.apply(allItems.get(0));
		int numVars = firstPosition.length;
		double[] meanArray = new double[numVars];
		for(T item : allItems) {
			double[] position = function.apply(item);
			MathUtil.addInPlace(meanArray, position);
		}
		MathUtil.divideInPlace(meanArray, allItems.size());
		return meanArray;		
	}

	public static double[] meanPosition(List<? extends VectorialItem> allItems) {
		if(allItems.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		int numVars = allItems.get(0).getPosition().length;
		double[] meanArray = new double[numVars];
		for(int feature = 0; feature < numVars; feature++) {
			meanArray[feature] = ItemUtil.mean(allItems, feature);
		}
		return meanArray;			
	}

	public static double[] standardDeviations(List<? extends VectorialItem> allItems) {
		if(allItems.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		int numVars = allItems.get(0).getPosition().length;
		double[] sdArray = new double[numVars];
		for(int feature = 0; feature < numVars; feature++) {
			double sd = ItemUtil.standardDeviation(allItems, feature);
			sdArray[feature] = sd;
		}
		return sdArray;			
	}

	public static double[] standardDeviations(List<? extends VectorialItem> allItems, double[] means) {
		return standardDeviations(allItems, means, 0);
	}

	public static double[] standardDeviations(List<? extends VectorialItem> allItems, double[] means, double valueForZero) {
		if(allItems.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		int numVars = allItems.get(0).getPosition().length;
		double[] sdArray = new double[numVars];
		for(int feature = 0; feature < numVars; feature++) {
			double mean = means[feature];
			double sd = ItemUtil.standardDeviation(allItems, feature, mean);
			sdArray[feature] = sd == 0 ? valueForZero : sd;
		}
		return sdArray;			
	}

	public static double[] standardDeviations(List<? extends VectorialItem> allItems, int[] subspace) {
		if(allItems.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		int length = subspace.length;
		double[] sdArray = new double[length];
		for(int f = 0; f < length; f++) {
			int feature = subspace[f];
			double mean = ItemUtil.mean(allItems, feature);
			double sd = ItemUtil.standardDeviation(allItems, feature, mean);
			sdArray[f] = sd;
		}
		return sdArray;			
	}

	public static double[] variances(List<? extends VectorialItem> allItems) {
		if(allItems.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		int numVars = allItems.get(0).getPosition().length;
		double[] sdArray = new double[numVars];
		for(int feature = 0; feature < numVars; feature++) {
			sdArray[feature] = ItemUtil.variance(allItems, feature);
		}
		return sdArray;			
	}

	public static double[] variances(List<? extends VectorialItem> allItems, double[] meanPosition) {
		if(allItems.isEmpty()) {
			throw new IllegalArgumentException("Empty item list.");
		}
		int numVars = allItems.get(0).getPosition().length;
		double[] sdArray = new double[numVars];
		for(int feature = 0; feature < numVars; feature++) {
			sdArray[feature] = ItemUtil.variance(allItems, feature, meanPosition[feature]);
		}
		return sdArray;			
	}

	public static double standardDeviation(List<? extends VectorialItem> allItems, int feature) {
		int size = allItems.size();
		double[] values = new double[size];
		for(int i = 0; i < size; i++) {
			values[i] = allItems.get(i).getPosition()[feature];
		}
		double sd = MathUtil.standardDev(values);
		if(Double.isNaN(sd)) {
			double mean = MathUtil.mean(values);
			double variance = MathUtil.variance(values, mean);
			throw new IllegalStateException("NaN standard deviation for array with mean " + mean + ", variance " + variance + ": " + Arrays.toString(values) + ".");
		}
		return sd;		
	}

	public static double standardDeviation(List<? extends VectorialItem> allItems, int feature, double mean) {
		int size = allItems.size();
		double[] values = new double[size];
		for(int i = 0; i < size; i++) {
			values[i] = allItems.get(i).getPosition()[feature];
		}
		double sd = MathUtil.standardDev(values, mean);
		if(Double.isNaN(sd)) {
			double variance = MathUtil.variance(values, mean);
			throw new IllegalStateException("NaN standard deviation for array with mean " + mean + ", variance " + variance + ": " + Arrays.toString(values) + ".");
		}
		return sd;
	}

	public static double variance(List<? extends VectorialItem> allItems, int feature) {
		int size = allItems.size();
		double[] values = new double[size];
		for(int i = 0; i < size; i++) {
			values[i] = allItems.get(i).getPosition()[feature];
		}
		return MathUtil.variance(values);
	}

	public static double variance(List<? extends VectorialItem> allItems, int feature, double mean) {
		int size = allItems.size();
		double[] values = new double[size];
		for(int i = 0; i < size; i++) {
			values[i] = allItems.get(i).getPosition()[feature];
		}
		return MathUtil.variance(values, mean);
	}
}
