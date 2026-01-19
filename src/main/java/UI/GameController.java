package UI;

import java.util.EnumMap;
import java.util.Map;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Duration;

import AI.AiPlayer.AIAdaptationConfig;
import AI.AiPlayer.AIAgent;
import AI.AiPlayer.AIAgentFactory;
import AI.AiPlayer.AIBoardAdapter;
import AI.mcts.HexGame.Move;
import Game.BoardAdapter;
import Game.Color;
import Game.Player;
import Game.Rules;

/**
 * GameController: owns turn logic + mediates between UI.BoardView and Game.BoardAdapter.
 * Runs AI on background thread and applies move on FX thread.
 */
public final class GameController {

    private final HexBoardView boardView;          // IMPORTANT: UI.BoardView (JavaFX)
    private final BoardAdapter adapter;

    private final Map<Player, AIAgent> aiAgents = new EnumMap<>(Player.class);

    private Player currentPlayer = Player.RED;
    private boolean gameOver = false;
    private boolean aiThinking = false;
    private int moveCount = 0;

    // Used to ignore stale AI results after reset/back/menu etc.
    private long revision = 0;

    private GameEndListener gameEndListener;

    public interface GameEndListener {
        void onGameEnd(Player winner);
    }

    public GameController(BoardAdapter adapter, HexBoardView boardView) {
        if (adapter == null) throw new IllegalArgumentException("BoardAdapter cannot be null");
        if (boardView == null) throw new IllegalArgumentException("BoardView cannot be null");

        this.adapter = adapter;
        this.boardView = boardView;

        boardView.setController(this);
        boardView.update(adapter);
        boardView.updateTurnDisplay(currentPlayer);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setGameEndListener(GameEndListener listener) {
        this.gameEndListener = listener;
    }

    public boolean hasAnyAIAgent() {
        return !aiAgents.isEmpty();
    }

    public boolean isAIControlled(Player player) {
        return aiAgents.containsKey(player);
    }

    public void setupAIAgent(Player player, AIAgent agent) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (agent == null) throw new IllegalArgumentException("Agent cannot be null");

        aiAgents.put(player, agent);
        agent.initialize();

        // If it's already this agent's turn, start thinking immediately.
        if (!gameOver && currentPlayer == player) {
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
        AIAgent agent = aiAgents.remove(player);
        if (agent != null) agent.cleanup();
    }

    public void removeAllAIAgents() {
        for (AIAgent a : aiAgents.values()) {
            try { a.cleanup(); } catch (Exception ignored) {}
        }
        aiAgents.clear();
        aiThinking = false;
        revision++; // invalidate any pending AI result
    }

    public void resetForNewGame() {
        revision++;
        aiThinking = false;

        adapter.reset();
        currentPlayer = Player.RED;
        gameOver = false;
        moveCount = 0;

        boardView.update(adapter);
        boardView.updateTurnDisplay(currentPlayer);

        if (isAIControlled(currentPlayer)) {
            requestAIMove(false);
        }
    }

    /**
     * Called by UI.BoardView on click.
     */
    public void handleCellClick(int row, int col) {
        if (gameOver || aiThinking) return;

        // If current player is AI-controlled, ignore human clicks.
        if (isAIControlled(currentPlayer)) return;

        // Pie rule (second player's first move): click opponent stone to "swap" (your implementation)
        if (Rules.pieRuleAvailable(moveCount, currentPlayer)) {
            Color clicked = adapter.getCell(row, col); // FIX: getCell, not getCellColor
            if (clicked == currentPlayer.other().stone) {
                adapter.undoMove(row, col);
                adapter.makeMove(row, col, currentPlayer);

                boardView.update(adapter);
                moveCount++;

                advanceTurnOrEnd();
                return;
            }
        }

        if (!adapter.makeMove(row, col, currentPlayer)) return;

        boardView.update(adapter);
        moveCount++;

        advanceTurnOrEnd();
    }

    private void advanceTurnOrEnd() {
        if (adapter.isGameOver()) {
            endGame(adapter.getWinner());
            return;
        }

        currentPlayer = currentPlayer.other();
        boardView.updateTurnDisplay(currentPlayer);

        if (isAIControlled(currentPlayer)) {
            requestAIMove(false);
        }
    }

    private void endGame(Player winner) {
        gameOver = true;
        boardView.updateWinDisplay(winner);
        if (gameEndListener != null) gameEndListener.onGameEnd(winner);
    }

    /**
     * Computes an AI move in background. Applies it safely if still current (revision token).
     */
    private void requestAIMove(boolean delayed) {
        if (gameOver || aiThinking) return;

        final AIAgent agent = aiAgents.get(currentPlayer);
        if (agent == null) return;

        aiThinking = true;
        final long myRevision = revision;
        final Player myPlayer = currentPlayer;

        Runnable start = () -> {
            Task<Move> task = new Task<>() {
                @Override
                protected Move call() {
                    // IMPORTANT: never give AI the live adapter.
                    AIBoardAdapter snapshot = adapter.copy();
                    return agent.getBestMove(snapshot, myPlayer);
                }
            };

            task.setOnSucceeded(e -> {
                aiThinking = false;

                // Ignore stale result
                if (revision != myRevision || gameOver || currentPlayer != myPlayer) return;

                Move move = task.getValue();
                if (move == null) return;

                Platform.runLater(() -> applyAIMove(move));
            });

            task.setOnFailed(e -> {
                aiThinking = false;
                System.out.println("AI move failed: " + task.getException());
            });

            Thread t = new Thread(task, "AI-Move");
            t.setDaemon(true);
            t.start();
        };

        if (!delayed) {
            start.run();
        } else {
            PauseTransition pause = new PauseTransition(Duration.millis(80));
            pause.setOnFinished(ev -> start.run());
            pause.play();
        }
    }

    private void applyAIMove(Move move) {
        if (gameOver) return;

        boolean ok = adapter.makeMove(move.row, move.col, currentPlayer);
        if (!ok) {
            System.out.println("AI produced illegal move: (" + move.row + "," + move.col + ")");
            return;
        }

        boardView.update(adapter);
        moveCount++;

        if (adapter.isGameOver()) {
            endGame(adapter.getWinner());
            return;
        }

        currentPlayer = currentPlayer.other();
        boardView.updateTurnDisplay(currentPlayer);

        // AI vs AI chaining
        if (isAIControlled(currentPlayer)) {
            requestAIMove(true);
        }
    }
}
