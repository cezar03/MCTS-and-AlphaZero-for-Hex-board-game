package AI.mcts.HexGame;

import java.util.ArrayList;
import java.util.List;

import AI.mcts.Optimazation.ShortestPath;
import game.core.Board;
import game.core.Color;
import game.core.Move;
import game.core.Player;
import game.core.Rules;

public class GameState {
    private final Board board;
    private Player toMove;
    private boolean terminal = false;
    private int winnerId = 0;

    public GameState(Board board, Player toMove) {
        this.board = board;
        this.toMove = toMove;
        recomputeTerminal();
    }

    public boolean isTerminal() { return terminal; }
    public int getWinnerId() { return winnerId; }
    public Player getToMove() { return toMove; }
    public Board getBoard() { return board; }

    public List<Move> getLegalMoves() {
        List<Move> out = new ArrayList<>();
        for (int[] rc : board.legalMoves()) { out.add(Move.get(rc[0], rc[1]));}
        return out;
    }

    public void doMove(Move move) {
        if (terminal) { return; }
        if (!Rules.validMove(board, move.row, move.col)) { return;}
        if (toMove == Player.RED) {
            board.getMoveRed(move.row, move.col, Color.RED);
            toMove = Player.BLACK;
        } else {
            board.getMoveBlack(move.row, move.col, Color.BLACK);
            toMove = Player.RED;
        }
        recomputeTerminal();
    }

    public GameState copy() {
        Board board2 = board.fastCopy();
        return new GameState(board2, toMove);
    }

    private void recomputeTerminal() {
        if (board.redWins()) {
            terminal = true;
            winnerId = 1;
        }
        if (board.blackWins()) {
            terminal = true;
            winnerId = 2;
        }
    }

    private Color toColor(Player p) { return (p == Player.RED ? Color.RED : Color.BLACK);}

    public int estimateAfterMove(Move m) {
        Player mover = toMove;
        GameState copy = this.copy();
        copy.doMove(m);
        return copy.estimateShortestPathForPlayer(mover);
    }

    public int estimateShortestPathForCurrentPlayer() { return ShortestPath.shortestPath(board, toColor(toMove));}
    public int estimateShortestPathForPlayer(Player p) { return ShortestPath.shortestPath(board, toColor(p)); }
}











