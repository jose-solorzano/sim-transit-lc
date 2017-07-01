package jhs.lc.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jhs.math.util.MatrixUtil;

import org.junit.Test;

public class TestSphereViewport {
	@Test
	public void testBackFlux() {
		PointTransformer transformer = PointTransformer.getPointTransformer(Planes.inclinedPlane(0), Planes.yzPlane());
		Sphere[] spheres = new Sphere[] { this.getSphere() };
		Point3D[] relativePositions = new Point3D[] { new Point3D(0, 0, 0) };
		SphereCollectionSphere viewSphere = new SphereCollectionSphere(3.0, transformer, spheres, relativePositions);
		SphereViewport viewport = new SphereViewport(viewSphere);
		viewSphere.setRotationAngle(Math.PI);
		double[][] targetMatrix = new double[16][16];
		MatrixUtil.fill(targetMatrix, Double.NaN);
		double diff = viewport.fluxDifference(targetMatrix, false);
		assertTrue(diff >= targetMatrix.length * targetMatrix.length * 0.5);
	}

	@Test
	public void testBackIgnored() {
		PointTransformer transformer = PointTransformer.getPointTransformer(Planes.inclinedPlane(0), Planes.yzPlane());
		Sphere[] spheres = new Sphere[] { new SolidSphere(1.0, LimbDarkeningParams.SUN, -0.5) };
		Point3D[] relativePositions = new Point3D[] { new Point3D(0, 0, 0) };
		SphereCollectionSphere viewSphere = new SphereCollectionSphere(3.0, transformer, spheres, relativePositions);
		SphereViewport viewport = new SphereViewport(viewSphere);
		viewSphere.setRotationAngle(Math.PI);
		int width = 3;
		int height = 3;
		double[][] targetMatrix = new double[3][3];
		MatrixUtil.fill(targetMatrix, Double.NaN);
		Sphere mainStar = new SolidSphere(1.0);
		SphereViewport mainVeiwport = new SphereViewport(mainStar);
		mainVeiwport.populateBrightness(targetMatrix, true);
		double flux = SphereViewport.totalBrightness(targetMatrix, width, height);
		assertTrue(flux >= 0.5 * width * height);
		viewport.populateBrightness(targetMatrix, false);
		double newFlux = SphereViewport.totalBrightness(targetMatrix, width, height);
		assertEquals(flux, newFlux, 0.001);
	}

	private Sphere getSphere() {
		return new SolidSphere(1.0);
	}
}
