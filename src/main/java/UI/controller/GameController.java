package ui.controller;

import ai.api.AIAdaptationConfig;
import ai.api.AIAgent;
import ai.api.AIAgentFactory;
import game.core.Move;
import bridge.BoardAdapter;
import game.core.Player;
import ui.session.GameSession;
import ui.view.HexBoardView;

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

    public interface GameEndListener {
        void onGameEnd(Player winner);
    }

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

    public Player getCurrentPlayer() {
        return session.getCurrentPlayer();
    }

    public void setGameEndListener(GameEndListener listener) {
        this.gameEndListener = listener;
    }

    public boolean hasAnyAIAgent() {
        return aiCoordinator.hasAnyAIAgent();
    }

    public boolean isAIControlled(Player player) {
        return aiCoordinator.isAIControlled(player);
    }

    public void setupAIAgent(Player player, AIAgent agent) {
        aiCoordinator.addAgent(player, agent);

        // If it's already this agent's turn, start thinking immediately.
        if (!session.isGameOver() && session.getCurrentPlayer() == player) {
            requestAIMove(false);
        }
    }

    public void setupAIAgent(Player player, AIAgentFactory factory, AIAdaptationConfig config) {
        if (factory == null) throw new IllegalArgumentException("Factory cannot be null");
        if (config == null) throw new IllegalArgumentException("Config cannot be null");
        if (config.getPlayer() != player) {
            throw new IllegalArgumentException("Config player " + config.getPlayer() + " != requested slot " + player);
        }
        setupAIAgent(player, factory.createAgent(config));
    }

    public void removeAIAgent(Player player) {
        aiCoordinator.removeAgent(player);
    }

    public void removeAllAIAgents() {
        aiCoordinator.removeAllAgents();
    }

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
     */
    public void handleCellClick(int row, int col) {
        if (session.isGameOver() || aiCoordinator.isThinking()) return;

        // If current player is AI-controlled, ignore human clicks.
        if (isAIControlled(session.getCurrentPlayer())) return;

        if (!session.applyHumanMove(row, col)) return;

        boardView.update(adapter);
        advanceTurnOrEnd();
    }

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

    private void endGame(Player winner) {
        boardView.updateWinDisplay(winner);
        if (gameEndListener != null) gameEndListener.onGameEnd(winner);
    }

    /**
     * Computes an AI move in background and applies it on the FX thread.
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

        // AI vs AI chaining
        if (isAIControlled(session.getCurrentPlayer())) {
            requestAIMove(true);
        }
    }
}











