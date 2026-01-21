package AI.registry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import AI.alphazero.config.AlphaZeroConfig;
import AI.alphazero.player.AlphaZeroPlayerFactory;
import AI.api.AIAdaptationConfig;
import AI.api.AIAgent;
import AI.api.AIAgentFactory;
import AI.mcts.MCTSPlayerFactory;
import AI.random.RandomPlayerFactory;
import game.core.Player;

/**
 * Central registry for managing available AI agent types and their factories.
 * <p>
 * This class handles the registration of different AI implementations (e.g., MCTS, Random, AlphaZero)
 * and provides a unified interface for creating factories, configurations, and agent instances
 * based on string identifiers.
 */
public final class AgentRegistry {
    private final AlphaZeroConfig alphaZeroConfig;
    private final Map<String, Supplier<AIAgentFactory>> factories = new LinkedHashMap<>();

    /**
     * Constructs the registry and registers the default agent types.
     * * @param alphaZeroConfig the configuration required for the AlphaZero factory
     * @throws IllegalArgumentException if alphaZeroConfig is null
     */
    public AgentRegistry(AlphaZeroConfig alphaZeroConfig) {
        if (alphaZeroConfig == null) throw new IllegalArgumentException("AlphaZeroConfig cannot be null");
        this.alphaZeroConfig = alphaZeroConfig;

        factories.put("MCTS", MCTSPlayerFactory::new);
        factories.put("Random", RandomPlayerFactory::new);
        factories.put("AlphaZero", () -> new AlphaZeroPlayerFactory(alphaZeroConfig));
    }

    /**
     * Retrieves the names of all registered agent types.
     * * @return a list of agent type strings (e.g., "MCTS", "Random", "AlphaZero")
     */
    public List<String> getAgentTypes() {
        return List.copyOf(factories.keySet());
    }

    /**
     * Creates an {@link AIAgentFactory} for the specified agent type.
     * * @param type the name of the agent type
     * @return the factory instance associated with the type
     * @throws IllegalArgumentException if the agent type is unknown
     */
    public AIAgentFactory createFactory(String type) {
        Supplier<AIAgentFactory> supplier = factories.get(type);
        if (supplier == null) { throw new IllegalArgumentException("Unknown agent type: " + type);}
        return supplier.get();
    }

    /**
     * Creates a suitable {@link AIAdaptationConfig} for the specified agent type.
     * <p>
     * This method abstracts the specific configuration needs of different agents,
     * such as setting iteration counts for MCTS.
     * * @param type the agent type
     * @param player the player the agent will control
     * @param iterations the number of iterations (relevant for MCTS-based agents)
     * @return a configured AIAdaptationConfig object
     */
    public AIAdaptationConfig createConfig(String type, Player player, int iterations) {
        if ("MCTS".equals(type)) {
            return new AIAdaptationConfig.Builder(player)
                    .iterations(Math.max(1, iterations))
                    .build();
        }
        return new AIAdaptationConfig.Builder(player).build();
    }

    /**
     * Convenience method to create a fully initialized AI agent in one step.
     * * @param type the agent type
     * @param player the player the agent will control
     * @param iterations the number of iterations (if applicable)
     * @return a new AIAgent instance
     */
    public AIAgent createAgent(String type, Player player, int iterations) {
        return createFactory(type).createAgent(createConfig(type, player, iterations));
    }
}
