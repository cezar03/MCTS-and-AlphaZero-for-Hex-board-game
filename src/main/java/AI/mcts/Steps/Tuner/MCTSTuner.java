package AI.mcts.Steps.Tuner;

import game.core.Player;
import java.util.Random;
import AI.api.AIAgent;
import AI.tools.AITester;
import AI.mcts.MCTSPlayer;

/**
 * A hyperparameter tuning utility for MCTS MovePruner heuristics.
 * This class performs random search over the hyperparameter space to find
 * configurations that improve MCTS performance.
 * 
 * <p>The tuner supports two modes:</p>
 * <ul>
 *   <li>Full random search: samples all parameters including exploration constant</li>
 *   <li>Per-C search: fixes the exploration constant and tunes other parameters</li>
 * </ul>
 * 
 * <p>Configurations are evaluated by playing matches between base MCTS and
 * optimized MCTS with color-swapping to ensure fairness.</p>
 */
public final class MCTSTuner {

    private final int boardSize;
    private final int iterations;
    private final int gamesPerSide;
    private final Random rng;

    /**
     * Constructs an MCTSTuner with the specified parameters and random seed.
     *
     * @param boardSize the size of the Hex board for testing
     * @param iterations the number of MCTS iterations per move
     * @param gamesPerSide the number of games to play for each color assignment
     * @param seed the random seed for reproducible results
     */
    public MCTSTuner(int boardSize, int iterations, int gamesPerSide, long seed) {
        this.boardSize = boardSize;
        this.iterations = iterations;
        this.gamesPerSide = gamesPerSide;
        this.rng = new Random(seed);
    }

    /**
     * Constructs an MCTSTuner with the specified parameters and a random seed
     * based on the current time.
     *
     * @param boardSize the size of the Hex board for testing
     * @param iterations the number of MCTS iterations per move
     * @param gamesPerSide the number of games to play for each color assignment
     */
    public MCTSTuner(int boardSize, int iterations, int gamesPerSide) {
        this(boardSize, iterations, gamesPerSide, System.currentTimeMillis());
    }

    /**
     * Samples a completely random configuration including all parameters
     * and the exploration constant.
     * 
     * <p>Parameter ranges:</p>
     * <ul>
     *   <li>threshold: [0.7, 1.0]</li>
     *   <li>centralityWeight: [0.0, 1.0]</li>
     *   <li>biasScale: [0.02, 0.08]</li>
     *   <li>spWeight: [0.0, 0.2]</li>
     *   <li>cExploration: [0.5, 2.0]</li>
     * </ul>
     *
     * @return a randomly sampled PrunerConfig
     */
    private PrunerConfig sampleRandomConfig() {
        double threshold = 0.7 + rng.nextDouble() * 0.3; // [0.7, 1.0]
        double centralityWeight = rng.nextDouble();      // [0.0, 1.0]
        double connectivityWeight = 1.0 - centralityWeight;
        double biasScale = 0.02 + rng.nextDouble() * 0.06; // [0.02, 0.08]
        double spWeight  = rng.nextDouble() * 0.2;         // [0.0, 0.2]
        double cExploration = 0.5 + rng.nextDouble() * 1.5; // [0.5, 2.0]
        return new PrunerConfig( threshold, centralityWeight, connectivityWeight, biasScale, spWeight, cExploration);
    }

    /**
     * Samples a random configuration with a fixed exploration constant.
     * All other parameters are randomly sampled within their ranges.
     * 
     * <p>Parameter ranges:</p>
     * <ul>
     *   <li>threshold: [0.3, 1.0]</li>
     *   <li>centralityWeight: [0.0, 1.0]</li>
     *   <li>biasScale: [0.02, 0.08]</li>
     *   <li>spWeight: [0.0, 0.2]</li>
     * </ul>
     *
     * @param cExploration the fixed exploration constant to use
     * @return a randomly sampled PrunerConfig with the specified exploration constant
     */
    private PrunerConfig sampleRandomConfigForC(double cExploration) {
        double threshold = 0.3 + rng.nextDouble() * 0.7; // [0.3, 1.0]
        double centralityWeight = rng.nextDouble();      // [0.0, 1.0]
        double connectivityWeight = 1.0 - centralityWeight;
        double biasScale = 0.02 + rng.nextDouble() * 0.06; // [0.02, 0.08]
        double spWeight = rng.nextDouble() * 0.2;         // [0.0, 0.2]
        return new PrunerConfig(threshold, centralityWeight, connectivityWeight, biasScale, spWeight, cExploration);
    }

    /**
     * Evaluates a configuration by playing a color-swapped tournament between
     * base MCTS and optimized MCTS. Each agent plays both colors to eliminate
     * first-player advantage bias.
     *
     * @param cfg the configuration to evaluate
     * @param printMatches whether to print individual match results
     * @return the win rate percentage of the optimized configuration against the base
     */
    public double evaluateConfig(PrunerConfig cfg, boolean printMatches) {
        AIAgent baseRed = new MCTSPlayer(Player.RED, iterations);
        AIAgent optBlack = new MCTSPlayer(Player.BLACK, iterations, cfg.threshold, cfg.centralityWeight, cfg.connectivityWeight, cfg.biasScale, cfg.spWeight, cfg.cExploration);
        AITester.TestResult result1 = AITester.runMatch(baseRed, optBlack, gamesPerSide, boardSize, printMatches);
        AIAgent optRed = new MCTSPlayer(Player.RED, iterations, cfg.threshold, cfg.centralityWeight, cfg.connectivityWeight, cfg.biasScale, cfg.spWeight, cfg.cExploration);
        AIAgent baseBlack = new MCTSPlayer(Player.BLACK, iterations);
        AITester.TestResult result2 = AITester.runMatch(optRed, baseBlack, gamesPerSide, boardSize, printMatches);
        int optWins = result1.blackWins + result2.redWins;
        int totalGames = result1.totalGames + result2.totalGames;
        return (totalGames > 0) ? (optWins * 100.0 / totalGames) : 0.0;
    }

    /**
     * Performs a random search over the entire hyperparameter space including
     * the exploration constant. Evaluates the specified number of random
     * configurations and reports the best one found.
     *
     * @param trials the number of random configurations to evaluate
     */
    public void randomSearch(int trials) {
        PrunerConfig bestCfg = null;
        double bestWinRate = -1.0;
        for (int i = 0; i < trials; i++) {
            PrunerConfig cfg = sampleRandomConfig();
            System.out.printf("Trial %d/%d: %s%n", i + 1, trials, cfg);
            double winRate = evaluateConfig(cfg, false);
            System.out.printf("  => optimized win rate vs base: %.2f%%%n", winRate);
            if (winRate > bestWinRate) {
                bestWinRate = winRate;
                bestCfg = cfg;
                System.out.printf("  NEW BEST! (winRate=%.2f%%)%n", bestWinRate);
            }
        }

        if (bestCfg != null) {
            System.out.println("\n==== BEST CONFIG FOUND ====");
            System.out.printf("  %s, winRate=%.2f%%%n", bestCfg, bestWinRate);
        } else {
            System.out.println("\nNo valid configs evaluated.");
        }
    }

    /**
     * Performs random search for each fixed exploration constant value.
     * For each C value, samples and evaluates multiple random configurations
     * of the other parameters. Reports the best configuration overall.
     *
     * @param cValues array of exploration constant values to test
     * @param trialsPerC number of random configurations to try for each C value
     */
    public void randomSearchPerC(double[] cValues, int trialsPerC) {
        PrunerConfig globalBestCfg = null;
        double globalBestWinRate = -1.0;
        for (double c : cValues) {
            System.out.printf("%n##### Tuning for c = %.5f #####%n", c);
            PrunerConfig bestCfgForC = null;
            double bestWinRateForC = -1.0;
            for (int i = 0; i < trialsPerC; i++) {
                PrunerConfig cfg = sampleRandomConfigForC(c);
                System.out.printf("  Trial %d/%d (c=%.5f): %s%n",
                        i + 1, trialsPerC, c, cfg);
                double winRate = evaluateConfig(cfg, false);
                System.out.printf("    => optimized win rate vs base: %.2f%%%n", winRate);
                if (winRate > bestWinRateForC) {
                    bestWinRateForC = winRate;
                    bestCfgForC = cfg;
                    System.out.printf("    NEW BEST FOR c=%.5f! (winRate=%.2f%%)%n",
                            c, bestWinRateForC);
                }
            }
            if (bestCfgForC != null) {
                System.out.printf("%n== Best config for c=%.5f ==%n", c);
                System.out.printf("  %s, winRate=%.2f%%%n%n",
                        bestCfgForC, bestWinRateForC);
            } else {
                System.out.printf("%nNo valid configs evaluated for c=%.5f%n", c);
            }
            if (bestWinRateForC > globalBestWinRate) {
                globalBestWinRate = bestWinRateForC;
                globalBestCfg = bestCfgForC;
            }
        }

        if (globalBestCfg != null) {
            System.out.println("\n==== GLOBAL BEST CONFIG ACROSS ALL c ====");
            System.out.printf("  %s, winRate=%.2f%%%n",
                    globalBestCfg, globalBestWinRate);
        }
    }
}











