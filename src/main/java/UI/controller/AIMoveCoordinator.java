package UI.controller;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import AI.api.AIAgent;
import AI.api.AIBoardAdapter;
import game.core.Move;
import game.core.Player;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Duration;

/**
 * AIMoveCoordinator: manages AI agents, handles move requests on background threads,
 * and ensures moves are applied on the JavaFX Application thread.
 */
public final class AIMoveCoordinator {
    private final Map<Player, AIAgent> aiAgents = new EnumMap<>(Player.class);
    private boolean aiThinking = false;
    private long revision = 0;

    /**
     * Checks if any AI agents are registered.
     * @return true if there is at least one AI agent registered; false otherwise
     */
    public boolean hasAnyAIAgent() {
        return !aiAgents.isEmpty();
    }

    /**
     * Checks if the specified player is controlled by an AI agent.
     * @param player the player to check
     * @return true if the player is controlled by an AI agent; false otherwise
     */
    public boolean isAIControlled(Player player) {
        return aiAgents.containsKey(player);
    }

    /**
     * Checks if an AI agent is currently thinking.
     * @return true if an AI agent is processing a move; false otherwise
     */
    public boolean isThinking() {
        return aiThinking;
    }

    /**
     * Registers an AI agent for the specified player.
     * @param player the player to be controlled by the AI agent
     * @param agent the AI agent to register
     */
    public void addAgent(Player player, AIAgent agent) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (agent == null) throw new IllegalArgumentException("Agent cannot be null");
        aiAgents.put(player, agent);
        agent.initialize();
    }

    /**
     * Removes the AI agent associated with the specified player.
     * @param player the player whose AI agent should be removed
     */
    public void removeAgent(Player player) {
        AIAgent agent = aiAgents.remove(player);
        if (agent != null) agent.cleanup();
    }

    /**
     * Removes all registered AI agents and cleans up resources.
     */
    public void removeAllAgents() {
        for (AIAgent a : aiAgents.values()) {
            try { a.cleanup(); } catch (Exception ignored) {}
        }
        aiAgents.clear();
        invalidate();
    }

    /**
     * Invalidates the current AI state, cancelling any ongoing computations.
     */
    public void invalidate() {
        revision++;
        aiThinking = false;
    }

    /**
     * Requests a move from the AI agent controlling the specified player.
     * <p>
     * The AI computation is performed on a background thread, and the resulting move
     * is delivered via the provided callback on the JavaFX Application thread.
     * 
     * @param player the player for whom to request a move
     * @param snapshotSupplier a supplier that provides a snapshot of the current board state
     * @param onMoveReady a callback to receive the computed move
     * @param delayed if true, introduces a slight delay before starting computation
     * @return true if an AI agent was found and the request was initiated; false otherwise
     */
    public boolean requestMove(Player player,
                               Supplier<AIBoardAdapter> snapshotSupplier,
                               Consumer<Move> onMoveReady,
                               boolean delayed) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (snapshotSupplier == null) throw new IllegalArgumentException("Snapshot supplier cannot be null");
        if (onMoveReady == null) throw new IllegalArgumentException("Move callback cannot be null");
        if (aiThinking) return false;

        final AIAgent agent = aiAgents.get(player);
        if (agent == null) return false;

        aiThinking = true;
        final long myRevision = revision;

        Runnable start = () -> {
            Task<Move> task = new Task<>() {
                @Override
                protected Move call() {
                    AIBoardAdapter snapshot = snapshotSupplier.get();
                    return agent.getBestMove(snapshot, player);
                }
            };

            task.setOnSucceeded(e -> {
                aiThinking = false;
                if (revision != myRevision) return;

                Move move = task.getValue();
                if (move == null) return;

                deliverMove(onMoveReady, move);
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

        return true;
    }

    /**
     * Delivers the computed move to the provided callback on the JavaFX Application thread.
     * @param onMoveReady the callback to receive the move
     * @param move the computed move
     */
    private void deliverMove(Consumer<Move> onMoveReady, Move move) {
        if (Platform.isFxApplicationThread()) {
            onMoveReady.accept(move);
        } else {
            Platform.runLater(() -> onMoveReady.accept(move));
        }
    }
}
