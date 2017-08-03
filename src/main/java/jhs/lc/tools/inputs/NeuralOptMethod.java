package jhs.lc.tools.inputs;


import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.lc.opt.nn.InputFilterType;
import jhs.lc.opt.nn.NNFluxFunctionSource;
import jhs.lc.opt.nn.NeuralNetworkMetaInfo;
import jhs.lc.util.Initializable;

public class NeuralOptMethod extends AbstractOptMethod {
	private static final Logger logger = Logger.getLogger(NeuralOptMethod.class.getName());
	private double imageWidth;
	private double imageHeight;
	private double lambda = 1.0;
	private double parameterRange = 7.0;
	private double combinedParameterRange = 6.0;

	private InputFilterType inputFilter = InputFilterType.PLAIN;	
	private Map<String, Object> inputFilterProperties;
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

	public final double getLambda() {
		return lambda;
	}

	public final void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public final double getParameterRange() {
		return parameterRange;
	}

	public final void setParameterRange(double parameterRange) {
		this.parameterRange = parameterRange;
	}

	@JsonProperty(required = false)
	public final InputFilterType getInputFilter() {
		return inputFilter;
	}

	public final void setInputFilter(InputFilterType inputType) {
		this.inputFilter = inputType;
	}

	public final double getCombinedParameterRange() {
		return combinedParameterRange;
	}

	public final void setCombinedParameterRange(double combinedParameterRange) {
		this.combinedParameterRange = combinedParameterRange;
	}

	public final Map<String, Object> getInputFilterProperties() {
		return inputFilterProperties;
	}

	public final void setInputFilterProperties(Map<String, Object> inputFilterProperties) {
		this.inputFilterProperties = inputFilterProperties;
	}

	@Override
	public ParametricFluxFunctionSource createFluxFunctionSource(File context) throws Exception {
		InputFilterType inputType = this.inputFilter;
		InputFilterFactory inputFilterFactory = inputType.getFactory();
		this.initInputFilterFactory(inputFilterFactory);
		NeuralNetworkMetaInfo[] metaInfos = this.createMetaInfos(context, inputFilterFactory);
		return new NNFluxFunctionSource(metaInfos, inputFilterFactory, imageWidth, imageHeight, parameterRange, combinedParameterRange, lambda);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initInputFilterFactory(InputFilterFactory inputFilterFactory) {
		Map<String, Object> props = this.inputFilterProperties;
		if(props != null) {
			boolean initialized = false;
			Type[] genericInterfaces = inputFilterFactory.getClass().getGenericInterfaces();
			for(Type gi : genericInterfaces) {
				if(gi instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) gi;
					if(pt.getRawType() == Initializable.class) {
						Type[] arguments = pt.getActualTypeArguments();
						if(arguments.length != 1) {
							throw new IllegalStateException();
						}
						Class tpClass = (Class) arguments[0];
						Initializable i = (Initializable) inputFilterFactory;
						Object propsPojo = SpecMapper.mapToPojo(props, tpClass);
						i.init(propsPojo);
						initialized = true;
					}
				}
			}
			if(!initialized) {
				logger.warning("Input filter factory is not Initializable, so provided properties were ignored.");
			}
		}
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
