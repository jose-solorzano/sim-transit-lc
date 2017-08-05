package jhs.lc.geom;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import jhs.math.util.MathUtil;

public class TransitDepictionProducer {
	private static final double PADDING = 0.2;
	private final TransitFunction brightnessFunction;

	public TransitDepictionProducer(TransitFunction translucenceFunction) {
		super();
		this.brightnessFunction = translucenceFunction;
	}

	public final double minWidth(boolean withStar) {
		Rectangle2D transitBounds = this.brightnessFunction.getBoundingBox();
		if(withStar) {
			return MathUtil.max(2.0, (transitBounds.getX() + transitBounds.getWidth()) * 2, -transitBounds.getX() * 2) + PADDING * 2;
		}
		else {
			return transitBounds.getWidth();
		}
	}
	
	public final double minHeight(boolean withStar) {
		Rectangle2D transitBounds = this.brightnessFunction.getBoundingBox();
		if(withStar) {
			return MathUtil.max(2.0, (transitBounds.getY() + transitBounds.getHeight()) * 2, -transitBounds.getY() * 2) + PADDING * 2;
		}
		else {
			return transitBounds.getHeight();
		}
	}
	
	public BufferedImage produceDepiction(int numPixels, boolean drawStarCircle) {
		double imageWidth = this.minWidth(drawStarCircle);
		double imageHeight = this.minHeight(drawStarCircle);
		if(imageHeight == 0) {
			throw new IllegalStateException("Image has height of zero.");
		}
		double aspectRatio = imageWidth / imageHeight;
		int heightPixels = (int) Math.round(Math.sqrt(numPixels / aspectRatio));
		int widthPixels = (int) Math.round((double) numPixels / heightPixels);
		BufferedImage image = new BufferedImage(widthPixels, heightPixels, BufferedImage.TYPE_INT_ARGB);
		Rectangle2D boundingBox = this.brightnessFunction.getBoundingBox();
		Rectangle2D stellarImageRectangle = drawStarCircle ? 
			new Rectangle2D.Double(-imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight) :
			boundingBox;
		this.drawDepiction(image, stellarImageRectangle, boundingBox, false, drawStarCircle);
		return image;
	}

	private void drawDepiction(BufferedImage image, Rectangle2D stellarImageRectangle, Rectangle2D transitBounds, boolean allowExtrapolation, boolean drawStarCircle) {
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
			if(drawStarCircle) {
				double centerX = numColumns / 2.0;
				double centerY = numRows / 2.0;			
				int sx = (int) Math.floor(centerX - diameter / 2);
				int sy = (int) Math.floor(centerY - diameter / 2);
				int sw = (int) Math.round(diameter);
				int sh = sw;
				g.setColor(Color.BLUE);
				g.drawOval(sx, sy, sw, sh);
			}
			TransitFunction fof = this.brightnessFunction;
			for(int c = 0; c < numColumns; c++) {
				double x = fromX + (c + 0.5) * xf;
				if(allowExtrapolation || x >= transitFromX && x < transitToX) {
					for(int r = 0; r < numRows; r++) {
						double y = toY - (r + 0.5) * yf;
						if(allowExtrapolation || y >= transitFromY && y < transitToY) {
							double b = fof.fluxOrTransmittance(x, y, 1.0); 
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
