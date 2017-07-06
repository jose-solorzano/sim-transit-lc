package jhs.lc.opt.nn.inputs;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;

public final class ShiftInputFilterFactory implements InputFilterFactory {
	@Override
	public final int getNumParameters() {
		return 4;
	}

	@Override
	public final InputFilter createInputFilter(double[] parameters) {
		final double pivotX = parameters[0];
		final double pivotY = parameters[1];
		double rotA = parameters[2];
		double rotB = parameters[3];
		double mag = Math.sqrt(rotA * rotA + rotB * rotB);
		final double cosAngle = rotB / mag;
		final double sinAngle = rotA / mag;
		return new InputFilter() {			
			@Override
			public final double[] getInput(double x, double y) {
				double vx = x - pivotX;
				double vy = y - pivotY;
				double mag = Math.sqrt(vx * vx + vy * vy);				
				return new double[] { pivotX + mag * cosAngle, pivotY + mag * sinAngle };
			}
		};
	}
}
