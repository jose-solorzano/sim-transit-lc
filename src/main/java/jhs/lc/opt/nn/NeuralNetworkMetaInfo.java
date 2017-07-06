package jhs.lc.opt.nn;

import jhs.math.nn.NeuralNetworkStructure;

public class NeuralNetworkMetaInfo {
	private final NeuralNetworkStructure structure;
	private final OutputType outputType;
	private final double outputBias;
	
	public NeuralNetworkMetaInfo(NeuralNetworkStructure structure, OutputType outputType, double outputBias) {
		super();
		this.structure = structure;
		this.outputType = outputType;
		this.outputBias = outputBias;
	}

	public final NeuralNetworkStructure getStructure() {
		return structure;
	}

	public final OutputType getOutputType() {
		return outputType;
	}

	public final double getOutputBias() {
		return outputBias;
	}
}
