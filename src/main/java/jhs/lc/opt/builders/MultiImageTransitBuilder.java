package jhs.lc.opt.builders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.ImageUtil;
import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.geom.TransitFunction;
import jhs.lc.opt.params.ParameterSet;
import jhs.lc.opt.transits.DecayType;
import jhs.lc.opt.transits.MultiImageTransit;
import jhs.lc.opt.transits.MultiImageTransit.ImageInfo;
import jhs.math.util.MatrixUtil;

public class MultiImageTransitBuilder implements ParametricTransitFunctionSource {	
	private final ParameterSet<String> parameterSet;
	private final ResolvedImageSpec[] resolvedImageSpecs;
	private final double lambda;
	
	public MultiImageTransitBuilder(ParameterSet<String> parameterSet, ResolvedImageSpec[] resolvedImageSpecs, double lambda) {
		this.parameterSet = parameterSet;
		this.resolvedImageSpecs = resolvedImageSpecs;
		this.lambda = lambda;
	}

	@JsonCreator
	public static MultiImageTransitBuilder create(
			@JsonProperty(value="parameters", required=true) Map<String, double[]> parameters,
			@JsonProperty(value="images", required=true) BuilderImageSpec[] imageSpecs,
			@JsonProperty(value="lambda", required=false) double lambda,
			@JsonProperty(value = "__file_context", required = false) File fileContext
		) throws IOException {		
		ParameterSet<String> parameterSet = new ParameterSet<>(parameters.size());
		for(Map.Entry<String, double[]> paramSpec : parameters.entrySet()) {
			String paramId = paramSpec.getKey();
			double[] bounds = paramSpec.getValue();
			parameterSet.addParameterDef(paramId, bounds);
		}
		if(parameterSet.getNumParameters() != parameterSet.getUniqueParameterCount()) {
			throw new IllegalStateException("There are duplicate parameters names.");
		}		
		ResolvedImageSpec[] resolvedImageSpecs = resolveImageSpecs(imageSpecs, fileContext, parameterSet);
		return new MultiImageTransitBuilder(parameterSet, resolvedImageSpecs, lambda);
	}
	
	private static ResolvedImageSpec[] resolveImageSpecs(BuilderImageSpec[] imageSpecs, File fileContext, ParameterSet<String> parameterSet) throws IOException {
		int length = imageSpecs.length;
		ResolvedImageSpec[] result = new ResolvedImageSpec[length];
		for(int i = 0; i < length; i++) {
			result[i] = ResolvedImageSpec.fromImageSpec(imageSpecs[i], fileContext, parameterSet);
		}
		return result;
	}

	@Override
	public final TransitFunction getTransitFunction(double[] parameters) {		
		double extraOptimizerError = this.parameterSet.getExtraOptimizerError(parameters, this.lambda);
		ResolvedImageSpec[] ris = this.resolvedImageSpecs;
		ImageInfo[] images = new ImageInfo[ris.length];
		for(int i = 0; i < ris.length; i++) {
			images[i] = this.getImage(ris[i], parameters);
		}
		return MultiImageTransit.create(images, extraOptimizerError);
	}

	@Override
	public final int getNumParameters() {
		return this.parameterSet.getNumParameters();
	}

	@Override
	public final double getParameterScale(int paramIndex) {
		return 1.0;
	}
	
	private ImageInfo getImage(ResolvedImageSpec resolvedImageSpec, double[] parameters) {
		double opacityFactor = resolvedImageSpec.opacity.getValue(parameters);
		double imageHeight = resolvedImageSpec.height.getValue(parameters);
		double aspectRatio = resolvedImageSpec.aspectRatio.getValue(parameters);
		double originX = resolvedImageSpec.originX.getValue(parameters);
		double originY = resolvedImageSpec.originY.getValue(parameters);
		double tilt = resolvedImageSpec.tilt.getValue(parameters);

		float[][] baseTransmittanceMatrix = resolvedImageSpec.transmittanceMatrix;
		float[][] transmittanceMatrix = MatrixUtil.copyOf(baseTransmittanceMatrix);
		MultiImageTransit.adjustTransmittance(transmittanceMatrix, opacityFactor);
		return MultiImageTransit.getImageInfo(transmittanceMatrix, 
				resolvedImageSpec.widthPixels, resolvedImageSpec.heightPixels, imageHeight, aspectRatio, originX, originY, tilt);
	}
	
	private static class ResolvedImageSpec {
		private final float[][] transmittanceMatrix;
		private final int widthPixels, heightPixels;
		private final ValueInfo tilt;
		private final ValueInfo originX;
		private final ValueInfo originY;
		private final ValueInfo height;
		private final ValueInfo aspectRatio;
		private final ValueInfo opacity;
		
		public ResolvedImageSpec(float[][] transmittanceMatrix, int widthPixels, int heightPixels, ValueInfo tilt,
				ValueInfo originX, ValueInfo originY, ValueInfo height, ValueInfo aspectRatio, ValueInfo opacity) {
			super();
			this.transmittanceMatrix = transmittanceMatrix;
			this.widthPixels = widthPixels;
			this.heightPixels = heightPixels;
			this.tilt = tilt;
			this.originX = originX;
			this.originY = originY;
			this.height = height;
			this.aspectRatio = aspectRatio;
			this.opacity = opacity;
		}

		public static ResolvedImageSpec fromImageSpec(BuilderImageSpec imageSpec, File fileContext, ParameterSet<String> parameterSet) throws IOException {
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
			ValueInfo tilt = getValueInfo(imageSpec.getTilt(), parameterSet);
			ValueInfo originX = getValueInfo(imageSpec.getOriginX(), parameterSet);
			ValueInfo originY = getValueInfo(imageSpec.getOriginY(), parameterSet);
			ValueInfo height = getValueInfo(imageSpec.getHeight(), parameterSet);
			ValueInfo aspectRatio = getValueInfo(imageSpec.getAspectRatio(), parameterSet);
			ValueInfo opacity = getValueInfo(imageSpec.getOpacity(), parameterSet);
			return new ResolvedImageSpec(transmittanceMatrix, image.getWidth(), image.getHeight(), tilt, originX, originY, height, aspectRatio, opacity);
		}
		
		private static ValueInfo getValueInfo(String valueText, ParameterSet<String> parameterSet) {
			try {
				double value = Double.parseDouble(valueText);
				return new SimpleValueInfo(value);
			} catch(NumberFormatException nfe) {
				int paramIndex = parameterSet.getParameterIndex(valueText);
				return new ParamValueInfo(paramIndex);
			}
		}
	}
	
	private static interface ValueInfo {
		public double getValue(double[] parameters);
	}
	
	private static final class SimpleValueInfo implements ValueInfo {
		private final double value;

		public SimpleValueInfo(double value) {
			super();
			this.value = value;
		}

		public double getValue(double[] parameters) {
			return this.value;
		}
	}
	
	private static final class ParamValueInfo implements ValueInfo {
		private final int paramIndex;

		public ParamValueInfo(int paramIndex) {
			super();
			this.paramIndex = paramIndex;
		}

		public double getValue(double[] parameters) {
			return parameters[this.paramIndex];
		}
	}	
	
	public static class BuilderImageSpec {
		private String filePath;
		private String tilt;
		private String originX;
		private String originY;
		private String height;
		private String aspectRatio;
		private String opacity;

		public final String getFilePath() {
			return filePath;
		}

		public final void setFilePath(String filePath) {
			this.filePath = filePath;
		}

		public final String getTilt() {
			return tilt;
		}

		public final void setTilt(String tilt) {
			this.tilt = tilt;
		}

		public final String getOriginX() {
			return originX;
		}

		public final void setOriginX(String originX) {
			this.originX = originX;
		}

		public final String getOriginY() {
			return originY;
		}

		public final void setOriginY(String originY) {
			this.originY = originY;
		}

		public final String getHeight() {
			return height;
		}

		public final void setHeight(String height) {
			this.height = height;
		}

		public final String getAspectRatio() {
			return aspectRatio;
		}

		public final void setAspectRatio(String aspectRatio) {
			this.aspectRatio = aspectRatio;
		}

		public final String getOpacity() {
			return opacity;
		}

		public final void setOpacity(String opacity) {
			this.opacity = opacity;
		}
	}
}
