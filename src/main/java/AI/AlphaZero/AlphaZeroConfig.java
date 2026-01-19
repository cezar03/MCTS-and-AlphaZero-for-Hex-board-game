package AI.AlphaZero;

/**
 * Configuration class for AlphaZero setup.
 * Encapsulates all configuration parameters needed to set up an AlphaZero agent.
 * Uses the builder pattern for flexible and readable configuration.
 * 
 * <p>This class provides a centralized way to manage all AlphaZero hyperparameters
 * and settings, including:
 * <ul>
 *   <li>Board size</li>
 *   <li>MCTS simulation count</li>
 *   <li>Temperature for move selection</li>
 *   <li>CPUCT exploration constant</li>
 *   <li>Model file paths</li>
 *   <li>Model loading preferences</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * AlphaZeroConfig config = new AlphaZeroConfig.Builder()
 *     .boardSize(7)
 *     .mctsIterations(200)
 *     .temperature(1.0)
 *     .cpuct(1.4)
 *     .modelPath("hex_alphazero_7x7.zip")
 *     .loadExistingModel(false)
 *     .build();
 * </pre>
 * 
 * @author Team 04
*/
public class AlphaZeroConfig {
    private final int boardSize;
    private final int mctsIterations;
    private final double temperature;
    private final double cpuct;
    private final String modelPath;
    private final boolean loadExistingModel;
    
    /**
     * Private constructor for builder pattern.
     * Creates an immutable AlphaZeroConfig instance with parameters from the builder.
     * This constructor is only accessible through the Builder class to enforce
     * the builder pattern and ensure all configurations are properly validated.
     * 
     * @param builder the builder containing validated configuration parameters
    */
    private AlphaZeroConfig(Builder builder) {
        this.boardSize = builder.boardSize;
        this.mctsIterations = builder.mctsIterations;
        this.temperature = builder.temperature;
        this.cpuct = builder.cpuct;
        this.modelPath = builder.modelPath;
        this.loadExistingModel = builder.loadExistingModel;
    }
    
    // Getter methods

    /**
     * Returns the size of the Hex board.
     * 
     * @return the board size
    */
    public int getBoardSize() {
        return boardSize;
    }
    
    /**
     * Returns the number of MCTS simulations to perform per move.
     * 
     * @return the number of MCTS iterations per move
    */
    public int getMctsIterations() {
        return mctsIterations;
    }
    
    /**
     * Returns the temperature parameter for move selection.
     * Temperature controls the randomness of move selection from the MCTS policy:
     * <ul>
     *   <li>0.0: Deterministic selection (always pick most-visited move)</li>
     *   <li>1.0: Stochastic selection proportional to visit counts</li>
     * </ul>
     * 
     * @return the temperature value for move selection
    */
    public double getTemperature() {
        return temperature;
    }
    
    /**
     * Returns the CPUCT (PUCT) exploration constant.
     * This constant controls the exploration-exploitation tradeoff in the PUCT formula.
     * Higher values encourage more exploration of less-visited moves.
     * 
     * @return the CPUCT exploration constant
    */
    public double getCpuct() {
        return cpuct;
    }
    
    /**
     * Returns the file path for saving or loading the neural network model.
     * 
     * @return the path to the model file
    */
    public String getModelPath() {
        return modelPath;
    }
    
    /**
     * Returns whether to load an existing model from disk or start with a fresh model.
     * 
     * @return true if an existing model should be loaded, false to initialize a new model
    */
    public boolean isLoadExistingModel() {
        return loadExistingModel;
    }
    
    /**
     * Builder class for creating AlphaZeroConfig instances.
     * Provides a fluent API for configuration with sensible defaults and validation.
     * 
     * <p>Default values:
     * <ul>
     *   <li>boardSize: 11</li>
     *   <li>mctsIterations: 100</li>
     *   <li>temperature: 1.0</li>
     *   <li>cpuct: 1.4</li>
     *   <li>modelPath: "src/main/resources/models/hex_model_correct.zip"</li>
     *   <li>loadExistingModel: false</li>
     * </ul>
     * 
     * <p>All setter methods return the builder instance to enable method chaining.
    */
    public static class Builder {
        private int boardSize = 11;
        private int mctsIterations = 100;
        private double temperature = 1.0;
        private double cpuct = 1.4;
        private String modelPath = "src//main//resources//models//hex_model_correct.zip";
        private boolean loadExistingModel = false;
        
        /**
         * Sets the size of the Hex board.
         * 
         * @param boardSize the board size (must be positive)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if boardSize is not positive
        */
        public Builder boardSize(int boardSize) {
            if (boardSize <= 0) {
                throw new IllegalArgumentException("Board size must be positive");
            }
            this.boardSize = boardSize;
            return this;
        }
        
        /**
         * Sets the number of MCTS simulations to perform per move.
         * More iterations improve move quality but increase computation time.
         * 
         * @param mctsIterations the number of MCTS iterations (must be positive)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if mctsIterations is not positive
        */
        public Builder mctsIterations(int mctsIterations) {
            if (mctsIterations <= 0) {
                throw new IllegalArgumentException("MCTS iterations must be positive");
            }
            this.mctsIterations = mctsIterations;
            return this;
        }
        
        /**
         * Sets the temperature parameter for move selection.
         * Controls the randomness of move selection from the MCTS visit distribution.
         * 
         * @param temperature the temperature value (must be non-negative,
         *                    where 0.0 = deterministic and higher values = more random)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if temperature is negative
        */
        public Builder temperature(double temperature) {
            if (temperature < 0) {
                throw new IllegalArgumentException("Temperature must be non-negative");
            }
            this.temperature = temperature;
            return this;
        }
        
        /**
         * Sets the CPUCT exploration constant for the PUCT formula.
         * Higher values encourage exploration of less-visited nodes.
         * 
         * @param cpuct the CPUCT constant (must be non-negative, typical values: 1.0-2.0)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if cpuct is negative
        */
        public Builder cpuct(double cpuct) {
            if (cpuct < 0) {
                throw new IllegalArgumentException("CPUCT must be non-negative");
            }
            this.cpuct = cpuct;
            return this;
        }
        
        /**
         * Sets the file path for saving or loading the neural network model.
         * 
         * @param modelPath the path to the model file (must not be null or empty)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if modelPath is null or empty
        */
        public Builder modelPath(String modelPath) {
            if (modelPath == null || modelPath.isEmpty()) {
                throw new IllegalArgumentException("Model path cannot be null or empty");
            }
            this.modelPath = modelPath;
            return this;
        }
        
        /**
         * Sets whether to load an existing model from disk.
         * If true, the model will be loaded from the specified modelPath.
         * If false, a new model will be initialized with random weights.
         * 
         * @param loadExistingModel true to load existing model, false to create new
         * @return this builder instance for method chaining
        */
        public Builder loadExistingModel(boolean loadExistingModel) {
            this.loadExistingModel = loadExistingModel;
            return this;
        }
        
        /**
         * Builds and returns an immutable AlphaZeroConfig instance with the configured parameters.
         * All parameters have been validated by their respective setter methods.
         * 
         * @return a new AlphaZeroConfig instance with the specified configuration
        */
        public AlphaZeroConfig build() {
            return new AlphaZeroConfig(this);
        }
    }
}
