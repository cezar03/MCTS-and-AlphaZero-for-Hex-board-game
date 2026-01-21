package UI.controller;

import AI.api.AIAdaptationConfig;
import AI.api.AIAgent;
import AI.api.AIAgentFactory;
import bridge.BoardAdapter;
import game.core.Move;
import game.core.Player;
import UI.session.GameSession;
import UI.view.HexBoardView;

/**
 * GameController: owns turn logic + mediates between ui.BoardView and bridge.BoardAdapter.
 * Runs AI on background thread and applies move on FX thread.
 */
public final class GameController {

    private final HexBoardView boardView;          // IMPORTANT: ui.BoardView (JavaFX)
    private final BoardAdapter adapter;
    private final GameSession session;
    private final AIMoveCoordinator aiCoordinator = new AIMoveCoordinator();

    private GameEndListener gameEndListener;

    /**
     * Listener for game end events.
     */
    public interface GameEndListener {
        void onGameEnd(Player winner);
    }

    /**
     * Constructs a GameController with the specified BoardAdapter and BoardView.
     * @param adapter the BoardAdapter to interact with the game logic
     * @param boardView the HexBoardView to update the UI
     * @throws IllegalArgumentException if adapter or boardView is null
     */
    public GameController(BoardAdapter adapter, HexBoardView boardView) {
        if (adapter == null) throw new IllegalArgumentException("BoardAdapter cannot be null");
        if (boardView == null) throw new IllegalArgumentException("BoardView cannot be null");

        this.adapter = adapter;
        this.boardView = boardView;
        this.session = new GameSession(adapter);

        boardView.setController(this);
        boardView.update(adapter);
        boardView.updateTurnDisplay(session.getCurrentPlayer());
    }

    /**
     * Gets the current player whose turn it is.
     * @return the current Player
     */
    public Player getCurrentPlayer() {
        return session.getCurrentPlayer();
    }

    /**
     * Sets the listener for game end events.
     * @param listener the GameEndListener to notify when the game ends
     */
    public void setGameEndListener(GameEndListener listener) {
        this.gameEndListener = listener;
    }

    /**
     * Checks if any AI agents are registered.
     * @return true if there is at least one AI agent registered; false otherwise
     */
    public boolean hasAnyAIAgent() {
        return aiCoordinator.hasAnyAIAgent();
    }

    /**
     * Checks if the specified player is controlled by an AI agent.
     * @param player the player to check
     * @return true if the player is controlled by an AI agent; false otherwise
     */
    public boolean isAIControlled(Player player) {
        return aiCoordinator.isAIControlled(player);
    }

    /**
     * Registers an AI agent for the specified player.
     * @param player the player to be controlled by the AI agent
     * @param agent the AI agent to register
     */
    public void setupAIAgent(Player player, AIAgent agent) {
        aiCoordinator.addAgent(player, agent);

        if (!session.isGameOver() && session.getCurrentPlayer() == player) {
            requestAIMove(false);
        }
    }

    /**
     * Registers an AI agent for the specified player using the provided factory and configuration.
     * @param player the player to be controlled by the AI agent
     * @param factory the AIAgentFactory to create the agent
     * @param config the AIAdaptationConfig for the agent
     * @throws IllegalArgumentException if factory or config is null, or if config's player does not match
     */
    public void setupAIAgent(Player player, AIAgentFactory factory, AIAdaptationConfig config) {
        if (factory == null) throw new IllegalArgumentException("Factory cannot be null");
        if (config == null) throw new IllegalArgumentException("Config cannot be null");
        if (config.getPlayer() != player) {
            throw new IllegalArgumentException("Config player " + config.getPlayer() + " != requested slot " + player);
        }
        setupAIAgent(player, factory.createAgent(config));
    }

    /**
     * Removes the AI agent associated with the specified player.
     * @param player the player whose AI agent should be removed
     */
    public void removeAIAgent(Player player) {
        aiCoordinator.removeAgent(player);
    }

    /**
     * Removes all registered AI agents and cleans up resources.
     */
    public void removeAllAIAgents() {
        aiCoordinator.removeAllAgents();
    }

    /**
     * Resets the game to start a new session.
     */
    public void resetForNewGame() {
        aiCoordinator.invalidate();
        session.reset();

        boardView.update(adapter);
        boardView.updateTurnDisplay(session.getCurrentPlayer());

        if (isAIControlled(session.getCurrentPlayer())) {
            requestAIMove(false);
        }
    }

    /**
     * Called by ui.BoardView on click.
     * @param row the row index of the clicked cell
     * @param col the column index of the clicked cell
     */
    public void handleCellClick(int row, int col) {
        if (session.isGameOver() || aiCoordinator.isThinking()) return;
        if (isAIControlled(session.getCurrentPlayer())) return;
        if (!session.applyHumanMove(row, col)) return;
        boardView.update(adapter);
        advanceTurnOrEnd();
    }

    /**
     * Advances the turn or ends the game if over.
     */
    private void advanceTurnOrEnd() {
        if (session.isGameOver()) {
            endGame(session.getWinner());
            return;
        }

        boardView.updateTurnDisplay(session.getCurrentPlayer());

        if (isAIControlled(session.getCurrentPlayer())) {
            requestAIMove(false);
        }
    }

    /**
     * Handles end-of-game logic and notifies the listener.
     * @param winner the Player who won the game
     */
    private void endGame(Player winner) {
        boardView.updateWinDisplay(winner);
        if (gameEndListener != null) gameEndListener.onGameEnd(winner);
    }

    /**
     * Computes an AI move in background and applies it on the FX thread.
     * @param delayed whether to introduce a small delay before computing the move
     */
    private void requestAIMove(boolean delayed) {
        if (session.isGameOver()) return;
        aiCoordinator.requestMove(
                session.getCurrentPlayer(),
                adapter::copy,
                this::applyAIMove,
                delayed
        );
    }

    /**
     * Applies the AI move to the game session and updates the UI.
     * @param move the Move produced by the AI
     */
    private void applyAIMove(Move move) {
        if (session.isGameOver()) return;

        boolean ok = session.applyMove(move.row, move.col);
        if (!ok) {
            System.out.println("AI produced illegal move: (" + move.row + "," + move.col + ")");
            return;
        }

        boardView.update(adapter);

        if (session.isGameOver()) {
            endGame(session.getWinner());
            return;
        }

        boardView.updateTurnDisplay(session.getCurrentPlayer());

        if (isAIControlled(session.getCurrentPlayer())) {
            requestAIMove(true);
        }
    }
}











