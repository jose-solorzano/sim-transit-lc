package jhs.math.nn;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import jhs.math.nn.aa.AtanActivationFunction;
import jhs.math.nn.aa.GaussianActivationFunction;
import jhs.math.nn.aa.IdentityActivationFunction;
import jhs.math.nn.aa.LeakyReluActivationFunction;
import jhs.math.nn.aa.MaxActivationFunction;
import jhs.math.nn.aa.MinActivationFunction;
import jhs.math.nn.aa.MonodActivationFunction;
import jhs.math.nn.aa.PiecewiseActivationFunction;
import jhs.math.nn.aa.RbfActivationFunction;
import jhs.math.nn.aa.RbfType;
import jhs.math.nn.aa.SigmoidActivationFunction;
import jhs.math.nn.aa.SignActivationFunction;
import jhs.math.nn.aa.SimpleMaxActivationFunction;
import jhs.math.nn.aa.SimpleMinActivationFunction;
import jhs.math.nn.aa.SumActivationFunction;
import jhs.math.util.MathUtil;

public class TestActivationFunctions {
	private static final int NUM_INPUTS = 30;

	@Test
	public void testResponseDistribution() {
		this.checkDistribution(new IdentityActivationFunction(NUM_INPUTS));
		this.checkDistribution(new SigmoidActivationFunction(NUM_INPUTS));
		this.checkDistribution(new LeakyReluActivationFunction(NUM_INPUTS));
		this.checkDistribution(new GaussianActivationFunction(NUM_INPUTS));
		this.checkDistribution(new AtanActivationFunction(NUM_INPUTS, 1.5));
		this.checkDistribution(new MonodActivationFunction(NUM_INPUTS, 5.4, -3.333));
		this.checkDistribution(new PiecewiseActivationFunction(NUM_INPUTS, 1.41));
		this.checkDistribution(new SignActivationFunction(NUM_INPUTS));
		this.checkDistribution(new SimpleMaxActivationFunction(NUM_INPUTS));
		this.checkDistribution(new SimpleMinActivationFunction(NUM_INPUTS));
		this.checkDistribution(new MaxActivationFunction(NUM_INPUTS));
		this.checkDistribution(new MinActivationFunction(NUM_INPUTS));
		this.checkDistribution(new RbfActivationFunction(NUM_INPUTS, RbfType.EUCLIDEAN));
		this.checkDistribution(new RbfActivationFunction(NUM_INPUTS, RbfType.MANHATTAN));
		this.checkDistribution(new RbfActivationFunction(NUM_INPUTS, RbfType.TRIANGULAR));
		this.checkDistribution(new SumActivationFunction(NUM_INPUTS));
	}
	
	private void checkDistribution(ActivationFunction af) {
		int numParams = af.getNumParameters(NUM_INPUTS);
		int n = 1000;
		Random random = new Random(17);
		double[] a = new double[n];
		for(int i = 0; i < n; i++) {
			double[] inputs = MathUtil.sampleGaussian(random, 1.0, NUM_INPUTS);
			double[] parameters = MathUtil.sampleGaussian(random, 1.0, numParams);
			a[i] = af.activation(inputs, parameters, 0);
		}
		double mean = MathUtil.mean(a);
		double sd = MathUtil.standardDev(a, mean);
		System.out.println("Mean: " + mean);
		System.out.println("SD: " + sd);
		assertEquals(0, mean, 0.3);
		assertEquals(1.0, sd, 0.1);
	}
}
