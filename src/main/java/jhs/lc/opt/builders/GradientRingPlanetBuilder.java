package jhs.lc.opt.builders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.geom.TransitFunction;
import jhs.lc.opt.params.ParameterSet;
import jhs.lc.opt.transits.DecayType;
import jhs.lc.opt.transits.GradientRingPlanet;
import jhs.lc.opt.transits.RingedPlanet;
import jhs.math.util.ArrayUtil;

public class GradientRingPlanetBuilder implements ParametricTransitFunctionSource {
	private static final double LAMBDA = 0.1;
	private final ParameterSet<ParamId> parameterSet;
	private final DecayType decayType;
	
	public GradientRingPlanetBuilder(ParameterSet<ParamId> parameterSet, DecayType decayType) {
		super();
		this.parameterSet = parameterSet;
		this.decayType = decayType;
	}

	@JsonCreator
	public static GradientRingPlanetBuilder create(
			@JsonProperty(value="decayType", required=true) DecayType decayType,
			@JsonProperty(value="originXBounds", required=true) double[] originXBounds,
			@JsonProperty(value="impactParameterBounds", required=true) double[] impactParameterBounds,
			@JsonProperty(value="tiltBounds", required=true) double[] tiltBounds,
			@JsonProperty(value="obliquityBounds", required=true) double[] obliquityBounds,
			@JsonProperty(value="planetRadiusBounds", required=true) double[] planetRadiusBounds,
			@JsonProperty(value="ringInnerRadiusBounds", required=true) double[] ringInnerRadiusBounds,
			@JsonProperty(value="ringOuterRadiusBounds", required=true) double[] ringOuterRadiusBounds,
			@JsonProperty(value="ringHalfOpacityRadiusBounds", required=true) double[] ringHalfOpacityRadiusBounds,
			@JsonProperty(value="ringMaxOpacityBounds", required=true) double[] maxOpacityBounds
		) {
		
		ParameterSet<ParamId> ps = new ParameterSet<>(ParamId.values());
		
		ps.addParameterDef(ParamId.ORIGIN_X, originXBounds);
		ps.addParameterDef(ParamId.IMPACT_PARAMETER, impactParameterBounds);
		ps.addParameterDef(ParamId.TILT, tiltBounds);
		ps.addParameterDef(ParamId.OBLIQUITY, obliquityBounds);
		ps.addParameterDef(ParamId.PLANET_RADIUS, planetRadiusBounds);
		ps.addParameterDef(ParamId.RING_INNER_RADIUS, ringInnerRadiusBounds);
		ps.addParameterDef(ParamId.RING_OUTER_RADIUS, ringOuterRadiusBounds);
		ps.addParameterDef(ParamId.RING_HALF_OPACITY_RADIUS, ringHalfOpacityRadiusBounds);
		ps.addParameterDef(ParamId.MAX_OPACITY, maxOpacityBounds);

		return new GradientRingPlanetBuilder(ps, decayType);
	}
	
	@Override
	public final TransitFunction getTransitFunction(double[] parameters) {
		ParameterSet<ParamId> ps = this.parameterSet;
		double originX = ps.getValue(ParamId.ORIGIN_X, parameters);
		double impactParameter = ps.getValue(ParamId.IMPACT_PARAMETER, parameters);
		double tilt = ps.getValue(ParamId.TILT, parameters);
		double obliquity = ps.getValue(ParamId.OBLIQUITY, parameters);
		double planetRadius = ps.getValue(ParamId.PLANET_RADIUS, parameters);
		double ringInnerRadius = ps.getValue(ParamId.RING_INNER_RADIUS, parameters);
		double ringOuterRadius = ps.getValue(ParamId.RING_OUTER_RADIUS, parameters);
		double halfOpacityRadius = ps.getValue(ParamId.RING_HALF_OPACITY_RADIUS, parameters);
		double maxRingOpacity = ps.getValue(ParamId.MAX_OPACITY, parameters);
		
		double extraOptimizerError = ps.getExtraOptimizerError(parameters, LAMBDA);
		
		return GradientRingPlanet.create(originX, impactParameter, tilt, obliquity, planetRadius, ringInnerRadius, ringOuterRadius, 
				halfOpacityRadius, maxRingOpacity, this.decayType, extraOptimizerError);
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
		RING_INNER_RADIUS,
		RING_OUTER_RADIUS,
		RING_HALF_OPACITY_RADIUS,
		MAX_OPACITY
	}
}
