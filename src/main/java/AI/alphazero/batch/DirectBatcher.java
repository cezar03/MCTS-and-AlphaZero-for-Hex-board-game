package AI.alphazero.batch;

import java.util.concurrent.CompletableFuture;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import AI.alphazero.net.AlphaZeroNet;

/**
 * A simple implementation of {@link Batcher} that processes requests immediately
 * on the calling thread (or strictly sequentially).
 * <p>
 * This is primarily used for UI gameplay where high concurrency and batching
 * are not required, or for debugging purposes.
 */
public final class DirectBatcher implements Batcher {
    private final AlphaZeroNet net;

    /**
     * Constructs a DirectBatcher for the given network.
     * * @param net the neural network to use for inference
     */
    public DirectBatcher(AlphaZeroNet net) {
        if (net == null) throw new IllegalArgumentException("net cannot be null");
        this.net = net;
    }

    /**
     * Submits an input for immediate prediction.
     * * @param input the encoded board state
     * @return a Future that will complete with the network's output (policy and value)
     */
    @Override
    public CompletableFuture<NeuralNetBatcher.Output> predict(float[] input) {
        if (input == null) throw new IllegalArgumentException("input cannot be null");

        int side = (int) Math.sqrt(input.length / 3.0);
        if (3 * side * side != input.length) {
            throw new IllegalArgumentException("Bad input length: " + input.length);
        }

        INDArray x = null;
        INDArray p = null;
        INDArray v = null;
        try {
            x = Nd4j.create(input, new int[]{1, 3, side, side});
            INDArray[] out = net.getModel().output(x);

            p = out[0];
            v = out[1];

            float[] policy = p.toFloatMatrix()[0];
            double value = v.getDouble(0);

            return CompletableFuture.completedFuture(new NeuralNetBatcher.Output(policy, value));
        } finally {
            if (p != null) p.close();
            if (v != null) v.close();
            if (x != null) x.close();
        }
    }
}











