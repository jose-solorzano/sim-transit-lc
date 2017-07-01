package jhs.lc.tools.inputs;

import jhs.lc.geom.LimbDarkeningParams;
import jhs.lc.geom.Point3D;
import jhs.lc.geom.SolidSphere;
import jhs.lc.geom.Sphere;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SphereSpec {
	private static final double DEFAULT_MAX_BRIGHTNESS = 1.0;
	
	private double radius;
	private Double maxBrightness;
	private double relX, relY, relZ;
	private boolean limbDarkeningEnabled;
	private double[] limbDarkeningParams;

	public final double getRelX() {
		return relX;
	}

	public final void setRelX(double relX) {
		this.relX = relX;
	}

	public final double getRelY() {
		return relY;
	}

	public final void setRelY(double relY) {
		this.relY = relY;
	}

	public final double getRelZ() {
		return relZ;
	}

	public final void setRelZ(double relZ) {
		this.relZ = relZ;
	}

	@JsonProperty(required = true)
	public final double getRadius() {
		return radius;
	}

	public final void setRadius(double radius) {
		this.radius = radius;
	}

	public final double getMaxBrightness() {
		Double m = this.maxBrightness;
		return m == null ? DEFAULT_MAX_BRIGHTNESS : m.doubleValue();
	}

	public final void setMaxBrightness(double maxBrightness) {
		this.maxBrightness = maxBrightness;
	}

	public final double[] getLimbDarkeningParams() {
		return limbDarkeningParams;
	}

	public final void setLimbDarkeningParams(double[] limbDarkeningParams) {
		this.limbDarkeningParams = limbDarkeningParams;
	}

	@JsonProperty(required = false, defaultValue = "false")
	public final boolean isLimbDarkeningEnabled() {
		return limbDarkeningEnabled;
	}

	public final void setLimbDarkeningEnabled(boolean limbDarkeningEnabled) {
		this.limbDarkeningEnabled = limbDarkeningEnabled;
	}
	
	public Sphere asSphere() {
		LimbDarkeningParams ldParams = null;
		if(this.limbDarkeningEnabled) {
			ldParams = this.limbDarkeningParams == null ? LimbDarkeningParams.SUN : new LimbDarkeningParams(this.limbDarkeningParams);
		}
		return new SolidSphere(this.radius, ldParams, this.maxBrightness);
	}
	
	public Point3D asPoint3D() {
		return new Point3D(this.relX, this.relY, this.relZ);
	}
}
