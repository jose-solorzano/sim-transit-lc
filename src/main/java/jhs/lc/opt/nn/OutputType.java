package jhs.lc.opt.nn;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OutputType {
	BINARY, OPACITY, OPACITY_LOGISTIC, BRIGHTNESS, ALL;
	
	@JsonCreator
	public static OutputType create(String value) {
		return OutputType.valueOf(value.toUpperCase());
	}
}
