package jhs.lc.opt.nn.inputs;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.math.util.MathUtil;

public class TestInputFilters {

	@Test
	public void testOutputDistribution() {
		this.confirmInputFilterDistribution(new ShiftInputFilterFactory());
		this.confirmInputFilterDistribution(new ShiftRotateInputFilterFactory());
		//TODO:
		//this.confirmInputFilterDistribution(new QuadraticInputFilterFactory());
		//this.confirmInputFilterDistribution(new QuadraticXyInputFilterFactory());
	}
	
	private void confirmInputFilterDistribution(InputFilterFactory factory) {
		Random random = new Random(11);
		int numParams = factory.getNumParameters();
		int numTests = 5000;
		double[] values = new double[numTests];
		for(int i = 0; i < numTests; i++) {
			double[] parameters = MathUtil.sampleUniformSymmetric(random, 1.0, numParams);
			InputFilter filter = factory.createInputFilter(parameters);
			double[] position = MathUtil.sampleUniformSymmetric(random, 1.0, 2);
			double[] input = filter.getInput(position[0], position[1]);
			values[i] = MathUtil.sum(input) / Math.sqrt(input.length);
		}
		double mean = MathUtil.mean(values);
		double sd = MathUtil.standardDev(values, mean);
		assertEquals(0, mean, 0.05);
		assertEquals(1.0, sd, 0.05);
	}

}
