package AI.mcts.Steps.Tuner;

import Game.Player;

import java.io.ObjectInputFilter.Config;
import java.util.Random;

import AI.AiPlayer.AIAgent;
import AI.AiPlayer.AITester;
import AI.AiPlayer.MCTSPlayer;

/**
 * Simple hyperparameter tuner for the MCTS MovePruner heuristics.
 *
 * It samples random (threshold, centralityWeight, connectivityWeight)
 * configurations, builds an "optimized" MCTSPlayer with those parameters,
 * and evaluates it against a baseline "base" MCTSPlayer (no pruning, random rollouts)
 * at equal iterations.
 */
public final class MCTSTuner {

    /**
     * Configuration for the MovePruner / heuristic rollout.
     */
    private final int boardSize;
    private final int iterations;
    private final int gamesPerSide;
    private final Random rng;

    /**
     * @param boardSize    board size (e.g. 7)
     * @param iterations   MCTS iterations for both base and optimized
     * @param gamesPerSide games as RED + games as BLACK for each evaluation (per round)
     * @param seed         RNG seed (for reproducibility)
     */
    public MCTSTuner(int boardSize, int iterations, int gamesPerSide, long seed) {
        this.boardSize = boardSize;
        this.iterations = iterations;
        this.gamesPerSide = gamesPerSide;
        this.rng = new Random(seed);
    }

    public MCTSTuner(int boardSize, int iterations, int gamesPerSide) {
        this(boardSize, iterations, gamesPerSide, System.currentTimeMillis());
    }

    /**
     * Sample a random configuration in a reasonable range for 7x7 Hex.
     * Adjust ranges as needed.
     */
    private PrunerConfig sampleRandomConfig() {
        // threshold in [0.1, 0.6] (how wide we keep around best score)
        double threshold = 0.1 + rng.nextDouble() * 0.5;

        // centralityWeight in [0.0, 0.7], connectivityWeight = 1 - centrality
        double centralityWeight = rng.nextDouble() * 0.7;
        double connectivityWeight = 1.0 - centralityWeight;

        return new PrunerConfig(threshold, centralityWeight, connectivityWeight);
    }

    /**
     * Evaluate a single configuration by playing a color-swapped mini-tournament
     * between:
     *   - base MCTS (no pruning) and
     *   - optimized MCTS (with given config)
     *
     * @return win rate of the optimized agent (in %, 0..100) vs the base agent.
     */
    public double evaluateConfig(PrunerConfig cfg, boolean printMatches) {
        // Round 1: base as RED, optimized as BLACK
        AIAgent baseRed = new MCTSPlayer(Player.RED, iterations);
        AIAgent optBlack = new MCTSPlayer(Player.BLACK, iterations,
                cfg.threshold, cfg.centralityWeight, cfg.connectivityWeight);

        AITester.TestResult r1 = AITester.runMatch(
                baseRed, optBlack, gamesPerSide, boardSize, printMatches
        );

        // Round 2: optimized as RED, base as BLACK
        AIAgent optRed = new MCTSPlayer(Player.RED, iterations,
                cfg.threshold, cfg.centralityWeight, cfg.connectivityWeight);
        AIAgent baseBlack = new MCTSPlayer(Player.BLACK, iterations);

        AITester.TestResult r2 = AITester.runMatch(
                optRed, baseBlack, gamesPerSide, boardSize, printMatches
        );

        int optWins = r1.blackWins + r2.redWins;
        int totalGames = r1.totalGames + r2.totalGames;

        return (totalGames > 0) ? (optWins * 100.0 / totalGames) : 0.0;
    }

    /**
     * Run random search for a number of trials, printing progress
     * and reporting the best configuration found.
     */
    public void randomSearch(int trials) {
        PrunerConfig bestCfg = null;
        double bestWinRate = -1.0;

        for (int i = 0; i < trials; i++) {
            PrunerConfig cfg = sampleRandomConfig();
            System.out.printf(
                    "Trial %d/%d: %s%n",
                    i + 1, trials, cfg.toString()
            );

            // false => don't spam per-game prints; you still get match summaries
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
            System.out.printf("  %s, winRate=%.2f%%%n",
                    bestCfg.toString(), bestWinRate);
        } else {
            System.out.println("\nNo valid configs evaluated.");
        }
    }
}
