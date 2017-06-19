package jhs.lc.opt.nn;

import java.util.Arrays;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.math.nn.NeuralNetwork;
import jhs.math.nn.NeuralNetworkStructure;
import jhs.math.nn.PlainNeuralNetwork;

public class NNFluxFunctionSource implements ParametricFluxFunctionSource {
	private final NeuralNetworkStructure structure;
	private final InputType inputType;
	private final OutputType outputType;
	private final double imageWidth, imageHeight;
	private final int numNetworks;

	public NNFluxFunctionSource(NeuralNetworkStructure structure, InputType inputType, OutputType outputType,
			double imageWidth, double imageHeight, int numNetworks) {
		super();
		this.structure = structure;
		this.inputType = inputType;
		this.outputType = outputType;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.numNetworks = numNetworks;
	}

	@Override
	public final FluxOrOpacityFunction getFluxOrOpacityFunction(double[] parameters) {
		int n = this.numNetworks;
		int nppn = this.structure.getNumParameters();
		if(parameters.length != nppn * n) {
			throw new IllegalArgumentException("Incorrect number of parameters: " + parameters.length + ". Expecting multiple of " + nppn + ".");
		}
		final NeuralNetwork nn[] = new NeuralNetwork[n];
		for(int i = 0; i < n; i++) {
			double[] nparams = Arrays.copyOfRange(parameters, i * nppn, (i + 1) * nppn);
			nn[i] = new PlainNeuralNetwork(this.structure, nparams);
		}
		return NNFluxOrOpacityFunction.create(nn, inputType, outputType, imageWidth, imageHeight);
	}

	@Override
	public final int getNumParameters() {
		return this.structure.getNumParameters() * this.numNetworks;
	}

	@Override
	public final double getParameterScale(int paramIndex) {
		// NN takes care of scaling.
		return 1.0;
	}	
}
