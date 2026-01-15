package AI.AlphaZero;

/**
 * Entry point for training the AlphaZero model for Hex.
 * This class configures and initiates the self-play training process.
 * 
 * <p>Training involves:
 * <ul>
 *   <li>Running self-play games</li>
 *   <li>Using MCTS with neural network guidance to generate training data</li>
 *   <li>Periodically updating the network with accumulated game data</li>
 *   <li>Saving the trained model for later use</li>
 * </ul>
*/
public class AlphaZeroRunner {
    /**
     * Main method to start the AlphaZero training process.
     * 
     * <p>Training parameters to configure:
     * <ul>
     *   <li>Board size (set to 11 for 11×11 Hex)</li>
     *   <li>Number of self-play games (100,000 games for thorough training)</li>
     *   <li>Batch size (500 games before each network update)</li>
     *   <li>MCTS iterations per move (200 simulations for each decision)</li>
     * </ul>
     * 
     * @param args command-line arguments
    */
    public static void main(String[] args) {
        // Train on a board (specify the size in the constructor, I do not specify it here because this allows us to be a bit more flexible during testing without having to change the comments every time)
        AlphaZeroTrainer trainer = new AlphaZeroTrainer(11);
        
        // TODO: Decide on the number of self-play games, batch size, and MCTS iterations per move.
        // Run self-play games, with a number MCTS simulations per move to be specified below.
        trainer.train(100_000, 500, 200);
    }
}