package jhs.lc.tools.inputs;

import jhs.lc.opt.nn.ActivationFunctionType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NeuralLayerSpec {
	private int numUnits;
	private ActivationFunctionType[] activationFunctions;

	@JsonProperty(required = true)
	public final int getNumUnits() {
		return numUnits;
	}

	public final void setNumUnits(int numUnits) {
		this.numUnits = numUnits;
	}

	public final ActivationFunctionType[] getActivationFunctions() {
		return activationFunctions;
	}

	public final void setActivationFunctions(ActivationFunctionType[] activationFunctions) {
		this.activationFunctions = activationFunctions;
	}
}
