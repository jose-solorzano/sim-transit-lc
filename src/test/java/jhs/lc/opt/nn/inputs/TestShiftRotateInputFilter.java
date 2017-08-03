package jhs.lc.opt.nn.inputs;

import static org.junit.Assert.*;

import org.junit.Test;

import jhs.lc.opt.nn.InputFilter;
import jhs.lc.opt.nn.InputFilterFactory;
import jhs.lc.opt.nn.InputFilterType;

public class TestShiftRotateInputFilter {
	@Test
	public void testShift() {
		this.confirmShiftRotate(0, 0, 0, 0, 0, 0, 0);
		this.confirmShiftRotate(-1.5, +3.5, 0, 0, 0, +1.5, -3.5);
	}

	@Test
	public void testRotate() {
		this.confirmShiftRotate(0, 0, -2 * Math.PI, 
				1, 0, 
				1, 0);
		this.confirmShiftRotate(0, 0, Math.PI / 2, 
				1, 0, 
				0, 1);
		this.confirmShiftRotate(0, 0, Math.PI, 
				1, 0, 
				-1, 0);
		this.confirmShiftRotate(0, 0, 3 * Math.PI / 2, 
				1, 0, 
				0, -1);
	}

	@Test
	public void testShiftRotate() {
		this.confirmShiftRotate(+1, +1, Math.PI / 2, 
				1, 0, 
				1, 0);		
		this.confirmShiftRotate(-1, -1, Math.PI, 
				0, 0, 
				-1, -1);		
	}

	private void confirmShiftRotate(double shiftX, double shiftY, double angle, double inX, double inY, double outX, double outY) {
		double scaleX = 2.33;
		double scaleY = 0.66;
		ShiftRotateInputFilterFactory factory = new ShiftRotateInputFilterFactory();
		ShiftRotateInputFilterFactory.Props properties = new ShiftRotateInputFilterFactory.Props();
		properties.setScaleX(scaleX);
		properties.setScaleY(scaleY);
		factory.init(properties);
		InputFilter filter = factory.createInputFilter(new double[] { shiftX / scaleX, shiftY / scaleY, angle / ShiftRotateInputFilterFactory.PI_FACTOR });
		double[] result = filter.getInput(inX, inY);
		assertEquals(outX / Math.sqrt(1.0 + scaleX * scaleX), result[0], 0.0001);
		assertEquals(outY / Math.sqrt(1.0 + scaleY * scaleY), result[1], 0.0001);
	}
}
