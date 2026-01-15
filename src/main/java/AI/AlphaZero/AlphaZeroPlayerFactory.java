package AI.AlphaZero;

import AI.AiPlayer.AIAdaptationConfig;
import AI.AlphaZero.AlphaZeroConfig.Builder;
import AI.AiPlayer.AIAgent;
import AI.AiPlayer.AIAgentFactory;
import Game.Player;

/**
 * Factory implementation for creating AlphaZero agents.
 * Handles all complexity of AlphaZeroPlayer instantiation with appropriate configuration.
 * Implements the factory pattern for decoupled agent creation.
 * 
 * @author Team 04
 */
public class AlphaZeroPlayerFactory implements AIAgentFactory {
    private final AlphaZeroConfig alphaZeroConfig;
    
    /**
     * Creates an AlphaZeroPlayerFactory with custom configuration.
     * @param config The AlphaZero configuration
     */
    public AlphaZeroPlayerFactory(AlphaZeroConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("AlphaZero configuration cannot be null");
        }
        this.alphaZeroConfig = config;
    }
    
    /**
     * Creates an AlphaZeroPlayerFactory with default configuration.
     */
    public AlphaZeroPlayerFactory() {
        this.alphaZeroConfig = new AlphaZeroConfig.Builder().build();
    }
    
    @Override
    public AIAgent createAgent(AIAdaptationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
        Player player = config.getPlayer();
        
        // Create or load the neural network
        AlphaZeroNet network = createNetwork();
        
        // Create MCTS with the network
        AlphaZeroMCTS mcts = new AlphaZeroMCTS(network);
        
        // Create and return the AlphaZero player agent
        return new AlphaZeroPlayer(player, mcts, alphaZeroConfig);
    }
    
    @Override
    public String getAgentTypeName() {
        return "AlphaZero";
    }
    
    /**
     * Creates or loads a neural network based on configuration.
     * @return An initialized AlphaZeroNet
     */
    private AlphaZeroNet createNetwork() {
        // If loadExistingModel is true, try to load from the specified path
        if (alphaZeroConfig.isLoadExistingModel()) {
            try {
                return AlphaZeroNet.load(alphaZeroConfig.getModelPath());
            } catch (Exception e) {
                System.err.println("Failed to load model from " + alphaZeroConfig.getModelPath() + 
                                 ", creating new model. Error: " + e.getMessage());
                // Fall through to create a new model
            }
        }
        
        // Create a new network
        return new AlphaZeroNet(alphaZeroConfig.getBoardSize());
    }
    
    /**
     * Gets the underlying AlphaZero configuration used by this factory.
     * @return The AlphaZero configuration
     */
    public AlphaZeroConfig getConfig() {
        return alphaZeroConfig;
    }
}
