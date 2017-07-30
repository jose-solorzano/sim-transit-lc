package jhs.math.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.logging.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArrayUtil {
	public static <T> double[] doubleValueVector(List<T> list, Function<T,Double> mapping) {
		int length = list.size();
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = mapping.apply(list.get(i));
		}
		return result;
	}
	
	public static <T> double[] doubleValueVector(T[] array, Function<T,Double> mapping) {
		int length = array.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = mapping.apply(array[i]);
		}
		return result;
	}
	
	public static double[] filter(double[] array, Predicate<Double> predicate) {
		List<Double> resultList = new ArrayList<>();
		for(double value : array) {
			if(predicate.test(value)) {
				resultList.add(value);
			}
		}
		return ListUtil.asDoubleArray(resultList);
	}
	
	public static <T> T[] sample(T[] array, int maxElements, Random random) {
		T[] copy = Arrays.copyOf(array, array.length);
		int me = Math.min(maxElements, array.length);
		shuffle(copy, random, me);
		return Arrays.copyOf(copy, me);
	}
	
	public static <T> void reorder(T[] array, int[] sortIndexes) {
		int length = array.length;
		Object[] newArray = new Object[length];
		for(int si = 0; si < length; si++) {
			newArray[si] = array[sortIndexes[si]];
		}
		System.arraycopy(newArray, 0, array, 0, length);
	}

	public static <T> void reorder(double[] array, int[] sortIndexes) {
		int length = array.length;
		double[] newArray = new double[length];
		for(int si = 0; si < length; si++) {
			newArray[si] = array[sortIndexes[si]];
		}
		System.arraycopy(newArray, 0, array, 0, length);
	}

	public static double[] asDouble(String[] array) {
		int len = array.length;
		double[] result = new double[len];
		for(int i = 0; i < len; i++) {
			try {
				result[i] = Double.parseDouble(array[i]);
			} catch(Exception err) {
				result[i] = Double.NaN;
			}
		}
		return result;
	}

	public static String[] asString(double[] array) {
		int len = array.length;
		String[] result = new String[len];
		for(int i = 0; i < len; i++) {
			result[i] = String.valueOf(array[i]);
		}
		return result;
	}

	public static double[] extract(double[] array, int[] indexes) {
		int len = indexes.length;
		double[] result = new double[len];
		for(int i = 0; i < len; i++) {
			result[i] = array[indexes[i]];
		}
		return result;
	}
	
	public static <T> int indexOf(T[] array, T object) {
		int length = array.length;
		for(int i = 0; i < length; i++) {
			if(Objects.equals(array[i], object)) {
				return i;
			}
		}
		return -1;
	}
	
	public static int[] repeat(int value, int n) {
		int[] array = new int[n];
		Arrays.fill(array, value);
		return array;
	}

	public static double[] repeat(double value, int n) {
		double[] array = new double[n];
		Arrays.fill(array, value);
		return array;
	}

	public static int[] indexes(boolean[] mask, boolean value) {
		List<Integer> indexList = new ArrayList<Integer>();
		int length = mask.length;
		for(int i = 0; i < length; i++) {
			if(mask[i] == value) {
				indexList.add(i);
			}
		}
		return unbox(indexList.toArray(new Integer[indexList.size()]));		
	}
	
	public static boolean contains(boolean[] array, boolean value) {
		for(boolean b : array) {
			if(b == value) {
				return true;
			}
		}
		return false;
	}
	
	public static <T> Map<T,Integer> createIndexMap(T[] objects) {
		Map<T,Integer> map = new HashMap<T, Integer>();
		int length = objects.length;
		for(int i = 0; i < length; i++) {
			T object = objects[i];
			if(!map.containsKey(object)) {
				map.put(object, i);
			}
		}
		return map;
	}
	
	public static boolean contains(int[] array, int value) {
		for(int v : array) {
			if(v == value) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(double[] array, double value) {
		if(Double.isNaN(value)) {
			for(double v : array) {
				if(Double.isNaN(v)) {
					return true;
				}
			}			
		}
		else {
			for(double v : array) {
				if(v == value) {
					return true;
				}
			}
		}
		return false;
	}

	public static double[] sortedArray(double[] values) {
		double[] copy = Arrays.copyOf(values, values.length);
		Arrays.sort(copy);
		return copy;
	}

	public static int[] sortedArray(int[] values) {
		int[] copy = Arrays.copyOf(values, values.length);
		Arrays.sort(copy);
		return copy;
	}

	public static <T> T[] sortedArray(T[] values, Comparator<? super T> comparator) {
		T[] copy = Arrays.copyOf(values, values.length);
		Arrays.sort(copy, comparator);
		return copy;
	}

	public static String toString(double[][] matrix) {
		int length = matrix.length;
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < length; i++) {
			if(i > 0) {
				buffer.append("\r\n");
			}
			buffer.append(Arrays.toString(matrix[i]));
		}
		return buffer.toString();
	}
	
	public static void copyRangeTo(int[] source, int fromIndex, int toIndex, int[] target) {
		for(int i = fromIndex; i < toIndex; i++) {
			target[i - fromIndex] = source[i];
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] remove(T[] array, T item) {
		List<T> list = new ArrayList<T>(array.length);
		for(T ai : array) {
			if(!item.equals(ai)) {
				list.add(ai);
			}
		}
		T[] newArray = (T[]) Array.newInstance(item.getClass(), list.size());
		return (T[]) list.toArray(newArray);
	}
	
	public static double[] remove(double[] array, double value) {
		List<Double> list = new ArrayList<Double>(array.length);
		if(Double.isNaN(value)) {
			for(double v : array) {
				if(!Double.isNaN(v)) {
					list.add(v);
				}
			}			
		}
		else {
			for(double v : array) {
				if(v != value) {
					list.add(v);
				}
			}
		}
		return ListUtil.asDoubleArray(list);
	}

	public static int[] remove(int[] array, int ... indexes) {
		Set<Integer> indexSet = new HashSet<Integer>();
		for(int idx : indexes) {
			indexSet.add(idx);
		}
		List<Integer> list = new ArrayList<Integer>(array.length);
		for(int v : array) {
			if(!indexSet.contains(v)) {
				list.add(v);
			}
		}
		return ListUtil.asIntArray(list);
	}

	public static Set<Double> asSet(double[] array) {
		Set<Double> set = new HashSet<Double>();
		int length = array.length;
		for(int i = 0; i < length; i++) {
			set.add(array[i]);
		}
		return set;
	}

	public static Set<Float> asSet(float[] array) {
		Set<Float> set = new HashSet<Float>();
		int length = array.length;
		for(int i = 0; i < length; i++) {
			set.add(array[i]);
		}
		return set;
	}

	public static Set<Integer> asSet(int ... array) {
		Set<Integer> set = new HashSet<Integer>();
		int length = array.length;
		for(int i = 0; i < length; i++) {
			set.add(array[i]);
		}
		return set;
	}

	public static Set<Integer> asSet(int[] ... arrays) {
		Set<Integer> set = new HashSet<Integer>();
		int length = arrays.length;
		for(int i = 0; i < length; i++) {
			int[] array = arrays[i];
			for(int v : array) {
				set.add(v);
			}
		}
		return set;
	}
	
	
	public static <T> Set<T> asObjectSet(T ... array) {
		Set<T> set = new HashSet<T>();
		int length = array.length;
		for(int i = 0; i < length; i++) {
			set.add(array[i]);
		}
		return set;
	}

	public static double[] shortToDouble(short[] vector) {
		int length = vector.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = (double) vector[i];
		}
		return result;
	}

	public static float[][] doubleToFloat(double[][] matrix) {
		int length = matrix.length;
		float[][] result = new float[length][];
		for(int i = 0; i < length; i++) {
			result[i] = doubleToFloat(matrix[i]);
		}
		return result;
	}

	public static float[] doubleToFloat(double[] vector) {
		int length = vector.length;
		float[] result = new float[length];
		for(int i = 0; i < length; i++) {
			result[i] = (float) vector[i];
		}
		return result;
	}

	public static double[][] floatToDouble(float[][] matrix) {
		int length = matrix.length;
		double[][] result = new double[length][];
		for(int i = 0; i < length; i++) {
			result[i] = floatToDouble(matrix[i]);
		}
		return result;
	}

	public static double[] floatToDouble(float[] vector) {
		int length = vector.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = (double) vector[i];
		}
		return result;
	}

	public static double[] intToDouble(int[] array) {
		double[] result = new double[array.length];
		for(int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}
	
 	public static double[] copyAndSort(double[] values) {
		double[] copy = Arrays.copyOf(values, values.length);
		Arrays.sort(copy);
		return copy;
	}
	
	public static int randomIndex(double[] weights, Random random) {
		double sum = MathUtil.sum(weights);
		double r = random.nextDouble() * sum;
		double rs = 0;
		for(int i = 0; i < weights.length; i++) {
			double nextRs = rs + weights[i];
			if(r >= rs && r < nextRs) {
				return i;
			}
			rs = nextRs;
		}
		for(int i = weights.length; --i >= 0;) {
			if(weights[i] != 0) {
				return i;
			}
		}
		return -1;
	}

	public static int[] randomIndexes(int totalNumIndexes, int resultSize, Random random) {
		return randomIndexes(totalNumIndexes, resultSize, random, false);
	}

	public static int[] randomIndexes(int totalNumIndexes, int resultSize, Random random, boolean withReplacement) {
		if(withReplacement) {
			int[] array = new int[resultSize];
			for(int idx1 = 0; idx1 < resultSize; idx1++) {
				array[idx1] = Math.abs(random.nextInt() % totalNumIndexes);
			}
			return array;			
		}
		else {
			int[] ii = indexIdentity(totalNumIndexes);
			shuffle(ii, random);
			return Arrays.copyOf(ii, resultSize);
		}
	}

	public static int[] randomIndexes(double[] weights, int numIndexes, Random random) {
		if(numIndexes > weights.length) {
			throw new IllegalArgumentException("Too many indexes requested.");
		}
		else {
			//TODO: Could be more efficient.
			int countGZ = 0;
			for(int i = 0; i < weights.length; i++) {
				double weight = weights[i];
				if(Double.isNaN(weight)) {
					throw new IllegalArgumentException("Weight " + i + " is NaN.");					
				}
				if(weight > 0) {
					countGZ++;
				}
			}
			int[] array = new int[numIndexes];
			Set<Integer> alreadyThere = new HashSet<Integer>(numIndexes);
			int count = 0;
			while(count < numIndexes) {
				if(count == countGZ - 1) {
					// Only one non-zero weight left.
					INNER:
					for(int ci = 0; ci < weights.length; ci++) {
						if(!alreadyThere.contains(ci)) {
							array[count++] = ci;
							alreadyThere.add(ci);
							if(count >= numIndexes) {
								break INNER;
							}
						}
					}
				}
				else {
					int ci = randomIndex(weights, random);
					if(ci == -1) {
						throw new IllegalStateException("Unable to find random index for weights " + Arrays.toString(weights) + ".");
					}
					if(!alreadyThere.contains(ci)) {
						array[count++] = ci;
						alreadyThere.add(ci);
					}
				}
			}
			// One left
			return array;
		}
	}

	public static int[] ranks(double[] values) {
		DoubleIndex[] dia = new DoubleIndex[values.length];
		for(int i = 0; i < values.length; i++) {
			dia[i] =  new DoubleIndex(values[i], i);
		}
		Arrays.sort(dia);
		int[] ranks = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			ranks[dia[i].index] = i;
		}
		return ranks;
	}
	
	public static <T> int[] ranks(T[] objects, Function<T,Double> mapper) {
		DoubleIndex[] dia = new DoubleIndex[objects.length];
		for(int i = 0; i < objects.length; i++) {
			dia[i] =  new DoubleIndex(mapper.apply(objects[i]), i);
		}
		Arrays.sort(dia);
		int[] ranks = new int[objects.length];
		for(int i = 0; i < objects.length; i++) {
			ranks[dia[i].index] = i;
		}
		return ranks;		
	}

	public static int[] sortIndexes(double[] values) {
		DoubleIndex[] dia = new DoubleIndex[values.length];
		for(int i = 0; i < values.length; i++) {
			dia[i] =  new DoubleIndex(values[i], i);
		}
		Arrays.sort(dia);
		int[] indexes = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			indexes[i] = dia[i].index;
		}
		return indexes;
	}

	public static int[] sortIndexes(int[] values) {
		DoubleIndex[] dia = new DoubleIndex[values.length];
		for(int i = 0; i < values.length; i++) {
			dia[i] =  new DoubleIndex(values[i], i);
		}
		Arrays.sort(dia);
		int[] indexes = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			indexes[i] = dia[i].index;
		}
		return indexes;
	}

	public static <T extends Comparable<T>> int[] sortIndexes(T[] values) {
		@SuppressWarnings("unchecked")
		ObjectIndex<T>[] dia = new ObjectIndex[values.length];
		for(int i = 0; i < values.length; i++) {
			dia[i] =  new ObjectIndex<T>(values[i], i);
		}
		Arrays.sort(dia);
		int[] indexes = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			indexes[i] = dia[i].index;
		}
		return indexes;
	}

	public static <T extends Comparable<T>> int[] sortIndexes(T[] values, final Comparator<T> comparator) {
		@SuppressWarnings("unchecked")
		ObjectIndex<T>[] dia = new ObjectIndex[values.length];
		for(int i = 0; i < values.length; i++) {
			dia[i] =  new ObjectIndex<T>(values[i], i);
		}
		Arrays.sort(dia, new Comparator<ObjectIndex<T>>() {
			@Override
			public int compare(ObjectIndex<T> o1, ObjectIndex<T> o2) {
				if(o1 == o2) {
					return 0;
				}
				return comparator.compare(o1.value, o2.value);
			}			
		});
		int[] indexes = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			indexes[i] = dia[i].index;
		}
		return indexes;
	}

	public static String[] stringConcat(String[] array, String suffix) {
		String[] newArray = new String[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i] + suffix;
		}
		return newArray;
	}

	public static double[] concat(double[] array, double ... values) {
		double[] result = Arrays.copyOf(array, array.length + values.length);
		System.arraycopy(values, 0, result, array.length, values.length);
		return result;
	}

	public static double[] concat(double[] array1, double[] array2, double ... values) {
		double[] result = Arrays.copyOf(array1, array1.length + array2.length + values.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		System.arraycopy(values, 0, result, array1.length + array2.length, values.length);
		return result;
	}

	public static int[] concat(int[] ... arrays) {
		int numArrays = arrays.length;
		int totalLength = 0;
		for(int i = 0; i < numArrays; i++) {
			int[] array = arrays[i];
			totalLength += array.length;
		}		
		int[] concatArray = new int[totalLength];
		int offset = 0;
		for(int i = 0; i < numArrays; i++) {
			int[] array = arrays[i];
			int alength = array.length;
			System.arraycopy(array, 0, concatArray, offset, alength);
			offset += alength;
		}
		return concatArray;				
	}

	public static short[] concat(short[] ... arrays) {
		int numArrays = arrays.length;
		int totalLength = 0;
		for(int i = 0; i < numArrays; i++) {
			short[] array = arrays[i];
			totalLength += array.length;
		}		
		short[] concatArray = new short[totalLength];
		int offset = 0;
		for(int i = 0; i < numArrays; i++) {
			short[] array = arrays[i];
			int alength = array.length;
			System.arraycopy(array, 0, concatArray, offset, alength);
			offset += alength;
		}
		return concatArray;				
	}

	public static double[] concat(double[] ... arrays) {
		int numArrays = arrays.length;
		int totalLength = 0;
		for(int i = 0; i < numArrays; i++) {
			double[] array = arrays[i];
			totalLength += array.length;
		}		
		double[] concatArray = new double[totalLength];
		int offset = 0;
		for(int i = 0; i < numArrays; i++) {
			double[] array = arrays[i];
			int alength = array.length;
			System.arraycopy(array, 0, concatArray, offset, alength);
			offset += alength;
		}
		return concatArray;				
	}

	public static double[] concat(float[] array1, double[] array2) {
		int length1 = array1.length;
		int totalLength = length1 + array2.length;
		double[] concatArray = new double[totalLength];
		for(int i = 0; i < length1; i++) {
			concatArray[i] = array1[i];
		}
		System.arraycopy(array2, 0, concatArray, length1, array2.length);
		return concatArray;				
	}

	public static float[] concat(float[] array1, float ... extraValues) {
		int length1 = array1.length;
		int totalLength = length1 + extraValues.length;
		float[] concatArray = new float[totalLength];
		for(int i = 0; i < length1; i++) {
			concatArray[i] = array1[i];
		}
		System.arraycopy(extraValues, 0, concatArray, length1, extraValues.length);
		return concatArray;				
	}

	public static <T> T[] concat(T[] ... arrays) {
		int numArrays = arrays.length;
		int totalLength = 0;
		Class<? extends Object> elementType = Object.class;
		for(int i = 0; i < numArrays; i++) {
			T[] array = arrays[i];
			if(array.length > 0) {
				elementType = array[0].getClass();
			}
			totalLength += array.length;
		}		
		@SuppressWarnings("unchecked")
		T[] concatArray = (T[]) Array.newInstance(elementType, totalLength);
		int offset = 0;
		for(int i = 0; i < numArrays; i++) {
			T[] array = arrays[i];
			int alength = array.length;
			System.arraycopy(array, 0, concatArray, offset, alength);
			offset += alength;
		}
		return concatArray;				
	}

	public static <T> T[] concat(T[] array, T ... values) {
		Class<? extends Object> elementType = Object.class;
		if(array.length > 0) {
			elementType = array[0].getClass();
		}		
		else if(values.length > 0) {
			elementType = values[0].getClass();
		}
		int totalLength = array.length + values.length;
		@SuppressWarnings("unchecked")
		T[] concatArray = (T[]) Array.newInstance(elementType, totalLength);
		System.arraycopy(array, 0, concatArray, 0, array.length);
		System.arraycopy(values, 0, concatArray, array.length, values.length);
		return concatArray;				
	}

	public static <T> T[] concat(Class<? super T> elementType, T[] ... arrays) {
		int numArrays = arrays.length;
		int totalLength = 0;
		for(int i = 0; i < numArrays; i++) {
			T[] array = arrays[i];
			totalLength += array.length;
		}		
		@SuppressWarnings("unchecked")
		T[] concatArray = (T[]) Array.newInstance(elementType, totalLength);
		int offset = 0;
		for(int i = 0; i < numArrays; i++) {
			T[] array = arrays[i];
			int alength = array.length;
			System.arraycopy(array, 0, concatArray, offset, alength);
			offset += alength;
		}
		return concatArray;				
	}

	public static <T> T[] reverse(T[] array) {
		int length = array.length;
		Class<? extends Object[]> arrayClass = array.getClass();
		@SuppressWarnings("unchecked")
		T[] newArray = (T[]) Array.newInstance(arrayClass.getComponentType(), length);
		for(int i = 0; i < length; i++) {
			newArray[i] = array[length-i-1];
		}
		return newArray;
	}

	public static int[] reverse(int[] array) {
		int length = array.length;
		int[] newArray = new int[length];
		for(int i = 0; i < length; i++) {
			newArray[i] = array[length-i-1];
		}
		return newArray;
	}

	public static double[] reverse(double[] array) {
		int length = array.length;
		double[] newArray = new double[length];
		for(int i = 0; i < length; i++) {
			newArray[i] = array[length-i-1];
		}
		return newArray;
	}

	public static int[] indexIdentity(int length) {
		int[] array = new int[length];
		for(int i = 0; i < length; i++) {
			array[i] = i;
		}
		return array;
	}

	public static int[] range(int fromIndex, int toIndex) {
		int[] array = new int[toIndex-fromIndex];
		for(int i = fromIndex; i < toIndex; i++) {
			array[i-fromIndex] = i;
		}
		return array;
	}

	public static int[] unbox(Integer[] array) {
		int length = array.length;
		int[] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static long[] unbox(Long[] array) {
		int length = array.length;
		long[] result = new long[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static short[] unbox(Short[] array) {
		int length = array.length;
		short[] result = new short[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static float[] unbox(Float[] array) {
		int length = array.length;
		float[] result = new float[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static double[] convert(Integer[] array) {
		int length = array.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static double[] convert(int[] array) {
		int length = array.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static double[] convert(short[] array) {
		int length = array.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static double[] convert(float[] array) {
		int length = array.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static int[] round(double[] array) {
		int length = array.length;
		int[] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[i] = (int) Math.round(array[i]);
		}
		return result;
	}

	public static double[] unbox(Double[] array) {
		int length = array.length;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}
	
	public static double[] trimNaN(double[] array) {
		int length = array.length;
		int start;
		for(start = 0; start < length; start++) {
			if(!Double.isNaN(array[start])) {
				break;
			}
		}
		int end;
		for(end = length; end > 0; end--) {
			if(!Double.isNaN(array[end-1])) {
				break;
			}
		}
		return Arrays.copyOfRange(array, start, end);
	}
	
	public static float[] trimNaN(float[] array) {
		int length = array.length;
		int start;
		for(start = 0; start < length; start++) {
			if(!Double.isNaN(array[start])) {
				break;
			}
		}
		int end;
		for(end = length; end > 0; end--) {
			if(!Double.isNaN(array[end-1])) {
				break;
			}
		}
		return Arrays.copyOfRange(array, start, end);
	}

	public static short[] trimZero(short[] array) {
		int length = array.length;
		int start;
		for(start = 0; start < length; start++) {
			if(array[start] != 0) {
				break;
			}
		}
		int end;
		for(end = length; end > 0; end--) {
			if(array[end-1] != 0) {
				break;
			}
		}
		return Arrays.copyOfRange(array, start, end);
	}

	public static int countTrue(boolean[] array) {
		int count = 0;
		for(boolean value : array) {
			if(value) {
				count++;
			}
		}
		return count;
	}

	public static boolean allZero(int[] array) {
		for(int value : array) {
			if(value != 0) {
				return false;
			}
		}
		return true;
	}

	public static boolean allZero(double[] array) {
		for(double value : array) {
			if(value != 0) {
				return false;
			}
		}
		return true;
	}

	public static int countZero(int[] array) {
		int count = 0;
		for(int value : array) {
			if(value == 0) {
				count++;
			}
		}
		return count;
	}

	public static int countZero(double[] array) {
		int count = 0;
		for(double value : array) {
			if(value == 0) {
				count++;
			}
		}
		return count;
	}

	public static int intersectionCount(int[] array1, int[] array2, boolean alreadySorted, boolean skipDuplicates) {
		if(!alreadySorted) {
			Arrays.sort(array1);
			Arrays.sort(array2);
		}
		int length1 = array1.length;
		int length2 = array2.length;
		int index1 = 0;
		int index2 = 0;
		int interCount = 0;
		boolean justMatched = false;
		while(index1 < length1 && index2 < length2) {
			int value1 = array1[index1];
			int value2 = array2[index2];
			if(value1 == value2) {
				if(!justMatched || !skipDuplicates) {
					interCount++;
				}
				justMatched = true;
				index1++;
				index2++;
			}
			else {
				justMatched = false;
				if(value1 < value2) {
					index1++;
				}	
				else {
					index2++;
				}
			}
		}
		return interCount;
	}
	
	public static final int distinctCount(int[] sortedArray) {
		int length = sortedArray.length;
		if(length == 0) {
			return 0;
		}
		int prev = sortedArray[0];
		int numDuplicates = 0;
		for(int i = 1; i < length; i++) {
			int value = sortedArray[i];
			if(value == prev) {
				numDuplicates++;
			}
			prev = value;
		}
		return length - numDuplicates;
	}
	
	public static final <T> void shuffle(T[] array, java.util.Random random) {
		int length = array.length;
		for(int index1 = 0; index1 < length; index1++) {
			int index2 = Math.abs(random.nextInt() % length);
			T helper = array[index1];
			array[index1] = array[index2];
			array[index2] = helper;			
		}
	}
	
	public static final <T> void shuffle(T[] array, java.util.Random random, int maxNumShuffles) {
		int length = array.length;
		int top = Math.min(length, maxNumShuffles);
		for(int index1 = 0; index1 < top; index1++) {
			int index2 = Math.abs(random.nextInt() % length);
			T helper = array[index1];
			array[index1] = array[index2];
			array[index2] = helper;
		}		
	}

	public static final void shuffle(int[] array, java.util.Random random) {
		int length = array.length;
		for(int index1 = 0; index1 < length; index1++) {
			int index2 = Math.abs(random.nextInt() % length);
			int helper = array[index1];
			array[index1] = array[index2];
			array[index2] = helper;			
		}
	}

	public static final void shuffle(int[] array, java.util.Random random, int maxNumShuffles) {
		int length = array.length;
		int top = Math.min(length, maxNumShuffles);
		for(int index1 = 0; index1 < top; index1++) {
			int index2 = Math.abs(random.nextInt() % length);
			int helper = array[index1];
			array[index1] = array[index2];
			array[index2] = helper;			
		}
	}

	public static final void shuffle(boolean[] array, java.util.Random random) {
		int length = array.length;
		for(int index1 = 0; index1 < length; index1++) {
			int index2 = Math.abs(random.nextInt() % length);
			boolean helper = array[index1];
			array[index1] = array[index2];
			array[index2] = helper;			
		}
	}

	public static final void shuffle(double[] array, java.util.Random random) {
		int length = array.length;
		for(int index1 = 0; index1 < length; index1++) {
			int index2 = Math.abs(random.nextInt() % length);
			double helper = array[index1];
			array[index1] = array[index2];
			array[index2] = helper;			
		}
	}

	public static final <T> List<T> asList(T[] array, int offset, int length) {
		List<T> list = new ArrayList<T>(array.length);
		int top = offset + length;
		for(int i = offset; i < top; i++) {
			list.add(array[i]);
		}
		return list;
	}
	
	public static final double[] fromDoubleCollection(Collection<Double> list) {
		Double[] boxed = list.toArray(new Double[list.size()]);
		return unbox(boxed);
	}

	public static final int[] fromIntCollection(Collection<Integer> list) {
		Integer[] boxed = list.toArray(new Integer[list.size()]);
		return unbox(boxed);
	}

	public static final long[] fromLongCollection(Collection<Long> list) {
		Long[] boxed = list.toArray(new Long[list.size()]);
		return unbox(boxed);
	}

	public static final float[] fromFloatCollection(Collection<Float> list) {
		Float[] boxed = list.toArray(new Float[list.size()]);
		return unbox(boxed);
	}

	public static final <T> T[] fromCollection(Collection<? extends T> list, Class<? extends T> elementType) {
		@SuppressWarnings("unchecked")
		T[] buffer = (T[]) Array.newInstance(elementType, list.size());
		return list.toArray(buffer);
	}

	private static class DoubleIndex implements Comparable<DoubleIndex> {
		private final double value;
		private final int index;
		
		public DoubleIndex(double value, int index) {
			super();
			this.value = value;
			this.index = index;
		}

		public int compareTo(DoubleIndex o) {
			double thisValue = this.value;
			double otherValue = o.value;
			if(thisValue == otherValue) {
				return 0;
			}
			else if(thisValue > otherValue) {
				return +1;
			}
			else {
				return -1;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(value);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if(!(obj instanceof DoubleIndex)) {
				return false;
			}
			DoubleIndex other = (DoubleIndex) obj;
			return this.value == other.value;
		}
	}
	
	private static class ObjectIndex<T extends Comparable<T>> implements Comparable<ObjectIndex<T>> {
		private final T value;
		private final int index;
		
		public ObjectIndex(T value, int index) {
			super();
			this.value = value;
			this.index = index;
		}

		public int compareTo(ObjectIndex<T> o) {
			T thisValue = this.value;
			T otherValue = o.value;
			if(thisValue == otherValue) {
				return 0;
			}
			else if(thisValue == null) {
				return otherValue == null ? 0 : -1;
			}
			else {
				return thisValue.compareTo(otherValue);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("unchecked")
			ObjectIndex<T> other = (ObjectIndex<T>) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}
}
