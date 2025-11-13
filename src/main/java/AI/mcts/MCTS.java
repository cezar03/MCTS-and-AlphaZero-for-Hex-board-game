package AI.mcts;

import java.util.*;

public class MCTS {
    private final Selection selection = new Selection();
    private final Expansion expansion = new Expansion();
    private final Backpropagation backprop = new Backpropagation();
    private final Simulation simulation = new Simulation();

    private final int iterations;

    public MCTS(int iterations) {
        this.iterations = iterations;
    }

    public Node search(Node root, GameState rootState) {
        for (int i = 0; i < iterations; i++) {
            GameState simState = rootState.copy();
            Node leaf = selection.select(root, simState);
            Node expanded = expansion.expand(leaf, simState);
            if (expanded != leaf) {
                simState.doMove(expanded.move);
            }

            int winner;
            if (simState.isTerminal()) {
                winner = simState.getWinnerId();
            } else {
                winner = simulation.simulate(simState);
            }
            backprop.backpropagate(expanded, winner);
        }

        Node best = null;
        int bestVisits = -1;
        for (Node child : root.children.values()) {
            if (child.visits > bestVisits) {
                bestVisits = child.visits;
                best = child;
            }
        }
        return best;
    }
}
