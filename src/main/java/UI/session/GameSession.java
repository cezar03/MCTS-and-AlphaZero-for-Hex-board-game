package UI.session;

import bridge.BoardAdapter;
import game.core.Color;
import game.core.Player;
import game.core.Rules;

public final class GameSession {
    private final BoardAdapter adapter;
    private Player currentPlayer = Player.RED;
    private boolean gameOver = false;
    private int moveCount = 0;

    public GameSession(BoardAdapter adapter) {
        if (adapter == null) throw new IllegalArgumentException("BoardAdapter cannot be null");
        this.adapter = adapter;
    }

    public BoardAdapter getAdapter() {
        return adapter;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public Player getWinner() {
        return adapter.getWinner();
    }

    public void reset() {
        adapter.reset();
        currentPlayer = Player.RED;
        gameOver = false;
        moveCount = 0;
    }

    public boolean applyHumanMove(int row, int col) {
        return applyMoveInternal(row, col, true);
    }

    public boolean applyMove(int row, int col) {
        return applyMoveInternal(row, col, false);
    }

    private boolean applyMoveInternal(int row, int col, boolean allowPieRuleSwap) {
        if (gameOver) return false;

        if (allowPieRuleSwap && Rules.pieRuleAvailable(moveCount, currentPlayer)) {
            Color clicked = adapter.getCell(row, col);
            if (clicked == currentPlayer.other().stone) {
                adapter.undoMove(row, col);
                adapter.makeMove(row, col, currentPlayer);
                moveCount++;
                advanceTurnOrEnd();
                return true;
            }
        }

        if (!adapter.makeMove(row, col, currentPlayer)) return false;

        moveCount++;
        advanceTurnOrEnd();
        return true;
    }

    private void advanceTurnOrEnd() {
        if (adapter.isGameOver()) {
            gameOver = true;
            return;
        }
        currentPlayer = currentPlayer.other();
    }
}
