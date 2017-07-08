package jhs.lc.opt.nn;

public interface InputFilterFactory {
	int getNumParameters();
	int getNumTransformedInputs();
	InputFilter createInputFilter(double[] parameters);
}
