package jhs.lc.tools.inputs;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jhs.lc.geom.ParametricFluxFunctionSource;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"
)
@JsonSubTypes({
	    @Type(value = ClassOptMethod.class, name = "code"),
	    @Type(value = ImageOptMethod.class, name = "image"),
	    @Type(value = NeuralOptMethod.class, name = "neural")
})
public abstract class AbstractOptMethod {
	public abstract ParametricFluxFunctionSource createFluxFunctionSource(File context) throws Exception;	
}
