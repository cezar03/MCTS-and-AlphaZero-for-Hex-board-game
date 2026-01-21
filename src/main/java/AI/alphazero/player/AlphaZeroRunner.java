package AI.alphazero.player;

import AI.alphazero.train.AlphaZeroTrainer;

/**
 * Entry point for running AlphaZero training sessions from the command line.
 * <p>
 * Parses command-line arguments for the number of games and iterations, then
 * initiates the {@link AlphaZeroTrainer}.
 */
public class AlphaZeroRunner {

    /**
     * The main method.
     * <p>
     * Accepts two optional integer arguments:
     * 1. Number of games (default: 50,000)
     * 2. MCTS iterations per move (default: 50)
     * * @param args command line arguments
     */
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Shutting down...")));
        System.out.println(">>> 1. Initializing...");
        AlphaZeroTrainer trainer = new AlphaZeroTrainer(11);
        System.out.println(">>> 2. Running Smoke Test (Fast)...");

        int games = 50_000;
        int iterations = 50;
        int integersFound = 0;
        
        for (String arg : args) {
            try {
                int val = Integer.parseInt(arg);
                if (integersFound == 0) {
                    games = val;
                    integersFound++;
                } else if (integersFound == 1) {
                    iterations = val;
                    integersFound++;
                }
            } catch (NumberFormatException e) {
            }
        }
        
        System.out.println("Running " + games + " games with " + iterations + " iterations.");
        trainer.train(games, iterations);
        System.out.println(">>> 3. Finished.");
    }
}










