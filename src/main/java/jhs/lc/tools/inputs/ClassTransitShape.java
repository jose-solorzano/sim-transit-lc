package jhs.lc.tools.inputs;

import java.io.File;

import jhs.lc.geom.EvaluatableSurfaceSphereFactory;
import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.RotationAngleSphereFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassTransitShape extends AbstractTransitShape {
	private String className;

	@JsonProperty(required = true)
	public final String getClassName() {
		return className;
	}

	public final void setClassName(String className) {
		this.className = className;
	}

	@Override
	public RotationAngleSphereFactory createSphereFactory(File context) throws Exception {
		try {
			@SuppressWarnings("rawtypes")
			Class c = Class.forName(this.className);
			if(!FluxOrOpacityFunction.class.isAssignableFrom(c)) {
				throw new IllegalStateException("Class " + this.className + " is not assignable to " + FluxOrOpacityFunction.class.getName() + ".");
			}
			FluxOrOpacityFunction bf = (FluxOrOpacityFunction) c.newInstance();
			return new EvaluatableSurfaceSphereFactory(bf);
		} catch(ClassNotFoundException cnf) {
			throw new IllegalStateException("Class not found: " + this.className + ".");
		}
	}
}
