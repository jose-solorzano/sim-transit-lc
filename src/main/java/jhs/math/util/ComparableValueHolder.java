package jhs.math.util;

public abstract class ComparableValueHolder<T extends ComparableValueHolder<?>> implements Comparable<T> {
	protected final long id;
	private static long idCounter = 0;
	protected abstract double getValue();
	
	public ComparableValueHolder() {
		synchronized(ComparableValueHolder.class) {
			this.id = idCounter++;
		}
	}

	public final long getId() {
		return id;
	}

	public final int compareTo(T o) {
		double value1 = this.getValue();
		double value2 = o.getValue();
		if(value1 > value2) {
			return +1;
		}
		else if(value2 > value1) {
			return -1;
		}
		else {
			if(this.id > o.id) {
				return +1;
			}
			else if(this.id < o.id) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}

	@Override
	public final int hashCode() {
		return (int) this.id;
	}

	@Override
	public final boolean equals(Object obj) {
		return this == obj;
	}	
}
