package AI.api;

import game.core.Player;

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
     * Private constructor for builder pattern.
     * @param builder
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
    
    /**
     * Gets the player associated with this configuration.
     * * @return the Player (RED or BLACK)
     */
    public Player getPlayer() { return player;}

    /**
     * Gets the number of MCTS iterations to perform.
     * * @return the iteration count
     */
    public int getIterations() { return iterations;}

    /**
     * Gets the pruning threshold for move selection.
     * * @return the threshold value
     */
    public double getThreshold() { return threshold;}

    /**
     * Gets the weight applied to the centrality heuristic.
     * * @return the centrality weight
     */
    public double getCentralityWeight() { return centralityWeight; }

    /**
     * Gets the weight applied to the connectivity heuristic.
     * * @return the connectivity weight
     */
    public double getConnectivityWeight() { return connectivityWeight;}
    
    /**
     * Gets the scaling factor for the exploration bias in MCTS.
     * * @return the bias scale
     */
    public double getBiasScale() { return biasScale;}
    
    /**
     * Gets the weight applied to the shortest path heuristic.
     * * @return the shortest path weight
     */
    public double getShortestPathWeight() { return shortestPathWeight; }
    
    /**
     * Gets the exploration constant (C) used in the UCT formula.
     * * @return the exploration constant
     */
    public double getExplorationConstant() { return explorationConstant;}
    
    /**
     * Builder pattern for constructing {@link AIAdaptationConfig} instances.
     * <p>
     * Allows for fluent configuration of the many parameters required for AI agents,
     * providing sensible defaults where appropriate.
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
         * Initializes the builder with the mandatory player parameter.
         * * @param player the player for whom the agent is being configured
         */
        public Builder(Player player) { this.player = player;}
        
        /**
         * Sets the number of MCTS iterations.
         * * @param iterations the number of iterations (default is 1000)
         * @return this builder instance
         */
        public Builder iterations(int iterations) {
            this.iterations = iterations;
            return this;
        }
        
        /**
         * Sets the move pruning threshold.
         * * @param threshold the threshold value (default is 0.0)
         * @return this builder instance
         */
        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }
        
        /**
         * Sets the weight for the centrality heuristic.
         * * @param weight the weight (default is 0.0)
         * @return this builder instance
         */
        public Builder centralityWeight(double weight) {
            this.centralityWeight = weight;
            return this;
        }
        
        /**
         * Sets the weight for the connectivity heuristic.
         * * @param weight the weight (default is 0.0)
         * @return this builder instance
         */
        public Builder connectivityWeight(double weight) {
            this.connectivityWeight = weight;
            return this;
        }
        
        /**
         * Sets the bias scale for MCTS expansion.
         * * @param biasScale the bias scale (default is 0.046)
         * @return this builder instance
         */
        public Builder biasScale(double biasScale) {
            this.biasScale = biasScale;
            return this;
        }
        
        /**
         * Sets the weight for the shortest path heuristic.
         * * @param weight the weight (default is 0.039)
         * @return this builder instance
         */
        public Builder shortestPathWeight(double weight) {
            this.shortestPathWeight = weight;
            return this;
        }
        
        /**
         * Sets the exploration constant for the UCT formula.
         * * @param constant the exploration constant (default is sqrt(2))
         * @return this builder instance
         */
        public Builder explorationConstant(double constant) {
            this.explorationConstant = constant;
            return this;
        }
        
        /**
         * Builds the {@link AIAdaptationConfig} instance with the configured parameters.
         * * @return the constructed AIAdaptationConfig
         */
        public AIAdaptationConfig build() {
            return new AIAdaptationConfig(this);
        }
    }
}











