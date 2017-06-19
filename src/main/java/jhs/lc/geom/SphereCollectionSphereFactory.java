package jhs.lc.geom;

public class SphereCollectionSphereFactory implements RotationAngleSphereFactory {
	private final Sphere[] spheres;
	private final Point3D[] relativePositions;
	
	public SphereCollectionSphereFactory(Sphere[] spheres, Point3D[] relativePositions) {
		super();
		this.spheres = spheres;
		this.relativePositions = relativePositions;
	}

	@Override
	public AbstractRotationAngleSphere create(double radius, double inclineAngle) {
		Plane3D rotationPlane = Planes.inclinedPlane(inclineAngle);
		PointTransformer transformer = PointTransformer.getPointTransformer(rotationPlane, Planes.yzPlane());
		return new SphereCollectionSphere(radius, transformer, spheres, relativePositions);
	}
}
