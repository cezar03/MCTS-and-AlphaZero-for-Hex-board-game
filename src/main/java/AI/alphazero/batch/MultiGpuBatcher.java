package AI.alphazero.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.nd4j.linalg.factory.Nd4j;

import AI.alphazero.net.AlphaZeroNet;

/**
 * A high-performance batcher that distributes inference across multiple worker threads,
 * potentially utilizing multiple GPUs.
 * <p>
 * It creates a pool of {@link NeuralNetBatcher} workers. Incoming requests are
 * round-robin distributed to these workers. This allows for massive parallelism
 * during self-play training.
 */
public class MultiGpuBatcher implements Runnable, Batcher {
    private final List<NeuralNetBatcher> workers = new ArrayList<>();
    private final List<AlphaZeroNet> workerNets = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    
    /**
     * Initializes the multi-GPU batcher.
     * <p>
     * Clones the master network for each worker to ensure thread safety and independent
     * memory contexts.
     * * @param masterNet the primary neural network
     * @param batchSize the target batch size for each worker
     * @param numWorkers the number of parallel workers (often corresponds to number of GPUs or CPU cores)
     */
    public MultiGpuBatcher(AlphaZeroNet masterNet, int batchSize, int numWorkers) {
        int numDevices = 0;
        
        if (numWorkers > 0) {
            numDevices = numWorkers;
            System.out.println("MultiGpuBatcher: Manual worker count specified: " + numWorkers);
        } else {
            try {
                numDevices = Nd4j.getAffinityManager().getNumberOfDevices();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (numDevices < 2) {
                 System.out.println("WARNING: AffinityManager reported fewer than 2 devices (" + numDevices + "). Forcing usage of 2 devices as requested.");
                 numDevices = 2; 
            }
            System.out.println("DEBUG: MultiGpuBatcher detected " + numDevices + " devices via AffinityManager.");
        }
        
        System.out.println("MultiGpuBatcher: Detected " + numDevices + " GPUs. Initializing workers...");
        
        for (int i = 0; i < numDevices; i++) {
            AlphaZeroNet childNet = new AlphaZeroNet(masterNet.getBoardSize()); 
            NeuralNetBatcher worker = new NeuralNetBatcher(childNet, batchSize, i);
            workers.add(worker);
            workerNets.add(childNet);
            System.out.println("  -> Worker " + i + " created.");
        }
        
        updateWeights(masterNet);
    }

    /**
     * Submits an input for prediction, distributing requests in round-robin fashion
     * across the available workers.
     * * @param input the encoded board state
     * @return a Future that will complete with the network's output (policy and value)
     */
    @Override
    public CompletableFuture<NeuralNetBatcher.Output> predict(float[] input) {
        int idx = Math.abs(counter.getAndIncrement() % workers.size());
        return workers.get(idx).predict(input);
    }
    
    /**
     * Broadcasts the weights from the master network to all worker networks.
     * <p>
     * This is called after a training step to ensure all workers generate self-play
     * data using the latest model.
     * * @param master the network containing the updated weights
     */
    public void updateWeights(AlphaZeroNet master) {
        System.out.println(">>> Broadcasting weights to " + workers.size() + " Workers...");
        for (NeuralNetBatcher worker : workers) {
             worker.updateModelWeights(master);
        }
    }
    
    /**
     * Returns the board size configuration of the underlying networks.
     * * @return the board size
     */
    public int getBoardSize() {
        if (!workerNets.isEmpty()) return workerNets.get(0).getBoardSize();
        return 11;
    }

    /**
     * Starts all worker threads for processing incoming requests.
     */
    @Override
    public void run() {
        startGlobalHeartbeat();
        for (NeuralNetBatcher worker : workers) {
            Thread t = new Thread(worker);
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * Starts a global heartbeat thread that periodically logs the total
     * number of samples processed across all workers.
     */
    private void startGlobalHeartbeat() {
        Thread t = new Thread(() -> {
            long lastCount = 0;
            long lastTime = System.currentTimeMillis();
            while (true) {
                try {
                    Thread.sleep(10000);
                    long total = 0;
                    for (NeuralNetBatcher w : workers) total += w.getSamplesProcessed();
                    long now = System.currentTimeMillis();
                    double rate = (total - lastCount) / ((now - lastTime) / 1000.0);
                    System.out.println(String.format("GLOBAL HEARTBEAT: Total Processed: %d. (Rate: %.1f samples/sec)", total, rate));
                    lastCount = total;
                    lastTime = now;
                } catch (Exception e) { break; }
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    /**
     * Pauses all workers. Useful during model updates to prevent race conditions.
     */
    public void pause() { for (NeuralNetBatcher w : workers) w.pause(); }
    
    /**
     * Resumes all workers after a pause.
     */
    public void resume() { for (NeuralNetBatcher w : workers) w.resume(); }
    
    /**
     * Stops all workers and releases resources.
     */
    public void stop() { for (NeuralNetBatcher w : workers) w.stop(); }
}











