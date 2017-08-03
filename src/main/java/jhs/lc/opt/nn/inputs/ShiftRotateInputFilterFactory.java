package jhs.lc.opt.nn.inputs;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.lc.opt.nn.inputs.ShiftInputFilterFactory.Props;
import jhs.lc.util.Initializable;

public final class ShiftRotateInputFilterFactory implements InputFilterFactory, Initializable<ShiftRotateInputFilterFactory.Props> {
	static final double PI_FACTOR = 1.813;

	private double scaleX = 1.0;
	private double scaleY = 1.0;
	
	public void init(Props properties) {
		this.scaleX = properties.getScaleX();
		this.scaleY = properties.getScaleY();
	}
	

	@Override
	public final int getNumParameters() {
		return 3;
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
		double angle = parameters[2] * PI_FACTOR;
		double rotA = Math.cos(angle);
		double rotB = -Math.sin(angle);
		double xFactor = 1.0 / Math.sqrt(1.0 + scaleX * scaleX);
		double yFactor = 1.0 / Math.sqrt(1.0 + scaleY * scaleY);
		return new FilterImpl(originX, originY, rotA, rotB, xFactor, yFactor);
	}
	
	private static final class FilterImpl implements InputFilter {
		private final double originX;
		private final double originY;
		private final double rotA, rotB;
		private final double xFactor;
		private final double yFactor;
		
		public FilterImpl(double originX, double originY, double rotA, double rotB, double xFactor, double yFactor) {
			super();
			this.originX = originX;
			this.originY = originY;
			this.rotA = rotA;
			this.rotB = rotB;
			this.xFactor = xFactor;
			this.yFactor = yFactor;
		}


		@Override
		public final double[] getInput(double x, double y) {
			double originX = this.originX;
			double originY = this.originY;
			
			double sx = x - originX;
			double sy = y - originY;
			double vxr = sx * rotA + sy * rotB;
			double vyr = sx * (-rotB) + sy * rotA;
			return new double[] { vxr * xFactor, vyr * yFactor };
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
