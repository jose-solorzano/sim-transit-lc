package jhs.lc.geom;

import java.awt.geom.Rectangle2D;

public interface TransitFunction extends java.io.Serializable {
	/**
	 * Objects that emit light return a positive value up to 1, representing flux.
	 * Occulters return a number between -1 and 0 representing the negative transmittance of the occulter. 
	 * Transmittance is 1 minus opacity, or exp(-opticalDepth).
	 */
	public double fluxOrTransmittance(double x, double y, double z);
	
	/**
	 * 
	 * @return
	 */
	public Rectangle2D getBoundingBox();
	
	public double getExtraOptimizerError();
}
