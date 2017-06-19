package jhs.lc.geom;

import java.awt.geom.Rectangle2D;
import java.util.Random;

import jhs.math.classification.ClassificationUtil;
import jhs.math.util.MathUtil;

public class ExtraNoiseSphere extends AbstractRotatableSphere {
	private static final long EXTRA_SEED = 20160220001L;
	private final AbstractRotatableSphere delegate;
	private final double noiseSd;

	public ExtraNoiseSphere(AbstractRotatableSphere delegate, double noiseSd) {
		super(delegate.getRadius(), delegate.getTransformer());
		this.delegate = delegate;
		this.noiseSd = noiseSd;
	}
	
	@Override
	public final boolean isOnlyFrontVisible() {
		return this.delegate.isOnlyFrontVisible();
	}

	@Override
	public final double getUnrotatedBrightness(double x, double y, double z) {
		double rawB = this.delegate.getUnrotatedBrightness(x, y, z);
		if(Double.isNaN(rawB) || rawB >= 0) {
			return rawB;
		}
		int dx = Float.floatToIntBits((float) MathUtil.round(x, 2));
		int dy = Float.floatToIntBits((float) MathUtil.round(y, 2));
		double noise = new Random(dx ^ dy ^ EXTRA_SEED).nextGaussian() * this.noiseSd;
		double logit = ClassificationUtil.probabilityToLogit(-rawB);
		if(Double.isNaN(logit) || Double.isInfinite(logit)) {
			return rawB;
		}
		return -ClassificationUtil.logitToProbability(logit + noise);
	}

	@Override
	public final Rectangle2D getUnrotatedBoundingBox() {
		return this.delegate.getUnrotatedBoundingBox();
	}
	
}
