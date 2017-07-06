package jhs.lc.opt.nn;

public interface InputFilterFactory {
	int getNumParameters();
	InputFilter createInputFilter(double[] parameters);
}
