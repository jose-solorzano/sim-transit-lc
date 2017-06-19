package jhs.lc.geom;

public class Point3D {
	public final double x, y, z;

	public Point3D(double x, double y, double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public final double distance(Point3D point) {
		double dx = point.x - this.x;
		double dy = point.y - this.y;
		double dz = point.z - this.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public final double distance(double x, double y, double z) {
		double dx = x - this.x;
		double dy = y - this.y;
		double dz = z - this.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@Override
	public String toString() {
		return "Point3D [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

}
