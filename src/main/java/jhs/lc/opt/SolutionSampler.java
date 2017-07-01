package jhs.lc.opt;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.Port.Info;

import org.apache.commons.math.MathException;

import jhs.lc.data.LightCurve;
import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.sims.AngularSimulation;
import jhs.lc.sims.ImageElementInfo;
import jhs.lc.sims.SimulatedFlux;
import jhs.lc.sims.SimulatedFluxSource;
import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

public class SolutionSampler {
	private static final Logger logger = Logger.getLogger(SolutionSampler.class.getName());
	private static final int NUM_EXTRA_PARAMS = 1;
	private static final double WF = 3.0;
	private static final int WL = 11;
	
	private final Random random;
	private final double baseOrbitRadius;
	private final double logRadiusSD;
	private final SimulatedFluxSource fluxSource;
	private final ParametricFluxFunctionSource opacitySource;
	
	private double numMutateParamsFraction = 0.20;
	private double mutateSD = 0.03;
	private double peakFraction = 0.5;

	public SolutionSampler(Random random, double baseRadius, double logRadiusSD,
			SimulatedFluxSource fluxSource, ParametricFluxFunctionSource opacitySource) {
		if(baseRadius <= 1.0) {
			throw new IllegalArgumentException("Invalid baseRadius: " + baseRadius);
		}
		this.random = random;
		this.baseOrbitRadius = baseRadius;
		this.logRadiusSD = logRadiusSD;
		this.fluxSource = fluxSource;
		this.opacitySource = opacitySource;
	}

	public final double getNumMutateParamsFraction() {
		return numMutateParamsFraction;
	}

	public final void setNumMutateParamsFraction(double numMutateParamsFraction) {
		this.numMutateParamsFraction = numMutateParamsFraction;
	}

	public final double getMutateSD() {
		return mutateSD;
	}

	public final void setMutateSD(double mutateSD) {
		this.mutateSD = mutateSD;
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

	public double[] createFluxWeights(double[] fluxArray) {
		if(fluxArray.length < 2) {
			throw new IllegalArgumentException("Length of flux array must be at least 2.");
		}
		double[] trendChangeProfile = LightCurve.trendChangeProfile(fluxArray, WL);
		double[] weights = MathUtil.abs(trendChangeProfile);
		double mean = MathUtil.mean(weights);
		MathUtil.addInPlace(weights, mean * WF);
		return weights;
	}

	public final Solution sample() {
		double[] osParameters = this.sampleOpacityFunctionParameters();
		FluxOrOpacityFunction of = this.opacitySource.getFluxOrOpacityFunction(osParameters);
		double orbitRadius = this.baseOrbitRadius * Math.exp(this.random.nextGaussian() * this.logRadiusSD);
		SimulatedFlux modeledFlux = this.fluxSource.produceModeledFlux(this.peakFraction, of, orbitRadius);
		return new Solution(this.fluxSource, of, orbitRadius, peakFraction, osParameters, modeledFlux);
	}
	
	public final int getNumParameters() {
		return this.opacitySource.getNumParameters() + NUM_EXTRA_PARAMS;
	}
	
	public final double[] solutionAsParameters(Solution solution) {
		ParametricFluxFunctionSource source = this.opacitySource;
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
		int extraIndex = solParameters.length;
		parameters[extraIndex++] = this.getOrbitRadiusChangeParameter(solution);
		return parameters;
	}
	
	private double getOrbitRadiusChangeParameter(Solution solution) {
		double orbitRadius = solution.getOrbitRadius();
		if(orbitRadius == 0) {
			throw new IllegalStateException("Simulation orbit radius is zero.");
		}
		double logDiff = Math.log(orbitRadius / this.baseOrbitRadius);
		return logDiff / this.logRadiusSD;
	}
	
	public final Solution parametersAsSolution(double[] optimizerParameters) {
		if(optimizerParameters.length != this.getNumParameters()) {
			throw new IllegalStateException();
		}
		double[] osParameters = this.opacitySourceParameters(optimizerParameters);
		FluxOrOpacityFunction of = this.opacitySource.getFluxOrOpacityFunction(osParameters);
		double orbitRadius = this.getOrbitRadius(optimizerParameters);
		SimulatedFlux modeledFlux = this.fluxSource.produceModeledFlux(this.peakFraction, of, orbitRadius);
		return new Solution(this.fluxSource, of, orbitRadius, peakFraction, osParameters, modeledFlux);
	}
	
	private int getOrbitRadiusChangeParamIndex() {
		return this.opacitySource.getNumParameters();		
	}
	
	private double getOrbitRadius(double[] optimizerParameters) {
		int orcIndex = this.getOrbitRadiusChangeParamIndex();
		double changeParam = optimizerParameters[orcIndex];
		double logDiff = changeParam * this.logRadiusSD;
		return this.baseOrbitRadius * Math.exp(logDiff);
	}

	public final double sampleParameter(int paramIndex) {
		return this.random.nextGaussian();
	}
	
	private double[] opacitySourceParameters(double[] optimizerParameters) {
		ParametricFluxFunctionSource source = this.opacitySource;
		int np = source.getNumParameters();
		double[] osParameters = new double[np];
		for(int i = 0; i < np; i++) {
			double scale = source.getParameterScale(i);
			osParameters[i] = optimizerParameters[i] * (scale == 0 ? 1 : scale);
		}
		return osParameters;
	}
	
	private double[] sampleOpacityFunctionParameters() {
		ParametricFluxFunctionSource source = this.opacitySource;
		Random r = this.random;
		int n = source.getNumParameters();
		double[] parameters = new double[n];
		for(int i = 0; i < n; i++) {
			double scale = source.getParameterScale(i);
			parameters[i] = r.nextGaussian() * scale;
		}
		return parameters;
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
		MathUtil.divideInPlace(baseVector, Math.sqrt(optimizerParameters.length));
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
	
	private static final double PRECISION = 1E-6;
	private static final int MAX_DISP_ITERATIONS = 10;
	
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
		FluxOrOpacityFunction bf = solution.getBrightnessFunction();
		ImageElementInfo imageElementInfo = this.fluxSource.createImageElementInfo(bf);
		double orbitRadius = this.getOrbitRadius(optimizerParameters);
		int problemArcPixels = (int) Math.round(this.fluxSource.numPixelsInTimeSpanArc(bf, orbitRadius));
		return new ImageState(imageElementInfo, problemArcPixels);
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
