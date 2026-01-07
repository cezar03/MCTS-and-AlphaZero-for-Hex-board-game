package AI.AiPlayer;

/**
 * Factory implementation for creating RandomPlayer agents.
 * Provides a simple factory for random agent creation.
 * 
 * @author Team 04
 */
public class RandomPlayerFactory implements AIAgentFactory {
    
    @Override
    public AIAgent createAgent(AIAdaptationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
        return new RandomPlayer(config.getPlayer());
    }
    
    @Override
    public String getAgentTypeName() {
        return "Random";
    }
}
