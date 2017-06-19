package jhs.math.common;

import java.util.List;

import org.apache.commons.math.MathException;

import jhs.math.regression.RegressionItem;

public interface ModelFactory<T extends RegressionItem> {
	public Model<T> createModel(List<? extends T> items) throws MathException;
}
