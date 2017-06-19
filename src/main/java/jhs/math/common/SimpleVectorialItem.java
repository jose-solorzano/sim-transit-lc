package jhs.math.common;

public class SimpleVectorialItem implements VectorialItem, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final double[] position;
	
	public SimpleVectorialItem(double[] position) {
		super();
		this.position = position;
	}

	public final double[] getPosition() {
		return this.position;
	}
}
