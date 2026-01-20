package game.view;

import java.util.List;

import game.core.Board;
import game.core.Color;
import game.core.Player;
import game.core.Rules;

/**
 * Pure Game-side view of Board. No AI dependencies.
 */
public final class BoardStateView {
    private final Board board;

    public BoardStateView(Board board) {
        this.board = board;
    }

    public Board getBoard() { return board; }

    public int getSize() { return board.getSize(); }
    public Color getCell(int r, int c) { return board.getCell(r, c); }
    public boolean inBounds(int r, int c) { return board.inBounds(r, c); }
    public boolean isEmpty(int r, int c) { return board.isEmpty(r, c); }

    public List<int[]> legalMoves() { return board.legalMoves(); }
    public List<int[]> neighbors(int r, int c) { return board.neighbors(r, c); }

    public boolean isTerminal() { return board.isTerminal(); }
    public boolean redWins() { return board.redWins(); }
    public boolean blackWins() { return board.blackWins(); }

    public boolean makeMove(int r, int c, Player p) {
        if (!Rules.validMove(board, r, c)) return false;
        if (p == Player.RED) board.getMoveRed(r, c, null);
        else board.getMoveBlack(r, c, null);
        return true;
    }
}











