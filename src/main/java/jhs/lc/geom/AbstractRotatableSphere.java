package jhs.lc.geom;

import java.awt.geom.Rectangle2D;

public abstract class AbstractRotatableSphere extends AbstractRotationAngleSphere {
	public AbstractRotatableSphere(double radius, PointTransformer transformer) {
		super(radius, transformer);
	}

	public abstract double getUnrotatedBrightness(double x, double y, double z);
	public abstract Rectangle2D getUnrotatedBoundingBox();

	@Override
	public final double getBrightness(double x, double y, boolean front) {
		double r = this.radius;
		double absZ = Math.sqrt(r * r - x * x - y * y);
		double z = front ? +absZ : -absZ;
		Point3D fp = this.unrotatedPoint(x, y, z);
		return this.getUnrotatedBrightness(fp.x, fp.y, fp.z);
	}
	
	@Override
	public final Rectangle2D getBoundingBox() {
		Rectangle2D ubb = this.getUnrotatedBoundingBox();
		if(ubb == null) {
			return null;
		}
		Point3D topLeft = this.rotatedPoint(ubb.getX(), ubb.getY(), true);
		Point3D bottomLeft = this.rotatedPoint(ubb.getX(), ubb.getY() + ubb.getHeight(), true);
		Point3D topRight = this.rotatedPoint(ubb.getX() + ubb.getWidth(), ubb.getY(), true);
		Point3D bottomRight = this.rotatedPoint(ubb.getX() + ubb.getHeight(), ubb.getY() + ubb.getHeight(), true);
		return this.enclosingBox(topLeft, bottomLeft, topRight, bottomRight);
	}
}
