package jhs.lc.opt.transits;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.TransitFunction;

public final class RingedPlanet implements TransitFunction {
	private static final long serialVersionUID = 1L;
	private final double originX, originY;
	private final double sinTilt, cosTilt;
	private final double sinObliquity;
	private final double planetRadiusSquared;
	private final double[] ringInnerRadiiSquared;
	private final double[] ringOuterRadiiSquared;
	private final double[] ringTransmittances;
	private final double extraOptimizationError;
	
	public RingedPlanet(double originX, double originY, double sinTilt, double cosTilt, double sinObliquity,
			double planetRadiusSquared, double[] ringInnerRadiiSquared, double[] ringOuterRadiiSquared,
			double[] ringTransmittances, double extraOptimizationError) {
		this.originX = originX;
		this.originY = originY;
		this.cosTilt = cosTilt;
		this.sinTilt = sinTilt;
		this.sinObliquity = sinObliquity;
		this.planetRadiusSquared = planetRadiusSquared;
		this.ringInnerRadiiSquared = ringInnerRadiiSquared;
		this.ringOuterRadiiSquared = ringOuterRadiiSquared;
		this.ringTransmittances = ringTransmittances;
		this.extraOptimizationError = extraOptimizationError;
	}
	
	@JsonCreator
	public static RingedPlanet create(
		@JsonProperty("numRings") int numRings,
		@JsonProperty("originX") double originX,	
		@JsonProperty("impactParameter") double impactParameter,
		@JsonProperty("tilt") double tilt,
		@JsonProperty(value="obliquity", required=false) double obliquity,
		@JsonProperty(value="planetRadius", required=true) double planetRadius,
		@JsonProperty(value="ringInnerRadius", required=false) double ringInnerRadius,
		@JsonProperty(value="ringWidth", required=false) double ringWidth,
		@JsonProperty(value="ringGap", required=false) double ringGap,
		@JsonProperty(value="ringTransmittances", required=false) double[] ringTransmittances,
		@JsonProperty(value="ringTransmittancesWhenPerpendicular", required=false) double[] ringTransmittancesWhenPerpendicular,
		@JsonProperty("__do_not_use_01") double extraOptimizerError) {
		
		double sinObliquity = Math.sin(obliquity);
		if(numRings != 0) {
			if(ringTransmittances == null && ringTransmittancesWhenPerpendicular == null) {
				throw new IllegalArgumentException("One of ringTranmittances or ringTranmittancesWhenPerpendicular must be provided when there are rings.");
			}
			if(ringTransmittances != null && ringTransmittancesWhenPerpendicular != null) {
				throw new IllegalArgumentException("Only one of ringTranmittances or ringTranmittancesWhenPerpendicular must be provided.");				
			}
			if(ringTransmittances == null) {
				if(ringTransmittancesWhenPerpendicular.length != numRings) {
					throw new IllegalArgumentException("ringTranmittancesWhenPerpendicular must be an array of length equal to numRings (" + numRings + ").");
				}
				double transmittanceExponent = sinObliquity == 0 ? 1.0 : 1.0 / Math.abs(sinObliquity);
				ringTransmittances = new double[numRings];
				for(int i = 0; i < numRings; i++) {
					double stwp = ringTransmittancesWhenPerpendicular[i];
					if(stwp > 1 || stwp < 0) {
						throw new IllegalArgumentException("Transmittance of " + stwp + " is invalid.");
					}
					ringTransmittances[i] = stwp == 1.0 ? 1.0 : Math.pow(stwp, transmittanceExponent);
				}
			}
			else {				
				if(ringTransmittances.length != numRings) {
					throw new IllegalArgumentException("ringTranmittances must be an array of length equal to numRings (" + numRings + ").");
				}
			}
		}
		if(numRings != 0 && ringWidth == 0) {
			throw new IllegalArgumentException("A ringWidth must be specified when there are rings.");			
		}
		if(numRings != 0 && ringInnerRadius == 0) {
			throw new IllegalArgumentException("A ringInnerRadius must be specified when there are rings.");			
		}
		double sinTilt = Math.sin(tilt);
		double cosTilt = Math.cos(tilt);
		double planetRadiusSquared = planetRadius * planetRadius;
		double[] ringInnerRadiiSquared = new double[numRings];
		double[] ringOuterRadiiSquared = new double[numRings];
		double rr = ringInnerRadius;
		for(int i = 0; i < numRings; i++) {
			ringInnerRadiiSquared[i] = rr * rr;
			rr += ringWidth;
			ringOuterRadiiSquared[i] = rr * rr;
			rr += ringGap;
		}
		double originY = -impactParameter;
		return new RingedPlanet(originX, originY, sinTilt, cosTilt, sinObliquity, planetRadiusSquared, ringInnerRadiiSquared, ringOuterRadiiSquared, ringTransmittances, extraOptimizerError);
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
		double[] ringTransmittances = this.ringTransmittances;
		int nr = ringTransmittances == null ? 0 : ringTransmittances.length;
		if(nr == 0) {
			return Double.NaN;
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
		double[] rir = this.ringInnerRadiiSquared;
		double[] ror = this.ringOuterRadiiSquared;
		for(int i = 0; i < nr; i++) {
			if(rrs >= rir[i] && rrs <= ror[i]) {
				double b = -ringTransmittances[i];
				if(b > 0) {
					b = 0;
				}
				return b;
			}
		}
		return Double.NaN;
	}

	@Override
	public final Rectangle2D getBoundingBox() {
		double[] ror = this.ringOuterRadiiSquared;
		double planetRadius = Math.sqrt(this.planetRadiusSquared);
		double originX = this.originX;
		double originY = this.originY;
		double outerRadius = ror.length == 0 ? planetRadius : Math.max(planetRadius, Math.sqrt(ror[ror.length - 1]));				
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
		return "RingedPlanet [originX=" + originX + ", originY=" + originY + ", sinTilt=" + sinTilt + ", cosTilt="
				+ cosTilt + ", sinObliquity=" + sinObliquity + ", planetRadiusSquared=" + planetRadiusSquared
				+ ", ringInnerRadiiSquared=" + Arrays.toString(ringInnerRadiiSquared) + ", ringOuterRadiiSquared="
				+ Arrays.toString(ringOuterRadiiSquared) + ", ringTransmittances=" + Arrays.toString(ringTransmittances) + "]";
	}
}
