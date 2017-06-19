package jhs.math.regression;

public class SimpleVectorialRegressionItem implements VectorialRegressionItem, java.io.Serializable {
	private static final long serialVersionUID = 0L;
	private final double[] position;
	private final double response;

	public SimpleVectorialRegressionItem(double[] position, double response) {
		super();
		this.position = position;
		this.response = response;
	}

	public final double getResponse() {
		return response;
	}

	public final double[] getPosition() {
		return this.position;
	}
}
