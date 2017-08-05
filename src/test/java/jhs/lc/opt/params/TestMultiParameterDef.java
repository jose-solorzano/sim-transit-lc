package jhs.lc.opt.params;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestMultiParameterDef {
	@Test
	public void testLinearConversion() {
		double min = -5.75;
		double max = +0.33;
		MultiParameterDef pd = new MultiParameterDef(1, min, max, 0);
		assertEquals(min, pd.getValue(new double[] { -1000 }, 0), 0.0001);
		assertEquals(max, pd.getValue(new double[] { +1000 }, 0), 0.0001);
		assertEquals((min + max) / 2, pd.getValue(new double[] { 0 }, 0), 0.0001);
		assertEquals(0, pd.getSquaredDeviationFromRange(new double[] { 0 }), 0.0001);
	}
}
