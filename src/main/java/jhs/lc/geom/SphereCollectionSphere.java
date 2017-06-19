package jhs.lc.geom;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jhs.math.util.ComparableValueHolder;

public class SphereCollectionSphere extends AbstractRotationAngleSphere {
	private final Sphere[] spheres;
	private final Point3D[] relativePositions;
	
	public SphereCollectionSphere(double radius, PointTransformer transformer, Sphere[] spheres,
			Point3D[] relativePositions) {
		super(radius, transformer);
		if(spheres.length != relativePositions.length) {
			throw new IllegalArgumentException("Arrays of diffent lengths.");
		}
		this.spheres = spheres;
		this.relativePositions = relativePositions;
	}

	@Override
	public final boolean isOnlyFrontVisible() {
		return false;
	}

	@Override
	public final double getBrightness(double x, double y, boolean front) {
		double r = this.radius;
		Sphere[] spheres = this.spheres;
		Point3D[] relativePositions = this.relativePositions;
		int n = spheres.length;
		List<BrightnessZ> blist = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			Sphere s = spheres[i];
			Point3D up = relativePositions[i];
			Point3D rp = this.rotatedPoint(up.x, up.y, r + up.z);
			if((front && rp.z >= 0) || (!front && rp.z < 0)) {
				double xdiff = x - rp.x;
				double ydiff = y - rp.y;
				double d = Math.sqrt(xdiff * xdiff + ydiff * ydiff);
				if(d < s.getRadius()) {
					blist.add(new BrightnessZ(s.getBrightness(xdiff, ydiff, true), rp.z));
				}
			}
		}
		if(blist.isEmpty()) {			
			return Double.NaN;
		}
		Collections.sort(blist);
		double brightness = Double.NaN;
		for(BrightnessZ bz : blist) {
			double b = bz.brightness;
			if(!Double.isNaN(b)) {
				if(b >= 0) {
					brightness = b;
				}
				else {
					if(Double.isNaN(brightness)) {
						brightness = b;
					}
					else {
						brightness *= (-b);
					}
				}
			}
		}
		return brightness;
	}

	@Override
	public final Rectangle2D getBoundingBox() {
		Sphere[] spheres = this.spheres;
		Point3D[] relativePositions = this.relativePositions;
		int n = spheres.length;
		if(n == 0) {
			return null;
		}
		double r = this.radius;
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < n; i++) {
			Sphere s = spheres[i];
			double radius = s.getRadius();
			Point3D up = relativePositions[i];
			Point3D point = this.rotatedPoint(up.x, up.y, r + up.z);
			if(point.x - radius < minX) {
				minX = point.x - radius;
			}
			if(point.x + radius > maxX) {
				maxX = point.x + radius;
			}
			if(point.y + radius > maxY) {
				maxY = point.y + radius;
			}
			if(point.y - radius < minY) {
				minY = point.y - radius;
			}
		}
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);		
	}

	private static class BrightnessZ extends ComparableValueHolder<BrightnessZ> {
		private final double brightness;
		private final double z;
		
		public BrightnessZ(double brightness, double z) {
			super();
			this.brightness = brightness;
			this.z = z;
		}

		@Override
		protected final double getValue() {
			return this.z;
		}
	}
	
}
