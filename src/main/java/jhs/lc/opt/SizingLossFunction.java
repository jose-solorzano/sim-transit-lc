package jhs.lc.opt;

import jhs.lc.data.LightCurve;

public class SizingLossFunction extends AbstractLossFunction {
	private final double centerOfMassFraction;
	private final double massDevFraction;
	private final double mass;
	
	public SizingLossFunction(SolutionSampler sampler, double[] fluxArray, double centerOfMassFraction,
			double massDevFraction, double mass) {
		super(sampler, fluxArray);
		this.centerOfMassFraction = centerOfMassFraction;
		this.massDevFraction = massDevFraction;
		this.mass = mass;
	}
	
	public SizingLossFunction(SolutionSampler sampler, double[] fluxArray) {
		super(sampler, fluxArray);
		double com = LightCurve.centerOfMass(fluxArray);
		this.centerOfMassFraction = com / (fluxArray.length - 1);
		this.massDevFraction = LightCurve.massDeviationAsFraction(fluxArray, com);
		this.mass = LightCurve.mass(fluxArray);		
	}
	
	@Override
	protected final double baseLoss(double[] testFluxArray) {
		double testMass = LightCurve.mass(testFluxArray);
		double testCom = LightCurve.centerOfMass(testFluxArray);
		double testComf = testCom / (testFluxArray.length - 1);
		double testMdevf = LightCurve.massDeviationAsFraction(testFluxArray, testCom);
		double massDiff = testMass - this.mass;
		double comDiff = testComf - this.centerOfMassFraction;
		double massDevDiff = testMdevf - this.massDevFraction;
		return massDiff * massDiff + comDiff * comDiff + massDevDiff * massDevDiff;
	}
}
