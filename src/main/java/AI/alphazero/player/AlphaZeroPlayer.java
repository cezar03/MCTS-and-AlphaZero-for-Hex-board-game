package AI.alphazero.player;

import AI.alphazero.config.AlphaZeroConfig;
import AI.alphazero.mcts.AlphaZeroMCTS;
import AI.api.AIAgent;
import AI.api.AIBoardAdapter;
import AI.mcts.Node;
import bridge.BoardConverters;
import game.core.Board;
import game.core.Color;
import game.core.Move;
import game.core.Player;

public class AlphaZeroPlayer implements AIAgent {
    private final Player player;
    private final AlphaZeroMCTS mcts;
    private final AlphaZeroConfig config;

    public AlphaZeroPlayer(Player player, AlphaZeroMCTS mcts, AlphaZeroConfig config) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (mcts == null) throw new IllegalArgumentException("MCTS cannot be null");
        if (config == null) throw new IllegalArgumentException("Config cannot be null");
        this.player = player;
        this.mcts = mcts;
        this.config = config;
    }

    @Override
    public Move getBestMove(AIBoardAdapter boardAdapter, Player currentPlayer) {
        if (boardAdapter == null) throw new IllegalArgumentException("Board cannot be null");
        if (currentPlayer == null) throw new IllegalArgumentException("Current player cannot be null");
        if (!controlsPlayer(currentPlayer)) { throw new IllegalArgumentException("This agent does not control player: " + currentPlayer);}
        Color color = currentPlayer.stone;
        Board gameBoard = BoardConverters.toBoard(boardAdapter);
        Node root = mcts.search(gameBoard, color, config.getMctsIterations(), false);
        double[] policy = mcts.getSearchPolicy(root, config.getTemperature());
        return selectBestLegalMove(policy, boardAdapter);
    }

    @Override public Player getPlayer() { return player;}
    @Override public boolean controlsPlayer(Player p) { return this.player == p;}
    @Override public void initialize() {}
    @Override public void cleanup() {}

    private Move selectBestLegalMove(double[] policy, AIBoardAdapter boardAdapter) {
        int n = boardAdapter.getSize();
        double best = Double.NEGATIVE_INFINITY;
        int bestR = -1, bestC = -1;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (boardAdapter.getCell(r, c) != Color.EMPTY) continue;
                int idx = r * n + c;
                if (idx >= 0 && idx < policy.length && policy[idx] > best) {
                    best = policy[idx];
                    bestR = r;
                    bestC = c;
                }
            }
        }

        if (bestR == -1) return null;
        return Move.get(bestR, bestC);
    }
}











