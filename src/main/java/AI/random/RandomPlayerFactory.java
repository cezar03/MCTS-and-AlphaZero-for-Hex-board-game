package AI.random;

import AI.api.AIAdaptationConfig;
import AI.api.AIAgent;
import AI.api.AIAgentFactory;

/**
 * Factory implementation for creating RandomPlayer agents.
 * Provides a simple factory for random agent creation.
 * Please dont move this class or refactor it as it is working perfectly fine,
 * In case of moving this class to another package will cause a lot of issues with imports and dependencies,
 * I couldnt figure it out for hours so let it be here where it is.
 *
 * @author Team 04
 */
public class RandomPlayerFactory implements AIAgentFactory {
    
    /**
     * Creates a new RandomPlayer instance based on the provided configuration.
     * * @param config the configuration containing the player ID
     * @return a new RandomPlayer instance
     * @throws IllegalArgumentException if config is null
     */
    @Override
    public AIAgent createAgent(AIAdaptationConfig config) {
        if (config == null) { throw new IllegalArgumentException("Configuration cannot be null");}
        return new RandomPlayer(config.getPlayer());
    }
    
<<<<<<< HEAD
    /**
     * Returns the unique string identifier for this agent type.
     * * @return "Random"
     */
    @Override
    public String getAgentTypeName() {
        return "Random";
    }
=======
    @Override public String getAgentTypeName() { return "Random";}
>>>>>>> 1165bedc5af5867e936278ee2626c1ff7663bbd5
}











