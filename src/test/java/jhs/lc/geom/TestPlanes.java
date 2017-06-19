package jhs.lc.geom;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestPlanes {
	@Test
	public void testPointProjection() {
		Point3D point = new Point3D(5, -6, 3);
		Plane3D plane = new Plane3D(3, -2, 1, -2);
		Point3D projection = Planes.project(point, plane);
		assertEquals(projection.x, -1, 0.01);
		assertEquals(projection.y, -2, 0.01);
		assertEquals(projection.z, 1, 0.01);
	}

}
