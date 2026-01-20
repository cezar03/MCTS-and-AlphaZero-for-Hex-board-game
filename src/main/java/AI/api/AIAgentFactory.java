package ai.api;

/**
 * Factory interface for creating AI agents.
 * Decouples agent instantiation from client code (GameController, NavigationService).
 * Allows runtime flexibility in agent creation and configuration.
 * 
 * @author Team 04
 */
public interface AIAgentFactory {
    /**
     * Creates an AI agent with the given configuration.
     * @param config The configuration for the agent
     * @return A new AIAgent instance configured according to the config
     */
    AIAgent createAgent(AIAdaptationConfig config);
    
    /**
     * Gets a descriptive name for this factory's agent type.
     * @return A string describing the agent type (e.g., "MCTS", "Random")
     */
    String getAgentTypeName();
}











