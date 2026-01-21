package AI.alphazero.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import AI.alphazero.net.AlphaZeroNet;

/**
 * A worker that aggregates individual inference requests into efficient batches
 * for processing by a single neural network instance.
 * <p>
 * It uses a producer-consumer pattern:
 * <ol>
 * <li>Client threads put requests into an input queue.</li>
 * <li>The batch builder thread drains the queue and forms a batch.</li>
 * <li>The batch is passed to the inference logic (likely on GPU).</li>
 * <li>Results are distributed back to the futures of the individual requests.</li>
 * </ol>
 */
public class NeuralNetBatcher implements Runnable, Batcher {
    private final AlphaZeroNet network;
    private final BlockingQueue<Request> inputQueue;
    private final BlockingQueue<BatchJob> gpuQueue; 
    
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final int maxBatchSize;
    private final int deviceIndex;

    /**
     * Constructs a batcher for a specific network instance.
     * * @param network the neural network instance dedicated to this batcher
     * @param maxBatchSize the maximum number of requests to aggregate in one forward pass
     * @param deviceIndex the ID of the device (GPU) this batcher is intended for
     */
    public NeuralNetBatcher(AlphaZeroNet network, int maxBatchSize, int deviceIndex) {
        this.network = network;
        this.maxBatchSize = Math.min(Math.max(maxBatchSize, 256), 8192);
        this.deviceIndex = deviceIndex;
        this.inputQueue = new LinkedBlockingQueue<>((int)(this.maxBatchSize * 1.5));
        this.gpuQueue = new LinkedBlockingQueue<>(2);
    }

    /**
     * Updates the local network's parameters to match the master network.
     * * @param master the source network
     */
    public void updateModelWeights(AlphaZeroNet master) {
        pause();
        try {
            this.network.getModel().setParams(master.getModel().params().dup());
        } catch (Exception e) {
            e.printStackTrace();
        }
        resume();
    }
    
    private static final long MAX_WAIT_NANOS = 5_000_000;
    private final AtomicLong totalSamplesProcessed = new AtomicLong(0);

    /**
     * Submits an input for prediction.
     * * @param input the encoded board state
     * @return a Future that will complete with the network's output (policy and value)
     */
    @Override
    public CompletableFuture<Output> predict(float[] input) {
        CompletableFuture<Output> future = new CompletableFuture<>();
        try {
            inputQueue.put(new Request(input, future));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return future;
    }

    /**
     * Starts the batcher's main processing loops.
     */
    @Override
    public void run() {
        startHeartbeat();
        Thread builderThread = new Thread(this::runBatchBuilder, "Batch-Builder");
        builderThread.setDaemon(true);
        builderThread.start();
        runGpuInference(); 
    }

    /**
     * The main loop that builds batches from incoming requests.
     */
    private void runBatchBuilder() {
        List<Request> batchBuffer = new ArrayList<>(maxBatchSize);
        while (running.get()) {
            try {
                while (paused.get() && running.get()) Thread.sleep(10);
                Request first = inputQueue.take();
                batchBuffer.add(first);
                inputQueue.drainTo(batchBuffer, maxBatchSize - 1);
                if (batchBuffer.size() < maxBatchSize) {
                    long start = System.nanoTime();
                    while (batchBuffer.size() < maxBatchSize && (System.nanoTime() - start) < MAX_WAIT_NANOS) {
                        Request next = inputQueue.poll();
                        if (next != null) batchBuffer.add(next);
                        else Thread.onSpinWait();
                    }
                }
                BatchJob job = prepareBatch(new ArrayList<>(batchBuffer));
                batchBuffer.clear();
                gpuQueue.put(job);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Prepares a batch job from a list of requests.
     * @param requests the list of individual requests
     * @return the prepared batch job
     */
    private BatchJob prepareBatch(List<Request> requests) {
        int batchSize = requests.size();
        int singleInputLength = requests.get(0).inputData.length;
        int side = (int) Math.sqrt(singleInputLength / 3);
        float[] batchBuffer = new float[batchSize * singleInputLength];
        for (int i = 0; i < batchSize; i++) {
            System.arraycopy(requests.get(i).inputData, 0, batchBuffer, i * singleInputLength, singleInputLength);
        }
        INDArray inputTensor = Nd4j.create(batchBuffer, new int[]{batchSize, 3, side, side});
        return new BatchJob(inputTensor, requests);
    }

    /**
     * The main loop that performs GPU inference on batches.
     */
    private void runGpuInference() {
        try {
        } catch (Exception e) {
            System.err.println("ERROR: Failed to set device affinity: " + e.getMessage());
            System.err.println("WARNING: Worker for device " + deviceIndex + " will run on the DEFAULT device (likely 0).");
        }
        
        while (running.get()) {
            try {
                BatchJob job = gpuQueue.poll(1, TimeUnit.SECONDS);
                if (job == null) continue;
                INDArray[] results = network.getModel().output(job.input);
                INDArray policyBatch = results[0];
                INDArray valueBatch = results[1];
                float[][] policies = policyBatch.toFloatMatrix();
                double[] values = valueBatch.toDoubleVector();
                policyBatch.close();
                valueBatch.close();
                job.input.close();
                int currentBatchSize = job.requests.size();
                IntStream.range(0, currentBatchSize).parallel().forEach(i -> {
                    job.requests.get(i).future.complete(new Output(policies[i], values[i]));
                });
                long processed = totalSamplesProcessed.addAndGet(currentBatchSize);
                if ((processed / maxBatchSize) % 50 == 0) {
                     Nd4j.getMemoryManager().invokeGc(); 
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Pauses the batcher, waiting for current jobs to finish.
     */
    public void pause() {
        paused.set(true);
        while (!inputQueue.isEmpty() || !gpuQueue.isEmpty()) try { Thread.sleep(50); } catch (Exception e) {}
    }

    /**
     * Resumes the batcher after a pause.
     */
    public void resume() { paused.set(false); }
    
    /**
     * Stops the batcher gracefully.
     */
    public void stop() { running.set(false); }

    /**
     * Returns the total number of samples processed by this batcher.
     * * @return the total samples processed
     */
    public long getSamplesProcessed() {
        return totalSamplesProcessed.get();
    }

    /**
     * Starts the heartbeat logging thread.
     */
    private void startHeartbeat() {
        // Implement heartbeat logging if needed
    }

    /**
     * Internal class representing a single inference request.
     */
    private static class Request {
        float[] inputData;
        CompletableFuture<Output> future;
        Request(float[] d, CompletableFuture<Output> f) { inputData = d; future = f; }
    }

    /**
     * Internal class representing a batch job.
     */
    private static class BatchJob {
        INDArray input;
        List<Request> requests;
        BatchJob(INDArray i, List<Request> r) { input = i; requests = r; }
    }
    
    /**
     * Container for the results of a neural network prediction.
     */
    public static class Output {
        public float[] policy; 
        public double value;

        /**
         * Creates an output result.
         * * @param p the policy array (probabilities)
         * @param v the value scalar (win estimate)
         */
        public Output(float[] p, double v) { policy = p; value = v; }
    }
}










