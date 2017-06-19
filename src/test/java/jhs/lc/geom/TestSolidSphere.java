package jhs.lc.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestSolidSphere {
	@Test
	public void testNaNCorners() {
		SolidSphere sphere1 = new SolidSphere(1.0, null);
		this.assertNaNCorners(sphere1);
		SolidSphere sphere2 = new SolidSphere(1.0, LimbDarkeningParams.SUN);
		this.assertNaNCorners(sphere2);
	}
	
	@Test
	public void testLimbDarkeningStar() {
		SolidSphere sphere = new SolidSphere(1.0, LimbDarkeningParams.SUN);
		double b1 = sphere.getBrightness(0, 0, true);
		double b2 = sphere.getBrightness(-0.7, -0.7, true);
		assertTrue(b1 >= 1.5 * b2);
	}

	@Test
	public void testLimbDarkeningCloud() {
		SolidSphere sphere = new SolidSphere(1.0, LimbDarkeningParams.SUN, -0.5);
		double b1 = sphere.getBrightness(0, 0, true);
		double b2 = sphere.getBrightness(-0.7, -0.7, true);
		assertTrue(b1 < 0);
		assertTrue(b2 < 0);
		assertTrue(b1 > b2);
	}

	private void assertNaNCorners(Sphere sphere) {
		assertEquals(Double.NaN, sphere.getBrightness(-0.99, -0.99, true), 0.001);
		assertEquals(Double.NaN, sphere.getBrightness(-0.99, +0.99, true), 0.001);
		assertEquals(Double.NaN, sphere.getBrightness(+0.99, -0.99, true), 0.001);
		assertEquals(Double.NaN, sphere.getBrightness(+0.99, +0.99, true), 0.001);		
	}
}
