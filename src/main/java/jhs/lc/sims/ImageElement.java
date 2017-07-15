package jhs.lc.sims;

public final class ImageElement {
	final double x, y;
	final int colIdx;
	final int rowIdx;
	final double brightness;

	public ImageElement(double x, double y, int colIdx, int rowIdx, double brightness) {
		this.x = x;
		this.y = y;
		this.colIdx = colIdx;
		this.rowIdx = rowIdx;
		this.brightness = brightness;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(brightness);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + colIdx;
		result = prime * result + rowIdx;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if(!(obj instanceof ImageElement)) {
			return false;
		}
		ImageElement other = (ImageElement) obj;
		if (colIdx != other.colIdx)
			return false;
		if (rowIdx != other.rowIdx)
			return false;
		if (Double.doubleToLongBits(brightness) != Double.doubleToLongBits(other.brightness))
			return false;
		return true;
	}
}