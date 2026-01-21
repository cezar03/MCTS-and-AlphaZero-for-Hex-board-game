package AI.alphazero.player;

import AI.alphazero.batch.Batcher;
import AI.alphazero.batch.DirectBatcher;
import AI.alphazero.config.AlphaZeroConfig;
import AI.alphazero.mcts.AlphaZeroMCTS;
import AI.alphazero.net.AlphaZeroNet;
import AI.api.AIAdaptationConfig;
import AI.api.AIAgent;
import AI.api.AIAgentFactory;
import game.core.Player;

/**
 * Factory for creating {@link AlphaZeroPlayer} instances.
 * <p>
 * This factory manages the complexity of initializing the AlphaZero neural network
 * and the associated batching mechanisms (e.g., {@link AI.alphazero.batch.DirectBatcher} for UI play).
 */
public class AlphaZeroPlayerFactory implements AIAgentFactory {
    private final AlphaZeroConfig alphaZeroConfig;

    /**
     * Constructs a factory with a specific configuration.
     * * @param config the AlphaZero configuration
     * @throws IllegalArgumentException if config is null
     */
    public AlphaZeroPlayerFactory(AlphaZeroConfig config) {
        if (config == null) throw new IllegalArgumentException("AlphaZero configuration cannot be null");
        this.alphaZeroConfig = config;
    }

    /**
     * Constructs a factory with a default configuration.
     */
    public AlphaZeroPlayerFactory() {
        this.alphaZeroConfig = new AlphaZeroConfig.Builder().build();
    }

    /**
     * Creates an AlphaZero agent.
     * <p>
     * Initializes the neural network (loading from disk if configured) and sets up
     * the inference batcher.
     * * @param config the general AI adaptation configuration
     * @return a new AlphaZeroPlayer
     * @throws IllegalArgumentException if config is null
     */
    @Override
    public AIAgent createAgent(AIAdaptationConfig config) {
        if (config == null) throw new IllegalArgumentException("Configuration cannot be null");
        Player player = config.getPlayer();
        AlphaZeroNet network = createNetwork();
        Batcher batcher = new DirectBatcher(network);
        AlphaZeroMCTS mcts = new AlphaZeroMCTS(batcher, alphaZeroConfig);
        return new AlphaZeroPlayer(player, mcts, alphaZeroConfig);
    }

    @Override public String getAgentTypeName() { return "AlphaZero";}

    private AlphaZeroNet createNetwork() {
        if (alphaZeroConfig.isLoadExistingModel()) {
            try {
                return AlphaZeroNet.load(alphaZeroConfig.getModelPath(), alphaZeroConfig.getBoardSize());
            } catch (Exception e) {
                System.err.println("Failed to load model from " + alphaZeroConfig.getModelPath() +
                        ", creating new model. Error: " + e.getMessage());
            }
        }
        return new AlphaZeroNet(alphaZeroConfig.getBoardSize());
    }

    public AlphaZeroConfig getConfig() { return alphaZeroConfig; }
}











