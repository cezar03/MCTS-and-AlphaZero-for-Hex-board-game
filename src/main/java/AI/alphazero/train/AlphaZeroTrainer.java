package ai.alphazero.train;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.MultiDataSet;
import org.nd4j.linalg.factory.Nd4j;

import ai.alphazero.batch.MultiGpuBatcher;
import ai.alphazero.config.AlphaZeroConfig;
import ai.alphazero.mcts.AlphaZeroMCTS;
import ai.alphazero.net.AlphaZeroNet;
import ai.alphazero.net.BoardEncoder;
import ai.alphazero.net.TrainingExampleData;
import ai.mcts.Node;
import game.core.Board;
import game.core.Color;
import game.core.Move;

public class AlphaZeroTrainer {
    private final AlphaZeroConfig mctsCfg;
    private AlphaZeroNet network;
    private int boardSize;

    // CONFIGURATION - Optimized for dual A100 + 256 threads (respectful usage)
    // We use Virtual Threads (Java 21), so we can spawn thousands of lightweight threads.
    // This allows us to run many games concurrently to fill the massive GPU batch size.
    
    // A100 is huge.
    private static final int BATCH_SIZE = 4096; 
    
    // OVERSUBSCRIPTION:
    // With 2TB RAM, we can run massive concurrency.
    // 25,000 games ensures we always have enough data to fill the 40960 batch queues.
    private static final int PLAY_BATCH_SIZE = 25000;

    private static final int TRAINING_EPOCHS = 3; 

    public AlphaZeroTrainer(int boardSize) {
        this.boardSize = boardSize;
        
        this.mctsCfg = new AlphaZeroConfig.Builder()
            .boardSize(boardSize)
            .cpuct(Math.sqrt(2))     // or whatever you want
            .temperature(1.0)        // not used by MCTS directly, fine to keep
            .build();
        System.out.println("Backend: " + Nd4j.getBackend().getClass().getSimpleName());
        try {
            int numDevices = Nd4j.getAffinityManager().getNumberOfDevices();
            System.out.println("Available GPU devices: " + numDevices);
        } catch (Exception e) {
            System.out.println("GPU device info not available");
        }
        
        File modelFile = new File("hex_model_latest.zip");
        if (modelFile.exists()) {
            System.out.println(">>> FOUND SAVED MODEL: hex_model_latest.zip");
            System.out.println(">>> Resuming training from saved state...");
            try {
                this.network = AlphaZeroNet.load("hex_model_latest.zip", boardSize);
            } catch (Exception e) {
                System.out.println("xxx Failed to load model: " + e.getMessage());
                System.out.println("xxx Starting from scratch.");
                this.network = new AlphaZeroNet(boardSize);
            }
        } else {
            System.out.println(">>> No saved model found. Starting fresh.");
            this.network = new AlphaZeroNet(boardSize);
        }
        
        // A100 has 80GB.
        Nd4j.getMemoryManager().setAutoGcWindow(5000); 
    }

    public void train(int totalGames, int mctsIterations) {
        // Calculate how many "Generations" (Play Batches) we need
        // Each generation runs PLAY_BATCH_SIZE games in parallel
        
        int gamesToRun = totalGames;
        
        // Split into Play Batches (Generations)
        int numBatches = (int) Math.ceil((double) gamesToRun / PLAY_BATCH_SIZE);

        System.out.println("======================================================================");
        System.out.println("               ALPHA ZERO TRAINING SESSION (OPTIMIZED)");
        System.out.println("======================================================================");
        System.out.println("Target Total Games: " + gamesToRun);
        System.out.println("Batch Size (GPU):   " + BATCH_SIZE);
        System.out.println("Concurrency:        " + PLAY_BATCH_SIZE + " (Oversubscribed)");
        System.out.println("Generations:        " + numBatches);

        // Detect CPU vs GPU mode
        int availableDevices = 0;
        int numWorkers = 0; // 0 = Auto-detect (for GPU)
        try {
            availableDevices = Nd4j.getAffinityManager().getNumberOfDevices();
        } catch(Exception e) { }

        if (availableDevices < 2) {
            // CPU MODE: Scale up workers to saturate cores
            int cores = Runtime.getRuntime().availableProcessors();
            // USER REQUEST: "Use all cores".
            // CPU MODE: 
            // CPU MODE: Dual-Socket Optimization.
            // You have 2 physical CPUs (EPYC 7713).
            // We spawn 2 Workers so each CPU handles its own memory/thread-pool (NUMA friendly).
            numWorkers = 2; 
            
            System.out.println("CPU Mode Detected (" + cores + " cores). Using Dual-Socket Strategy (2 Workers).");
            System.out.println("ADVICE: Set OMP_NUM_THREADS=" + (cores/2/2) + " (Physical) or " + (cores/2) + " (Logical).");
            System.out.println("        e.g. OMP_NUM_THREADS=64 (Total 128 threads) or 128 (Total 256 threads).");
        } else {
            System.out.println("GPU Mode Detected. Using default device mapping.");
        }

        MultiGpuBatcher batcher = new MultiGpuBatcher(network, BATCH_SIZE, numWorkers);
        Thread batcherThread = new Thread(batcher);
        batcherThread.setDaemon(true);
        batcherThread.start();

        // Use Virtual Thread Executor
        // This is the MAGIC key to performance here.
        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            
            for (int b = 0; b < numBatches; b++) {
                // Determine how many games to run in this generation
                int gamesInThisGeneration = Math.min(PLAY_BATCH_SIZE, gamesToRun - (b * PLAY_BATCH_SIZE));
                if (gamesInThisGeneration <= 0) break;

                AtomicInteger completedGames = new AtomicInteger(0);
                List<java.util.concurrent.Callable<List<TrainingExampleData>>> tasks = new ArrayList<>();
                
                final int generationIdx = b + 1;
                final int totalGamesInGen = gamesInThisGeneration;

                for (int i = 0; i < gamesInThisGeneration; i++) {
                    tasks.add(() -> {
                        // Cast or access facade if needed, but MultiGpuBatcher.predict matches signature of what MCTS needs? 
                        // MCTS expects a NeuralNetBatcher type probably?
                        // Let's check AlphaZeroMCTS constructor signature.
                        // If it expects NeuralNetBatcher, we might have a problem if MultiGpuBatcher is not a subclass.
                        // Wait, MultiGpuBatcher implements Runnable but is NOT a NeuralNetBatcher.
                        // I need to check AlphaZeroMCTS.
                        AlphaZeroMCTS localMcts = new AlphaZeroMCTS(batcher, mctsCfg);
                        List<TrainingExampleData> result = selfPlay(localMcts, mctsIterations);
                        int done = completedGames.incrementAndGet();
                        if (done % 500 == 0 || done == totalGamesInGen) {
                            System.out.println(String.format("    [Gen %d] Game %5d/%d finished.", generationIdx, done, totalGamesInGen));
                        }
                        return result;
                    });
                }

                System.out.println(">>> Starting Generation " + generationIdx + " with " + gamesInThisGeneration + " concurrent games...");
                List<java.util.concurrent.Future<List<TrainingExampleData>>> futures = executor.invokeAll(tasks);
                
                List<TrainingExampleData> batchExamples = new ArrayList<>();
                for (var future : futures) {
                    batchExamples.addAll(future.get()); // Collect results
                }

                System.out.println(">>> Generation finished. Pausing batcher for training...");
                batcher.pause();
                
                System.out.println(">>> Training Network on " + batchExamples.size() + " positions...");
                trainNetwork(batchExamples);
                
                // Update worker GPUs with new weights
                batcher.updateWeights(network);

                batcher.resume();
                try { network.save("hex_model_latest.zip"); } catch (Exception e) {}
                
                // AUTOMATED EVALUATION
                System.out.println(">>> Playing Evaluation Games against RandomBot...");
                evaluate(20, batcher); 
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            batcher.stop();
        }
    }

    private void evaluate(int numGames, MultiGpuBatcher batcher) {
        AtomicInteger azWins = new AtomicInteger(0);

        java.util.stream.IntStream.range(0, numGames).parallel().forEach(i -> {
            Board board = new Board(boardSize);
            Color currentPlayer = Color.RED;

            boolean azIsRed = (i % 2 == 0);
            AlphaZeroMCTS mcts = new AlphaZeroMCTS(batcher, mctsCfg);

            while (!board.isTerminal()) {
                boolean azTurn = (currentPlayer == Color.RED && azIsRed) ||
                                (currentPlayer == Color.BLACK && !azIsRed);

                if (azTurn) {
                    Node root = mcts.search(board, currentPlayer, 100, false); // no noise
                    double[] policy = mcts.getSearchPolicy(root, 0.01);
                    Move bestMove = selectMoveFromPolicy(policy);

                    if (bestMove == null) bestMove = randomLegalMove(board);
                    if (bestMove == null) break;
                    if (currentPlayer == Color.RED) board.getMoveRed(bestMove.row, bestMove.col, null);
                    else board.getMoveBlack(bestMove.row, bestMove.col, null);
                } else {
                    // Pure random opponent on Board (no AIBoardAdapter nonsense)
                    Move rm = randomLegalMove(board);
                    if (rm == null) break;

                    if (currentPlayer == Color.RED) board.getMoveRed(rm.row, rm.col, null);
                    else board.getMoveBlack(rm.row, rm.col, null);
                }

                currentPlayer = (currentPlayer == Color.RED) ? Color.BLACK : Color.RED;
            }

            boolean redWon = board.redWins();
            if (redWon && azIsRed) azWins.incrementAndGet();
            if (!redWon && !azIsRed) azWins.incrementAndGet();
        });

        double winRate = (double) azWins.get() / numGames * 100.0;
        System.out.printf("EVALUATION RESULTS: AlphaZero Win Rate: %.1f%% (%d/%d)%n",
                winRate, azWins.get(), numGames);
    }

    private Move selectMoveFromPolicy(double[] policy) {
        // policy is already only nonzero on legal root children (from your MCTS)
        double r = Math.random();
        double sum = 0.0;

        for (int i = 0; i < policy.length; i++) {
            sum += policy[i];
            if (r <= sum) {
                int row = i / boardSize;
                int col = i % boardSize;
                return Move.get(row, col);
            }
        }

        // fallback: first nonzero
        for (int i = 0; i < policy.length; i++) {
            if (policy[i] > 0) {
                int row = i / boardSize;
                int col = i % boardSize;
                return Move.get(row, col);
            }
        }

        // catastrophic: policy all zeros
        return null;
    }

    private Move randomLegalMove(Board board) {
        var legal = board.legalMoves();
        if (legal.isEmpty()) return null;
        int[] rc = legal.get((int)(Math.random() * legal.size()));
        return Move.get(rc[0], rc[1]);
    }


    private List<TrainingExampleData> selfPlay(AlphaZeroMCTS localMcts, int iterations){
        List<TrainingExampleData> gameHistory = new ArrayList<>();
        Board board = new Board(boardSize);
        Color currentPlayer = Color.RED;

        while (!board.isTerminal()) {
            // Training mode = true (Enables Dirichlet Noise)
            Node root = localMcts.search(board, currentPlayer, iterations, true);
            double temp = 1.0; 
            double[] policy = localMcts.getSearchPolicy(root, temp);

            // OPTIMIZATION: Store raw floats (Java Heap), NOT INDArrays (Native Heap)
            float[] encodedData = (root.cachedEncoding != null) ? root.cachedEncoding : BoardEncoder.encode(board, currentPlayer);
            
            // Convert double[] policy to float[] for storage
            float[] policyFloat = new float[policy.length];
            if (currentPlayer == Color.RED) {
                // RED: Direct copy
                for(int i=0; i<policy.length; i++) policyFloat[i] = (float)policy[i];
            } else {
                // BLACK: Transpose to match Canonical Input
                // Canonical[r, c] maps to Real[c, r]
                int size = board.getSize();
                for (int r = 0; r < size; r++) {
                    for (int c = 0; c < size; c++) {
                        int canonIdx = r * size + c;
                        int realIdx = c * size + r;
                        policyFloat[canonIdx] = (float)policy[realIdx];
                    }
                }
            }

            gameHistory.add(new TrainingExampleData(encodedData, policyFloat, 0.0f));

            Move bestMove = selectMoveFromPolicy(policy, board);
            if (currentPlayer == Color.RED) board.getMoveRed(bestMove.row, bestMove.col, null);
            else board.getMoveBlack(bestMove.row, bestMove.col, null);
            
            currentPlayer = (currentPlayer == Color.RED) ? Color.BLACK : Color.RED;
        }

        double result = board.redWins() ? 1.0 : (board.blackWins() ? -1.0 : 0.0);
        Color historyPlayer = Color.RED; 
        for (TrainingExampleData example : gameHistory) {
            float val = (float) ((historyPlayer == Color.RED) ? result : -result);
            example.targetValue[0] = val;
            historyPlayer = (historyPlayer == Color.RED) ? Color.BLACK : Color.RED;
        }
        return gameHistory;
    }

    private Move selectMoveFromPolicy(double[] policy, Board board) {
        double randomNumber = Math.random();
        double sum = 0;
        int selectedIdx = -1;
        int policyLength = policy.length;
        
        for (int i = 0; i < policyLength; i++) {
            sum += policy[i];
            if (randomNumber <= sum) {
                selectedIdx = i;
                break;
            }
        }
        if (selectedIdx == -1) {
            for (int i = 0; i < policyLength; i++) {
                if (policy[i] > 0) {
                    selectedIdx = i;
                    break;
                }
            }
        }
        int row = selectedIdx / boardSize;
        int col = selectedIdx % boardSize;
        
        return Move.get(row, col);
    }

    private void trainNetwork(List<TrainingExampleData> examples) {
        if (examples.isEmpty()) return;
        java.util.Collections.shuffle(examples);

        int totalExamples = examples.size();
        int miniBatchSize = 4096; // 4096 is safer for stability

        for (int epoch = 0; epoch < TRAINING_EPOCHS; epoch++) {
            for (int i = 0; i < totalExamples; i += miniBatchSize) {
                int end = Math.min(i + miniBatchSize, totalExamples);
                List<TrainingExampleData> batch = examples.subList(i, end);
                int currentBatchSize = batch.size();

                // CONVERT TO INDARRAY JUST IN TIME (And then discard)
                // 1. Flatten data into large buffers
                int inputSize = batch.get(0).inputBoard.length;
                int policySize = batch.get(0).targetPolicy.length;
                int side = (int)Math.sqrt(inputSize / 3);

                float[] inputsBuffer = new float[currentBatchSize * inputSize];
                float[] policiesBuffer = new float[currentBatchSize * policySize];
                float[] valuesBuffer = new float[currentBatchSize];

                for (int k = 0; k < currentBatchSize; k++) {
                    System.arraycopy(batch.get(k).inputBoard, 0, inputsBuffer, k*inputSize, inputSize);
                    System.arraycopy(batch.get(k).targetPolicy, 0, policiesBuffer, k*policySize, policySize);
                    valuesBuffer[k] = batch.get(k).targetValue[0];
                }

                // 2. Create NDArrays
                INDArray inputND = Nd4j.create(inputsBuffer, new int[]{currentBatchSize, 3, side, side});
                INDArray policyND = Nd4j.create(policiesBuffer, new int[]{currentBatchSize, policySize});
                INDArray valueND = Nd4j.create(valuesBuffer, new int[]{currentBatchSize, 1});

                try {
                    network.getModel().fit(new MultiDataSet(new INDArray[]{inputND}, new INDArray[]{policyND, valueND}));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 3. CLOSE IMMEDIATELY to free GPU memory
                    inputND.close();
                    policyND.close();
                    valueND.close();
                }
            }
        }
    }
}










