package jhs.lc.geom;

import java.util.Random;

import jhs.math.util.MathUtil;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

public class PointTransformer {
	private final double[][] transformMatrix;
	private final double[][] inverseTransformMatrix;

	public PointTransformer(double[][] transformMatrix,
			double[][] inverseTransformMatrix) {
		super();
		this.transformMatrix = transformMatrix;
		this.inverseTransformMatrix = inverseTransformMatrix;
	}

	public final Point3D transformPoint(double x, double y, double z) {
		return transformPoint(x, y, z, this.transformMatrix);
	}

	public final Point3D inverseTransformPoint(double x, double y, double z) {
		return transformPoint(x, y, z, this.inverseTransformMatrix);
	}

	private final Point3D transformPoint(double x, double y, double z, double[][] matrix) {
		double[] point = new double[] { x, y, z };
		double tx = MathUtil.dotProduct(point, matrix[0]);
		double ty = MathUtil.dotProduct(point, matrix[1]);
		double tz = MathUtil.dotProduct(point, matrix[2]);
		return new Point3D(tx, ty, tz);
	}

	public static PointTransformer getPointTransformer(Plane3D basePlane) {
		if(basePlane.d != 0) {
			throw new IllegalArgumentException("Plane expected to intersect (0,0,0).");
		}
		double a = basePlane.a;
		double b = basePlane.b;
		double c = basePlane.c;
		if(a == 0 && b == 0 && c == 0) {
			throw new IllegalArgumentException("All plane parameters are zero.");
		}
		double[] initVector = new double[] { a, b, c };
		double[][] vectors = new OrthogonalVectorProducer(new Random(747), 1E-7).produceOrthogonalVectors(3, 3, initVector);
		double[] helper = vectors[1];
		vectors[1] = vectors[0];
		vectors[0] = helper;
		RealMatrix transformMatrix = new Array2DRowRealMatrix(vectors);
		RealMatrix inverseMatrix = new LUDecompositionImpl(transformMatrix).getSolver().getInverse();
		double[][] inverseVectors = inverseMatrix.getData();
		return new PointTransformer(vectors, inverseVectors);
	}
	
	public static PointTransformer getPointTransformer(Plane3D xzPlane, Plane3D yzPlane) {
		double ax = yzPlane.a;
		double ay = yzPlane.b;
		double az = yzPlane.c;
		
		double bx = xzPlane.a;		
		double by = xzPlane.b;
		double bz = xzPlane.c;

		// Cross product
		double cx = ay * bz - az * by;
		double cy = az * bx - ax * bz;
		double cz = ax * by - ay * bx;
		
		double[] xvector = new double[] { ax, ay, az };
		double[] yvector = new double[] { bx, by, bz };
		double[] zvector = new double[] { cx, cy, cz };
		
		double[][] vectors = new double[][] {
			xvector,
			yvector,
			zvector
		};

		RealMatrix transformMatrix = new Array2DRowRealMatrix(vectors);
		RealMatrix inverseMatrix = new LUDecompositionImpl(transformMatrix).getSolver().getInverse();
		double[][] inverseVectors = inverseMatrix.getData();
		return new PointTransformer(vectors, inverseVectors);
	}
	
}