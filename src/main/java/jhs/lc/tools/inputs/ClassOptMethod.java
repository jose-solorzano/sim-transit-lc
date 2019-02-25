package jhs.lc.tools.inputs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.ParametricTransitFunctionSource;
import jhs.lc.geom.TransitFunction;
import jhs.lc.opt.builders.RingedPlanetBuilder;

public class ClassOptMethod extends AbstractOptMethod {
	private static final Logger logger = Logger.getLogger(ClassOptMethod.class.getName());
	public static final String SPECIAL_PARAM_FILE_CONTEXT = "__file_context";
	private String className;
	private Map<String, Object> properties;
	
	@JsonProperty(required = true)
	public final String getClassName() {
		return className;
	}

	public final void setClassName(String className) {
		this.className = className;
	}

	public final Map<String, Object> getProperties() {
		return properties;
	}

	public final void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ParametricTransitFunctionSource createFluxFunctionSource(File context) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Class c;
		try {
			c = Class.forName(this.className);
		} catch(ClassNotFoundException cnf) {
			try {
				c = Class.forName(RingedPlanetBuilder.class.getPackage().getName() + "." + this.className);			
			} catch(ClassNotFoundException cnf2) {
				throw cnf;
			}
		}
		if(!ParametricTransitFunctionSource.class.isAssignableFrom(c)) {
			throw new IllegalStateException("Class " + this.className + " is not assignable to " + ParametricTransitFunctionSource.class.getName() + ".");
		}
		Map<String, Object> params = this.getProperties();
		if(params == null) {
			params = new HashMap<>();
		}
		params.put(SPECIAL_PARAM_FILE_CONTEXT, context);
		@SuppressWarnings("unchecked")
		ParametricTransitFunctionSource pfs = SpecMapper.mapToPojo(params, (Class<ParametricTransitFunctionSource>) c);
		return pfs;
	}

}
