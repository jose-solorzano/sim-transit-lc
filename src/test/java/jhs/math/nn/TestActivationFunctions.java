package jhs.math.nn;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import jhs.math.nn.aa.AtanActivationFunction;
import jhs.math.nn.aa.GaussianActivationFunction;
import jhs.math.nn.aa.LinearActivationFunction;
import jhs.math.nn.aa.LinearNoBiasActivationFunction;
import jhs.math.nn.aa.LeakyReluActivationFunction;
import jhs.math.nn.aa.MaxActivationFunction;
import jhs.math.nn.aa.MinActivationFunction;
import jhs.math.nn.aa.MonodActivationFunction;
import jhs.math.nn.aa.PiecewiseActivationFunction;
import jhs.math.nn.aa.PulseActivationFunction;
import jhs.math.nn.aa.RbfLogActivationFunction;
import jhs.math.nn.aa.RbfOriginLogActivationFunction;
import jhs.math.nn.aa.RbfType;
import jhs.math.nn.aa.SigmoidActivationFunction;
import jhs.math.nn.aa.SignActivationFunction;
import jhs.math.nn.aa.SimpleMaxActivationFunction;
import jhs.math.nn.aa.SimpleMinActivationFunction;
import jhs.math.nn.aa.SumActivationFunction;
import jhs.math.util.MathUtil;

import org.junit.Test;

public class TestActivationFunctions {
	private static final int NUM_INPUTS = 2;

	@Test
	public void placeholder() {
	}
	
	@Test
	public void testResponseDistribution() {
		this.checkDistribution(new PulseActivationFunction(NUM_INPUTS));
		this.checkDistribution(new LinearActivationFunction(NUM_INPUTS));
		this.checkDistribution(new LinearNoBiasActivationFunction(NUM_INPUTS));		
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
		this.checkDistribution(new RbfLogActivationFunction(NUM_INPUTS, RbfType.EUCLIDEAN));
		this.checkDistribution(new RbfLogActivationFunction(NUM_INPUTS, RbfType.MANHATTAN));
		this.checkDistribution(new RbfLogActivationFunction(NUM_INPUTS, RbfType.TRIANGULAR));
		this.checkDistribution(new RbfLogActivationFunction(NUM_INPUTS, RbfType.SQUARE));		
		this.checkDistribution(new RbfOriginLogActivationFunction(NUM_INPUTS, RbfType.EUCLIDEAN));
		this.checkDistribution(new RbfOriginLogActivationFunction(NUM_INPUTS, RbfType.MANHATTAN));
		this.checkDistribution(new RbfOriginLogActivationFunction(NUM_INPUTS, RbfType.TRIANGULAR));
		this.checkDistribution(new RbfOriginLogActivationFunction(NUM_INPUTS, RbfType.SQUARE));		
		this.checkDistribution(new SumActivationFunction(NUM_INPUTS));
	}
	
	@Test
	public void testInputParamBoundaries() {
		this.checkBoundaries(new PulseActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new LinearActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new LinearNoBiasActivationFunction(NUM_INPUTS));		
		this.checkBoundaries(new SigmoidActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new LeakyReluActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new GaussianActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new AtanActivationFunction(NUM_INPUTS, 1.5));
		this.checkBoundaries(new MonodActivationFunction(NUM_INPUTS, 5.4, -3.333));
		this.checkBoundaries(new PiecewiseActivationFunction(NUM_INPUTS, 1.41));
		this.checkBoundaries(new SignActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new SimpleMaxActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new SimpleMinActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new MaxActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new MinActivationFunction(NUM_INPUTS));
		this.checkBoundaries(new RbfLogActivationFunction(NUM_INPUTS, RbfType.EUCLIDEAN));
		this.checkBoundaries(new RbfLogActivationFunction(NUM_INPUTS, RbfType.MANHATTAN));
		this.checkBoundaries(new RbfLogActivationFunction(NUM_INPUTS, RbfType.TRIANGULAR));
		this.checkBoundaries(new RbfLogActivationFunction(NUM_INPUTS, RbfType.SQUARE));
		this.checkBoundaries(new RbfOriginLogActivationFunction(NUM_INPUTS, RbfType.EUCLIDEAN));
		this.checkBoundaries(new RbfOriginLogActivationFunction(NUM_INPUTS, RbfType.MANHATTAN));
		this.checkBoundaries(new RbfOriginLogActivationFunction(NUM_INPUTS, RbfType.TRIANGULAR));
		this.checkBoundaries(new RbfOriginLogActivationFunction(NUM_INPUTS, RbfType.SQUARE));
		this.checkBoundaries(new SumActivationFunction(NUM_INPUTS));
	}
	
	private void checkBoundaries(ActivationFunction af) {
		Random random = new Random(19);
		double[] paramsBuffer = MathUtil.sampleGaussian(random, 1.0, NUM_INPUTS + 30);
		double[] inputBuffer = MathUtil.sampleGaussian(random, 1.0, NUM_INPUTS + 20);
		int inputIndex = 4;
		int paramIndex = 6;
		int numParams = af.getNumParameters(NUM_INPUTS);
		double baseActivation = af.activation(inputBuffer, inputIndex, NUM_INPUTS, paramsBuffer, paramIndex);
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < inputBuffer.length; j++) {
				if(j < inputIndex || j >= inputIndex + NUM_INPUTS) {
					inputBuffer[j] = random.nextGaussian();
				}
			}
			for(int j = 0; j < paramsBuffer.length; j++) {
				if(j < paramIndex || j >= paramIndex + numParams) {
					paramsBuffer[j] = random.nextGaussian();
				}
			}
			double activation = af.activation(inputBuffer, inputIndex, NUM_INPUTS, paramsBuffer, paramIndex);
			assertEquals(baseActivation, activation, 0.00001);
		}
	}
	
	private void checkDistribution(ActivationFunction af) {
		int numParams = af.getNumParameters(NUM_INPUTS);
		int n = 5000;
		Random random = new Random(17);
		double[] a = new double[n];
		for(int i = 0; i < n; i++) {
			double[] inputs = MathUtil.sampleGaussian(random, 1.0, NUM_INPUTS);
			double[] parameters = MathUtil.sampleGaussian(random, 1.0, numParams);
			a[i] = af.activation(inputs, 0, inputs.length, parameters, 0);
		}
		double mean = MathUtil.mean(a);
		double sd = MathUtil.standardDev(a, mean);
		System.out.println("Mean: " + mean);
		System.out.println("SD: " + sd);
		assertEquals(0, mean, 0.5);
		assertEquals(1.0, sd, 0.1);
	}
}
