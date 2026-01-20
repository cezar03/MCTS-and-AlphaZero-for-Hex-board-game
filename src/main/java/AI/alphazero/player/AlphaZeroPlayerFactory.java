package ai.alphazero.player;

import ai.alphazero.batch.Batcher;
import ai.alphazero.batch.DirectBatcher;
import ai.alphazero.config.AlphaZeroConfig;
import ai.alphazero.mcts.AlphaZeroMCTS;
import ai.alphazero.net.AlphaZeroNet;
import ai.api.AIAdaptationConfig;
import ai.api.AIAgent;
import ai.api.AIAgentFactory;
import game.core.Player;

public class AlphaZeroPlayerFactory implements AIAgentFactory {
    private final AlphaZeroConfig alphaZeroConfig;

    public AlphaZeroPlayerFactory(AlphaZeroConfig config) {
        if (config == null) throw new IllegalArgumentException("AlphaZero configuration cannot be null");
        this.alphaZeroConfig = config;
    }

    public AlphaZeroPlayerFactory() {
        this.alphaZeroConfig = new AlphaZeroConfig.Builder().build();
    }

    @Override
    public AIAgent createAgent(AIAdaptationConfig config) {
        if (config == null) throw new IllegalArgumentException("Configuration cannot be null");

        Player player = config.getPlayer();

        AlphaZeroNet network = createNetwork();

        // UI mode: DirectBatcher. Training mode uses MultiGpuBatcher elsewhere (AlphaZeroTrainer).
        Batcher batcher = new DirectBatcher(network);

        AlphaZeroMCTS mcts = new AlphaZeroMCTS(batcher, alphaZeroConfig);

        return new AlphaZeroPlayer(player, mcts, alphaZeroConfig);
    }

    @Override
    public String getAgentTypeName() {
        return "AlphaZero";
    }

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

    public AlphaZeroConfig getConfig() {
        return alphaZeroConfig;
    }
}











