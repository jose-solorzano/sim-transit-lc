package jhs.lc.tools.inputs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jhs.lc.geom.Point3D;
import jhs.lc.geom.RotationAngleSphereFactory;
import jhs.lc.geom.Sphere;
import jhs.lc.geom.SphereCollectionSphereFactory;
import jhs.math.util.ListUtil;

public class SpheresTransitShape extends AbstractTransitShape {
	private List<SphereSpec> spheres = new ArrayList<>();
	
	@JsonProperty(required = true)
	public final List<SphereSpec> getSpheres() {
		return spheres;
	}

	public final void setSpheres(List<SphereSpec> spheres) {
		this.spheres = spheres;
	}

	@Override
	public RotationAngleSphereFactory createSphereFactory(File context) throws Exception {
		List<SphereSpec> sphereSpecs = this.spheres;
		Sphere[] spheres = ListUtil.map(sphereSpecs, ss -> ss.asSphere()).toArray(new Sphere[0]);
		Point3D[] relativePositions = ListUtil.map(sphereSpecs, ss -> ss.asPoint3D()).toArray(new Point3D[0]);
		return new SphereCollectionSphereFactory(spheres, relativePositions);
	}
}
