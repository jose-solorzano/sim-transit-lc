package jhs.lc.opt.transits;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.ImageUtil;
import jhs.lc.geom.TransitFunction;
import jhs.math.util.MatrixUtil;

public class MultiImageTransit implements TransitFunction {
	private static final Logger logger = Logger.getLogger(MultiImageTransit.class.getName());
	private static final long serialVersionUID = 1L;
	private final ImageInfo[] images;
	private final Rectangle2D boundingBox;
	private final double extraOptimizerError;

	public MultiImageTransit(ImageInfo[] images, Rectangle2D boundingBox, double extraOptimizerError) {
		this.images = images;
		this.boundingBox = boundingBox;
		this.extraOptimizerError = extraOptimizerError;
	}

	@JsonCreator
	public static MultiImageTransit create(@JsonProperty(value = "images", required = true) ImageSpec[] imageSpecs,
			@JsonProperty(value = "__file_context", required = false) File fileContext,
			@JsonProperty(value = "__do_not_use_01", required = false) double extraOptimizerError) throws IOException {
		if(fileContext == null) {
			logger.warning("File context is null.");
		}
		ImageInfo[] images = getImages(imageSpecs, fileContext);
		Rectangle2D boundingBox = getBoundingBox(images);
		return new MultiImageTransit(images, boundingBox, extraOptimizerError);
	}
	
	public static MultiImageTransit create(ImageInfo[] images, double extraOptimizerError) {
		Rectangle2D boundingBox = getBoundingBox(images);
		return new MultiImageTransit(images, boundingBox, extraOptimizerError);
	}

	@Override
	public final double fluxOrTransmittance(double x, double y, double z) {
		double t = 1.0;
		for (ImageInfo image : this.images) {
			t *= image.getTransmittance(x, y);
		}
		return -t;
	}

	@Override
	public final Rectangle2D getBoundingBox() {
		return this.boundingBox;
	}

	@Override
	public final double getExtraOptimizerError() {
		return this.extraOptimizerError;
	}

	private static Rectangle2D getBoundingBox(ImageInfo[] images) {
		List<Point2D> boundList = new ArrayList<>(); 
		for(ImageInfo image : images) {
			image.populateBounds(boundList);
		}
		return getBoundingBox(boundList);
	}
	
	private static Rectangle2D getBoundingBox(List<Point2D> boundList) {
		if(boundList.isEmpty()) {
			return new Rectangle2D.Double(0, 0, 0, 0);
		}
		double x1 = boundList.stream().mapToDouble(v -> v.getX()).min().orElse(Double.NaN);
		double x2 = boundList.stream().mapToDouble(v -> v.getX()).max().orElse(Double.NaN);
		double y1 = boundList.stream().mapToDouble(v -> v.getY()).min().orElse(Double.NaN);
		double y2 = boundList.stream().mapToDouble(v -> v.getY()).max().orElse(Double.NaN);
		return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
	}
	
	private static ImageInfo[] getImages(ImageSpec[] imageSpecs, File fileContext) throws IOException {
		ImageInfo[] images = new ImageInfo[imageSpecs.length];
		for (int i = 0; i < imageSpecs.length; i++) {
			images[i] = getImage(imageSpecs[i], fileContext);
		}
		return images;
	}

	private static ImageInfo getImage(ImageSpec imageSpec, File fileContext) throws IOException {
		File imageFile;
		if (new File(imageSpec.filePath).exists()) {
			imageFile = new File(imageSpec.filePath);
		} else {
			File parent = fileContext == null ? new File(".") : (fileContext.isDirectory() ? fileContext : fileContext.getParentFile());
			imageFile = new File(parent, imageSpec.filePath);
		}
		if(!imageFile.exists()) {
			throw new IllegalArgumentException("Image file does not exist: " + imageFile);
		}
		BufferedImage image = ImageIO.read(imageFile);
		float[][] transmittanceMatrix = ImageUtil.blackOnWhiteToTransmittanceMatrix(image);
		adjustTransmittance(transmittanceMatrix, imageSpec.getOpacity());
		return getImageInfo(transmittanceMatrix, image.getWidth(), image.getHeight(), 
				imageSpec.getHeight(), imageSpec.getAspectRatio(), imageSpec.getOriginX(), imageSpec.getOriginY(), imageSpec.getTilt());		
	}

	public static ImageInfo getImageInfo(float[][] transmittanceMatrix, int widthPixels, int heightPixels, double imageHeight, double aspectRatio, double originX, double originY, double tilt) {
		double imageWidth = imageHeight * aspectRatio;

		double rotA = Math.cos(tilt);
		double rotB = Math.sin(tilt);
		double wf = widthPixels / imageWidth;
		double hf = heightPixels / imageHeight;
		
		double colA = rotA * wf;
		double colB = rotB * wf;
		double colC = widthPixels / 2.0 - (originX * rotA + originY * rotB) * wf;
		
		double rowA = rotB * hf;
		double rowB = -rotA * hf;
		double rowC = heightPixels / 2.0 + (originX * (-rotB) + originY * rotA) * hf;

		return new ImageInfo(transmittanceMatrix, widthPixels, heightPixels, colA, colB, colC, rowA, rowB, rowC);		
	}
	
	public static void adjustTransmittance(float[][] transmittanceMatrix, double opacityFactor) {
		int numCols = transmittanceMatrix.length;
		for(int x = 0; x < numCols; x++) {
			float[] column = transmittanceMatrix[x];
			int numRows = column.length;
			for(int y = 0; y < numRows;  y++) {
				double cellOpacity = 1.0 - column[y];
				double newOpacity = cellOpacity * opacityFactor;
				column[y] = (float) (1.0 - newOpacity);
			}
		}
	}
	
	public static final class ImageInfo {
		private final float[][] transmittanceMatrix;
		private final int widthPixels, heightPixels;
		private final double colA, colB, colC;
		private final double rowA, rowB, rowC;

		public ImageInfo(float[][] transmittanceMatrix, int widthPixels, int heightPixels, double colA, double colB,
				double colC, double rowA, double rowB, double rowC) {
			super();
			this.transmittanceMatrix = transmittanceMatrix;
			this.widthPixels = widthPixels;
			this.heightPixels = heightPixels;
			this.colA = colA;
			this.colB = colB;
			this.colC = colC;
			this.rowA = rowA;
			this.rowB = rowB;
			this.rowC = rowC;
		}

		public final double getTransmittance(double x, double y) {
			int column = (int) Math.floor(this.colA * x + this.colB * y + this.colC);
			int row = (int) Math.floor(this.rowA * x + this.rowB * y + this.rowC);
			float[][] tm = this.transmittanceMatrix;
			if (column < 0 || column >= tm.length) {
				return 1.0;
			}
			float[] columnArray = tm[column];
			if (row < 0 || row >= columnArray.length) {
				return 1.0;
			}
			return columnArray[row];
			/*
			// TODO these calculations can be included in the transmittance matrix.
			double baseOpacity = 1.0 - columnArray[row];
			double opacity = baseOpacity * this.generalOpacity;
			return 1.0 - opacity;
			*/
		}
		
		public final Point2D getStarPosition(int column, int row) {
			double colA = this.colA;
			double colB = this.colB;
			double colC = this.colC;
			double rowA = this.rowA;
			double rowB = this.rowB;
			double rowC = this.rowC;
			double denominator = colA * rowB - colB * rowA;
			if(denominator == 0) {
				return null;
			}
			double x = (rowB * column - colB * row + colB * rowC - rowB * colC) / denominator;
			double y = (colA * row - rowA * column + rowA * colC - colA * rowC) / denominator;
			return new Point2D.Double(x, y);
		}
		
		public final void populateBounds(List<Point2D> boundList) {
			Point2D upperLeft = this.getStarPosition(0, 0);
			if(upperLeft != null) {
				boundList.add(upperLeft);
			}
			Point2D upperRight = this.getStarPosition(this.widthPixels, 0);
			if(upperRight != null) {
				boundList.add(upperRight);
			}
			Point2D lowerRight = this.getStarPosition(this.widthPixels, this.heightPixels);
			if(lowerRight != null) {
				boundList.add(lowerRight);
			}
			Point2D lowerLeft = this.getStarPosition(0, this.heightPixels);
			if(lowerLeft != null) {
				boundList.add(lowerLeft);
			}
		}
	}	
	
	public static class ImageSpec {
		private String filePath;
		private double tilt;
		private double originX;
		private double originY;
		private double height;
		private double aspectRatio;
		private double opacity;

		public final String getFilePath() {
			return filePath;
		}

		public final void setFilePath(String imageFilePath) {
			this.filePath = imageFilePath;
		}

		public final double getTilt() {
			return tilt;
		}

		public final void setTilt(double rotationAngle) {
			this.tilt = rotationAngle;
		}

		public final double getOriginX() {
			return originX;
		}

		public final void setOriginX(double originX) {
			this.originX = originX;
		}

		public final double getOriginY() {
			return originY;
		}

		public final void setOriginY(double originY) {
			this.originY = originY;
		}

		public final double getAspectRatio() {
			return aspectRatio;
		}

		public final void setAspectRatio(double aspectRatio) {
			this.aspectRatio = aspectRatio;
		}

		public final double getHeight() {
			return height;
		}

		public final void setHeight(double height) {
			this.height = height;
		}

		public final double getOpacity() {
			return opacity;
		}

		public final void setOpacity(double maxOpacity) {
			this.opacity = maxOpacity;
		}
	}
}
