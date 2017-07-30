package jhs.lc.opt.nn.inputs;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;

public final class ShiftInputFilterFactory implements InputFilterFactory {
	private static final double K = Math.sqrt(2);
	
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
		final double originX = parameters[0];
		final double originY = parameters[1];
		return new InputFilter() {			
			@Override
			public final double[] getInput(double x, double y) {
				return new double[] { (x - originX) / K, (y - originY) / K };
			}
		};
	}
}
