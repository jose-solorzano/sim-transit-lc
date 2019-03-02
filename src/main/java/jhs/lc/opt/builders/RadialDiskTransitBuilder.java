package jhs.lc.opt.builders;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.geom.TransitFunction;
import jhs.lc.opt.params.ParameterSet;
import jhs.lc.opt.transits.RadialDiskTransit;
import jhs.lc.opt.transits.RingedPlanet;
import jhs.math.util.ArrayUtil;

public class RadialDiskTransitBuilder implements ParametricTransitFunctionSource {
	private static final double LAMBDA = 0.1;
	private final ParameterSet<ParamId> parameterSet;

	protected RadialDiskTransitBuilder(ParameterSet<ParamId> parameterSet) {
		this.parameterSet = parameterSet;
	}

	@JsonCreator
	public static RadialDiskTransitBuilder create(
			@JsonProperty(value="originXBounds", required=true) double[] originXBounds,
			@JsonProperty(value="impactParameterBounds", required=true) double[] impactParameterBounds,
			@JsonProperty(value="tiltBounds", required=true) double[] tiltBounds,
			@JsonProperty(value="obliquityBounds", required=true) double[] obliquityBounds,
			@JsonProperty(value="diskRadiusBounds", required=true) double[] diskRadiusBounds,
			@JsonProperty(value="decayCoefficientBounds", required=true) double[] decayCoefficientBounds
		) {
		
		ParameterSet<ParamId> ps = new ParameterSet<>(ParamId.values());
		
		ps.addParameterDef(ParamId.ORIGIN_X, originXBounds);
		ps.addParameterDef(ParamId.IMPACT_PARAMETER, impactParameterBounds);
		ps.addParameterDef(ParamId.TILT, tiltBounds);
		ps.addParameterDef(ParamId.OBLIQUITY, obliquityBounds);
		ps.addParameterDef(ParamId.DECAY_COEFFICIENT, decayCoefficientBounds);
		ps.addParameterDef(ParamId.DISK_RADIUS, diskRadiusBounds);
				
		return new RadialDiskTransitBuilder(ps);
	}
	
	@Override
	public final TransitFunction getTransitFunction(double[] parameters) {
		ParameterSet<ParamId> ps = this.parameterSet;
		double originX = ps.getValue(ParamId.ORIGIN_X, parameters);
		double impactParameter = ps.getValue(ParamId.IMPACT_PARAMETER, parameters);
		double tilt = ps.getValue(ParamId.TILT, parameters);
		double obliquity = ps.getValue(ParamId.OBLIQUITY, parameters);
		double diskRadius = ps.getValue(ParamId.DISK_RADIUS, parameters);
		double decayCoefficient = ps.getValue(ParamId.DECAY_COEFFICIENT, parameters);
		
		double extraOptimizerError = ps.getExtraOptimizerError(parameters, LAMBDA);
		
		return RadialDiskTransit.create(originX, impactParameter, tilt, obliquity, diskRadius, decayCoefficient, extraOptimizerError);
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
		DISK_RADIUS,
		DECAY_COEFFICIENT,
	}
}
