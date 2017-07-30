package jhs.lc.opt.nn;

import java.awt.geom.Rectangle2D;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.math.nn.NeuralNetwork;

public final class NNFluxOrOpacityFunction implements FluxOrOpacityFunction {
	private static final long serialVersionUID = 1L;
	static final double SF = 3.46;
	
	private final NeuralNetworkMetaInfo[] neuralNetworkSpecs;
	private final NeuralNetwork[] neuralNetworks;
	private final InputFilter inputFilter;
	private final double scale;
	private final Rectangle2D boundingBox;
	private final double extraOptimizerError;

	public NNFluxOrOpacityFunction(NeuralNetworkMetaInfo[] neuralNetworkSpecs, NeuralNetwork[] neuralNetworks,
			InputFilter inputFilter, double scale, Rectangle2D boundingBox, double extraOptimizerError) {
		super();
		this.neuralNetworkSpecs = neuralNetworkSpecs;
		this.neuralNetworks = neuralNetworks;
		this.inputFilter = inputFilter;
		this.scale = scale;
		this.boundingBox = boundingBox;
		this.extraOptimizerError = extraOptimizerError;
	}

	public static NNFluxOrOpacityFunction create(NeuralNetworkMetaInfo[] neuralNetworkSpecs, NeuralNetwork[] neuralNetworks, InputFilter inputFilter, double imageWidth, double imageHeight, double extraOptimizerError) {
		Rectangle2D boundingBox = new Rectangle2D.Double(-imageWidth / 2.0, -imageHeight / 2.0, imageWidth, imageHeight);
		double dim = Math.sqrt((imageWidth * imageWidth + imageHeight * imageHeight) / 2);
		double scale = SF / dim;
		return new NNFluxOrOpacityFunction(neuralNetworkSpecs, neuralNetworks, inputFilter, scale, boundingBox, extraOptimizerError);
	}

	@Override
	public final double fluxOrOpacity(double x, double y, double z) {
		double scale = this.scale;
		double scaledX = x * scale;
		double scaledY = y * scale;
		return this.combinedOpacity(this.inputFilter.getInput(scaledX, scaledY));
	}
	
	private final double combinedOpacity(double[] inputData) {
		double combinedValue = -1;
		NeuralNetwork[] nns = this.neuralNetworks;
		NeuralNetworkMetaInfo[] nnmis = this.neuralNetworkSpecs;
		for(int i = 0; i < nns.length; i++) {
			NeuralNetwork nn = nns[i];
			NeuralNetworkMetaInfo nnmi = nnmis[i];
			double[] aa = nn.activations(inputData);
			double a = aa[0] + nnmi.getOutputBias();
			double b;
			switch(nnmi.getOutputType()) {
			case BINARY:
				b = a >= 0 ? 0 : -1.0;
				break;
			case OPACITY:
				b = -0.5 + a / 2.0;
				if(b < -1) {
					b = -1;
				}
				else if(b > 0) {
					b = 0;
				}
				break;
			default:
				throw new IllegalStateException(nnmi.getOutputType().name());
			}
			if(b >= 0) {
				combinedValue = b;
			}
			else {
				combinedValue = (-b) * combinedValue;
			}
		}
		return combinedValue;
	}

	@Override
	public final Rectangle2D getBoundingBox() {
		return this.boundingBox;
	}
	
	@Override
	public final double getExtraOptimizerError() {
		return this.extraOptimizerError;
	}
}
