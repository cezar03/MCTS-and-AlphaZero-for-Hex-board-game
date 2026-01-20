package ai.alphazero.batch;

import java.util.concurrent.CompletableFuture;

public interface Batcher {
    CompletableFuture<NeuralNetBatcher.Output> predict(float[] input);
}











