package jhs.lc.sims;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Random;

import jhs.lc.geom.TransitFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

import org.junit.Test;

public class TestPixelatedStarFluxSource {
	@Test
	public void testFluxArrayMatchesAngularOne() {
		// Note: It doesn't work with positive flux values, because the viewport is only 2x2 in the angular flux source.
		
		double discRadius = 0.5;
		TransitFunction brightnessSource = new TransitFunction() {			
			private static final long serialVersionUID = 1L;

			@Override
			public Rectangle2D getBoundingBox() {
				return new Rectangle2D.Double(-1.8, -1.4, 3.3, 2.4);
			}
			
			@Override
			public double fluxOrTransmittance(double x, double y, double z) {
				double d = Math.sqrt(x * x + y * y);
				if(d >= discRadius) {
					return Double.NaN;
				}
				if(x >= 0 && y >= 0) {
					return -0.5;
				}
				else if(x <= 0 && y <= 0) {
					return -0.2;
				}
				else {
					return 0;
				}
			}
			
			@Override
			public final double getExtraOptimizerError() {
				return 0;
			}
		};
		double orbitRadius = 500.0;
		double orbitalPeriod = 300.0;
		double viewportAngle = Math.atan(1.0 / orbitRadius) * 2;
		double timeSpan = orbitalPeriod * viewportAngle / Math.PI;
		System.out.println("viewportAngle: " + viewportAngle);
		System.out.println("timeSpan: " + timeSpan);
		double timeOffset = 150.0;
		double startTimestamp = -(timeSpan / 2) + timeOffset;
		double endTimestamp = +(timeSpan / 2) + timeOffset;
		double[] timestamps = AngularSimulation.timestamps(startTimestamp, endTimestamp, 51);
		
		double inclineAngle = 0.001;

		SimulatedFluxSource angularFluxSource = this.getFluxSource(true, timestamps, orbitalPeriod, inclineAngle);
		double peakFraction = 0.5;
		double[] flux1 = angularFluxSource.produceModeledFlux(peakFraction, brightnessSource, orbitRadius).getFluxArray();
		double minFlux1 = MathUtil.min(flux1);
		System.out.println("MinFlux1: "+ minFlux1);
		SimulatedFluxSource fastFluxSource = this.getFluxSource(false, timestamps, orbitalPeriod, inclineAngle);
		assertTrue(fastFluxSource instanceof PixelatedStarFluxSource);
		double[] flux2 = fastFluxSource.produceModeledFlux(peakFraction, brightnessSource, orbitRadius).getFluxArray();
		double minFlux2 = MathUtil.min(flux2);
		System.out.println("MinFlux2: "+ minFlux2);
		assertArrayEquals("Differences=" + Arrays.toString(MathUtil.round(MathUtil.subtract(flux1, flux2), 3)), 
				flux1, flux2, 0.004);
		
		// Twice the orbital period, and twice the radius.
		double doubleRadius = orbitRadius * 2;
		double doublePeriod = orbitalPeriod * 2;
		SimulatedFluxSource doubleFastFluxSource = this.getFluxSource(false, timestamps, doublePeriod, 0);
		double[] doubleFlux = doubleFastFluxSource.produceModeledFlux(peakFraction, brightnessSource, doubleRadius).getFluxArray();
		SimulatedFluxSource fastFluxSource2 = this.getFluxSource(false, timestamps, orbitalPeriod, 0);
		double[] flux2NoIncline = fastFluxSource2.produceModeledFlux(peakFraction, brightnessSource, orbitRadius).getFluxArray();
		assertArrayEquals(flux2NoIncline, doubleFlux, 0.0001);		
	}	

	@Test
	public void testMidOpacityOnQuarter() {
		// Note: It doesn't work with positive flux values, because the viewport is only 2x2 in the angular flux source.
		
		TransitFunction brightnessSource = new TransitFunction() {			
			private static final long serialVersionUID = 1L;

			@Override
			public Rectangle2D getBoundingBox() {
				return new Rectangle2D.Double(-1, -1, 2, 2);
			}
			
			@Override
			public double fluxOrTransmittance(double x, double y, double z) {
				if(x >= 0 && y >= 0) {
					return -0.5;
				}
				else {
					return -1.0;
				}
			}
			
			@Override
			public final double getExtraOptimizerError() {
				return 0;
			}
		};
		double orbitRadius = 500.0;
		double orbitalPeriod = 300.0;
		double viewportAngle = Math.atan(1.0 / orbitRadius) * 2;
		double timeSpan = orbitalPeriod * viewportAngle / Math.PI;
		System.out.println("viewportAngle: " + viewportAngle);
		System.out.println("timeSpan: " + timeSpan);
		double startTimestamp = -(timeSpan / 2);
		double endTimestamp = +(timeSpan / 2);
		double[] timestamps = AngularSimulation.timestamps(startTimestamp, endTimestamp, 51);
		int timeZeroIndex = timestamps.length / 2;
		assertEquals(0, timestamps[timeZeroIndex], 0.0001);
		double inclineAngle = 0;

		
		SimulatedFluxSource angularFluxSource = this.getFluxSource(true, timestamps, orbitalPeriod, inclineAngle);
		double peakFraction = 0.5;
		
		SimulatedFluxSource fastFluxSource = this.getFluxSource(false, timestamps, orbitalPeriod, inclineAngle);
		assertTrue(fastFluxSource instanceof PixelatedStarFluxSource);
		double[] flux2 = fastFluxSource.produceModeledFlux(peakFraction, brightnessSource, orbitRadius).getFluxArray();				
		assertEquals(0.875, flux2[timeZeroIndex], 0.001);
		
		double[] flux1 = angularFluxSource.produceModeledFlux(peakFraction, brightnessSource, orbitRadius).getFluxArray();
		assertEquals(0.875, flux1[timeZeroIndex], 0.001);
		
		assertArrayEquals("Differences=" + Arrays.toString(MathUtil.round(MathUtil.subtract(flux1, flux2), 3)), 
				flux1, flux2, 0.005);
	}	

	@Test
	public void testOutOfBoundsShape() {
		double discRadius = 0.5;
		TransitFunction brightnessSource = new TransitFunction() {			
			private static final long serialVersionUID = 1L;

			@Override
			public Rectangle2D getBoundingBox() {
				return new Rectangle2D.Double(-2, -2, 1, 1);
			}
			
			@Override
			public double fluxOrTransmittance(double x, double y, double z) {
				double d = Math.sqrt(x * x + y * y);
				if(d >= discRadius) {
					return Double.NaN;
				}
				if(x >= 0) {
					return -0.5;
				}
				else {
					return 0;
				}
			}
			
			@Override
			public final double getExtraOptimizerError() {
				return 0;
			}
		};
		double orbitRadius = 200.0;
		double orbitalPeriod = 200.0;
		double viewportAngle = Math.atan(1.0 / orbitRadius) * 2;
		double timeSpan = orbitalPeriod * viewportAngle / Math.PI;
		double startTimestamp = -(timeSpan / 2);
		double endTimestamp = +(timeSpan / 2);
		double[] timestamps = AngularSimulation.timestamps(startTimestamp, endTimestamp, 51);		
		double inclineAngle = 0.002;
		SimulatedFluxSource fastFluxSource = this.getFluxSource(false, timestamps, orbitalPeriod, inclineAngle);
		double peakFraction = 0.5;
		assertArrayEquals(ArrayUtil.repeat(1.0, timestamps.length), fastFluxSource.produceModeledFlux(peakFraction, brightnessSource, orbitRadius).getFluxArray(), 0.0001);
	}	
	
	@Test
	public void testTimestampIndexBounds() {
		Random random = new Random(2);
		for(int t = 0; t < 50; t++) {
			this.testOneInstanceOfTimestampIndexBounds(random);
		}
	}
	
	private void testOneInstanceOfTimestampIndexBounds(Random random) {
		double[] timestamps = MathUtil.multiply(MathUtil.sampleUniform(random, 200), 100);
		Arrays.sort(timestamps);
		double scx = random.nextDouble() * 2 - 1;
		double orbitRadius = (random.nextDouble() + 0.1) * 1000.0;
		double startTimestamp = timestamps[0];
		double timeSpan = timestamps[timestamps.length - 1] - startTimestamp;
		
		double minFx = -random.nextDouble() * 5;
		double maxFx = minFx + random.nextDouble() * 3;
		double circumference = orbitRadius * Math.PI * 2;
		double orbitalPeriod = timeSpan * 10 + random.nextDouble() * 500;
		double transitSpeed = circumference / orbitalPeriod;
		double peakFraction = random.nextDouble() * 0.5 + 0.25;
		
		double peakTime = startTimestamp + timeSpan * peakFraction;
		double transitXAtTimeZero = transitSpeed * peakTime;

		double scxBiased = scx - transitXAtTimeZero;
		
		int lowerIndex = PixelatedStarFluxSource.lowerTimestampIndex(timestamps, maxFx, scxBiased, transitSpeed, transitXAtTimeZero);
		int upperIndex = PixelatedStarFluxSource.upperTimestampIndex(timestamps, minFx, scxBiased, transitSpeed, transitXAtTimeZero);
		int lowerIndexScanned = -1;
		int upperIndexScanned = -1;
		for (int i = 0; i < timestamps.length; i++) {
			double timestamp = timestamps[i];
			double fx = scxBiased - timestamp * transitSpeed;
			if(lowerIndexScanned == -1 && fx <= maxFx) {
				lowerIndexScanned = i;
			}
			if(upperIndexScanned == -1 && fx < minFx) {
				upperIndexScanned = i;
			}
		}
		if(lowerIndexScanned == -1) {
			lowerIndexScanned = timestamps.length;
		}
		if(upperIndexScanned == -1) {
			upperIndexScanned = timestamps.length;
		}
		assertEquals(lowerIndex, lowerIndexScanned);
		assertEquals(upperIndex, upperIndexScanned);
	}
	
	private SimulatedFluxSource getFluxSource(boolean angular, double[] timestamps, double orbitalPeriod, double inclineAngle) {
		LimbDarkeningParams ldParams = new LimbDarkeningParams(0.90, -0.2, 0.1);
		if(angular) {
			return new AngularFluxSource(timestamps, 500, 500, inclineAngle, orbitalPeriod, ldParams);
		}
		else {
			try {
				return new PixelatedStarFluxSource(timestamps, ldParams, inclineAngle, orbitalPeriod, 100, 100);
			} catch(AngleUnsupportedException au) {
				throw new IllegalStateException("Cannot handle a rotation of " + au.getValue() + " radians with 'fast' optimization. Use -angular option instead.");
			}
		}
	}
}
