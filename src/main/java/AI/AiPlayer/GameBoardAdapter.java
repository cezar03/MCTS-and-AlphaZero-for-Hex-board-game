package AI.AiPlayer;

import java.util.List;

import Game.Board;
import Game.Color;
import Game.Player;
import Game.Rules;

/**
 * AI-side adapter around Game.Board. This keeps Game independent from AI.
 */
public final class GameBoardAdapter implements AIBoardAdapter {
    private final Board board;

    public GameBoardAdapter(Board board) {
        this.board = board;
    }

    public Board getBoard() { return board; }

    @Override public Color getCell(int row, int col) { return board.getCell(row, col); }
    @Override public int getSize() { return board.getSize(); }
    @Override public boolean inBounds(int row, int col) { return board.inBounds(row, col); }
    @Override public boolean isEmpty(int row, int col) { return board.isEmpty(row, col); }
    @Override public List<int[]> legalMoves() { return board.legalMoves(); }
    @Override public boolean isTerminal() { return board.isTerminal(); }
    @Override public boolean redWins() { return board.redWins(); }
    @Override public boolean blackWins() { return board.blackWins(); }

    @Override
    public AIBoardAdapter copy() {
        Board b2 = board.fastCopy();
        b2.clearMoveHistory();
        return new GameBoardAdapter(b2);
    }

    @Override
    public boolean makeMove(int row, int col, Player player) {
        if (!Rules.validMove(board, row, col)) return false;
        if (player == Player.RED) board.getMoveRed(row, col, null);
        else board.getMoveBlack(row, col, null);
        return true;
    }

    @Override public void getMoveRed(int row, int col, Color ignored) { board.getMoveRed(row, col, null); }
    @Override public void getMoveBlack(int row, int col, Color ignored) { board.getMoveBlack(row, col, null); }
    @Override public List<int[]> neighbors(int row, int col) { return board.neighbors(row, col); }
}

