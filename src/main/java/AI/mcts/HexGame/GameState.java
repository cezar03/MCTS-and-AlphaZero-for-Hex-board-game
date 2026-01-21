package AI.mcts.HexGame;

import java.util.ArrayList;
import java.util.List;

import AI.mcts.Optimazation.ShortestPath;
import game.core.Board;
import game.core.Color;
import game.core.Move;
import game.core.Player;
import game.core.Rules;

/**
 * Encapsulates the state of a Hex game for use within the MCTS simulation.
 * <p>
 * This class wraps the core {@link Board} object and tracks the current player,
 * winner status, and terminal state. It provides methods for cloning states
 * and applying moves during tree traversal and rollouts.
 */
public class GameState {
    private final Board board;
    private Player toMove; // RED starts typically
    private boolean terminal = false;
    private int winnerId = 0; // 0 = none, 1 = RED, 2 = BLACK

    /**
     * Constructs a new game state.
     * * @param board the board configuration
     * @param toMove the player whose turn it is
     */
    public GameState(Board board, Player toMove) {
        this.board = board;
        this.toMove = toMove;
        recomputeTerminal();
    }

    /**
     * Checks if the game has ended in this state.
     * * @return true if a winner has been determined
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * Returns the ID of the winning player.
     * * @return 0 if no winner, 1 for RED, 2 for BLACK
     */
    public int getWinnerId() {
        return winnerId;
    }

    /**
     * Returns the player whose turn it is to move in this state.
     * * @return the Player enum
     */
    public Player getToMove() {
        return toMove;
    }

    /**
     * Retrieves the underlying board object.
     * * @return the Board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Generates a list of all legal moves available from this state.
     * * @return a list of {@link Move} objects representing empty cells
     */
    public List<Move> getLegalMoves() {
        List<Move> out = new ArrayList<>();
        for (int[] rc : board.legalMoves()) {
            // FIX: Use the Flyweight/Cache factory 'get' instead of 'new'
            out.add(Move.get(rc[0], rc[1]));
        }
        return out;
    }

    /**
     * Applies the specified move to the state.
     * <p>
     * Updates the board, switches the active player, and checks for a terminal condition.
     * * @param move the move to execute
     */
    public void doMove(Move move) {
        if (terminal) {
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
     * Creates a deep copy of this game state.
     * <p>
     * Uses efficient copying mechanisms for the board to support rapid MCTS simulations.
     * * @return a new GameState instance identical to the current one
     */
    public GameState copy() {
        // OPTIMIZATION: Use the fast memory copy we just created
        Board board2 = board.fastCopy();
        return new GameState(board2, toMove);
    }

    /**
     * Recomputes the terminal status and winner based on the current board.
     * <p>
     * Called after moves to update the game state accordingly.
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
     * Converts a Player enum to the corresponding Color.
     * @param p the player
     * @return the corresponding Color
     */
    private Color toColor(Player p) {
        return (p == Player.RED ? Color.RED : Color.BLACK);
    }

    /**
     * Estimates the strategic value of the board for the current player after making a specific move.
     * <p>
     * This is often used by heuristics to score moves based on how much they reduce
     * the shortest path to victory.
     * * @param m the move to evaluate
     * @return the estimated shortest path distance after the move
     */
    public int estimateAfterMove(Move m) {
        Player mover = toMove;
        GameState copy = this.copy();
        copy.doMove(m);
        return copy.estimateShortestPathForPlayer(mover);
    }

    /**
     * Calculates the shortest path distance for the player whose turn it is.
     * * @return the length of the shortest path
     */
    public int estimateShortestPathForCurrentPlayer() {
        return ShortestPath.shortestPath(board, toColor(toMove));
    }

    /**
     * Calculates the shortest path distance for the specified player.
     * * @param p the player whose path to evaluate
     * @return the length of the shortest path
     */
    public int estimateShortestPathForPlayer(Player p) {
        return ShortestPath.shortestPath(board, toColor(p));
    }
}











