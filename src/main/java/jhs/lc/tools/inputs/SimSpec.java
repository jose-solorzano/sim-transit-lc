package jhs.lc.tools.inputs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimSpec {
	private int widthPixels;
	private int heightPixels;
	private Double viewportWidth;
	private Double viewportHeight;
	private double noiseFraction;
	private double[] limbDarkeningParams;
	private double inclineAngle;
	private double orbitRadius;
	private double orbitPeriod;
	private double startTime;
	private double endTime;
	private int numSteps;
	
	private AbstractTransitShape transitShape;
	
	public final Double getViewportWidth() {
		return viewportWidth;
	}

	public final void setViewportWidth(Double viewportWidth) {
		this.viewportWidth = viewportWidth;
	}

	public final Double getViewportHeight() {
		return viewportHeight;
	}

	public final void setViewportHeight(Double viewportHeight) {
		this.viewportHeight = viewportHeight;
	}

	public final double[] getLimbDarkeningParams() {
		return limbDarkeningParams;
	}

	public final void setLimbDarkeningParams(double[] limbDarkeningParams) {
		this.limbDarkeningParams = limbDarkeningParams;
	}

	@JsonProperty(required = true)
	public final AbstractTransitShape getTransitShape() {
		return transitShape;
	}

	public final void setTransitShape(AbstractTransitShape transitShape) {
		this.transitShape = transitShape;
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

	@JsonProperty(required = true)
	public final double getOrbitPeriod() {
		return orbitPeriod;
	}

	public final void setOrbitPeriod(double orbitPeriod) {
		this.orbitPeriod = orbitPeriod;
	}

	@JsonProperty(required = true)
	public final double getStartTime() {
		return startTime;
	}

	public final void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	@JsonProperty(required = true)
	public final double getEndTime() {
		return endTime;
	}

	public final void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	@JsonProperty(required = true)
	public final int getNumSteps() {
		return numSteps;
	}

	public final void setNumSteps(int numSteps) {
		this.numSteps = numSteps;
	}

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

	public final double getNoiseFraction() {
		return noiseFraction;
	}
	
	public final void setNoiseFraction(double noiseStandardDev) {
		this.noiseFraction = noiseStandardDev;
	}
	
	
}
