package jhs.lc.opt.nn.inputs;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;

public final class ShiftRotateInputFilterFactory implements InputFilterFactory {
	private static final double PI_FACTOR = 1.813;
			
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
		final double pivotX = parameters[0];
		final double pivotY = parameters[1];
		double angle = parameters[2] * PI_FACTOR;
		//double rotA = parameters[2];
		//double rotB = parameters[3];
		double rotA = Math.cos(angle);
		double rotB = -Math.sin(angle);
		return new InputFilter() {			
			@Override
			public final double[] getInput(double x, double y) {
				double vx = x - pivotX;
				double vy = y - pivotY;
				double vxr = vx * rotA + vy * rotB;
				double vyr = vx * (-rotB) + vy * rotA;
				return new double[] { pivotX + vxr, pivotY + vyr };
			}
		};
	}
}
