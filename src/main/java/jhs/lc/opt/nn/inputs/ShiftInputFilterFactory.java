package jhs.lc.opt.nn.inputs;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.lc.util.Initializable;

public final class ShiftInputFilterFactory implements InputFilterFactory, Initializable<ShiftInputFilterFactory.Props> {
	private double scaleX = 1.0;
	private double scaleY = 1.0;
	
	public void init(Props properties) {
		this.scaleX = properties.getScaleX();
		this.scaleY = properties.getScaleY();
	}
	
	@Override
	public final int getNumParameters() {
		return 2;
	}

	@Override
	public final int getNumTransformedInputs() {
		return 2;
	}

	@Override
	public final InputFilter createInputFilter(double[] parameters) {
		double scaleX = this.scaleX;
		double scaleY = this.scaleY;
		double originX = parameters[0] * scaleX;
		double originY = parameters[1] * scaleY;
		double xFactor = 1.0 / Math.sqrt(1.0 + scaleX * scaleX);
		double yFactor = 1.0 / Math.sqrt(1.0 + scaleY * scaleY);
		return new FilterImpl(originX, originY, xFactor, yFactor);
	}
	
	private static final class FilterImpl implements InputFilter {
		private final double originX;
		private final double originY;
		private final double xFactor;
		private final double yFactor;

		public FilterImpl(double originX, double originY, double scaleX, double scaleY) {
			super();
			this.originX = originX;
			this.originY = originY;
			this.xFactor = scaleX;
			this.yFactor = scaleY;
		}

		@Override
		public final double[] getInput(double x, double y) {
			return new double[] { (x - originX) * xFactor, (y - originY) * yFactor };
		}		
	}
	
	public static class Props {
		private double scaleX = 1.0;
		private double scaleY = 1.0;

		public final double getScaleX() {
			return scaleX;
		}

		public final void setScaleX(double scaleX) {
			this.scaleX = scaleX;
		}

		public final double getScaleY() {
			return scaleY;
		}

		public final void setScaleY(double scaleY) {
			this.scaleY = scaleY;
		}
	}
}
