package jhs.lc.opt.nn;

import java.awt.geom.Rectangle2D;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.math.classification.ClassificationUtil;
import jhs.math.nn.NeuralNetwork;

public final class NNFluxOrOpacityFunction implements FluxOrOpacityFunction {
	private static final long serialVersionUID = 1L;
	private static final double SQ_MEAN = 1.0;
	private static final double SQ_SD = Math.sqrt(2.0);
	
	private final NeuralNetwork[] neuralNetworks;
	private final InputType inputDef;
	private final OutputType outputType;
	private final double imageWidth, imageHeight;
	private final Rectangle2D boundingBox;
	
	public NNFluxOrOpacityFunction(NeuralNetwork[] neuralNetworks, InputType inputDef, OutputType outputType,
			double imageWidth, double imageHeight, Rectangle2D boundingBox) {
		this.neuralNetworks = neuralNetworks;
		this.inputDef = inputDef;
		this.outputType = outputType;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.boundingBox = boundingBox;
	}
	
	public static NNFluxOrOpacityFunction create(NeuralNetwork[] neuralNetworks, InputType inputDef, OutputType outputType,
			double imageWidth, double imageHeight) {
		Rectangle2D boundingBox = new Rectangle2D.Double(-imageWidth / 2.0, -imageHeight / 2.0, imageWidth, imageHeight);
		return new NNFluxOrOpacityFunction(neuralNetworks, inputDef, outputType, imageWidth, imageHeight, boundingBox);
	}

	@Override
	public final double fluxOrOpacity(double x, double y, double z) {
		double xscale = 3.47 * x / this.imageWidth;
		double yscale = 3.47 * y / this.imageHeight;
		double a = this.maxActivation(getInputData(xscale, yscale, this.inputDef));
		switch(this.outputType) {
		case BINARY:
			return a >= 0 ? 0 : Double.NaN;
		case OPACITY: {
			double p = (a - 1) / 2;
			if(p < -1) {
				p = -1;
			}
			else if(p > 0) {
				p = 0;
			}
			return p;
		}
		case OPACITY_LOGISTIC: {
			double p = ClassificationUtil.logitToProbability(a);
			return -p;			
		}
		case BRIGHTNESS: {
			double p = ClassificationUtil.logitToProbability(a);
			return p;
		}
		case ALL: {
			double p = ClassificationUtil.logitToProbability(a);
			return p * 2.0 - 1.0;
		}
		default: 
			throw new IllegalStateException("Unknown outputType: " + this.outputType);
		}
	}
	
	private double maxActivation(double[] inputData) {
		double max = Double.NEGATIVE_INFINITY;
		for(NeuralNetwork nn : this.neuralNetworks) {
			double[] aa = nn.activations(inputData);
			double a = aa[0];
			if(a > max) {
				max = a;
			}
		}
		return max;
	}

	@Override
	public final Rectangle2D getBoundingBox() {
		return this.boundingBox;
	}

	public static final double[] getInputData(double x, double y, InputType inputDef) {
		switch(inputDef) {
		case PLAIN: {
			return new double[] { x, y };								
		}
		case QUADRATIC: {
			return new double[] { x, y, (x * x - SQ_MEAN) / SQ_SD,  (y * y - SQ_MEAN) / SQ_SD };				
		}
		case QUADRATIC_WP: {
			return new double[] { x, y, (x * x - SQ_MEAN) / SQ_SD,  (y * y - SQ_MEAN) / SQ_SD, x * y };				
		}
		default:
			throw new IllegalStateException(String.valueOf(inputDef));
		}
	}
	
	public static int getNumInputs(InputType inputType) {
		switch(inputType) {
		case PLAIN:
			return 2;
		case QUADRATIC:
			return 4;
		case QUADRATIC_WP:
			return 5;
		default:
			throw new IllegalStateException(String.valueOf(inputType));
		}
	}
}
