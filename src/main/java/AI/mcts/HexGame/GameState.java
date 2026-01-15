// AI/mcts/GameState.java
package AI.mcts.HexGame;

import java.util.ArrayList;
import java.util.List;

import AI.AiPlayer.AIBoardAdapter;
import AI.mcts.Optimazation.ShortestPath;
import Game.Color;
import Game.Player;
import Game.Rules;

/**
 * Represents the state of a Hex game, including the board configuration, current player,
 * and terminal state information. This class provides methods for game state manipulation,
 * move validation, and heuristic evaluation.
 * 
 * <p>The GameState maintains immutability through copy operations and tracks game progression
 * by monitoring win conditions for both players.</p>
*/
public class GameState {
    private final AIBoardAdapter board;
    private Player toMove;          // RED starts typically
    private boolean terminal = false;
    private int winnerId = 0;       // 0 = none, 1 = RED, 2 = BLACK

    public GameState(AIBoardAdapter board, Player toMove) {
        this.board = board;
        this.toMove = toMove;
        recomputeTerminal();
    }

    /**
     * Checks if the game has reached a terminal state (a player has won).
     *
     * @return true if the game is over, false otherwise
    */
    public boolean isTerminal(){
        return terminal;
    }

    /**
     * Returns the ID of the winning player.
     *
     * @return 0 if no winner, 1 if RED won, 2 if BLACK won
    */
    public int getWinnerId(){
        return winnerId;
    }

    /**
     * Returns the player whose turn it is to move.
     *
     * @return the current player to move
    */
    public Player getToMove(){
        return toMove;
    }

    public AIBoardAdapter getBoard(){
        return board;
    }

    /**
     * Retrieves all legal moves available in the current game state.
     *
     * @return a list of Move objects representing all valid moves
    */
    public List<Move> getLegalMoves() {
        List<Move> out = new ArrayList<>();
        for (int[] rc : board.legalMoves()) {
            out.add(new Move(rc[0], rc[1]));
        }
        return out;
    }

    /**
     * Applies a move to the game state, updating the board and switching players.
     * If the game is already terminal or the move is invalid, no action is taken.
     *
     * @param move the move to be executed
    */
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
    
    /**
     * Creates a deep copy of this game state, including a copy of the board.
     * The copied state is independent and can be modified without affecting the original.
     *
     * @return a new GameState object that is a copy of this state
    */
    public GameState copy() {
        AIBoardAdapter board2 = board.copy();
        return new GameState(board2, toMove);
    }

    /**
     * Recomputes the terminal status of the game by checking win conditions
     * for both players. Updates the terminal flag and winnerId accordingly.
    */
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
    
    /**
     * Converts a Player enum to its corresponding Color enum.
     *
     * @param p the player to convert
     * @return Color.RED if player is RED, Color.BLACK otherwise
    */
    private Color toColor(Player p) {
        return (p == Player.RED ? Color.RED : Color.BLACK);
    }
    
    /**
     * Estimates the board quality after applying the given move using shortest path heuristics.
     * This method creates a copy of the state, applies the move, and evaluates the resulting
     * position from the perspective of the player who made the move.
     *
     * @param m the move to evaluate
     * @return the shortest path distance for the player after making the move
    */
    public int estimateAfterMove(Move m) {
        Player mover = toMove;
        GameState copy = this.copy();
        copy.doMove(m);
        return copy.estimateShortestPathForPlayer(mover);
    }

    /**
     * Calculates the shortest path distance for the current player to move.
     * A lower value indicates the player is closer to winning.
     *
     * @return the shortest path distance for the player whose turn it is
    */
    public int estimateShortestPathForCurrentPlayer() {
        return ShortestPath.shortestPath(board, toColor(toMove));
    }

    /**
     * Calculates the shortest path distance for a specific player.
     * This is useful for evaluating positions from different perspectives.
     *
     * @param p the player for whom to calculate the shortest path
     * @return the shortest path distance for the specified player
    */
    public int estimateShortestPathForPlayer(Player p) {
        return ShortestPath.shortestPath(board, toColor(p));
    }
}
