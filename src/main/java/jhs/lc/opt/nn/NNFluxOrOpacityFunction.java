package jhs.lc.opt.nn;

import java.awt.geom.Rectangle2D;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.math.nn.NeuralNetwork;

public final class NNFluxOrOpacityFunction implements FluxOrOpacityFunction {
	private static final long serialVersionUID = 1L;
	private static final double SF = 3.46;
	
	private final NeuralNetwork[] neuralNetworks;
	private final OutputType[] outputTypes;
	private final double[] outputBiases;
	private final InputFilter inputFilter;
	private final double scale;
	private final Rectangle2D boundingBox;
	
	public NNFluxOrOpacityFunction(NeuralNetwork[] neuralNetworks, OutputType[] outputTypes, double[] outputBiases,
			InputFilter inputFilter, double scale, Rectangle2D boundingBox) {
		super();
		this.neuralNetworks = neuralNetworks;
		this.outputTypes = outputTypes;
		this.outputBiases = outputBiases;
		this.inputFilter = inputFilter;
		this.scale = scale;
		this.boundingBox = boundingBox;
	}

	public static NNFluxOrOpacityFunction create(NeuralNetwork[] neuralNetworks, InputFilter inputFilter, OutputType[] outputTypes, double[] outputBiases,
			double imageWidth, double imageHeight) {
		Rectangle2D boundingBox = new Rectangle2D.Double(-imageWidth / 2.0, -imageHeight / 2.0, imageWidth, imageHeight);
		double dim = Math.sqrt((imageWidth * imageWidth + imageHeight * imageHeight) / 2);
		double scale = SF / dim;
		return new NNFluxOrOpacityFunction(neuralNetworks, outputTypes, outputBiases, inputFilter, scale, boundingBox);
	}

	@Override
	public final double fluxOrOpacity(double x, double y, double z) {
		double scale = this.scale;
		double scaledX = x * scale;
		double scaledY = y * scale;
		return this.combinedOpacity(this.inputFilter.getInput(scaledX, scaledY));
	}
	
	private final double combinedOpacity(double[] inputData) {
		NeuralNetwork[] neuralNetworks = this.neuralNetworks;
		OutputType[] outputTypes = this.outputTypes;
		double[] outputBiases = this.outputBiases;		
		double combinedValue = -1;
		for(int i = 0; i < neuralNetworks.length; i++) {
			NeuralNetwork nn = neuralNetworks[i];
			double[] aa = nn.activations(inputData);
			double a = aa[0] + outputBiases[i];
			double b;
			switch(outputTypes[i]) {
			case BINARY:
				b = a >= 0 ? 0 : -1.0;
				break;
			case OPACITY:
				b = (a - 1) / 2;
				if(b < -1) {
					b = -1;
				}
				else if(b > 0) {
					b = 0;
				}
				break;
			default:
				throw new IllegalStateException(outputTypes[i].name());
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
}
