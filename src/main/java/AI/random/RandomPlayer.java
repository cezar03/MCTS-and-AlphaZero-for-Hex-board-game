package AI.random;

import AI.api.AIAgent;
import AI.api.AIBoardAdapter;
import game.core.Color;
import game.core.Move;
import game.core.Player;

public class RandomPlayer implements AIAgent {
    private final Player randomPlayer;

    public RandomPlayer(Player randomPlayer) {
        this.randomPlayer = randomPlayer;
    }

    @Override
    public Move getBestMove(AIBoardAdapter board, Player currentPlayer) {
        int n = board.getSize();
        int empties = 0;

        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (board.getCell(r, c) == Color.EMPTY) empties++;

        if (empties == 0) return null;

        int k = (int) (Math.random() * empties);

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board.getCell(r, c) == Color.EMPTY) { if (k-- == 0) return Move.get(r, c); }
            }
        }
        return null;
    }

    @Override public Player getPlayer() { return randomPlayer;}
    @Override public boolean controlsPlayer(Player player) { return this.randomPlayer == player; }
}











