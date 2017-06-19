package jhs.lc.tools.inputs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OptSpec {
	private int widthPixels;
	private int heightPixels;
	private double[] limbDarkeningParams;
	private double inclineAngle;
	private double orbitRadius;
	private double orbitRadiusFlexibility;
	private double orbitPeriod;
	
	private AbstractOptMethod method;
	
	@JsonProperty(required = true)
	public final int getWidthPixels() {
		return widthPixels;
	}
	
	public final void setWidthPixels(int widthPixels) {
		this.widthPixels = widthPixels;
	}
	
	@JsonProperty(required = true)
	public final int getHeightPixels() {
		return heightPixels;
	}
	
	public final void setHeightPixels(int heightPixels) {
		this.heightPixels = heightPixels;
	}

	@JsonProperty(required = true)
	public final AbstractOptMethod getMethod() {
		return method;
	}

	public final void setMethod(AbstractOptMethod method) {
		this.method = method;
	}

	public final double[] getLimbDarkeningParams() {
		return limbDarkeningParams;
	}

	public final void setLimbDarkeningParams(double[] limbDarkeningParams) {
		this.limbDarkeningParams = limbDarkeningParams;
	}

	public final double getInclineAngle() {
		return inclineAngle;
	}

	public final void setInclineAngle(double inclineAngle) {
		this.inclineAngle = inclineAngle;
	}

	@JsonProperty(required = true)
	public final double getOrbitRadius() {
		return orbitRadius;
	}

	public final void setOrbitRadius(double orbitRadius) {
		this.orbitRadius = orbitRadius;
	}

	public final double getOrbitRadiusFlexibility() {
		return orbitRadiusFlexibility;
	}

	public final void setOrbitRadiusFlexibility(double orbitRadiusFlexibility) {
		this.orbitRadiusFlexibility = orbitRadiusFlexibility;
	}

	@JsonProperty(required = true)
	public final double getOrbitPeriod() {
		return orbitPeriod;
	}

	public final void setOrbitPeriod(double orbitPeriod) {
		this.orbitPeriod = orbitPeriod;
	}
}
