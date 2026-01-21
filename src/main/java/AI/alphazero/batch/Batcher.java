package AI.alphazero.batch;

import java.util.concurrent.CompletableFuture;

/**
 * Defines the interface for batched neural network inference.
 * <p>
 * Implementations allow multiple threads (e.g., concurrent MCTS simulations) to
 * submit inference requests which are then aggregated into a single batch for
 * efficient GPU or CPU processing.
 */
public interface Batcher {

    /**
     * Submits an input for prediction.
     * * @param input the encoded board state
     * @return a Future that will complete with the network's output (policy and value)
     */
    CompletableFuture<NeuralNetBatcher.Output> predict(float[] input);
}











