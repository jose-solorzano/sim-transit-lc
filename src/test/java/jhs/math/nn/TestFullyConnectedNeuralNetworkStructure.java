package jhs.math.nn;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import jhs.math.util.MathUtil;

import org.junit.Test;

public class TestFullyConnectedNeuralNetworkStructure {
	@Test
	public void testParamCharacteristics() {
		int[] hiddenLayers = new int[] { 2 };
		int numOutputs = 3;
		int numVars = 4;
		ActivationFunctionFactory afFactory = new ActivationFunctionFactory() {			
			@Override
			public ActivationFunction createOutputActivationFunction(int numInputs) {
				return new LocalActivationFunction(4);
			}
			
			@Override
			public ActivationFunction createActivationFunction(int numInputs, int layerIndex, int unitIndex) {
				return new LocalActivationFunction(1 + layerIndex + unitIndex);
			}
		};
		FullyConnectedNeuralStructure structure = FullyConnectedNeuralStructure.create(hiddenLayers, numOutputs, numVars, afFactory);
		int np = structure.getNumParameters();
		assertEquals(5 + 6 + 6 + 6 + 6, np);
		assertEquals(3, structure.getNumOutputs());
		assertEquals(4, structure.getNumInputsOfLayer(0));
		assertEquals(2, structure.getNumInputsOfLayer(1));
		for(int paramIndex = 0; paramIndex < np; paramIndex++) {
			int ni = structure.getNumInputsOfParamNeuron(paramIndex);
			int li = structure.getLayerIndexOfParam(paramIndex);
			if(paramIndex < 5) {
				assertEquals(4, ni);
				assertEquals(0, li);
			}
			else if(paramIndex < 5 + 6) {
				assertEquals(4, ni);
				assertEquals(0, li);
			}
			else {
				assertEquals("paramIndex: " + paramIndex, 2, ni);
				assertEquals(1, li);
			}
		}
		
		Random random = new Random(17);
		for(int i = 0; i < 10; i++) {
			double[] inputs = MathUtil.sampleGaussian(random, 1.0, numVars);
			double[] parameters = MathUtil.sampleGaussian(random, 1.0, np);
			double expectedMax = Math.max(MathUtil.max(inputs), MathUtil.max(parameters));
			PlainNeuralNetwork nn = new PlainNeuralNetwork(structure, parameters);
			double[] activations = nn.activations(inputs);
			assertEquals(numOutputs, activations.length);
			assertEquals(expectedMax, MathUtil.max(activations), 0.0001);
		}
	}

	private static class LocalActivationFunction implements ActivationFunction {
		private static final long serialVersionUID = 1L;
		private final int numExtraParams;
				
		public LocalActivationFunction(int numExtraParams) {
			super();
			this.numExtraParams = numExtraParams;
		}

		@Override
		public int getNumParameters(int numInputs) {
			return numInputs + this.numExtraParams;
		}

		@Override
		public double activation(double[] inputs, double[] parameters, int paramIndex) {
			int len = inputs.length;
			return Math.max(MathUtil.max(inputs), MathUtil.max(parameters, paramIndex, len + this.numExtraParams));
		}		
	}
	
}
