package jhs.math.regression.linear;

import java.util.List;

public class UnivariateObservation {
	private static int idCounter = 0;
	private final double x, y;	
	private final double weight;
	final int id;

	public UnivariateObservation(double x, double y) {
		this.x = x;
		this.y = y;
		this.weight = 1.0;
		synchronized(UnivariateObservation.class) {
			this.id = idCounter++;
		}
	}

	public UnivariateObservation(double weight, double x, double y) {
		super();
		this.x = x;
		this.y = y;
		this.weight = weight;
		synchronized(UnivariateObservation.class) {
			this.id = idCounter++;
		}
	}

	public final double getX() {
		return x;
	}

	public final double getY() {
		return y;
	}

	public final double getWeight() {
		return weight;
	}
	
	public static double[] getXArray(List<UnivariateObservation> observations) {
		int size = observations.size();
		double[] xarray = new double[size];
		for(int i = 0; i < size; i++) {
			xarray[i] = observations.get(i).getX();
		}
		return xarray;
	}
	
	public static double[] getYArray(List<UnivariateObservation> observations) {
		int size = observations.size();
		double[] yarray = new double[size];
		for(int i = 0; i < size; i++) {
			yarray[i] = observations.get(i).getY();
		}
		return yarray;
	}

	public static UnivariateObservation getMaxXObservation(List<UnivariateObservation> observations) {
		UnivariateObservation maxObs = null;
		double maxX = 0;
		for(UnivariateObservation obs : observations) {
			double x = obs.getX();
			if(maxObs == null || x > maxX) {
				maxObs = obs;
				maxX = x;
			}
		}
		return maxObs;
	}

	public static double getMinX(UnivariateObservation[] observations) {
		double minX = Double.POSITIVE_INFINITY;
		for(UnivariateObservation obs : observations) {
			double x = obs.getX();
			if(x < minX) {
				minX = x;
			}
		}
		return minX;
	}

	public static double getMaxX(UnivariateObservation[] observations) {
		double maxX = Double.NEGATIVE_INFINITY;
		for(UnivariateObservation obs : observations) {
			double x = obs.getX();
			if(x > maxX) {
				maxX = x;
			}
		}
		return maxX;
	}

	public static double getMinY(UnivariateObservation[] observations) {
		double minY = Double.POSITIVE_INFINITY;
		for(UnivariateObservation obs : observations) {
			double y = obs.getY();
			if(y < minY) {
				minY = y;
			}
		}
		return minY;
	}

	public static double getMaxY(UnivariateObservation[] observations) {
		double maxY = Double.NEGATIVE_INFINITY;
		for(UnivariateObservation obs : observations) {
			double y = obs.getY();
			if(y > maxY) {
				maxY = y;
			}
		}
		return maxY;
	}

	public static double getMinY(List<UnivariateObservation> observations) {
		double minY = Double.POSITIVE_INFINITY;
		for(UnivariateObservation obs : observations) {
			double y = obs.getY();
			if(y < minY) {
				minY = y;
			}
		}
		return minY;
	}

	public static double getMaxY(List<UnivariateObservation> observations) {
		double maxY = Double.NEGATIVE_INFINITY;
		for(UnivariateObservation obs : observations) {
			double y = obs.getY();
			if(y > maxY) {
				maxY = y;
			}
		}
		return maxY;
	}

	public static double getMeanX(List<UnivariateObservation> observations) {
		double sum = 0;
		for(UnivariateObservation obs : observations) {
			double x = obs.getX();
			sum += x;
		}
		return sum / observations.size();
	}

	public static double getMeanY(List<UnivariateObservation> observations) {
		double sum = 0;
		for(UnivariateObservation obs : observations) {
			double x = obs.getY();
			sum += x;
		}
		return sum / observations.size();
	}

	public static UnivariateObservation getMinXObservation(List<UnivariateObservation> observations) {
		UnivariateObservation maxObs = null;
		double minX = 0;
		for(UnivariateObservation obs : observations) {
			double x = obs.getX();
			if(maxObs == null || x < minX) {
				maxObs = obs;
				minX = x;
			}
		}
		return maxObs;
	}

	public static UnivariateObservation getMaxYObservation(List<UnivariateObservation> observations) {
		UnivariateObservation maxObs = null;
		double maxY = 0;
		for(UnivariateObservation obs : observations) {
			double y = obs.getY();
			if(maxObs == null || y > maxY) {
				maxObs = obs;
				maxY = y;
			}
		}
		return maxObs;
	}

	
	public static UnivariateObservation getMinYObservation(List<UnivariateObservation> observations) {
		UnivariateObservation minObs = null;
		double minY = 0;
		for(UnivariateObservation obs : observations) {
			double y = obs.getY();
			if(minObs == null || y < minY) {
				minObs = obs;
				minY = y;
			}
		}
		return minObs;
	}
	

}
