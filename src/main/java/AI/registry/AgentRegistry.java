package ai.registry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import ai.alphazero.config.AlphaZeroConfig;
import ai.alphazero.player.AlphaZeroPlayerFactory;
import ai.api.AIAdaptationConfig;
import ai.api.AIAgent;
import ai.api.AIAgentFactory;
import ai.mcts.MCTSPlayerFactory;
import ai.random.RandomPlayerFactory;
import game.core.Player;

public final class AgentRegistry {
    private final AlphaZeroConfig alphaZeroConfig;
    private final Map<String, Supplier<AIAgentFactory>> factories = new LinkedHashMap<>();

    public AgentRegistry(AlphaZeroConfig alphaZeroConfig) {
        if (alphaZeroConfig == null) throw new IllegalArgumentException("AlphaZeroConfig cannot be null");
        this.alphaZeroConfig = alphaZeroConfig;

        factories.put("MCTS", MCTSPlayerFactory::new);
        factories.put("Random", RandomPlayerFactory::new);
        factories.put("AlphaZero", () -> new AlphaZeroPlayerFactory(alphaZeroConfig));
    }

    public List<String> getAgentTypes() {
        return List.copyOf(factories.keySet());
    }

    public AIAgentFactory createFactory(String type) {
        Supplier<AIAgentFactory> supplier = factories.get(type);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown agent type: " + type);
        }
        return supplier.get();
    }

    public AIAdaptationConfig createConfig(String type, Player player, int iterations) {
        if ("MCTS".equals(type)) {
            return new AIAdaptationConfig.Builder(player)
                    .iterations(Math.max(1, iterations))
                    .build();
        }
        return new AIAdaptationConfig.Builder(player).build();
    }

    public AIAgent createAgent(String type, Player player, int iterations) {
        return createFactory(type).createAgent(createConfig(type, player, iterations));
    }
}
