package jhs.lc.sims;

public class AngleUnsupportedException extends Exception {
	private static final long serialVersionUID = 1L;
	private final String angleName;
	private final double value;
	
	public AngleUnsupportedException(String angleName, double value) {
		super(angleName + " cannot have a value of " + value + ".");
		this.angleName = angleName;
		this.value = value;
	}

	public final String getAngleName() {
		return angleName;
	}

	public final double getValue() {
		return value;
	}
}
