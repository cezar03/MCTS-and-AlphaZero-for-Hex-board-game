package AI.AlphaZero;

import AI.AiPlayer.AIAgent;
import AI.AiPlayer.AIBoardAdapter;
import AI.AiPlayer.BoardConverters;
import AI.mcts.HexGame.Move;
import AI.mcts.Node;
import Game.Board;
import Game.Color;
import Game.Player;

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
        if (!controlsPlayer(currentPlayer)) {
            throw new IllegalArgumentException("This agent does not control player: " + currentPlayer);
        }

        Color color = currentPlayer.stone;

        Board gameBoard = BoardConverters.toBoard(boardAdapter);


        // IMPORTANT: UI play => training=false (no Dirichlet noise)
        Node root = mcts.search(gameBoard, color, config.getMctsIterations(), false);

        double[] policy = mcts.getSearchPolicy(root, config.getTemperature());

        return selectBestLegalMove(policy, boardAdapter);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean controlsPlayer(Player p) {
        return this.player == p;
    }

    @Override
    public void initialize() {}

    @Override
    public void cleanup() {}

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

        if (bestR == -1) return null; // no legal moves
        return Move.get(bestR, bestC);
    }
}
