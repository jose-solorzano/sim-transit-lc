package jhs.lc.geom;

import java.awt.geom.Rectangle2D;

public class EvaluatableSurfaceSphere extends AbstractRotatableSphere {
	private final FluxOrOpacityFunction opacityFunction;
	
	public EvaluatableSurfaceSphere(double radius, PointTransformer transformer, FluxOrOpacityFunction opacityFunction) {
		super(radius, transformer);
		this.opacityFunction = opacityFunction;
	}

	public static EvaluatableSurfaceSphere create(double radius, double inclineAngle, FluxOrOpacityFunction opacityFunction) {
		Plane3D rotationPlane = Planes.inclinedPlane(inclineAngle);
		PointTransformer transformer = PointTransformer.getPointTransformer(rotationPlane, Planes.yzPlane());
		return new EvaluatableSurfaceSphere(radius, transformer, opacityFunction);
	}
	
	@Override
	public final boolean isOnlyFrontVisible() {
		return true;
	}

	@Override
	public final double getUnrotatedBrightness(double x, double y, double z) {
		double a = this.opacityFunction.fluxOrOpacity(x, y, z);
		if(a < -1.0) {
			a = -1.0;
		} else if(a > +1.0) {
			a = +1.0;
		}
		return a;
	}

	@Override
	public Rectangle2D getUnrotatedBoundingBox() {
		return this.opacityFunction.getBoundingBox();
	}
}
