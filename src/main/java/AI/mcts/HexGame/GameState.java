// AI/mcts/GameState.java
package AI.mcts.HexGame;

import Game.*;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private final Board board;
    private Player toMove;          // RED starts typically
    private boolean terminal = false;
    private int winnerId = 0;       // 0 = none, 1 = RED, 2 = BLACK

    public GameState(Board board, Player toMove) {
        this.board = board;
        this.toMove = toMove;
        recomputeTerminal();
    }

    public boolean isTerminal(){
        return terminal;
    }

    public int getWinnerId(){
        return winnerId;
    }

    public Player getToMove(){
        return toMove;}

    public Board getBoard(){
        return board;
    }

    public List<Move> getLegalMoves() {
        List<Move> out = new ArrayList<>();
        for (int[] rc : board.legalMoves()) {
            out.add(new Move(rc[0], rc[1]));
        }
        return out;
    }

    public void doMove(Move move) {
        if (terminal){
            return;
        }
        if (!Rules.validMove(board, move.row, move.col)) {
            return;
        }

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
        Board b2 = board.copyBoard(board);
        return new GameState(b2, toMove);
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
}
