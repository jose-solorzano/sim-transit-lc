package jhs.lc.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.awt.geom.Rectangle2D;

import org.junit.Test;

public class TestEvaluatableSurfaceSphere {
	@Test
	public void testZeroRotation() {
		FluxOrOpacityFunction brightnessSource = new LocalFluxOrOpacityFunction();
		EvaluatableSurfaceSphere sphere = EvaluatableSurfaceSphere.create(3.0, 0, brightnessSource);
		sphere.setRotationAngle(0);
		Point3D up = sphere.unrotatedPoint(3, 4, 5);
		assertEquals(3, up.x, 0.001);
		assertEquals(4, up.y, 0.001);
		assertEquals(5, up.z, 0.001);
		Point3D rp = sphere.rotatedPoint(up.x, up.y, up.z);
		assertEquals(rp.x, up.x, 0.001);
		assertEquals(rp.y, up.y, 0.001);
		assertEquals(rp.z, up.z, 0.001);		
		double brightness = sphere.getBrightness(0.3, 0.4, true);
		assertEquals(0.7, brightness, 0.001);
	}

	@Test
	public void testPointRotation() {
		FluxOrOpacityFunction brightnessSource = new LocalFluxOrOpacityFunction();
		double radius = 7.0;
		EvaluatableSurfaceSphere sphere = EvaluatableSurfaceSphere.create(radius, 0.1, brightnessSource);
		sphere.setRotationAngle(Math.PI / 4.0);
		double origX = 0.6;
		double origY = -0.8;
		double origZ = Math.sqrt(radius * radius - origX * origX - origY * origY);
		Point3D up = sphere.unrotatedPoint(origX, origY, origZ);
		System.out.println("unrotated: " + up);
		assertNotEquals(up.x, origX, 0.001);
		assertNotEquals(up.y, origY, 0.001);
		Point3D rp = sphere.rotatedPoint(up.x, up.y, up.z);
		System.out.println("rotated: " + rp);
		assertEquals(origX, rp.x, 0.001);
		assertEquals(origY, rp.y, 0.001);
		assertEquals(origZ, rp.z, 0.001);
	}

	private static class LocalFluxOrOpacityFunction implements FluxOrOpacityFunction {
		private static final long serialVersionUID = 1L;

		@Override
		public double fluxOrOpacity(double x, double y, double z) {
			return (x + y);
		}

		@Override
		public Rectangle2D getBoundingBox() {
			return null;
		}
		
		@Override
		public final double getExtraOptimizerError() {
			return 0;
		}
	}

}
