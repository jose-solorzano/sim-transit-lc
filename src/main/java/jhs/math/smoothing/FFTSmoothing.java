package jhs.math.smoothing;

import java.util.Arrays;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import jhs.math.util.ArrayUtil;
import jhs.math.util.MathUtil;

public class FFTSmoothing {
	private static final double LOG2 = Math.log(2);
	
	public static double[] lowPassFilter(double[] data, double fillValue, double cutFraction) {
		double x = Math.log(data.length) / LOG2;
		double rx = Math.round(x);
		if(Math.abs(x - Math.round(x)) > 1E-6 || rx <= 1) {
			int cx = (int) Math.ceil(x);
			int expectedLength = (int) Math.round(MathUtil.powPosInt(2, cx));
			double[] filling = new double[expectedLength - data.length];
			Arrays.fill(filling, fillValue);
			data = ArrayUtil.concat(data, filling);
		}			
		FastFourierTransformer fft = new FastFourierTransformer();
		Complex[] result = fft.transform(data);
		int length = result.length;
		int lengthToCut = (int) (length * cutFraction);
		if((lengthToCut % 2) != 0) {
			lengthToCut++;
		}
		int fromIndex = (length / 2) - (lengthToCut / 2);
		int toIndex = (length / 2) + (lengthToCut / 2) + 1;
		Arrays.fill(result, fromIndex, toIndex, new Complex(0, 0));
		Complex[] inverse = fft.inversetransform(result);
		double[] realResult = new double[length];
		for(int i = 0; i < length; i++) {
			realResult[i] = inverse[i].getReal();
		}
		return realResult;		
	}
}
