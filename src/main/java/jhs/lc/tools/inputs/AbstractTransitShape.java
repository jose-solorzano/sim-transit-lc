package jhs.lc.tools.inputs;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jhs.lc.geom.RotationAngleSphereFactory;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"
)
@JsonSubTypes({
	    @Type(value = ClassTransitShape.class, name = "code"),
	    @Type(value = ImageTransitShape.class, name = "image"),
	    @Type(value = SpheresTransitShape.class, name = "spheres")
})
public abstract class AbstractTransitShape {	
	public abstract RotationAngleSphereFactory createSphereFactory(File context) throws Exception;	
}
