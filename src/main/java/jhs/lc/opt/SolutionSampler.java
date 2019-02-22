package jhs.lc.opt;

import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.commons.math.FunctionEvaluationException;

import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.geom.TransitFunction;
import jhs.lc.sims.ImageElementInfo;
import jhs.lc.sims.SimulatedFlux;
import jhs.lc.sims.SimulatedFluxSource;
import jhs.math.classification.ClassificationUtil;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

public class SolutionSampler {
	private static final Logger logger = Logger.getLogger(SolutionSampler.class.getName());
	private static final int MAX_DISP_ITERATIONS = 10;
	private static final double NPF = 20.0;
	private static final double PRECISION = 1E-6;
	private static final double OR_LAMBDA = 0.03;
	
	private static final double MAX_LOGIT = 15.0;
	private static final double LOGIT_FACTOR = 3.0;
	private static final double PENALIZED_OR_PARAM = MAX_LOGIT * 0.7 / LOGIT_FACTOR;
	
	private final Random random;
	private final SimulatedFluxSource fluxSource;
	private final ParametricTransitFunctionSource opacitySource;

	private final double minOrbitRadius;
	private final double maxOrbitRadius;
	
	private double peakFraction = 0.5;

	public SolutionSampler(Random random, SimulatedFluxSource fluxSource, ParametricTransitFunctionSource opacitySource, double minOrbitRadius, double maxOrbitRadius) {
		if(minOrbitRadius <= 1.0) {
			throw new IllegalArgumentException("Invalid minOrbitRadius: " + minOrbitRadius);
		}
		if(maxOrbitRadius < minOrbitRadius) {
			throw new IllegalArgumentException("maxOrbitRadius < minOrbitRadius");
		}
		this.minOrbitRadius = minOrbitRadius;
		this.maxOrbitRadius = maxOrbitRadius;
		this.random = random;
		this.fluxSource = fluxSource;
		this.opacitySource = opacitySource;
	}

	public Random getRandom() {
		return random;
	}
	
	public double getPeakFraction() {
		return peakFraction;
	}

	public void setPeakFraction(double peakFraction) {
		this.peakFraction = peakFraction;
	}
	
	private boolean hasOrbitRadiusParameter() {
		return this.minOrbitRadius != this.maxOrbitRadius;
	}
	
	private int getNumExtraParameters() {
		return this.hasOrbitRadiusParameter() ? 1 : 0;
	}

	public final int getNumParameters() {
		return this.opacitySource.getNumParameters() + this.getNumExtraParameters();
	}
	
	public final double[] solutionAsParameters(Solution solution) {
		ParametricTransitFunctionSource source = this.opacitySource;
		int np = this.getNumParameters();
		int snp = source.getNumParameters();
		double[] solParameters = solution.getOpacityFunctionParameters();
		if(solParameters.length != snp) {
			throw new IllegalStateException();
		}
		double[] parameters = new double[np];
		for(int i = 0; i < snp; i++) {
			double scale = source.getParameterScale(i);
			parameters[i] = solParameters[i] / (scale == 0 ? 1 : scale);
		}
		if(parameters.length > solParameters.length) {
			int extraIndex = solParameters.length;
			parameters[extraIndex++] = this.getOrbitRadiusChangeParameter(solution);
		}
		return parameters;
	}
	
	private double getOrbitRadiusChangeParameter(Solution solution) {
		if(!this.hasOrbitRadiusParameter()) {
			return 0;
		}
		double orbitRadius = solution.getOrbitRadius();
		double p = (orbitRadius - this.minOrbitRadius) / (this.maxOrbitRadius - this.minOrbitRadius);
		if(Double.isNaN(p) || Double.isInfinite(p)) {
			throw new IllegalStateException("orbitRadius: " + orbitRadius);
		}
		if(p <= 0) {
			p = 0;
		}
		else if(p >= 1) {
			p = 1;
		}
		double logit = ClassificationUtil.probabilityToLogit(p);
		if(Double.isNaN(logit)) {
			throw new IllegalStateException("p: " + p);
		}
		if(logit < -MAX_LOGIT) {
			logit = -MAX_LOGIT;
		}
		else if(logit > MAX_LOGIT) {
			logit = MAX_LOGIT;
		}
		return logit / LOGIT_FACTOR;
	}
	
	public final Solution parametersAsSolution(double[] optimizerParameters) {
		if(optimizerParameters.length != this.getNumParameters()) {
			throw new IllegalStateException();
		}
		double[] osParameters = this.opacitySourceParameters(optimizerParameters);
		TransitFunction of = this.opacitySource.getTransitFunction(osParameters);
		double orbitRadius = this.getOrbitRadius(optimizerParameters);
		SimulatedFlux modeledFlux = this.fluxSource.produceModeledFlux(this.peakFraction, of, orbitRadius);
		return new Solution(this.fluxSource, of, orbitRadius, peakFraction, osParameters, modeledFlux);
	}
	
	private int getOrbitRadiusChangeParamIndex() {
		return this.hasOrbitRadiusParameter() ? this.opacitySource.getNumParameters() : -1;		
	}
	
	private double getOrbitRadius(double[] optimizerParameters) {
		double range = this.maxOrbitRadius - this.minOrbitRadius;
		if(range == 0) {
			return this.minOrbitRadius;
		}
		int orcIndex = this.getOrbitRadiusChangeParamIndex();
		double changeParam = optimizerParameters[orcIndex];
		double logit = changeParam * LOGIT_FACTOR;
		return this.minOrbitRadius + ClassificationUtil.logitToProbability(logit) * range;
	}

	private double[] opacitySourceParameters(double[] optimizerParameters) {
		ParametricTransitFunctionSource source = this.opacitySource;
		int np = source.getNumParameters();
		double[] osParameters = new double[np];
		for(int i = 0; i < np; i++) {
			double scale = source.getParameterScale(i);
			osParameters[i] = optimizerParameters[i] * (scale == 0 ? 1 : scale);
		}
		return osParameters;
	}
	
	public final double getExtraParamError(double[] optimizerParameters) {
		int orcIndex = this.getOrbitRadiusChangeParamIndex();
		if(orcIndex == -1) {
			return 0;
		}
		double changeParam = optimizerParameters[orcIndex];
		if(changeParam <= +PENALIZED_OR_PARAM && changeParam >= -PENALIZED_OR_PARAM) {
			return 0;
		}
		double diff = changeParam - (changeParam >= 0 ? +PENALIZED_OR_PARAM : -PENALIZED_OR_PARAM);
		return diff * diff * OR_LAMBDA;		
	}
	
	public double[] minimalChangeThreshold(double[] optimizerParameters, double epsilon) {
		if(optimizerParameters.length == 0) {
			throw new IllegalArgumentException("Zero parameters.");
		}
		ImageState baseImageInfo = this.imageState(optimizerParameters);
		double[] baseVector = new double[optimizerParameters.length];
		DisplacementInfo[] dinfos = new DisplacementInfo[optimizerParameters.length];
		int orci = this.getOrbitRadiusChangeParamIndex();
		double sumDiscrete = 0;
		int numDiscrete = 0;
		for(int i = 0; i < optimizerParameters.length; i++) {
			DisplacementInfo dinfo;
			dinfo = this.varDisplacementInfo(baseImageInfo, optimizerParameters, i, epsilon);
			dinfos[i] = dinfo;			
			baseVector[i] = dinfo.noChangeRange;
			if(dinfo.apparentVariableType == VariableType.DISCRETE && i != orci) {
				numDiscrete++;
				sumDiscrete += dinfo.noChangeRange;
			}
		}
		if(numDiscrete == 0) {
			logger.info("minimalChangeThreshold(): No apparent discrete-effect parameters.");
		}
		else {
			double meanDiscrete = sumDiscrete / numDiscrete;
			for(int i = 0; i < optimizerParameters.length; i++) {
				VariableType vtype = dinfos[i].apparentVariableType;
				if(vtype == VariableType.CONTINUOUS) {
					baseVector[i] = meanDiscrete;
					break;
				}
			}
		}
		return baseVector;
	}
	
	private DisplacementInfo varDisplacementInfo(ImageState baseImageState, double[] optimizerParameters, int varIndex, double epsilon) {
		double[] vector = ArrayUtil.repeat(0.0, optimizerParameters.length);
		vector[varIndex] = +1.0;
		DisplacementInfo posDispInfo = this.vectorDisplacementInfo(baseImageState, optimizerParameters, vector, epsilon);
		vector[varIndex] = -1.0;
		DisplacementInfo negDispInfo = this.vectorDisplacementInfo(baseImageState, optimizerParameters, vector, epsilon);
		double posRange = posDispInfo.noChangeRange;
		double negRange = negDispInfo.noChangeRange;
		
		double totalDisp = Math.abs(posRange) + Math.abs(negRange);
		VariableType apparentVarType = VariableType.combine(posDispInfo.apparentVariableType, negDispInfo.apparentVariableType);
		if(apparentVarType == VariableType.BINARY) {
			totalDisp *= 0.5;
		}
		return new DisplacementInfo(totalDisp, apparentVarType);
	}
	
	private DisplacementInfo vectorDisplacementInfo(ImageState baseImageState, double[] optimizerParameters, double[] vector, double epsilon) {
		double lowerBound = 0;
		double upperBound = Double.POSITIVE_INFINITY;
		boolean foundUnchanged = false;
		boolean foundChanged = false;
		double multiplier = 1.0;
		for(int i = 0; i < MAX_DISP_ITERATIONS; i++) {
			if(Math.abs(upperBound - lowerBound) <= PRECISION) {
				break;
			}
			double factor;
			if(Double.isInfinite(upperBound)) {
				factor = lowerBound + epsilon * multiplier;
				multiplier *= 2;
			}
			else {
				factor = (lowerBound + upperBound) / 2.0;
			}
			double[] newParams = MathUtil.add(optimizerParameters, MathUtil.multiply(vector, factor));
			if(this.changed(baseImageState, newParams)) {				
				foundChanged = true;
				upperBound = factor;
			}
			else {
				foundUnchanged = true;
				lowerBound = factor;
			}
		}
		VariableType apparentVarType;
		if(!foundChanged) {
			apparentVarType = VariableType.NO_EFFECT;
		}
		else if(!foundUnchanged) {
			apparentVarType = VariableType.CONTINUOUS;
		}
		else {
			apparentVarType = VariableType.DISCRETE;
		}		
		double noChangeRange = Double.isInfinite(upperBound) ? 0 : (lowerBound + upperBound) / 2.0;
 		return new DisplacementInfo(noChangeRange, apparentVarType);
	}
	
	private boolean changed(ImageState baseImageState, double[] optimizerParameters) {
		ImageState newImageState = this.imageState(optimizerParameters);
		return !Objects.equals(baseImageState, newImageState);
	}

	private ImageState imageState(double[] optimizerParameters) {
		Solution solution = this.parametersAsSolution(optimizerParameters);
		TransitFunction bf = solution.getBrightnessFunction();
		double orbitRadius = this.getOrbitRadius(optimizerParameters);
		ImageElementInfo imageElementInfo = this.fluxSource.createImageElementInfo(bf, orbitRadius);
		int problemArcPixels = (int) Math.round(this.fluxSource.numPixelsInTimeSpanArc(bf, orbitRadius) * NPF);
		return new ImageState(imageElementInfo, problemArcPixels);
	}

	public EvaluationInfo getEvaluationInfo(double[] fluxArray, Solution solution) throws FunctionEvaluationException {
		SimulatedFlux sf = solution.produceModeledFlux();
		double[] modeledFlux = sf.getFluxArray();
		double w0 = 1, w1 = 0, w2 = 0;
		PrimaryLossFunction matcher = new PrimaryLossFunction(this, fluxArray, w0, w1, w2);
		double mse = MathUtil.euclideanDistanceSquared(fluxArray, modeledFlux) / fluxArray.length;
		double rmse = Math.sqrt(mse);
		double loss = matcher.baseLoss(modeledFlux);
		double fluxLoss = matcher.fluxLoss(modeledFlux);
		double trendLoss = matcher.trendLoss(modeledFlux);
		double trendChangeLoss = matcher.trendChangeLoss(modeledFlux);
		return new EvaluationInfo(rmse, loss, fluxLoss, trendLoss,trendChangeLoss);
	}
	

	private static class DisplacementInfo {
		private final double noChangeRange;
		private final VariableType apparentVariableType;
		
		public DisplacementInfo(double noChangeRange, VariableType apparentVariableType) {
			super();
			this.noChangeRange = noChangeRange;
			this.apparentVariableType = apparentVariableType;
		}

		@Override
		public String toString() {
			return "DI(" + MathUtil.round(noChangeRange, 7) + "," + apparentVariableType + ")";
		}
	}
	
	private static class ImageState {
		private final ImageElementInfo imageElementInfo;
		private final int problemArcPixels;
		
		
		public ImageState(ImageElementInfo imageElementInfo, int problemArcPixels) {
			super();
			this.imageElementInfo = imageElementInfo;
			this.problemArcPixels = problemArcPixels;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((imageElementInfo == null) ? 0 : imageElementInfo.hashCode());
			result = prime * result + problemArcPixels;
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ImageState other = (ImageState) obj;
			if (imageElementInfo == null) {
				if (other.imageElementInfo != null)
					return false;
			} else if (!imageElementInfo.equals(other.imageElementInfo))
				return false;
			if (problemArcPixels != other.problemArcPixels)
				return false;
			return true;
		}
	}
}
