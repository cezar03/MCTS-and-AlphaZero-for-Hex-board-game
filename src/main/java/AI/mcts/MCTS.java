package AI.mcts;

import AI.mcts.HexGame.GameState;
import AI.mcts.Steps.Backpropagation;
import AI.mcts.Steps.Expansion;
import AI.mcts.Steps.Selection;
import AI.mcts.Steps.SimulationStep.*;
import AI.mcts.Optimazation.*;

/**
 * Monte Carlo Tree Search (MCTS) implementation for game tree exploration.
 * <p>
 * This class orchestrates the four main phases of MCTS: Selection, Expansion,
 * Simulation, and Backpropagation. It performs a specified number of iterations
 * to build and evaluate a game tree, ultimately selecting the best move based
 * on visit counts.
 * </p>
 * 
 * @see Selection
 * @see Expansion
 * @see Simulation
 * @see Backpropagation
 */
public final class MCTS {
    private final Selection selection;
    private final Expansion expansion;
    private final Backpropagation backprop;
    private final Simulation simulation;

    private final int iterations;

    /**
     * Constructs an MCTS instance with specified components and iteration count.
     * <p>
     * This constructor allows for flexible configuration of the MCTS algorithm
     * by accepting custom implementations of each phase. The pruner can be
     * configured through the expansion component if move pruning is desired.
     * </p>
     *
     * @param iterations the number of MCTS iterations to perform during search
     * @param selection the selection strategy for traversing the tree
     * @param expansion the expansion strategy for adding new nodes
     * @param simulation the simulation strategy for evaluating leaf nodes
     */
    public MCTS(int iterations, Selection selection, Expansion expansion, Simulation simulation) {
        this.iterations = iterations;
        this.selection = selection;
        this.expansion = expansion;
        this.backprop = new Backpropagation();
        this.simulation = simulation;
    }

    /**
     * Performs MCTS search from the given root node and returns the best move.
     * <p>
     * Executes the complete MCTS algorithm for the specified number of iterations.
     * Each iteration consists of four phases:
     * <ol>
     *   <li>Selection: Traverse the tree to find a promising leaf node</li>
     *   <li>Expansion: Add a new child node to the selected leaf</li>
     *   <li>Simulation: Play out the game from the new node to a terminal state</li>
     *   <li>Backpropagation: Update statistics along the path from leaf to root</li>
     * </ol>
     * After all iterations complete, the child of the root with the highest visit
     * count is selected as the best move.
     * </p>
     *
     * @param root the root node of the search tree
     * @param rootState the current game state corresponding to the root node
     * @return the child node representing the best move based on visit counts
     */
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

    /**
     * Selects the child node with the highest number of visits.
     * <p>
     * This method implements the final move selection strategy, choosing the
     * most-visited child rather than the child with the highest win rate.
     * This approach is more robust as it favors moves that have been thoroughly
     * explored during the search process.
     * </p>
     *
     * @param root the node whose children should be evaluated
     * @return the child node with the maximum visit count, or null if no children exist
     */
    private Node bestChildByVisits(Node root) {
        Node best = null; int bestV = -1;
        for (Node child : root.children.values()) {
            if (child.visits > bestV) {
                bestV = child.visits; best = child;
            }
        }
        return best;
    }

    /**
     * Retrieves the move pruner from the expansion component.
     * <p>
     * The pruner can be used to restrict the set of legal moves considered
     * during expansion, potentially improving search efficiency by focusing
     * on more promising moves.
     * </p>
     *
     * @return the MovePruner instance used by the expansion strategy
     */
    public MovePruner getPruner() {
        return expansion.getPruner();
    }
}













