package jhs.lc.sims;

import java.awt.image.BufferedImage;
import java.util.Iterator;

public class SimulationImageSet implements Iterable<BufferedImage> {
	private final AngularSimulation simulation;
	private final double[] timestamps;
	private final double peakFraction;
	private final int starViewWidth;
	private final int lightCurveViewWidth;
	private final int height; 
	private final double noiseSd;
	private final String timestampPrefix;
	
	public SimulationImageSet(AngularSimulation simulation, double[] timestamps, double peakFraction, int starViewWidth,
			int lightCurveViewWidth, int height, double noiseSd, String timestampPrefix) {
		super();
		this.simulation = simulation;
		this.timestamps = timestamps;
		this.peakFraction = peakFraction;
		this.starViewWidth = starViewWidth;
		this.lightCurveViewWidth = lightCurveViewWidth;
		this.height = height;
		this.noiseSd = noiseSd;
		this.timestampPrefix = timestampPrefix;
	}
	
	public int getImageWidth() {
		return this.starViewWidth + this.lightCurveViewWidth;
	}
	
	public int getImageHeight() {
		return this.height;
	}

	@Override
	public final Iterator<BufferedImage> iterator() {
		return simulation.produceModelImages(timestamps, peakFraction, starViewWidth, lightCurveViewWidth, height, noiseSd, timestampPrefix);
	}
}
