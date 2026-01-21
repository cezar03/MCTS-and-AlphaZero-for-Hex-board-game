package UI.session;

import bridge.BoardAdapter;
import game.core.Color;
import game.core.Player;
import game.core.Rules;

/**
 * Represents a single playable game session for the UI layer.
 *
 * <p>{@code GameSession} wraps a {@link BoardAdapter} and adds session-level state:
 * whose turn it is, whether the game is over, and how many moves have been made.
 * It also provides convenience methods to apply moves from either a human player
 * (with optional Pie Rule handling) or non-human sources (AI, replay, etc.).</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Maintain the current player turn ({@link Player})</li>
 *   <li>Track move count to support rules that depend on turn number (e.g., Pie Rule)</li>
 *   <li>Prevent moves after the game ends</li>
 *   <li>Delegate all board mutations and win detection to {@link BoardAdapter}</li>
 * </ul>
 *
 * <h2>Pie Rule</h2>
 * <p>When applying a <i>human</i> move via {@link #applyHumanMove(int, int)},
 * this session checks whether the Pie Rule is available using
 * {@link Rules#pieRuleAvailable(int, Player)}. If available and the clicked cell
 * contains the opponent's stone, the move is treated as a swap-style action:
 * the cell is undone and then replayed for the current player.</p>
 *
 * <p>All win detection is based on {@link BoardAdapter#isGameOver()} and
 * {@link BoardAdapter#getWinner()}.</p>
 */
public final class GameSession {

    /**
     * Adapter that exposes the underlying board/game-state operations needed by the UI.
     * This is the single source of truth for board contents and game-over detection.
     */
    private final BoardAdapter adapter;

    /**
     * The player whose turn it currently is. Defaults to {@link Player#RED}.
     */
    private Player currentPlayer = Player.RED;

    /**
     * Whether the game session has ended. Once {@code true}, moves are rejected.
     */
    private boolean gameOver = false;

    /**
     * Number of moves applied so far in this session.
     * Used for rule checks such as Pie Rule availability.
     */
    private int moveCount = 0;

    /**
     * Creates a new {@code GameSession} around the provided board adapter.
     *
     * @param adapter the adapter used to read/update the board state
     * @throws IllegalArgumentException if {@code adapter} is {@code null}
     */
    public GameSession(BoardAdapter adapter) {
        if (adapter == null) throw new IllegalArgumentException("BoardAdapter cannot be null");
        this.adapter = adapter;
    }

    /**
     * Returns the board adapter backing this session.
     *
     * @return the session's {@link BoardAdapter}
     */
    public BoardAdapter getAdapter() {
        return adapter;
    }

    /**
     * Returns the player whose turn it is.
     *
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Returns whether the game is over.
     *
     * @return {@code true} if the game has ended, otherwise {@code false}
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the number of moves applied so far.
     *
     * @return the move count
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Returns the winner as reported by the adapter.
     *
     * <p>If the game is not over, this may return {@code null} or a sentinel value
     * depending on the {@link BoardAdapter} implementation.</p>
     *
     * @return the winning {@link Player}, or adapter-defined value if no winner yet
     */
    public Player getWinner() {
        return adapter.getWinner();
    }

    /**
     * Resets this session back to an initial state.
     *
     * <p>This delegates to {@link BoardAdapter#reset()} and also resets the session-level
     * state: current player becomes {@link Player#RED}, gameOver becomes {@code false},
     * and move count is set to 0.</p>
     */
    public void reset() {
        adapter.reset();
        currentPlayer = Player.RED;
        gameOver = false;
        moveCount = 0;
    }

    /**
     * Applies a move initiated by a human player.
     *
     * <p>This method allows Pie Rule swap behavior (if available) in addition to
     * normal placement rules. If the move is successful, the move count is
     * incremented and the turn advances (or the game ends).</p>
     *
     * @param row the board row index
     * @param col the board column index
     * @return {@code true} if the move was applied, otherwise {@code false}
     */
    public boolean applyHumanMove(int row, int col) {
        return applyMoveInternal(row, col, true);
    }

    /**
     * Applies a move without any Pie Rule swap behavior.
     *
     * <p>This is suitable for AI moves, scripted moves, or any context where
     * a swap action should not be possible. If the move is successful, the
     * move count is incremented and the turn advances (or the game ends).</p>
     *
     * @param row the board row index
     * @param col the board column index
     * @return {@code true} if the move was applied, otherwise {@code false}
     */
    public boolean applyMove(int row, int col) {
        return applyMoveInternal(row, col, false);
    }

    /**
     * Internal move application method shared by human and non-human move calls.
     *
     * <p>Order of operations:</p>
     * <ol>
     *   <li>Reject if {@link #gameOver} is {@code true}</li>
     *   <li>If Pie Rule swap is allowed and available, check for swap click</li>
     *   <li>Otherwise attempt a normal move via {@link BoardAdapter#makeMove(int, int, Player)}</li>
     *   <li>On success: increment {@link #moveCount} and advance turn or end game</li>
     * </ol>
     *
     * @param row the board row index
     * @param col the board column index
     * @param allowPieRuleSwap whether Pie Rule swap logic is enabled for this call
     * @return {@code true} if the move was applied, otherwise {@code false}
     */
    private boolean applyMoveInternal(int row, int col, boolean allowPieRuleSwap) {
        if (gameOver) return false;

        // Pie Rule: allow a "swap-like" action by clicking the opponent's stone when available.
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

    /**
     * Advances the game to the next state after a successful move.
     *
     * <p>If the adapter reports game over, {@link #gameOver} is set to {@code true}
     * and the current player is not changed. Otherwise, turn alternates to the
     * opponent via {@link Player#other()}.</p>
     */
    private void advanceTurnOrEnd() {
        if (adapter.isGameOver()) {
            gameOver = true;
            return;
        }
        currentPlayer = currentPlayer.other();
    }
}
