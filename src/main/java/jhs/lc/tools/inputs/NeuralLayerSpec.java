package jhs.lc.tools.inputs;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.opt.nn.ActivationFunctionType;

public class NeuralLayerSpec {
	private int numUnits;
	private int maxInputsPerUnit = Integer.MAX_VALUE;
	private ActivationFunctionType[] activationFunctions;

	private String comment;
	
	public final String getComment() {
		return comment;
	}

	public final void setComment(String comment) {
		this.comment = comment;
	}

	@JsonProperty(required = true)
	public final int getNumUnits() {
		return numUnits;
	}

	public final void setNumUnits(int numUnits) {
		this.numUnits = numUnits;
	}

	public int getMaxInputsPerUnit() {
		return maxInputsPerUnit;
	}

	public void setMaxInputsPerUnit(int maxInputsPerUnit) {
		this.maxInputsPerUnit = maxInputsPerUnit;
	}

	public final ActivationFunctionType[] getActivationFunctions() {
		return activationFunctions;
	}

	public final void setActivationFunctions(ActivationFunctionType[] activationFunctions) {
		this.activationFunctions = activationFunctions;
	}
}
