package jhs.math.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtil {
	public static <T extends Comparable<T>> List<T> sorted(List<T> list) {
		List<T> copy = new ArrayList<>(list);
		Collections.sort(copy);
		return copy;
	}
	
	public static <T> T max(List<T> list, Function<T,Double> evaluator) {
		double maxValue = Double.NEGATIVE_INFINITY;
		T maxItem = null;
		for(T item : list) {
			Double value = evaluator.apply(item);
			if(value != null && value.doubleValue() > maxValue) {
				maxValue = value.doubleValue();
				maxItem = item;
			}
		}
		return maxItem;
	}

	public static <T> T min(List<T> list, Function<T,Double> evaluator) {
		double minValue = Double.POSITIVE_INFINITY;
		T minItem = null;
		for(T item : list) {
			Double value = evaluator.apply(item);
			if(value != null && value.doubleValue() < minValue) {
				minValue = value.doubleValue();
				minItem = item;
			}
		}
		return minItem;
	}

	public static <T,U> List<U> map(List<T> list, Function<T,U> mapper) {
		return list.stream().map(mapper).collect(Collectors.toList());
	}
	
	public static <T> List<T> shuffledList(List<T> list, Random random) {
		return shuffledList(list, random, list.size());
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> shuffledList(List<T> list, Random random, int maxSuffles) {
		Object[] array = list.toArray();
		ArrayUtil.shuffle(array, random, maxSuffles);
		List<T> newList = new ArrayList<T>(array.length);
		for(Object item : array) {
			newList.add((T) item);
		}
		return newList;
	}
	
	public static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
		// Returns serializable list, unlike List.subList().
		List<T> sl = new ArrayList<T>();
		for(int i = fromIndex; i < toIndex; i++) {
			sl.add(list.get(i));
		}
		return sl;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> sample(List<? extends T> list, Random random, int maxSize) {
		if(list.size() <= maxSize) {
			return (List<T>) list;
		}
		List<? extends T> shuffled = shuffledList(list, random, maxSize);
		return (List<T>) subList(shuffled, 0, maxSize);
	}
	
	public static <T> String asString(Collection<T> list, String separator) {
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for(T object : list) {
			if(first) {
				first = false;
			}
			else {
				buffer.append(separator);
			}
			buffer.append(object);
		}
		return buffer.toString();
	}
	
	public static <T> String asCSV(List<T> list) {
		return asString(list, ",");
	}
	
	public static <T> List<T> reverse(List<T> list) {
		int size = list.size();
		List<T> newList = new ArrayList<T>(size);
		for(int i = size; --i >= 0; ) {
			newList.add(list.get(i));
		}
		return newList;
	}
	
	@SuppressWarnings({ "unchecked" })
	public static <T> T[] asArray(Collection<? extends T> items, Class<T> elementType) {
		T[] array = (T[]) Array.newInstance(elementType, items.size());
		return (T[]) items.toArray(array);
	}

	@SuppressWarnings({ "unchecked" })
	public static <T> T[] asArray(Collection<? extends T> items) {
		if(items.isEmpty()) {
			throw new IllegalArgumentException("Method requires non-empty list.");
		}
		T[] array = (T[]) Array.newInstance(items.iterator().next().getClass(), items.size());
		return (T[]) items.toArray(array);
	}

	public static <T> double[] asDoubleArray(Collection<Double> items) {
		Double[] array = items.toArray(new Double[items.size()]);
		return ArrayUtil.unbox(array);
	}

	public static <T> short[] asShortArray(Collection<Short> items) {
		Short[] array = items.toArray(new Short[items.size()]);
		return ArrayUtil.unbox(array);
	}

	public static <T> int[] asIntArray(Collection<Integer> items) {
		Integer[] array = items.toArray(new Integer[items.size()]);
		return ArrayUtil.unbox(array);
	}

	public static <T> List<T> subtract(List<? extends T> origList, Set<T> items) {
		List<T> newList = new ArrayList<T>();
		for(T item : origList) {
			if(!items.contains(item)) {
				newList.add(item);
			}
		}
		return newList;
	}

	public static <T> T randomEntry(List<T> list, Random random) {
		int size = list.size();
		int index = Math.abs(random.nextInt() % size);
		return list.get(index);
	}
	
	public static <T> List<T>[] randomSplit(List<T> list, int parts, Random random) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) list.toArray();
		int length = array.length;
		ArrayUtil.shuffle(array, random);
		@SuppressWarnings("unchecked")
		List<T>[] split = new List[parts];
		int start = 0;
		for(int p = 0; p < parts; p++) {
			int end = length * (p+1) / parts;
			split[p] = ArrayUtil.asList(array, start, end - start);
			start = end;
		}
		return split;
	}
	
	public static <T> List<T> randomSample(List<T> list, int numElements, Random random) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) list.toArray();
		ArrayUtil.shuffle(array, random);
		return ArrayUtil.asList(array, 0, numElements);				
	}

	public static <T> List<T> bootstrapSample(List<T> list, Random random) {
		return bootstrapSample(list, list.size(), random);
	}

	public static <T> List<T> bootstrapSample(List<T> list, int numElements, Random random) {
		int size = list.size();
		List<T> sample = new ArrayList<T>();
		for(int i = 0; i < numElements; i++) {
			int li = Math.abs(random.nextInt() % size);
			sample.add(list.get(li));
		}
		return sample;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> concat(List<? extends T> ... lists) {
		List<T> bigList = new ArrayList<T>();
		for(List<? extends T> list : lists) {
			bigList.addAll(list);
		}
		return bigList;
	}
}
