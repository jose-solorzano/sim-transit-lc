package jhs.lc.opt.nn;

import com.fasterxml.jackson.annotation.JsonCreator;
import jhs.lc.opt.nn.inputs.*;

public enum InputFilterType {
	PLAIN(new PlainInputFilterFactory()), 
	QUADRATIC(new QuadraticInputFilterFactory()), 
	QUADRATIC_XY(new QuadraticXyInputFilterFactory()), 
	SHIFT(new ShiftInputFilterFactory()), 
	SHIFT_ROTATE(new ShiftRotateInputFilterFactory());

	private final InputFilterFactory factory;
	
	private InputFilterType(InputFilterFactory factory) {
		this.factory = factory;
	}
	
	public final InputFilterFactory getFactory() {
		return factory;
	}

	@JsonCreator
	public static InputFilterType create(String value) {
		return InputFilterType.valueOf(value.toUpperCase());
	}
}
