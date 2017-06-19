package jhs.math.common;

import org.apache.commons.math.MathException;

public interface Model<T extends Item> {
	public double getModeledResponse(T item) throws MathException;
}
