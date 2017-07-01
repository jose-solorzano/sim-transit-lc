package jhs.math.common;

import java.util.List;

import jhs.math.regression.RegressionItem;

import org.apache.commons.math.MathException;

public interface ModelFactory<T extends RegressionItem> {
	public Model<T> createModel(List<? extends T> items) throws MathException;
}
