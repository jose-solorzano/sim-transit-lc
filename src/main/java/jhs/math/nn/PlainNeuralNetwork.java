package jhs.math.nn;

public class PlainNeuralNetwork implements java.io.Serializable, NeuralNetwork {
	private static final long serialVersionUID = 1L;
	private final NeuralNetworkStructure structure;
	private final double[] parameters;
	
	private transient ThreadLocal<double[][]> activationsBufferTL = new ThreadLocal<double[][]>();
	
	public PlainNeuralNetwork(NeuralNetworkStructure structure, double[] parameters) {
		super();
		this.structure = structure;
		this.parameters = parameters;
	}

	/* (non-Javadoc)
	 * @see jhs.math.common.nn.NeuralNetwork#getStructure()
	 */
	@Override
	public final NeuralNetworkStructure getStructure() {
		return structure;
	}


	@Override
	public final double[] getParameters() {
		return parameters;
	}
	
	/* (non-Javadoc)
	 * @see jhs.math.common.nn.NeuralNetwork#activations(double[], double[])
	 */
	@Override
	public final double[] activations(double[] inputData) {
		NeuralNetworkStructure nn = this.structure;
		ThreadLocal<double[][]> activationsBufferTL = this.activationsBufferTL;
		if(activationsBufferTL == null) {
			activationsBufferTL = this.activationsBufferTL = new ThreadLocal<double[][]>();
		}
		double[][] actBuffer = activationsBufferTL.get();
		if(actBuffer == null) {
			actBuffer = nn.allocateLayerNeuronDoubleMatrix();
			activationsBufferTL.set(actBuffer);
		}
		nn.populateActivations(actBuffer, this.parameters, inputData);
		return actBuffer[actBuffer.length - 1];
	}
}
