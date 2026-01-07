package AI.AiPlayer;

import Game.Player;

/**
 * Configuration class for AI agent setup and adaptation.
 * Encapsulates all configuration parameters needed to set up an AI agent,
 * making it easy to pass complex configurations without changing method signatures.
 * 
 * @author Team 04
 */
public class AIAdaptationConfig {
    private final Player player;
    private final int iterations;
    private final double threshold;
    private final double centralityWeight;
    private final double connectivityWeight;
    private final double biasScale;
    private final double shortestPathWeight;
    private final double explorationConstant;
    
    /**
     * Private constructor for builder pattern
     */
    private AIAdaptationConfig(Builder builder) {
        this.player = builder.player;
        this.iterations = builder.iterations;
        this.threshold = builder.threshold;
        this.centralityWeight = builder.centralityWeight;
        this.connectivityWeight = builder.connectivityWeight;
        this.biasScale = builder.biasScale;
        this.shortestPathWeight = builder.shortestPathWeight;
        this.explorationConstant = builder.explorationConstant;
    }
    
    // Getters
    public Player getPlayer() {
        return player;
    }
    
    public int getIterations() {
        return iterations;
    }
    
    public double getThreshold() {
        return threshold;
    }
    
    public double getCentralityWeight() {
        return centralityWeight;
    }
    
    public double getConnectivityWeight() {
        return connectivityWeight;
    }
    
    public double getBiasScale() {
        return biasScale;
    }
    
    public double getShortestPathWeight() {
        return shortestPathWeight;
    }
    
    public double getExplorationConstant() {
        return explorationConstant;
    }
    
    /**
     * Builder class for creating AIAdaptationConfig instances.
     * Provides a fluent API for configuration.
     */
    public static class Builder {
        private Player player;
        private int iterations = 1000;
        private double threshold = 0.0;
        private double centralityWeight = 0.0;
        private double connectivityWeight = 0.0;
        private double biasScale = 0.046;
        private double shortestPathWeight = 0.039;
        private double explorationConstant = Math.sqrt(2);
        
        /**
         * Creates a new builder with required player parameter
         */
        public Builder(Player player) {
            this.player = player;
        }
        
        /**
         * Sets the number of MCTS iterations
         */
        public Builder iterations(int iterations) {
            this.iterations = iterations;
            return this;
        }
        
        /**
         * Sets the pruning threshold
         */
        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }
        
        /**
         * Sets the centrality weight for heuristic
         */
        public Builder centralityWeight(double weight) {
            this.centralityWeight = weight;
            return this;
        }
        
        /**
         * Sets the connectivity weight for heuristic
         */
        public Builder connectivityWeight(double weight) {
            this.connectivityWeight = weight;
            return this;
        }
        
        /**
         * Sets the bias scale for expansion
         */
        public Builder biasScale(double biasScale) {
            this.biasScale = biasScale;
            return this;
        }
        
        /**
         * Sets the shortest path weight for heuristic
         */
        public Builder shortestPathWeight(double weight) {
            this.shortestPathWeight = weight;
            return this;
        }
        
        /**
         * Sets the exploration constant for UCT
         */
        public Builder explorationConstant(double constant) {
            this.explorationConstant = constant;
            return this;
        }
        
        /**
         * Builds the AIAdaptationConfig instance
         */
        public AIAdaptationConfig build() {
            return new AIAdaptationConfig(this);
        }
    }
}
