package AI.AlphaZero;

/**
 * Configuration class for AlphaZero setup.
 * Encapsulates all configuration parameters needed to set up an AlphaZero agent.
 * Uses the builder pattern for flexible configuration.
 * 
 * @author Team 04
 */
/**
 * Example usage:
 * AlphaZeroConfig config = new AlphaZeroConfig.Builder()
    .boardSize(7)
    .mctsIterations(200)
    .temperature(1.0)
    .cpuct(1.4)
    .modelPath("hex_alphazero_7x7.zip")
    .loadExistingModel(false)
    .build();
 */
public class AlphaZeroConfig {
    private final int boardSize;
    private final int mctsIterations;
    private final double temperature;
    private final double cpuct;
    private final String modelPath;
    private final boolean loadExistingModel;
    
    /**
     * Private constructor for builder pattern
     */
    private AlphaZeroConfig(Builder builder) {
        this.boardSize = builder.boardSize;
        this.mctsIterations = builder.mctsIterations;
        this.temperature = builder.temperature;
        this.cpuct = builder.cpuct;
        this.modelPath = builder.modelPath;
        this.loadExistingModel = builder.loadExistingModel;
    }
    
    // Getters
    public int getBoardSize() {
        return boardSize;
    }
    
    public int getMctsIterations() {
        return mctsIterations;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public double getCpuct() {
        return cpuct;
    }
    
    public String getModelPath() {
        return modelPath;
    }
    
    public boolean isLoadExistingModel() {
        return loadExistingModel;
    }
    
    /**
     * Builder class for creating AlphaZeroConfig instances.
     * Provides a fluent API for configuration.
     */
    public static class Builder {
        private int boardSize = 11;
        private int mctsIterations = 100;
        private double temperature = 1.0;
        private double cpuct = 1.4;
        private String modelPath = "hex_alphazero_model.zip";
        private boolean loadExistingModel = false;
        
        public Builder boardSize(int boardSize) {
            if (boardSize <= 0) {
                throw new IllegalArgumentException("Board size must be positive");
            }
            this.boardSize = boardSize;
            return this;
        }
        
        public Builder mctsIterations(int mctsIterations) {
            if (mctsIterations <= 0) {
                throw new IllegalArgumentException("MCTS iterations must be positive");
            }
            this.mctsIterations = mctsIterations;
            return this;
        }
        
        public Builder temperature(double temperature) {
            if (temperature < 0) {
                throw new IllegalArgumentException("Temperature must be non-negative");
            }
            this.temperature = temperature;
            return this;
        }
        
        public Builder cpuct(double cpuct) {
            if (cpuct < 0) {
                throw new IllegalArgumentException("CPUCT must be non-negative");
            }
            this.cpuct = cpuct;
            return this;
        }
        
        public Builder modelPath(String modelPath) {
            if (modelPath == null || modelPath.isEmpty()) {
                throw new IllegalArgumentException("Model path cannot be null or empty");
            }
            this.modelPath = modelPath;
            return this;
        }
        
        public Builder loadExistingModel(boolean loadExistingModel) {
            this.loadExistingModel = loadExistingModel;
            return this;
        }
        
        public AlphaZeroConfig build() {
            return new AlphaZeroConfig(this);
        }
    }
}
