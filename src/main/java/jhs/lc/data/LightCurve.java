package jhs.lc.data;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jhs.math.regression.linear.WeightedLinearRegression;
import jhs.math.util.ArrayUtil;


public class LightCurve {
	private final LightCurvePoint[] points;

	public LightCurve(LightCurvePoint[] points) {
		super();
		this.points = points;
	}

	public final LightCurvePoint[] getPoints() {
		return points;
	}
	
	public void write(File outFile) throws IOException {
		PrintWriter out = new PrintWriter(outFile);
		try {
			out.println("Timestamp,Flux"); 
			for(LightCurvePoint point : this.points) {
				out.println(point.getTimestamp() + "," + point.getFlux());
			}
		} finally {
			out.close();
		}
	}
	
	public static double[] trendChangeProfile(double[] fluxArray, int windowLength) {
		int length = fluxArray.length;
		int hwl = windowLength / 2;
		int start = hwl;
		int end = length - hwl;
		double[] trends = new double[length];
		for(int i = start; i < end; i++) {
			trends[i] = trend(fluxArray, i - hwl, i + hwl);
		}
		double[] trendChanges = new double[length];
		for(int i = 1; i < length - 1; i++) {
			double prev = trends[i - 1];
			double next = trends[i + 1];
			trendChanges[i] = next - prev;
		}
		return trendChanges;
	}
		
	private static double trend(double[] array, int firstIndex, int lastIndex) {
		WeightedLinearRegression regression = new WeightedLinearRegression();
		for(int x = firstIndex; x <= lastIndex; x++) {
			regression.addData(1.0, x, array[x]);
		}
		return regression.getSlope();
	}

}
