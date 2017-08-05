package jhs.lc.tools.inputs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.TransitFunction;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.opt.bfunctions.RingedPlanet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;

public class ClassTransitShape extends AbstractTransitShape {
	private String className;
	private Map<String, Object> params;

	@JsonProperty(required = true)
	public final String getClassName() {
		return className;
	}

	public final void setClassName(String className) {
		this.className = className;
	}

	public final Map<String, Object> getParams() {
		return params;
	}

	public final void setParams(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	public RotationAngleSphereFactory createSphereFactory(File context) throws Exception {
		try {
			@SuppressWarnings("rawtypes")
			Class c;
			try {
				c = Class.forName(this.className);
			} catch(ClassNotFoundException cnf) {
				try {
					c = Class.forName(RingedPlanet.class.getPackage().getName() + "." + this.className);
				} catch(ClassNotFoundException cnf2) {
					throw cnf;
				}
			}
			if(!TransitFunction.class.isAssignableFrom(c)) {
				throw new IllegalStateException("Class " + this.className + " is not assignable to " + TransitFunction.class.getName() + ".");
			}			
			Map<String, Object> params = this.getParams();
			if(params == null) {
				params = new HashMap<>();
			}
			@SuppressWarnings("unchecked")
			TransitFunction bf = SpecMapper.mapToPojo(params, (Class<TransitFunction>) c);
			return new EvaluatableSurfaceSphereFactory(bf);
		} catch(ClassNotFoundException cnf) {
			throw new IllegalStateException("Class not found: " + this.className + ".");
		}
	}
}
