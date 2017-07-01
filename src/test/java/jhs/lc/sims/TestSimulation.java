package jhs.lc.sims;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;

import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ImageOpacityFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.Point3D;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.geom.SolidSphere;
import jhs.lc.geom.Sphere;
import jhs.lc.geom.SphereCollectionSphereFactory;
import jhs.math.util.MathUtil;

import org.junit.Test;

public class TestSimulation {
	@Test
	public void testHalfDisk() {
		double inclineAngle = 0;
		double orbitRadius = 100.0;
		double orbitalPeriod = 200.0;
		double discRadius = 0.5;
		FluxOrOpacityFunction brightnessSource = new FluxOrOpacityFunction() {			
			private static final long serialVersionUID = 1L;

			@Override
			public Rectangle2D getBoundingBox() {
				return new Rectangle2D.Double(-discRadius, -discRadius, discRadius * 2, discRadius * 2);
			}
			
			@Override
			public double fluxOrOpacity(double x, double y, double z) {
				double d = Math.sqrt(x * x + y * y);
				return d < discRadius ? 0 : Double.NaN;
			}
		};
		RotationAngleSphereFactory sphereFactory = new EvaluatableSurfaceSphereFactory(brightnessSource);
		LimbDarkeningParams ldParams = LimbDarkeningParams.NONE;
		AngularSimulation simulation = new AngularSimulation(
			inclineAngle, 
			orbitRadius, 
			orbitalPeriod, 
			ldParams, 
			sphereFactory);
		
		double viewportAngle = Math.atan(1.0 / orbitRadius) * 2;
		double timeSpan = orbitalPeriod * viewportAngle / Math.PI;
		System.out.println("viewportAngle: " + viewportAngle);
		System.out.println("timeSpan: " + timeSpan);
		double startTimestamp = -(timeSpan / 2);
		double endTimestamp = +(timeSpan / 2);
		double[] timestamps = simulation.timestamps(startTimestamp, endTimestamp, 101);
		double[] flux = simulation.produceModeledFlux(timestamps, 0.5, 200, 200);
		assertEquals(0.75, flux[flux.length / 2], 0.01);
		assertEquals(flux[flux.length / 2], MathUtil.min(flux), 0.01);		
		assertEquals(1.0, MathUtil.max(flux), 0.01);
	}

	@Test
	public void testCompleteOrbit() {
		Sphere[] spheres = new Sphere[] { new SolidSphere(1.0) };
		Point3D[] relativePositions = new Point3D[] { new Point3D(0, 0, 0) };
		RotationAngleSphereFactory sphereFactory = new SphereCollectionSphereFactory(spheres, relativePositions);
		double inclineAngle = 0.5;
		double orbitRadius = 5.0;
		double orbitalPeriod = 100.0;
		AngularSimulation simulation = new AngularSimulation(
				inclineAngle, 
				orbitRadius, 
				orbitalPeriod, 
				LimbDarkeningParams.SUN, 
				sphereFactory);
		simulation.setBoxWidth(12.0);
		simulation.setBoxHeight(12.0);
		double startTimestamp = 0;
		double endTimestamp = orbitalPeriod;
		double[] timestamps = AngularSimulation.timestamps(startTimestamp, endTimestamp, 201);
		double[] flux = simulation.produceModeledFlux(timestamps, 0.5, 200, 200);		
		assertEquals(201, flux.length);
		double[] expectedFlux = new double[flux.length];
		Arrays.fill(expectedFlux, 2.0);
		assertArrayEquals(expectedFlux, flux, 0.01);
	}
	
	@Test
	public void testBlackOnWhiteImage() throws Exception {
		double inclineAngle = 0;
		double orbitRadius = 100.0;
		double orbitalPeriod = 100.0;
		double imageWidth = 1.8;
		double imageHeight = 1.8;

		BufferedImage image;
		InputStream in = this.getClass().getResourceAsStream("/opaque-triangle.png");
		if(in == null) {
			throw new IllegalStateException("Resource not found.");
		}
		try {
			image = ImageIO.read(in);
		} finally {
			in.close();
		}
		FluxOrOpacityFunction brightnessFunction = ImageOpacityFunction.createOpacitySource(image, imageWidth, imageHeight);
		assertEquals(0, brightnessFunction.fluxOrOpacity(0, 0, 1.0), 0.001);
		//assertEquals(0, brightnessFunction.fluxOrOpacity(0, -0.4, 1.0), 0.001);
		assertEquals(-1, brightnessFunction.fluxOrOpacity(+0.7, +0.7, 1.0), 0.001);
		assertEquals(-1, brightnessFunction.fluxOrOpacity(+0.7, -0.7, 1.0), 0.001);
		
		RotationAngleSphereFactory sphereFactory = new EvaluatableSurfaceSphereFactory(brightnessFunction);
		
		AngularSimulation simulation = new AngularSimulation(
				inclineAngle, 
				orbitRadius, 
				orbitalPeriod, 
				LimbDarkeningParams.SUN, 
				sphereFactory);
		
		double[] timestamps = new double[] { -0.001, +0.001 };		
		int width = 5;
		int height = 5;
		double noiseSd = 0;
		String timestampPrefix = "Day";
		Iterator<BufferedImage> images = simulation.produceModelImages(timestamps, 0.5, width, height, noiseSd, timestampPrefix);
		assertTrue(images.hasNext());
		BufferedImage simImage = images.next();
		double[] pixel = getCenterPixel(simImage);
		double mean = MathUtil.mean(pixel);
		assertTrue("Center color: " + mean, mean <= 1);		
	}
	
	private static double[] getCenterPixel(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		double[] pixel = image.getData().getPixel(width / 2, height / 2, (double[]) null);
		return pixel;
	}

}
