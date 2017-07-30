package jhs.math.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestArrayUtil {
	@Test
	public void testRank() {
		ValueHolder[] array = new ValueHolder[] {
			new ValueHolder(5),	
			new ValueHolder(3),	
			new ValueHolder(7),	
			new ValueHolder(4),	
			new ValueHolder(2),	
			new ValueHolder(1),	
			new ValueHolder(9),	
		};
		int[] ranks = ArrayUtil.ranks(array, ValueHolder::getValue);
		assertArrayEquals(new int[] { 4, 2, 5, 3, 1, 0, 6 }, ranks);
	}

	private static class ValueHolder {
		private final double value;

		public ValueHolder(double value) {
			super();
			this.value = value;
		}

		public final double getValue() {
			return value;
		}
	}
}
