package jhs.lc.tools.inputs;


import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.opt.nn.ActivationFunctionType;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.lc.opt.nn.InputFilterType;
import jhs.lc.opt.nn.NNFluxFunctionSource;
import jhs.lc.opt.nn.NNFluxOrOpacityFunction;
import jhs.lc.opt.nn.NeuralNetworkMetaInfo;
import jhs.lc.opt.nn.OutputType;
import jhs.math.nn.ActivationFunction;
import jhs.math.nn.ActivationFunctionFactory;
import jhs.math.nn.NeuralNetwork;
import jhs.math.nn.NeuralNetworkStructure;
import jhs.math.nn.PlainNeuralNetwork;
import jhs.math.nn.aa.SigmoidActivationFunction;
import jhs.math.nn.aa.SignActivationFunction;
import jhs.math.util.MathUtil;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NeuralOptMethod extends AbstractOptMethod {
	private double imageWidth;
	private double imageHeight;

	private InputFilterType inputFilter = InputFilterType.PLAIN;	
	private NeuralNetworkSpec[] networks;

	@JsonProperty(required = true)
	public final NeuralNetworkSpec[] getNetworks() {
		return networks;
	}

	public final void setNetworks(NeuralNetworkSpec[] networks) {
		this.networks = networks;
	}

	@JsonProperty(required = true)
	public final double getImageWidth() {
		return imageWidth;
	}

	public final void setImageWidth(double imageWidth) {
		this.imageWidth = imageWidth;
	}

	@JsonProperty(required = true)
	public final double getImageHeight() {
		return imageHeight;
	}

	public final void setImageHeight(double imageHeight) {
		this.imageHeight = imageHeight;
	}

	@JsonProperty(required = false)
	public final InputFilterType getInputFilter() {
		return inputFilter;
	}

	public final void setInputFilter(InputFilterType inputType) {
		this.inputFilter = inputType;
	}

	@Override
	public ParametricFluxFunctionSource createFluxFunctionSource(File context) throws Exception {
		InputFilterType inputType = this.inputFilter;
		InputFilterFactory inputFilterFactory = inputType.getFactory();
		NeuralNetworkMetaInfo[] metaInfos = this.createMetaInfos(context, inputFilterFactory);
		return new NNFluxFunctionSource(metaInfos, inputFilterFactory, imageWidth, imageHeight);
	}
	
	private NeuralNetworkMetaInfo[] createMetaInfos(File context, InputFilterFactory inputFilterFactory) throws Exception {
		NeuralNetworkSpec[] nnSpecs = this.networks;
		if(nnSpecs == null || nnSpecs.length == 0) {
			throw new IllegalStateException("At least one neural network must be specified using the 'networks' property.");
		}
		NeuralNetworkMetaInfo[] nnmis = new NeuralNetworkMetaInfo[nnSpecs.length];
		for(int i = 0; i < nnSpecs.length; i++) {
			nnmis[i] = nnSpecs[i].createNeuralMetaInfo(context, inputFilterFactory, this.imageWidth, this.imageHeight);
		}
		return nnmis;
	}
}
