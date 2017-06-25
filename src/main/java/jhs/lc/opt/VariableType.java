package jhs.lc.opt;

public enum VariableType {
	CONTINUOUS, BINARY, DISCRETE, NO_EFFECT;
	
	public static VariableType combine(VariableType v1, VariableType v2) {
		if(v1 == v2) {
			return v1;
		}
		if(v1 == NO_EFFECT || v2 == NO_EFFECT) {
			return BINARY;			
		}
		if(v1 == BINARY || v2 == BINARY) {
			return BINARY;
		}
		if(v1 == DISCRETE || v2 == DISCRETE) {
			return DISCRETE;
		}
		return v1;
	}
}
