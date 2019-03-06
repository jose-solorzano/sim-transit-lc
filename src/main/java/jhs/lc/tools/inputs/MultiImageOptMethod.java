package jhs.lc.tools.inputs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Logger;

import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.opt.builders.MultiImageTransitBuilder;
import jhs.lc.opt.builders.MultiImageTransitBuilder.BuilderImageSpec;

public class MultiImageOptMethod extends AbstractOptMethod {
	public static final String SPECIAL_PARAM_FILE_CONTEXT = "__file_context";
	private Map<String, double[]> parameters;
	private BuilderImageSpec[] images;
	
	public final Map<String, double[]> getParameters() {
		return parameters;
	}

	public final void setParameters(Map<String, double[]> properties) {
		this.parameters = properties;
	}
	

	public final BuilderImageSpec[] getImages() {
		return images;
	}

	public final void setImages(BuilderImageSpec[] images) {
		this.images = images;
	}

	@Override
	public ParametricTransitFunctionSource createFluxFunctionSource(File context) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, IOException {		
		return MultiImageTransitBuilder.create(parameters, images, context);
	}
}
