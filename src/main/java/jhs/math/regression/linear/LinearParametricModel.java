package jhs.math.regression.linear;

import java.util.Arrays;

import jhs.math.common.Model;
import jhs.math.common.VectorialItem;

public class LinearParametricModel<T extends VectorialItem> implements Model<T>, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final double[] params;
	private final boolean hasIntercept;

	public LinearParametricModel(double[] params, boolean hasIntercept) {
		super();
		this.params = params;
		this.hasIntercept = hasIntercept;
	}

	public final double[] getParams() {
		return params;
	}

	public final boolean hasIntercept() {
		return hasIntercept;
	}

	public final double getModeledResponse(T item) {
		double[] position = item.getPosition();
		int poslen = position.length;
		boolean hi = this.hasIntercept;
		double[] parameters = this.params;
		double sum = hi ? parameters[poslen] : 0;
		for(int i = 0; i < poslen; i++) {
			sum += parameters[i] * position[i];
		}
		return sum;
	}
	
	public static final double productSum(double[] varData, double[] parameters, boolean hasIntercept) {
		int len = varData.length;
		double sum = hasIntercept ? parameters[len] : 0;
		for(int i = 0; i < len; i++) {
			sum += varData[i] * parameters[i];
		}
		return sum;
	}

	@Override
	public String toString() {
		return "LinearParametricModel [params=" + Arrays.toString(params)
				+ ", hasIntercept=" + hasIntercept + "]";
	}
}
