package ui.controller;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import ai.api.AIAgent;
import ai.api.AIBoardAdapter;
import game.core.Move;
import game.core.Player;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Duration;

public final class AIMoveCoordinator {
    private final Map<Player, AIAgent> aiAgents = new EnumMap<>(Player.class);
    private boolean aiThinking = false;
    private long revision = 0;

    public boolean hasAnyAIAgent() {
        return !aiAgents.isEmpty();
    }

    public boolean isAIControlled(Player player) {
        return aiAgents.containsKey(player);
    }

    public boolean isThinking() {
        return aiThinking;
    }

    public void addAgent(Player player, AIAgent agent) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (agent == null) throw new IllegalArgumentException("Agent cannot be null");
        aiAgents.put(player, agent);
        agent.initialize();
    }

    public void removeAgent(Player player) {
        AIAgent agent = aiAgents.remove(player);
        if (agent != null) agent.cleanup();
    }

    public void removeAllAgents() {
        for (AIAgent a : aiAgents.values()) {
            try { a.cleanup(); } catch (Exception ignored) {}
        }
        aiAgents.clear();
        invalidate();
    }

    public void invalidate() {
        revision++;
        aiThinking = false;
    }

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

    private void deliverMove(Consumer<Move> onMoveReady, Move move) {
        if (Platform.isFxApplicationThread()) {
            onMoveReady.accept(move);
        } else {
            Platform.runLater(() -> onMoveReady.accept(move));
        }
    }
}
