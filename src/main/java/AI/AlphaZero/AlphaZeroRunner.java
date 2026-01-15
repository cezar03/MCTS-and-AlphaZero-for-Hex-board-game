package AI.AlphaZero;

/**
 * Entry point for AlphaZero training with factory pattern.
 * Demonstrates how to configure and run AlphaZero training.
 * 
 * @author Team 04
 */
public class AlphaZeroRunner {
    public static void main(String[] args) {
        // Example 1: Using the factory builder pattern with default settings
        // exampleBasicTraining();
        
        // Example 2: Using the factory builder pattern with custom settings
        exampleCustomTraining();
    }
    
    /**
     * Example: Basic training with default configuration
     */
    private static void exampleBasicTraining() {
        // Create a trainer with default configuration (5x5 board, 100 iterations)
        AlphaZeroTrainer trainer = new AlphaZeroTrainer(5);
        
        // Run self-play games
        trainer.train(50, 50);
    }
    
    /**
     * Example: Custom training with factory builder pattern
     * Demonstrates the factory builder pattern for AlphaZero configuration
     */
    private static void exampleCustomTraining() {
        // Build custom configuration using the builder pattern
        AlphaZeroConfig config = new AlphaZeroConfig.Builder()
            .boardSize(7)                           // 7x7 Hex board
            .mctsIterations(200)                    // 200 MCTS iterations per move
            .temperature(1.0)                       // Exploratory play (high temperature)
            .cpuct(1.4)                             // PUCT exploration constant
            .modelPath("hex_alphazero_7x7.zip")    // Custom model path
            .loadExistingModel(false)               // Start with new model
            .build();
        
        // Create trainer with the custom configuration
        AlphaZeroTrainer trainer = new AlphaZeroTrainer(config);
        
        // Run self-play games
        trainer.train(100, 200);
    }
}