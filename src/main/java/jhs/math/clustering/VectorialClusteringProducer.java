package jhs.math.clustering;

import java.util.List;

import jhs.math.common.VectorialItem;

public interface VectorialClusteringProducer<T extends VectorialItem> {
	public VectorialClusteringResults<T> produceClustering(List<? extends T> items);
}
