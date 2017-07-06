package jhs.lc.opt.nn;

import java.util.Arrays;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.math.nn.NeuralNetwork;
import jhs.math.nn.NeuralNetworkStructure;
import jhs.math.nn.PlainNeuralNetwork;

public class NNFluxFunctionSource implements ParametricFluxFunctionSource {
	private final NeuralNetworkStructure[] structures;
	private final InputFilterFactory inputFilterType;
	private final OutputType[] outputTypes;
	private final double[] outputBiases;
	private final double imageWidth, imageHeight;
	
	public NNFluxFunctionSource(NeuralNetworkStructure[] structures, InputFilterFactory inputFilterType,
			OutputType[] outputTypes, double[] outputBiases, double imageWidth, double imageHeight) {
		super();
		this.structures = structures;
		this.inputFilterType = inputFilterType;
		this.outputTypes = outputTypes;
		this.outputBiases = outputBiases;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	@Override
	public final FluxOrOpacityFunction getFluxOrOpacityFunction(double[] parameters) {
		NeuralNetworkStructure[] structures = this.structures;
		final NeuralNetwork nn[] = new NeuralNetwork[structures.length];
		int numInputFilterParams = this.inputFilterType.getNumParameters();
		double[] inputFilterParams = Arrays.copyOf(parameters, numInputFilterParams);
		InputFilter inputFilter = this.inputFilterType.createInputFilter(inputFilterParams);
		int paramIndex = numInputFilterParams;
		for(int i = 0; i < structures.length; i++) {
			NeuralNetworkStructure nns = structures[i];
			int toParamIndex = paramIndex + nns.getNumParameters();
			double[] nparams = Arrays.copyOfRange(parameters, paramIndex, toParamIndex);
			nn[i] = new PlainNeuralNetwork(nns, nparams);
			paramIndex = toParamIndex;
		}
		return NNFluxOrOpacityFunction.create(nn, inputFilter, outputTypes, outputBiases, imageWidth, imageHeight);
	}

	@Override
	public final int getNumParameters() {
		int total = this.inputFilterType.getNumParameters();
		NeuralNetworkStructure[] structures = this.structures;
		for(int i = 0; i < structures.length; i++) {
			total += structures[i].getNumParameters();
		}
		return total;
	}

	@Override
	public final double getParameterScale(int paramIndex) {
		// NN activation functions take care of scaling.
		return 1.0;
	}	
}
