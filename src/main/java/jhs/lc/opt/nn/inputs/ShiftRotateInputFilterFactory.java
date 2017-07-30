package jhs.lc.opt.nn.inputs;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;

public final class ShiftRotateInputFilterFactory implements InputFilterFactory {
	static final double PI_FACTOR = 1.813;
	static final double K = Math.sqrt(2);
			
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
		final double originX = parameters[0];
		final double originY = parameters[1];
		double angle = parameters[2] * PI_FACTOR;
		//double rotA = parameters[2];
		//double rotB = parameters[3];
		double rotA = Math.cos(angle);
		double rotB = -Math.sin(angle);
		return new InputFilter() {			
			@Override
			public final double[] getInput(double x, double y) {
				double sx = x - originX;
				double sy = y - originY;
				double vxr = sx * rotA + sy * rotB;
				double vyr = sx * (-rotB) + sy * rotA;
				return new double[] { vxr / K, vyr / K };
			}
		};
	}
}
