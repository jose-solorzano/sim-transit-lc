package jhs.lc.opt.nn;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum InputType {
	PLAIN, QUADRATIC, QUADRATIC_WP;

	@JsonCreator
	public static InputType create(String value) {
		return InputType.valueOf(value.toUpperCase());
	}
}
