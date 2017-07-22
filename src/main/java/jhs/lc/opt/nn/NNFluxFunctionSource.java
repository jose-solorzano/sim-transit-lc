package jhs.lc.opt.nn;

import java.util.Arrays;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.math.nn.NeuralNetwork;
import jhs.math.nn.NeuralNetworkStructure;
import jhs.math.nn.PlainNeuralNetwork;
import jhs.math.util.MathUtil;

public class NNFluxFunctionSource implements ParametricFluxFunctionSource {
	private static final double LAMBDA_FACTOR = 0.01;
	private final NeuralNetworkMetaInfo[] metaInfos;
	private final InputFilterFactory inputFilterType;
	private final double imageWidth, imageHeight;
	private final double lambda;
	
	public NNFluxFunctionSource(NeuralNetworkMetaInfo[] structures, InputFilterFactory inputFilterType, double imageWidth, double imageHeight, double lambda) {
		super();
		this.metaInfos = structures;
		this.inputFilterType = inputFilterType;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.lambda = lambda;
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
		double sdParams = MathUtil.standardDev(parameters, 0);
		double diffWithNormal = sdParams - 1.0;			
		double extraOptimizerError = diffWithNormal * diffWithNormal * this.lambda * LAMBDA_FACTOR;
		return NNFluxOrOpacityFunction.create(metaInfos, nn, inputFilter, imageWidth, imageHeight, extraOptimizerError);
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
