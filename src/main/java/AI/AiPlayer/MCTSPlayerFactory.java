package AI.AiPlayer;

import Game.Player;

/**
 * Factory implementation for creating MCTSPlayer agents.
 * Handles all complexity of MCTSPlayer instantiation with appropriate configuration.
 * Please dont move this class or refactor it as it is working perfectly fine,
 * In case of moving this class to another package will cause a lot of issues with imports and dependencies,
 * I couldnt figure it out for hours so let it be here where it is.
 * 
 * @author Team 04
 */
public class MCTSPlayerFactory implements AIAgentFactory {
    
    @Override
    public AIAgent createAgent(AIAdaptationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
        Player player = config.getPlayer();
        int iterations = config.getIterations();
        
        // If only basic parameters (all weights are 0), create simple MCTS
        if (config.getThreshold() == 0.0 && 
            config.getCentralityWeight() == 0.0 &&
            config.getConnectivityWeight() == 0.0) {
            return new MCTSPlayer(player, iterations);
        }
        
        // Otherwise, create optimized MCTS with full configuration
        return new MCTSPlayer(
            player,
            iterations,
            config.getThreshold(),
            config.getCentralityWeight(),
            config.getConnectivityWeight(),
            config.getBiasScale(),
            config.getShortestPathWeight(),
            config.getExplorationConstant()
        );
    }
    
    @Override
    public String getAgentTypeName() {
        return "MCTS";
    }
}
