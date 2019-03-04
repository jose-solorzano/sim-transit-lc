package jhs.lc.opt.transits;

import java.awt.geom.Rectangle2D;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.TransitFunction;

public final class RadialDiskTransit implements TransitFunction {
	private static final long serialVersionUID = 1L;
	
	private final double originX, originY;
	private final double sinTilt, cosTilt;
	private final double sinObliquity;
	private final double diskRadiusSquared;
	private final double planetRadiusSquared;
	private final double decayCoefficient;
	private final double extraOptimizationError;
	
	public RadialDiskTransit(double originX, double originY, double tilt, double obliquity,
			double planetRadius,
			double diskRadius, double decayCoefficient, double extraOptimizationError) {
		super();
		this.originX = originX;
		this.originY = originY;
		this.sinTilt = Math.sin(tilt);
		this.cosTilt = Math.cos(tilt);
		this.sinObliquity = Math.sin(obliquity);
		this.diskRadiusSquared = diskRadius * diskRadius;
		this.planetRadiusSquared = planetRadius * planetRadius;
		this.decayCoefficient = decayCoefficient;
		this.extraOptimizationError = extraOptimizationError;
	}

	@JsonCreator
	public static RadialDiskTransit create(
		@JsonProperty("originX") double originX,	
		@JsonProperty("impactParameter") double impactParameter,
		@JsonProperty("tilt") double tilt,
		@JsonProperty(value="obliquity", required=false) double obliquity,
		@JsonProperty(value="planetRadius", required=true) double planetRadius,
		@JsonProperty(value="diskRadius", required=true) double diskRadius,
		@JsonProperty(value="decayCoefficient", required=true) double decayCoefficient,
		@JsonProperty("__do_not_use_01") double extraOptimizerError) {
		
		double originY = -impactParameter;
		return new RadialDiskTransit(originX, originY, tilt, obliquity, planetRadius, diskRadius, decayCoefficient, extraOptimizerError);
	}

	/**
	 * Returns the negative transmittance at the given point, or -(1 - opacity).
	 */
	@Override
	public final double fluxOrTransmittance(double x, double y, double z) {
		double xdiff = x - this.originX;
		double ydiff = y - this.originY;
		/*
		double rs = xdiff * xdiff + ydiff * ydiff;
		if(rs <= this.planetRadiusSquared) {
			return 0;
		}
		*/
		double rotA = this.cosTilt;
		double rotB = this.sinTilt;
		double xr = xdiff * rotA + ydiff * rotB;
		double yr = xdiff * (-rotB) + ydiff * rotA;
		double sinOb = this.sinObliquity;
		if(sinOb == 0) {
			return Double.NaN;
		}
		yr /= sinOb;
		double xrSq = xr * xr;
		double yrSq = yr * yr;
		double rrs = xrSq + yrSq;
		if(rrs > this.diskRadiusSquared) {
			return Double.NaN;
		}

		/*
		if(rrs < this.planetRadiusSquared) {
			return 0;
		}
		*/
		
		
		/*
		double planetRadius = Math.sqrt(this.planetRadiusSquared);				
		double plainDistance = StrictMath.sqrt(rrs);				
		if(plainDistance <= planetRadius) {
			// TODO parameterize
			return -0.97;
		}
		double ratio = planetRadius / plainDistance;
		double ratio1m = 1 - ratio;
		double xrr = xr * ratio1m;
		double yrr = yr * ratio1m;
		double opacityDistance = StrictMath.sqrt(xrr * xrr * sinOb * sinOb + yrr * yrr);
		*/
		
		/*
		double opacityDistance = plainDistance - planetRadius;
		*/
		
		double opacityDistance = StrictMath.sqrt(xrSq * sinOb * sinOb + yrSq);
		
		double dc = this.decayCoefficient;
		double denominator1 = dc * opacityDistance;
		double opacity1 = denominator1 <= 1 ? 1.0 : 1.0 / denominator1;
		return -(1.0 - opacity1);
	}

	@Override
	public final Rectangle2D getBoundingBox() {
		double originX = this.originX;
		double originY = this.originY;
		double outerRadius = Math.sqrt(this.diskRadiusSquared);				
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
		return "RadialDiskTransit [originX=" + originX + ", originY=" + originY + ", sinTilt=" + sinTilt + ", cosTilt="
				+ cosTilt + ", sinObliquity=" + sinObliquity + 
				", planetRadiusSquared=" + planetRadiusSquared +
				", diskRadiusSquared=" + diskRadiusSquared
				+ ", decayCoefficient=" + decayCoefficient + ", extraOptimizationError=" + extraOptimizationError + "]";
	}
}
