package ai.mcts.Steps.Tuner;

/**
 * Test driver for the MCTSTuner that demonstrates hyperparameter tuning
 * for MCTS configurations. This class sets up and executes tuning experiments
 * with predefined parameters.
 * 
 * <p>This is a main class intended for running tuning experiments from the
 * command line.</p>
 */
public class TunerTester {
    /**
     * Main entry point for running MCTS tuning experiments.
     * Configures the tuner with a 7x7 board, 1000 iterations per move,
     * and 50 games per side. Tests exploration constants of sqrt(2) and 2.0
     * with 10 trials per constant.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        int boardSize    = 7;
        int iterations   = 1000;
        int gamesPerSide = 50;
        MCTSTuner tuner = new MCTSTuner(boardSize, iterations, gamesPerSide);
        double[] cValues = {
                Math.sqrt(2),
                2.0
        };
        int trialsPerC = 10;
        tuner.randomSearchPerC(cValues, trialsPerC);
        // tuner.randomSearch(10);
    }
}











