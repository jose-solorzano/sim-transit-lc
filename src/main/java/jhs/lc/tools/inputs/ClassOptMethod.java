package jhs.lc.tools.inputs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.ParametricFluxFunctionSource;
import jhs.lc.opt.pofs.PolygonalFluxFunctionSource;

public class ClassOptMethod extends AbstractOptMethod {
	private static final Logger logger = Logger.getLogger(ClassOptMethod.class.getName());
	private String className;
	private Map<String,Object> init;
	
	@JsonProperty(required = true)
	public final String getClassName() {
		return className;
	}

	public final void setClassName(String className) {
		this.className = className;
	}

	public final Map<String, Object> getInit() {
		return init;
	}

	public final void setInit(Map<String, Object> init) {
		this.init = init;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ParametricFluxFunctionSource createFluxFunctionSource(File context) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Class c;
		try {
			c = Class.forName(this.className);
		} catch(ClassNotFoundException cnf) {
			try {
				c = Class.forName(PolygonalFluxFunctionSource.class.getPackage().getName() + "." + this.className);			
			} catch(ClassNotFoundException cnf2) {
				throw cnf;
			}
		}
		if(!ParametricFluxFunctionSource.class.isAssignableFrom(c)) {
			throw new IllegalStateException("Class " + this.className + " is not assignable to " + ParametricFluxFunctionSource.class.getName() + ".");
		}
		ParametricFluxFunctionSource pfs = (ParametricFluxFunctionSource) c.newInstance();
		try {
			@SuppressWarnings("unchecked")
			Method method = c.getMethod("init", Map.class);
			method.invoke(pfs, this.getInit());
		} catch(NoSuchMethodException nsm) {
			if(logger.isLoggable(Level.INFO)) {
				logger.info("lookupAndInitClass(): No init method in " + c);
			}
		}
		return pfs;
	}

}
