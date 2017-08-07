package jhs.lc.opt.params;

public class ParameterSet<T extends Enum<T>> {
	private final ParameterDef[] paramMap;
	private int paramCount = 0;
	
	public ParameterSet(T[] allIds) {
		this.paramMap = new ParameterDef[allIds.length];
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
		int ordinal = id.ordinal();
		if(this.paramMap[ordinal] != null) {
			throw new IllegalArgumentException("ID " + id + " already added.");
		}		
		ParameterDef paramDef = new PlainParameterDef(numParameters, min, max, this.paramCount);
		this.paramMap[ordinal] = paramDef;
		this.paramCount += numParameters;
	}

	private void addExpParameterDefImpl(T id, int numParameters, double min, double max) {
		int ordinal = id.ordinal();
		if(this.paramMap[ordinal] != null) {
			throw new IllegalArgumentException("ID " + id + " already added.");
		}		
		ParameterDef paramDef = new ExpParameterDef(numParameters, min, max, this.paramCount);
		this.paramMap[ordinal] = paramDef;
		this.paramCount += numParameters;
	}

	public final int getNumParameters() {
		return this.paramCount;
	}
	
	public final double getValue(T id, double[] parameters) {
		return this.getValue(id, parameters, 0);
	}
	
	public final double getValue(T id, double[] parameters, int indexOffset) {
		ParameterDef paramDef = this.paramMap[id.ordinal()];
		if(paramDef == null) {
			throw new IllegalArgumentException("No parameter with that ID: " + id);
		}
		return paramDef.getValue(parameters, indexOffset);
	}

	public final double[] getValues(T id, double[] parameters) {
		ParameterDef paramDef = this.paramMap[id.ordinal()];
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
