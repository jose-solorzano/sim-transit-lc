package jhs.lc.opt.nn.inputs;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;

public final class PlainInputFilterFactory implements InputFilterFactory {
	@Override
	public final int getNumParameters() {
		return 0;
	}

	@Override
	public final InputFilter createInputFilter(double[] parameters) {
		return new InputFilter() {			
			@Override
			public final double[] getInput(double x, double y) {
				return new double[] { x, y };
			}
		};
	}
}
