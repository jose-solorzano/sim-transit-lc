package jhs.lc.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class TestPointTransformer {
	@Test
	public void testTransformation() {
		Plane3D basePlane = Planes.inclinedPlane(0.1);
		PointTransformer pt = PointTransformer.getPointTransformer(basePlane);
		double origX = +1.1;
		double origY = -2.2;
		double origZ = +3.3;
		Point3D tp = pt.transformPoint(origX, origY, origZ);
		assertNotEquals(tp.x, origX, 0.001);
		assertNotEquals(tp.y, origY, 0.001);
		assertNotEquals(tp.z, origZ, 0.001);
		Point3D itp = pt.inverseTransformPoint(tp.x, tp.y, tp.z);
		assertEquals(origX, itp.x, 0.001);
		assertEquals(origY, itp.y, 0.001);
		assertEquals(origZ, itp.z, 0.001);		
	}
}
