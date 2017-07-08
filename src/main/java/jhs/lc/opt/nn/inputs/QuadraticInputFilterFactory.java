package jhs.lc.opt.nn.inputs;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;

public final class QuadraticInputFilterFactory implements InputFilterFactory {
	private static final double SQ_MEAN = 1.0;
	private static final double SQ_SD = Math.sqrt(2.0);
	
	@Override
	public final int getNumParameters() {
		return 0;
	}

	@Override
	public final int getNumTransformedInputs() {
		return 4;
	}

	@Override
	public final InputFilter createInputFilter(double[] parameters) {
		return new InputFilter() {			
			@Override
			public final double[] getInput(double x, double y) {
				return new double[] { x, y, (x * x - SQ_MEAN) / SQ_SD,  (y * y - SQ_MEAN) / SQ_SD };
			}
		};
	}
}
