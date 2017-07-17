package jhs.lc.data;

import static org.junit.Assert.*;
import java.util.Random;
import org.junit.Test;

public class TestLightCurve {
	private double[] produceSynthetic(int length, LtType type, double depth, double fromFraction, double toFraction) {
		if(toFraction < fromFraction) {
			throw new IllegalArgumentException();
		}
		double midPoint = (fromFraction + toFraction) / 2;
		double halfRange = midPoint - fromFraction;
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			double f = (i + 0.5) / length;
			if(f >= fromFraction && f <= toFraction) {
				double x = (f - midPoint) * depth / halfRange;
				switch(type) {
				case ARC: {
					double y = Math.sqrt(depth * depth - x * x);
					result[i] = 1.0 - y;
					break;
				}
				case TRIANGLE: {
					double y = depth - Math.abs(x);
					result[i] = 1.0 - y;
					break;
				}
				}
			}
			else {
				result[i] = 1.0;
			}
		}
		return result;
	}

	private enum LtType {
			ARC, TRIANGLE;
	}
}
