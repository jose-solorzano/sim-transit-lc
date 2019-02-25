package jhs.lc.opt.transits;

import java.awt.geom.Rectangle2D;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.TransitFunction;

public final class GradientRingPlanet implements TransitFunction {
	private static final long serialVersionUID = 1L;
	private static final double LN_HALF = Math.log(0.5);
	
	private final double originX, originY;
	private final double sinTilt, cosTilt;
	private final double sinObliquity;
	private final double planetRadiusSquared;
	private final double ringInnerRadiusSquared;
	private final double ringInnerRadius;
	private final double ringOuterRadiusSquared;
	private final double maxOpacity;
	private final double extraOptimizationError;
	private final double decayCoefficient;
	private final DecayType decayType;
	
	public GradientRingPlanet(double originX, double originY, double tilt, double obliquity,
			double planetRadius, double innerRingRadius, double outerRingRadius,
			double halfOpacityRadius, double maxOpacity, double extraOptimizationError, DecayType decayType) {
		super();
		this.originX = originX;
		this.originY = originY;
		this.sinTilt = StrictMath.sin(tilt);
		this.cosTilt = StrictMath.cos(tilt);
		this.sinObliquity = StrictMath.sin(obliquity);
		this.planetRadiusSquared = planetRadius * planetRadius;
		this.ringInnerRadius = innerRingRadius;
		this.ringInnerRadiusSquared = innerRingRadius * innerRingRadius;
		this.ringOuterRadiusSquared = outerRingRadius * outerRingRadius;
		this.maxOpacity = maxOpacity;
		this.extraOptimizationError = extraOptimizationError;
		this.decayType = decayType;
		
		double diff = halfOpacityRadius - innerRingRadius;
		switch(decayType) {
		case LINEAR:
			this.decayCoefficient = diff <= 0 ? 0 : (maxOpacity - maxOpacity / 2) / diff;
			break;
		case MONOD:
			this.decayCoefficient = diff <= 0 ? 0 : diff;
			break;
		case EXPONENTIAL:
			this.decayCoefficient = diff <= 0 ? 0 : LN_HALF / (-diff);
			break;
		default:
			throw new IllegalArgumentException("decayType: " + decayType);
		}		
	}

	@JsonCreator
	public static GradientRingPlanet create(
		@JsonProperty("originX") double originX,	
		@JsonProperty("impactParameter") double impactParameter,
		@JsonProperty("tilt") double tilt,
		@JsonProperty(value="obliquity", required=false) double obliquity,
		@JsonProperty(value="planetRadius", required=true) double planetRadius,
		@JsonProperty(value="ringInnerRadius", required=true) double ringInnerRadius,
		@JsonProperty(value="ringOuterRadius", required=true) double ringOuterRadius,
		@JsonProperty(value="halfOpacityRadius", required=true) double halfOpacityRadius,
		@JsonProperty(value="maxRingOpacity", required=true) double maxRingOpacity,		
		@JsonProperty(value="decayType", required=true) DecayType decayType,
		@JsonProperty("__do_not_use_01") double extraOptimizerError) {
		
		double originY = -impactParameter;
		return new GradientRingPlanet(originX, originY, tilt, obliquity, planetRadius, 
				ringInnerRadius, ringOuterRadius, halfOpacityRadius, maxRingOpacity, extraOptimizerError, decayType);
	}

	/**
	 * Returns the negative transmittance at the given point, or -(1 - opacity).
	 */
	@Override
	public final double fluxOrTransmittance(double x, double y, double z) {
		double xdiff = x - this.originX;
		double ydiff = y - this.originY;
		double rs = xdiff * xdiff + ydiff * ydiff;
		if(rs <= this.planetRadiusSquared) {
			return 0;
		}
		double rotA = this.cosTilt;
		double rotB = this.sinTilt;
		double xr = xdiff * rotA + ydiff * rotB;
		double yr = xdiff * (-rotB) + ydiff * rotA;
		double sinOb = this.sinObliquity;
		if(sinOb == 0) {
			return Double.NaN;
		}
		yr /= sinOb;
		double rrs = xr * xr + yr * yr;
		if(rrs < this.ringInnerRadiusSquared) {
			return Double.NaN;
		}
		if(rrs > this.ringOuterRadiusSquared) {
			return Double.NaN;
		}
		double distanceFromInner = StrictMath.sqrt(rrs) - this.ringInnerRadius;
		double coeff = this.decayCoefficient;
		double opacity;
		if(coeff <= 0) {
			opacity = this.maxOpacity;
		}
		else {
			switch(this.decayType) {
			case LINEAR:
				opacity = this.maxOpacity - coeff * distanceFromInner;
				break;
			case MONOD:
				opacity = this.maxOpacity * coeff / (coeff + distanceFromInner);
				break;
			case EXPONENTIAL:
				opacity = this.maxOpacity * Math.exp(-coeff * distanceFromInner);
				break;
			default:
				throw new IllegalStateException("DecayType unknown: " + this.decayType);
			}
		}
		if(opacity < 0) {
			opacity = 0;
		}
		return -(1 - opacity);
	}

	@Override
	public final Rectangle2D getBoundingBox() {
		double planetRadius = Math.sqrt(this.planetRadiusSquared);
		double originX = this.originX;
		double originY = this.originY;
		double outerRadius = Math.max(planetRadius, Math.sqrt(this.ringOuterRadiusSquared));				
		double fromX = originX - outerRadius;
		double fromY = originY - outerRadius;
		double toX = originX + outerRadius;
		double toY = originY + outerRadius;
		if(fromY < -1.0) {
			fromY = -1.0;
		}
		if(toY > +1.0) {
			toY = +1.0;
		}
		return new Rectangle2D.Double(fromX, fromY, toX - fromX, toY - fromY);
	}

	@Override
	public final double getExtraOptimizerError() {
		return this.extraOptimizationError;
	}

	@Override
	public String toString() {
		return "GradientRingPlanet [originX=" + originX + ", originY=" + originY + ", sinTilt=" + sinTilt + ", cosTilt="
				+ cosTilt + ", sinObliquity=" + sinObliquity + ", planetRadiusSquared=" + planetRadiusSquared
				+ ", ringInnerRadiusSquared=" + ringInnerRadiusSquared + ", ringInnerRadius=" + ringInnerRadius
				+ ", ringOuterRadiusSquared=" + ringOuterRadiusSquared + ", maxOpacity=" + maxOpacity
				+ ", extraOptimizationError=" + extraOptimizationError + ", decayCoefficient=" + decayCoefficient
				+ ", decayType=" + decayType + "]";
	}
}
