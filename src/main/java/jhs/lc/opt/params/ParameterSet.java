package jhs.lc.opt.params;

import java.util.HashMap;
import java.util.Map;

public class ParameterSet<T> {
	private final ParameterDef[] paramMap;
	private final Map<T, Integer> ordinalMap = new HashMap<>();
	private int paramCount = 0;
	
	public ParameterSet(T[] allIds) {
		this.paramMap = new ParameterDef[allIds.length];
	}

	public ParameterSet(int numParams) {
		this.paramMap = new ParameterDef[numParams];
	}

	public final int getUniqueParameterCount() {
		return this.ordinalMap.size();
	}
	
	public final int getParameterIndex(T id) {
		if (id instanceof Enum) {
			return ((Enum<?>) id).ordinal();
		}
		Integer index = this.ordinalMap.get(id);
		if(index == null) {
			throw new IllegalArgumentException("Parameter ID " + id + " is undefined.");
		}
		return index.intValue();
	}
	
	public final void addParameterDef(T id, double ... bounds) {
		if(bounds == null || bounds.length != 2) {
			throw new IllegalArgumentException("Parameter bounds must be an array of length 2.");
		}
		this.addMultiParameterDefImpl(id, 1, bounds[0], bounds[1]);
	}

	public final void addExpParameterDef(T id, double ... bounds) {
		if(bounds == null || bounds.length != 2) {
			throw new IllegalArgumentException("Parameter bounds must be an array of length 2.");
		}
		this.addExpParameterDefImpl(id, 1, bounds[0], bounds[1]);
	}

	public final void addMultiParameterDef(T id, int numParameters, double ... bounds) {
		if(bounds == null || bounds.length != 2) {
			throw new IllegalArgumentException("Parameter bounds must be an array of length 2.");
		}
		this.addMultiParameterDefImpl(id, numParameters, bounds[0], bounds[1]);
	}

	private void addMultiParameterDefImpl(T id, int numParameters, double min, double max) {
		int ordinal = ordinal(id);
		if(this.paramMap[ordinal] != null) {
			throw new IllegalArgumentException("ID " + id + " already added.");
		}		
		ParameterDef paramDef = new PlainParameterDef(numParameters, min, max, this.paramCount);
		this.paramMap[ordinal] = paramDef;
		this.paramCount += numParameters;
	}

	private void addExpParameterDefImpl(T id, int numParameters, double min, double max) {
		int ordinal = ordinal(id);
		if(this.paramMap[ordinal] != null) {
			throw new IllegalArgumentException("ID " + id + " already added.");
		}		
		ParameterDef paramDef = new ExpParameterDef(numParameters, min, max, this.paramCount);
		this.paramMap[ordinal] = paramDef;
		this.paramCount += numParameters;
	}

	private final int ordinal(T value) {
		if(value instanceof Enum) {
			return ((Enum<?>) value).ordinal();
		}
		else {
			Integer ordinal = this.ordinalMap.get(value);
			if(ordinal == null) {
				ordinal = this.ordinalMap.size();
				this.ordinalMap.put(value, ordinal);
			}
			return ordinal;
		}		
	}
	
	public final int getNumParameters() {
		return this.paramCount;
	}
	
	public final double getValue(T id, double[] parameters) {
		return this.getValue(id, parameters, 0);
	}
	
	public final double getValue(T id, double[] parameters, int indexOffset) {
		ParameterDef paramDef = this.paramMap[ordinal(id)];
		if(paramDef == null) {
			throw new IllegalArgumentException("No parameter with that ID: " + id);
		}
		return paramDef.getValue(parameters, indexOffset);
	}

	public final double getValueFromIndex(int ordinal, double[] parameters) {
		ParameterDef paramDef = this.paramMap[ordinal];
		if(paramDef == null) {
			throw new IllegalArgumentException("No parameter with that ordinal: " + ordinal);
		}
		return paramDef.getValue(parameters, 0);
	}

	public final double[] getValues(T id, double[] parameters) {
		ParameterDef paramDef = this.paramMap[ordinal(id)];
		if(paramDef == null) {
			throw new IllegalArgumentException("No parameter with that ID: " + id);
		}
		return paramDef.getValues(parameters);
	}

	public final double getExtraOptimizerError(double[] parameterPool, double lambda) {
		ParameterDef[] paramMap = this.paramMap;
		double sum = 0;
		for(ParameterDef paramDef : paramMap) {
			if(paramDef != null) {
				double sqDev = paramDef.getSquaredDeviationFromRange(parameterPool);
				sum += sqDev;
			}
		}
		int n = this.paramCount;
		return n == 0 ? 0 : sum * lambda / n;
	}
}
