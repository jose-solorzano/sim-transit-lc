package jhs.lc.geom;

import java.awt.geom.Rectangle2D;

public interface Sphere {
	public double getRadius();
	public double getBrightness(double x, double y, boolean front);
	public Rectangle2D getBoundingBox();
}
