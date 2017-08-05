package jhs.lc.opt.builders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.TransitFunction;
import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.opt.bfunctions.RingedPlanet;
import jhs.lc.opt.params.ParameterSet;
import jhs.math.util.ArrayUtil;

public class RingedPlanetBuilder implements ParametricTransitFunctionSource {
	private static final double LAMBDA = 0.1;
	private final int numRings;
	private final ParameterSet<ParamId> parameterSet;

	protected RingedPlanetBuilder(int numRings, ParameterSet<ParamId> parameterSet) {
		super();
		this.numRings = numRings;
		this.parameterSet = parameterSet;
	}

	@JsonCreator
	public static RingedPlanetBuilder create(
			@JsonProperty(value="numRings", required=true) int numRings,
			@JsonProperty(value="originXBounds", required=true) double[] originXBounds,
			@JsonProperty(value="impactParameterBounds", required=true) double[] impactParameterBounds,
			@JsonProperty(value="tiltBounds", required=true) double[] tiltBounds,
			@JsonProperty(value="obliquityBounds", required=true) double[] obliquityBounds,
			@JsonProperty(value="planetRadiusBounds", required=true) double[] planetRadiusBounds,
			@JsonProperty(value="planetRingGapBounds", required=true) double[] planetRingGapBounds,
			@JsonProperty(value="ringWidthBounds", required=true) double[] ringWidthBounds,
			@JsonProperty(value="ringGapBounds", required=true) double[] ringGapBounds,
			@JsonProperty(value="ringOpticalDepthBounds", required=true) double[] ringOpticalDepthBounds
		) {
		
		ParameterSet<ParamId> ps = new ParameterSet<>(ParamId.values());
		
		ps.addParameterDef(ParamId.ORIGIN_X, originXBounds);
		ps.addParameterDef(ParamId.IMPACT_PARAMETER, impactParameterBounds);
		ps.addParameterDef(ParamId.TILT, tiltBounds);
		ps.addParameterDef(ParamId.OBLIQUITY, obliquityBounds);
		ps.addParameterDef(ParamId.PLANET_RADIUS, planetRadiusBounds);
		ps.addParameterDef(ParamId.PLANET_RING_GAP, planetRingGapBounds);
		ps.addParameterDef(ParamId.RING_WIDTH, ringWidthBounds);
		ps.addParameterDef(ParamId.RING_GAP, ringGapBounds);
		
		ps.addMultiParameterDef(ParamId.RING_OPTICAL_DEPTHS, numRings, ringGapBounds);
		
		if(ps.getNumParameters() != ParamId.values().length) {
			throw new IllegalStateException();
		}
		
		return new RingedPlanetBuilder(numRings, ps);
	}
	
	@Override
	public final TransitFunction getTransitFunction(double[] parameters) {
		ParameterSet<ParamId> ps = this.parameterSet;
		double originX = ps.getValue(ParamId.ORIGIN_X, parameters);
		double impactParameter = ps.getValue(ParamId.IMPACT_PARAMETER, parameters);
		double tilt = ps.getValue(ParamId.TILT, parameters);
		double obliquity = ps.getValue(ParamId.OBLIQUITY, parameters);
		double planetRadius = ps.getValue(ParamId.PLANET_RADIUS, parameters);
		double planetRingGap = ps.getValue(ParamId.PLANET_RING_GAP, parameters);
		double ringWidth = ps.getValue(ParamId.RING_WIDTH, parameters);
		double ringGap = ps.getValue(ParamId.RING_GAP, parameters);
		double[] ringOpticalDepths = ps.getValues(ParamId.RING_OPTICAL_DEPTHS, parameters);
		double[] ringOpacitiesAtMaxObliquity = ArrayUtil.map(ringOpticalDepths, od -> 1.0 - Math.exp(-od));
		
		double extraOptimizerError = ps.getExtraOptimizerError(parameters, LAMBDA);
		
		return RingedPlanet.create(numRings, originX, impactParameter, tilt, obliquity, planetRadius, planetRingGap, 
				ringWidth, ringGap, ringOpacitiesAtMaxObliquity, extraOptimizerError);
	}

	@Override
	public final int getNumParameters() {
		return this.parameterSet.getNumParameters();
	}

	@Override
	public final double getParameterScale(int paramIndex) {
		// Bounds scaling taken care of by ParameterSet.
		return 1.0;
	}
	
	private static enum ParamId {
		ORIGIN_X,
		IMPACT_PARAMETER,
		TILT,
		OBLIQUITY,		
		PLANET_RADIUS,
		PLANET_RING_GAP,
		RING_WIDTH,
		RING_GAP,
		RING_OPTICAL_DEPTHS
	}
}
