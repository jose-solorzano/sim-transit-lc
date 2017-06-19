package jhs.lc.geom;

import java.awt.geom.Rectangle2D;

public abstract class AbstractRotationAngleSphere implements Sphere {
	protected final double radius;
	protected final PointTransformer transformer;
	protected double rotationAngle = 0;

	public AbstractRotationAngleSphere(double radius, PointTransformer transformer) {
		this.radius = radius;
		this.transformer = transformer;
	}
	
	public abstract boolean isOnlyFrontVisible();
	
	public final PointTransformer getTransformer() {
		return transformer;
	}

	@Override
	public final double getRadius() {
		return this.radius;
	}

	public double getRotationAngle() {
		return rotationAngle;
	}

	public void setRotationAngle(double rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	protected Rectangle2D enclosingBox(Point3D ... points) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(Point3D point : points) {
			if(point.x < minX) {
				minX = point.x;
			}
			if(point.x > maxX) {
				maxX = point.x;
			}
			if(point.y > maxY) {
				maxY = point.y;
			}
			if(point.y < minY) {
				minY = point.y;
			}
		}
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	protected final Point3D rotatedPoint(double ux, double uy, boolean front) {
		double r = this.radius;
		double absZ = Math.sqrt(r * r - ux * ux - uy * uy);
		double uz = front ? +absZ : -absZ;
		return this.rotatedPoint(ux, uy, uz);
	}

	public final Point3D rotatedPoint(double ux, double uy, double uz) {
		PointTransformer transformer = this.transformer;
		Point3D tp = this.rotateAroundY(new Point3D(ux, uy, uz), +this.rotationAngle);
		Point3D rp = transformer.inverseTransformPoint(tp.x, tp.y, tp.z);
		return rp;
	}

	public final Point3D unrotatedPoint(double x, double y, double z) {
		PointTransformer transformer = this.transformer;
		Point3D tp = transformer.transformPoint(x, y, z);
		Point3D up = this.rotateAroundY(tp, -this.rotationAngle);
		return up;		
	}

	private Point3D rotateAroundY(Point3D point, double angleOffset) {
		double x = point.x;
		double z = point.z;
		double xzm = Math.sqrt(x * x + z * z);
		double startAngle = Math.atan2(point.z, point.x);
		double endAngle = startAngle - angleOffset;
		double endX = xzm * Math.cos(endAngle);
		double endZ = xzm * Math.sin(endAngle);
		return new Point3D(endX, point.y, endZ);
	}

}