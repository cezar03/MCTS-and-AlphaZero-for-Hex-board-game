package AI.mcts;

import java.util.*;

import AI.mcts.HexGame.GameState;
import AI.mcts.Steps.Backpropagation;
import AI.mcts.Steps.Expansion;
import AI.mcts.Steps.Selection;
import AI.mcts.Steps.Simulation;

public final class MCTS {
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
            GameState state = rootState.copy();
            Node leaf = selection.select(root, state);
            Node child = expansion.expand(leaf, state);
            if (child != leaf) state.doMove(child.move);

            int winner = state.isTerminal() ? state.getWinnerId()
                                            : simulation.simulate(state);
            backprop.backpropagate(child, winner);
        }
        return bestChildByVisits(root);
    }

    private Node bestChildByVisits(Node root) {
        Node best = null; int bestV = -1;
        for (Node child : root.children.values()) {
            if (child.visits > bestV) {
                bestV = child.visits; best = child;
            }
        }
        return best;
    }
}
