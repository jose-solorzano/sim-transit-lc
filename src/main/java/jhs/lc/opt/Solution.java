package jhs.lc.opt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.MathException;

import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.sims.AngularSimulation;
import jhs.lc.sims.SimulatedFluxSource;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

public class Solution implements java.io.Serializable {
	private static final long serialVersionUID = 2L;
	private static final double PADDING = 0.2;
	private final SimulatedFluxSource fluxSource;
	private final FluxOrOpacityFunction brightnessFunction;
	private final double orbitRadius;
	private final double[] osParameters;	

	public Solution(SimulatedFluxSource fluxSource, FluxOrOpacityFunction brightnessFunction, double orbitRadius,
			double[] osParameters) {
		super();
		this.fluxSource = fluxSource;
		this.brightnessFunction = brightnessFunction;
		this.orbitRadius = orbitRadius;
		this.osParameters = osParameters;
	}

	public final SimulatedFluxSource getFluxSource() {
		return fluxSource;
	}

	public final FluxOrOpacityFunction getBrightnessFunction() {
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

	public final double[] produceModeledFlux() {
		return this.fluxSource.produceModeledFlux(brightnessFunction, orbitRadius);
	}

	private double minWidth() {
		Rectangle2D transitBounds = this.brightnessFunction.getBoundingBox();
		return MathUtil.max(2.0, (transitBounds.getX() + transitBounds.getWidth()) * 2, -transitBounds.getX() * 2) + PADDING * 2;		
	}
	
	private double minHeight() {
		Rectangle2D transitBounds = this.brightnessFunction.getBoundingBox();
		return MathUtil.max(2.0, (transitBounds.getY() + transitBounds.getHeight()) * 2, -transitBounds.getY() * 2) + PADDING * 2;		
	}
	
	public Dimension suggestImageDimension(int numPixels) {
		double imageWidth = this.minWidth();
		double imageHeight = this.minHeight();
		if(imageHeight == 0) {
			throw new IllegalStateException("Image has height of zero.");
		}
		double aspectRatio = imageWidth / imageHeight;
		int heightPixels = (int) Math.round(Math.sqrt(numPixels / aspectRatio));
		int widthPixels = (int) Math.round((double) numPixels / heightPixels);
		return new Dimension(widthPixels, heightPixels);		
	}
	
	public BufferedImage produceDepiction(int numPixels) {
		double imageWidth = this.minWidth();
		double imageHeight = this.minHeight();
		if(imageHeight == 0) {
			throw new IllegalStateException("Image has height of zero.");
		}
		double aspectRatio = imageWidth / imageHeight;
		int heightPixels = (int) Math.round(Math.sqrt(numPixels / aspectRatio));
		int widthPixels = (int) Math.round((double) numPixels / heightPixels);
		BufferedImage image = new BufferedImage(widthPixels, heightPixels, BufferedImage.TYPE_INT_ARGB);
		Rectangle2D stellarImageRectangle = new Rectangle2D.Double(-imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight);
		this.drawDepiction(image, stellarImageRectangle, this.brightnessFunction.getBoundingBox(), false);
		return image;
	}
	
	public Iterator<BufferedImage> produceModelImages(double inclineAngle, double orbitalPeriod, LimbDarkeningParams ldParams, double[] timestamps, double peakFraction, String timestampPrefix, int numPixels) {
		RotationAngleSphereFactory sphereFactory = new EvaluatableSurfaceSphereFactory(this.brightnessFunction);
		AngularSimulation simulation = new AngularSimulation(inclineAngle, this.orbitRadius, orbitalPeriod, ldParams, sphereFactory);
		double noiseSd = 0;
		double imageWidth = this.minWidth();
		double imageHeight = this.minHeight();
		if(imageHeight == 0) {
			throw new IllegalStateException("Image has height of zero.");
		}
		double aspectRatio = imageWidth / imageHeight;
		int heightPixels = (int) Math.round(Math.sqrt(numPixels / aspectRatio));
		int widthPixels = (int) Math.round((double) numPixels / heightPixels);
		return simulation.produceModelImages(timestamps, peakFraction, widthPixels, heightPixels, noiseSd, timestampPrefix);
	}
	
	private void drawDepiction(BufferedImage image, Rectangle2D stellarImageRectangle, Rectangle2D transitBounds, boolean allowExtrapolation) {
		double transitFromX = transitBounds.getX();
		double transitFromY = transitBounds.getY();
		double transitToX = transitFromX + transitBounds.getWidth();
		double transitToY = transitFromY + transitBounds.getHeight();
		double fromX = stellarImageRectangle.getX();
		double fromY = stellarImageRectangle.getY();
		double toY = fromY + stellarImageRectangle.getHeight();
		int numColumns = image.getWidth();
		int numRows = image.getHeight();
		double xf = stellarImageRectangle.getWidth() / numColumns;
		double yf = stellarImageRectangle.getHeight() / numRows;
		Graphics2D g = image.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, numColumns, numRows);
			double diameter = numColumns * 2.0 / stellarImageRectangle.getWidth();
			double centerX = numColumns / 2.0;
			double centerY = numRows / 2.0;
			int sx = (int) Math.floor(centerX - diameter / 2);
			int sy = (int) Math.floor(centerY - diameter / 2);
			int sw = (int) Math.round(diameter);
			int sh = sw;
			g.setColor(Color.BLUE);
			g.drawOval(sx, sy, sw, sh);
			FluxOrOpacityFunction fof = this.brightnessFunction;
			for(int c = 0; c < numColumns; c++) {
				double x = fromX + (c + 0.5) * xf;
				if(allowExtrapolation || x >= transitFromX && x < transitToX) {
					for(int r = 0; r < numRows; r++) {
						double y = toY - (r + 0.5) * yf;
						if(allowExtrapolation || y >= transitFromY && y < transitToY) {
							double b = fof.fluxOrOpacity(x, y, 1.0); 
							if(b >= -1) {
								Color color = this.getColor(b);
								g.setColor(color);
								g.drawLine(c, r, c, r);
							}
						}					
					}
				}
			}		
		} finally {
			g.dispose();
		}
	}
	
	private Color getColor(double fluxOrOpacity) {
		int alpha = 0, red = 0, green = 0, blue = 0;
		if(fluxOrOpacity >= 0) {
			red = (int) Math.round(fluxOrOpacity * 255.0);
			alpha = 255;
		}
		else {
			alpha = (int) Math.round((1.0 - (-fluxOrOpacity)) * 255.0);
		}
		return new Color(red, green, blue, alpha);
	}
}
