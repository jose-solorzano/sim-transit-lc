package jhs.lc.opt.pof;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import jhs.lc.geom.FluxOrOpacityFunction;
import jhs.lc.geom.ParametricFluxFunctionSource;

public class PolygonalFluxFunctionSource implements ParametricFluxFunctionSource {
	private int numSides;
	private Rectangle2D imageBounds;
	
	public void init(Map<String,Object> params) {
		Object numSidesValue = params.get("numSides");
		if(numSidesValue == null) {
			throw new IllegalArgumentException("init parameter 'numSides' is required.");
		}
		Object imageWidthValue = params.get("imageWidth");
		if(imageWidthValue == null) {
			throw new IllegalArgumentException("init parameter 'imageWidthValue' is required.");
		}
		Object imageHeightValue = params.get("imageHeight");
		if(imageHeightValue == null) {
			throw new IllegalArgumentException("init parameter 'imageHeightValue' is required.");
		}
		this.numSides = Integer.parseInt(String.valueOf(numSidesValue));
		double imageWidth = Double.parseDouble(String.valueOf(imageWidthValue));
		double imageHeight = Double.parseDouble(String.valueOf(imageHeightValue));
		this.imageBounds = new Rectangle2D.Double(-imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight);
	}
	
	@Override
	public FluxOrOpacityFunction getFluxOrOpacityFunction(final double[] parameters) {
		return new FluxOrOpacityFunction() {			
			private static final long serialVersionUID = 1L;

			@Override
			public final Rectangle2D getBoundingBox() {
				return imageBounds;
			}
			
			@Override
			public final double fluxOrOpacity(double x, double y, double z) {
				int n = parameters.length / 3;
				for(int i = 0; i < n; i++) {
					int base = i * 3;
					double a = parameters[base];
					double b = parameters[base + 1];
					double c = parameters[base + 2];
					double v = a * x + b * y + c;
					if(v < 0) {
						return Double.NaN;
					}
				}
				return 0;				
			}
		};
	}

	@Override
	public final int getNumParameters() {
		return this.numSides * 3;
	}

	@Override
	public final double getParameterScale(int paramIndex) {
		return 0.5;
	}
}
