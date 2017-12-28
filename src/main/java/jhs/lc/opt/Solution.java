package jhs.lc.opt;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.geom.TransitDepictionProducer;
import jhs.lc.geom.TransitFunction;
import jhs.lc.sims.AngularSimulation;
import jhs.lc.sims.SimulatedFlux;
import jhs.lc.sims.SimulatedFluxSource;
import jhs.lc.sims.SimulationImageSet;

public class Solution implements java.io.Serializable {
	private static final long serialVersionUID = 2L;
	private final SimulatedFluxSource fluxSource;
	private final TransitFunction brightnessFunction;
	private final double orbitRadius;
	private final double peakFraction;
	private final double[] osParameters;
	private final SimulatedFlux modeledFlux;

	public Solution(SimulatedFluxSource fluxSource,
			TransitFunction brightnessFunction, double orbitRadius,
			double peakFraction, double[] osParameters, SimulatedFlux modeledFlux) {
		super();
		this.fluxSource = fluxSource;
		this.brightnessFunction = brightnessFunction;
		this.orbitRadius = orbitRadius;
		this.peakFraction = peakFraction;
		this.osParameters = osParameters;
		this.modeledFlux = modeledFlux;
	}

	public double getPeakFraction() {
		return peakFraction;
	}

	public final SimulatedFluxSource getFluxSource() {
		return fluxSource;
	}

	public final TransitFunction getBrightnessFunction() {
		return brightnessFunction;
	}

	public final double getOrbitRadius() {
		return orbitRadius;
	}

	public final double[] getOsParameters() {
		return osParameters;
	}

	public final double[] getOpacityFunctionParameters() {
		return osParameters;
	}

	public final SimulatedFlux produceModeledFlux() {
		return this.modeledFlux;
	}
	
	public final double getExtraOptimizerError() {
		return this.brightnessFunction.getExtraOptimizerError();
	}

	/*
	public Dimension suggestImageDimension(int numPixels) {
		TransitDepictionProducer tdp = new TransitDepictionProducer(this.brightnessFunction);
		double imageWidth = tdp.minWidth(true);
		double imageHeight = tdp.minHeight(true);
		if(imageHeight == 0) {
			throw new IllegalStateException("Image has height of zero.");
		}
		double aspectRatio = imageWidth / imageHeight;
		int heightPixels = (int) Math.round(Math.sqrt(numPixels / aspectRatio));
		int widthPixels = (int) Math.round((double) numPixels / heightPixels);
		return new Dimension(widthPixels, heightPixels);		
	}
	*/

	public BufferedImage produceDepiction(int numPixels) {
		return this.produceDepiction(numPixels, true);
	}

	public BufferedImage produceDepiction(int numPixels, boolean addStarCircle) {
		TransitDepictionProducer tdp = new TransitDepictionProducer(this.brightnessFunction);
		return tdp.produceDepiction(numPixels, addStarCircle);
	}

	public SimulationImageSet produceModelImages(double inclineAngle, double orbitalPeriod, LimbDarkeningParams ldParams, double[] timestamps, double peakFraction, String timestampPrefix, int numPixels, double lcvwf) {
		TransitDepictionProducer tdp = new TransitDepictionProducer(this.brightnessFunction);
		RotationAngleSphereFactory sphereFactory = new EvaluatableSurfaceSphereFactory(this.brightnessFunction);
		AngularSimulation simulation = new AngularSimulation(inclineAngle, this.orbitRadius, orbitalPeriod, ldParams, sphereFactory);
		double noiseSd = 0;
		double imageWidth = tdp.minWidth(true);
		double imageHeight = tdp.minHeight(true);
		if(imageHeight == 0) {
			throw new IllegalStateException("Image has height of zero.");
		}
		double aspectRatio = imageWidth / imageHeight;
		int heightPixels = (int) Math.round(Math.sqrt(numPixels / aspectRatio));
		if(heightPixels % 2 != 0) {
			heightPixels = heightPixels + 1;
		}
		int widthPixels = (int) Math.round((double) numPixels / heightPixels);
		if(widthPixels % 2 != 0) {
			widthPixels = widthPixels + 1;
		}
		int lightCurveViewWidth = (int) Math.round(widthPixels * lcvwf);
		if(lightCurveViewWidth % 2 != 0) {
			lightCurveViewWidth = lightCurveViewWidth + 1;
		}
		return new SimulationImageSet(simulation, timestamps, peakFraction, widthPixels, lightCurveViewWidth, heightPixels, noiseSd, timestampPrefix);
	}	
}
