package jhs.lc.opt.nn.inputs;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.math.util.MathUtil;

public class TestInputFilters {
	@Test
	public void temp() {
		double[] x = new double[20000];
		Random r = new Random(11);
		for(int i = 0; i < x.length; i++) {
			x[i] = r.nextGaussian() + r.nextGaussian() * 0.5;
		}
	}
	
	@Test
	public void testOutputDistribution() {
		this.confirmInputFilterDistribution(this.createShiftInputFilterFactory());
		this.confirmInputFilterDistribution(this.createShiftRotateInputFilterFactory());
		//TODO:
		//this.confirmInputFilterDistribution(new QuadraticInputFilterFactory());
		//this.confirmInputFilterDistribution(new QuadraticXyInputFilterFactory());
	}
	
	private ShiftInputFilterFactory createShiftInputFilterFactory() {
		ShiftInputFilterFactory factory = new ShiftInputFilterFactory();
		ShiftInputFilterFactory.Props props = new ShiftInputFilterFactory.Props();
		props.setScaleX(2.33);
		props.setScaleY(0.66);
		factory.init(props);
		return factory;
	}

	private ShiftRotateInputFilterFactory createShiftRotateInputFilterFactory() {
		ShiftRotateInputFilterFactory factory = new ShiftRotateInputFilterFactory();
		ShiftRotateInputFilterFactory.Props props = new ShiftRotateInputFilterFactory.Props();
		props.setScaleX(0.77);
		props.setScaleY(1.55);
		factory.init(props);
		return factory;
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
		assertEquals(0, mean, 0.1);
		assertEquals(1.0, sd, 0.1);
	}

}
