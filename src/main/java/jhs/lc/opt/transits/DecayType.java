package jhs.lc.opt.transits;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DecayType {
	LINEAR, EXPONENTIAL, MONOD;
	
	@JsonCreator
	public static DecayType create(String value) {
		return DecayType.valueOf(value.toUpperCase());
	}
}
