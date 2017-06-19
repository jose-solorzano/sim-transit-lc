package jhs.lc.geom;

import java.awt.geom.Rectangle2D;

public final class SolidSphere implements Sphere {
	private final double radius;
	private final double maxBrightness;
	private final Rectangle2D boundingBox;
	private final LimbDarkeningParams ldParams;

	public SolidSphere(double radius) {
		this(radius, LimbDarkeningParams.SUN, 1.0);
	}

	public SolidSphere(double radius, LimbDarkeningParams ldParams) {
		this(radius, ldParams, 1.0);
	}

	public SolidSphere(double radius, LimbDarkeningParams ldParams, double maxBrightness) {
		this.ldParams = ldParams;		
		this.radius = radius;
		this.maxBrightness = maxBrightness;
		double diameter = radius * 2;
		this.boundingBox = new Rectangle2D.Double(-radius, -radius, diameter, diameter);
	}

	@Override
	public final double getRadius() {
		return this.radius;
	}

	@Override
	public final double getBrightness(double x, double y, boolean front) {		
		double r = this.radius;
		double zSq = r * r - x * x - y * y;
		if(zSq < 0) {
			return Double.NaN;
		}
		LimbDarkeningParams ldParams = this.ldParams;		
		if(ldParams != null) {
			double absZ = Math.sqrt(zSq);
			double mb = this.maxBrightness;
			double ldf = ldParams.getLimbDarkeningFactor(absZ, r);
			return mb > 0 ? ldf * mb : -(1 - ldf * (1 + mb));
		}
		else {
			return this.maxBrightness;
		}
	}

	public final boolean hasLimbDarkening() {
		return this.ldParams != null;
	}

	@Override
	public final Rectangle2D getBoundingBox() {
		return this.boundingBox;
	}
}
