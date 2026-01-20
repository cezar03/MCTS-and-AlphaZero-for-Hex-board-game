package ai.alphazero.batch;

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

import ai.alphazero.net.AlphaZeroNet;

public class NeuralNetBatcher implements Runnable, Batcher {
    private final AlphaZeroNet network;
    private final BlockingQueue<Request> inputQueue;
    private final BlockingQueue<BatchJob> gpuQueue; 
    
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final int maxBatchSize;
    private final int deviceIndex;

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

    public NeuralNetBatcher(AlphaZeroNet network, int maxBatchSize, int deviceIndex) {
        this.network = network;
        this.maxBatchSize = Math.min(Math.max(maxBatchSize, 256), 8192);
        this.deviceIndex = deviceIndex;
        this.inputQueue = new LinkedBlockingQueue<>((int)(this.maxBatchSize * 1.5));
        this.gpuQueue = new LinkedBlockingQueue<>(2);
    }

    public CompletableFuture<Output> predict(float[] input) {
        CompletableFuture<Output> future = new CompletableFuture<>();
        try {
            inputQueue.put(new Request(input, future));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return future;
    }

    @Override
    public void run() {
        startHeartbeat();
        Thread builderThread = new Thread(this::runBatchBuilder, "Batch-Builder");
        builderThread.setDaemon(true);
        builderThread.start();
        runGpuInference(); 
    }

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

    public void pause() {
        paused.set(true);
        while (!inputQueue.isEmpty() || !gpuQueue.isEmpty()) try { Thread.sleep(50); } catch (Exception e) {}
    }
    public void resume() { paused.set(false); }
    public void stop() { running.set(false); }

    public long getSamplesProcessed() {
        return totalSamplesProcessed.get();
    }

    private void startHeartbeat() {
        // TODO: Implement heartbeat logging if needed
    }

    private static class Request {
        float[] inputData;
        CompletableFuture<Output> future;
        Request(float[] d, CompletableFuture<Output> f) { inputData = d; future = f; }
    }
    private static class BatchJob {
        INDArray input;
        List<Request> requests;
        BatchJob(INDArray i, List<Request> r) { input = i; requests = r; }
    }
    
    public static class Output {
        public float[] policy; 
        public double value;
        public Output(float[] p, double v) { policy = p; value = v; }
    }
}










