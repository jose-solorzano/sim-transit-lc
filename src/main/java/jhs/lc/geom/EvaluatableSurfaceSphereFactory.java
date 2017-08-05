package jhs.lc.geom;

public class EvaluatableSurfaceSphereFactory implements RotationAngleSphereFactory {
	private final TransitFunction brightnessFunction;
	
	public EvaluatableSurfaceSphereFactory(TransitFunction brightnessFunction) {
		super();
		this.brightnessFunction = brightnessFunction;
	}

	@Override
	public final AbstractRotationAngleSphere create(double radius, double inclineAngle) {
		return EvaluatableSurfaceSphere.create(radius, inclineAngle, this.brightnessFunction);
	}
}
