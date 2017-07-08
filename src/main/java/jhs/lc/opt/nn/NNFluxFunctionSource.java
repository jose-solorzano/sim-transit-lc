package jhs.lc.opt.nn;

import java.util.Arrays;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.math.nn.NeuralNetwork;
import jhs.math.nn.NeuralNetworkStructure;
import jhs.math.nn.PlainNeuralNetwork;

public class NNFluxFunctionSource implements ParametricFluxFunctionSource {
	private final NeuralNetworkMetaInfo[] metaInfos;
	private final InputFilterFactory inputFilterType;
	private final double imageWidth, imageHeight;
	
	public NNFluxFunctionSource(NeuralNetworkMetaInfo[] structures, InputFilterFactory inputFilterType, double imageWidth, double imageHeight) {
		super();
		this.metaInfos = structures;
		this.inputFilterType = inputFilterType;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	@Override
	public final FluxOrOpacityFunction getFluxOrOpacityFunction(double[] parameters) {
		NeuralNetworkMetaInfo[] metaInfos = this.metaInfos;
		final NeuralNetwork nn[] = new NeuralNetwork[metaInfos.length];
		int numInputFilterParams = this.inputFilterType.getNumParameters();
		double[] inputFilterParams = Arrays.copyOf(parameters, numInputFilterParams);
		InputFilter inputFilter = this.inputFilterType.createInputFilter(inputFilterParams);
		int paramIndex = numInputFilterParams;
		for(int i = 0; i < metaInfos.length; i++) {
			NeuralNetworkStructure nns = metaInfos[i].getStructure();
			int toParamIndex = paramIndex + nns.getNumParameters();
			double[] nparams = Arrays.copyOfRange(parameters, paramIndex, toParamIndex);
			nn[i] = new PlainNeuralNetwork(nns, nparams);
			paramIndex = toParamIndex;
		}
		return NNFluxOrOpacityFunction.create(metaInfos, nn, inputFilter, imageWidth, imageHeight);
	}

	@Override
	public final int getNumParameters() {
		int total = this.inputFilterType.getNumParameters();
		NeuralNetworkMetaInfo[] metaInfos = this.metaInfos;
		for(int i = 0; i < metaInfos.length; i++) {
			total += metaInfos[i].getStructure().getNumParameters();
		}
		return total;
	}

	@Override
	public final double getParameterScale(int paramIndex) {
		// NN activation functions take care of scaling.
		return 1.0;
	}	
}
