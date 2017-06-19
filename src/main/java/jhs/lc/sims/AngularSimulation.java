package jhs.lc.sims;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

import jhs.lc.geom.AbstractRotatableSphere;
import jhs.lc.geom.AbstractRotationAngleSphere;
import jhs.lc.geom.ExtraNoiseSphere;
import jhs.lc.geom.ImageUtil;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.geom.SolidSphere;
import jhs.lc.geom.Sphere;
import jhs.lc.geom.SphereViewport;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MatrixUtil;

public class AngularSimulation implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private static final NumberFormat NFORMAT = new DecimalFormat("#.00");
	private final double inclineAngle;
	private final double orbitRadius;
	private final double orbitalPeriod;
	private final LimbDarkeningParams ldParams;
	private final RotationAngleSphereFactory sphereFactory;
	
	private double boxWidth = 2.0;
	private double boxHeight = 2.0;

	public AngularSimulation(double inclineAngle, double orbitRadius, double orbitalPeriod,
			LimbDarkeningParams ldParams, RotationAngleSphereFactory sphereFactory) {
		super();
		this.inclineAngle = inclineAngle;
		this.orbitRadius = orbitRadius;
		this.orbitalPeriod = orbitalPeriod;
		this.ldParams = ldParams;
		this.sphereFactory = sphereFactory;
	}

	public final double getBoxWidth() {
		return boxWidth;
	}

	public final void setBoxWidth(double boxWidth) {
		this.boxWidth = boxWidth;
	}

	public final double getBoxHeight() {
		return boxHeight;
	}

	public final void setBoxHeight(double boxHeight) {
		this.boxHeight = boxHeight;
	}

	public double getInclineAngle() {
		return inclineAngle;
	}

	public double getOuterSphereRadiusFactor() {
		return orbitRadius;
	}

	public double getBaseFlux(int width, int height) {
		Sphere starSphere = new SolidSphere(1.0, this.ldParams);
		SphereViewport starViewport = new SphereViewport(starSphere, this.boxWidth, this.boxHeight);
		double[][] matrix = new double[width][height];
		starViewport.populateBrightness(matrix, true);
		return SphereViewport.totalBrightness(matrix, width, height);
	}
	
	public static double[] timestamps(double startTimestamp, double endTimestamp, int numSteps) {
		double[] timestamps = new double[numSteps];
		double gap = (endTimestamp - startTimestamp) / (numSteps - 1);
		for(int i = 0; i < numSteps; i++) {
			timestamps[i] = startTimestamp + i * gap;
		}
		return timestamps;
	}
	
	public final double[] produceModeledFlux(double[] timestamps, double peakTimespanFraction, int width, int height) {
		int length = timestamps.length;
		if(length < 2) {
			throw new IllegalArgumentException("Too few timestamps.");
		}
		double startTimestamp = timestamps[0];
		double endTimestamp = timestamps[length - 1];
		double timeSpan = endTimestamp - startTimestamp;
		double cycleFraction = timeSpan / this.orbitalPeriod;
				
		double angularRange = Math.PI * 2 * cycleFraction;
		double startAngle = -angularRange * peakTimespanFraction;
		double timeToAngleFactor = angularRange / timeSpan;
		
		AbstractRotationAngleSphere sphere = this.sphereFactory.create(this.orbitRadius, this.inclineAngle);
		SphereViewport viewport = new SphereViewport(sphere, this.boxWidth, this.boxHeight);
		Sphere starSphere = new SolidSphere(1.0, this.ldParams);
		SphereViewport starViewport = new SphereViewport(starSphere, this.boxWidth, this.boxHeight);
		double[][] baseMatrix = new double[width][height];
		MatrixUtil.fill(baseMatrix, Double.NaN);
		starViewport.populateBrightness(baseMatrix, true);
		double baseFlux = SphereViewport.totalBrightness(baseMatrix, width, height);
		
		double[] fluxArray = new double[length];
		boolean onlyFront = sphere.isOnlyFrontVisible();
		for(int i = 0; i < length; i++) {
			double timestamp = timestamps[i];
			double rotationAngle = startAngle + (timestamp - startTimestamp) * timeToAngleFactor;
			sphere.setRotationAngle(rotationAngle);
			double fluxDiff = viewport.fluxDifference(baseMatrix, onlyFront);
			double flux = baseFlux + fluxDiff;
			if(Double.isNaN(flux)) {
				throw new IllegalStateException("NaN flux at timestamp " + timestamp + "!");
			}
			double normFlux = flux / baseFlux;
			fluxArray[i] = normFlux;
		}
		return fluxArray;
	}	
	
	
	public final Iterator<BufferedImage> produceModelImages(final double[] timestamps, final double peakTimespanFraction, final int width, final int height, double noiseSd, final String timestampPrefix) {
		return this.produceModelImages(timestamps, peakTimespanFraction, width, height, noiseSd, timestampPrefix, Double.NaN, Double.NaN);
	}

	public final Iterator<BufferedImage> produceModelImages(final double[] timestamps, final double peakTimespanFraction, final int width, final int height, double noiseSd, final String timestampPrefix, double minTimestamp, double maxTimestamp) {
		final int length = timestamps.length;
		if(length < 2) {
			throw new IllegalArgumentException("Too few timestamps.");
		}
		final double startTimestamp = timestamps[0];
		double endTimestamp = timestamps[length - 1];
		double timeSpan = endTimestamp - startTimestamp;
		
		double cycleFraction = timeSpan / this.orbitalPeriod;
		double angularRange = Math.PI * 2 * cycleFraction;
		final double startAngle = -angularRange * peakTimespanFraction;
		final double timeToAngleFactor = angularRange / timeSpan;
		
		final double[] restrictedTimestamps = Double.isNaN(maxTimestamp - minTimestamp) ? timestamps : ArrayUtil.filter(timestamps, value -> value >= minTimestamp && value <= maxTimestamp);
		final int restrictedLength = restrictedTimestamps.length;
		
		AbstractRotationAngleSphere nnSphere = this.sphereFactory.create(this.orbitRadius, this.inclineAngle);
		final AbstractRotationAngleSphere sphere;
		if(nnSphere instanceof AbstractRotatableSphere) {
			sphere = new ExtraNoiseSphere((AbstractRotatableSphere) nnSphere, noiseSd); 
		} else {
			sphere = nnSphere;
		}
		
		final SphereViewport viewport = new SphereViewport(sphere, this.boxWidth, this.boxHeight);
		Sphere starSphere = new SolidSphere(1.0, this.ldParams);
		SphereViewport starViewport = new SphereViewport(starSphere, this.boxWidth, this.boxHeight);
		final double[][] baseMatrix = new double[width][height];
		MatrixUtil.fill(baseMatrix, Double.NaN);
		starViewport.populateBrightness(baseMatrix, true);
		final double[][] matrix = new double[width][height];
		final boolean onlyFront = sphere.isOnlyFrontVisible();
		final int fontSize = width / 35;	
		return new Iterator<BufferedImage>() {
			private int index = 0;
			
			@Override
			public boolean hasNext() {
				return this.index < restrictedLength;
			}

			@Override
			public BufferedImage next() {
				int i = this.index++;
				double timestamp = restrictedTimestamps[i];
				double rotationAngle = startAngle + (timestamp - startTimestamp) * timeToAngleFactor;
				sphere.setRotationAngle(rotationAngle);
				MatrixUtil.copyMatrix(baseMatrix, matrix);
				viewport.populateBrightness(matrix, onlyFront);
								
				BufferedImage image = ImageUtil.buildImage(matrix, width, height);
				if(fontSize >= 6) {
					this.addText(image, timestampPrefix  + " " + NFORMAT.format(timestamp));
				}
				return image;
			}

			private void addText(BufferedImage image, String text) {
				Graphics2D g = image.createGraphics();
				try {
					g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
					g.setColor(new Color(150, 150, 255));
					int y = image.getHeight() - (int) Math.round(fontSize * 1.5);
					int x = image.getWidth() - (int) Math.round(g.getFontMetrics().charsWidth(text.toCharArray(), 0, text.length()) * 1.2);
					g.drawString(text, x, y);
				} finally {
					g.dispose();
				}
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}			
		};	
	}
}
