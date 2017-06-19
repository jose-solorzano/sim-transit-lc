package jhs.lc.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class TestRotationMatrix {

	@Test
	public void testXZRotation() {
		Plane3D basePlane = new Plane3D(0, 1.0, 0, 0);
		PointTransformer transformer = PointTransformer.getPointTransformer(basePlane);
		double x = -5, y = 3, z = 7;
		Point3D tp = transformer.transformPoint(x, y, z);
		assertEquals(tp.y, y, 0.001);
		assertNotEquals(tp.x, x, 0.001);
		assertNotEquals(tp.z, z, 0.001);
		Point3D rp = transformer.inverseTransformPoint(tp.x, tp.y, tp.z);
		assertEquals(x, rp.x, 0.001);
		assertEquals(y, rp.y, 0.001);
		assertEquals(z, rp.z, 0.001);
	}

	@Test
	public void testGenericRotation() {
		Plane3D basePlane = new Plane3D(+1, -2, +4, 0);
		PointTransformer transformer = PointTransformer.getPointTransformer(basePlane);
		double x = -3, y = 5, z = 7;
		Point3D tp = transformer.transformPoint(x, y, z);
		Point3D rp = transformer.inverseTransformPoint(tp.x, tp.y, tp.z);
		assertEquals(x, rp.x, 0.001);
		assertEquals(y, rp.y, 0.001);
		assertEquals(z, rp.z, 0.001);
	}

}
