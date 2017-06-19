package jhs.lc.geom;

public class Planes {
	public static Point3D project(Point3D point, Plane3D plane) {
		double a = plane.a;
		double b = plane.b;
		double c = plane.c;
		double d = plane.d;
		double denom = a * a + b * b + c * c;
		if(denom == 0) {
			throw new IllegalArgumentException("Plane parameters all zero.");
		}
		double px = point.x;
		double py = point.y;
		double pz = point.z;
		double t = -(a * px + b * py + c * pz + d) / denom;
		double rx = px + a * t;
		double ry = py + b * t;
		double rz = pz + c * t;
		return new Point3D(rx, ry, rz);
	}
	
	public static Plane3D inclinedPlane(double angle) {
		double bx = 0;
		double by = Math.sin(angle);
		double bz = -Math.cos(angle);
		double ax = 1.0;
		double ay = 0;
		double az = 0;
		
		// Cross product
		double cpx = ay * bz - az * by;
		double cpy = az * bx - ax * bz;
		double cpz = ax * by - ay * bx;
		
		return new Plane3D(cpx, cpy, cpz, 0);
	}	
	
	public static Plane3D yzPlane() {
		return new Plane3D(1.0, 0, 0, 0);
	}
}
