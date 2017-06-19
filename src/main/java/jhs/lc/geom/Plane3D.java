package jhs.lc.geom;

public class Plane3D {
	public final double a, b, c, d;

	public Plane3D(double a, double b, double c, double d) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	@Override
	public String toString() {
		return "Plane3D [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + "]";
	}
}
