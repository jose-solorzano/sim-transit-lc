package jhs.lc.geom;

import java.awt.geom.Rectangle2D;

public interface FluxOrOpacityFunction extends java.io.Serializable {
	/**
	 * A positive number between 0 and 1 (an opaque brightness.) A value of zero is opaque with no brightness.
	 * A negative number between -1.0 and 0 represents opacity (with -1 or NaN meaning transparent.)
	 */
	public double fluxOrOpacity(double x, double y, double z);
	
	public Rectangle2D getBoundingBox();
}
