package AI.mcts.Steps;
import java.util.*;

import AI.mcts.Node;
import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.Optimazation.*;
import AI.mcts.Optimazation.Heuristic.*;
import Game.Player;

/**
 * Implements the expansion phase of Monte Carlo Tree Search (MCTS).
 * This class is responsible for creating new child nodes in the search tree
 * by selecting an untried move and adding it to the tree.
 * 
 * <p>The expansion process can be enhanced with move pruning to reduce the branching factor
 * and heuristic biasing to guide the search toward more promising moves.</p>
 */
public class Expansion {

    // pruner
    private final MovePruner pruner; // may be null
    private final Heuristic heuristic;  // may be null
    private final Random random = new Random();
    private final double biasScale; 

    /**
     * Constructs an Expansion object with the specified pruner, heuristic, and bias scale.
     *
     * @param pruner the MovePruner to filter moves (may be null for no pruning)
     * @param heuristic the Heuristic to evaluate moves (may be null for no heuristic guidance)
     * @param biasScale the scaling factor for heuristic bias values
     */
    public Expansion(MovePruner pruner, Heuristic heuristic, double biasScale) {
        this.pruner = pruner;
        this.heuristic = heuristic;
        this.biasScale = biasScale;
    }

    /**
     * Expands the given node by creating one new child node for an untried move.
     * If move pruning is enabled, it is applied to the untried moves before selection.
     * The chosen move is selected randomly from the remaining untried moves.
     * 
     * <p>If a heuristic is provided, the new child node receives a heuristic bias
     * that will influence its selection probability in future iterations.</p>
     *
     * @param node the node to expand
     * @param currentState the current game state at this node
     * @return the newly created child node, or the original node if no expansion is possible
     */
    public Node expand(Node node, GameState currentState) {
        // in case  the game is over there is nothing to expand
        if (currentState.isTerminal()) {
            return node;
        }

        // getting all legal moves from the current state
        List<Move> legalMoves = currentState.getLegalMoves();

        // find moves that have not been tried yet
        List<Move> untriedMoves = new ArrayList<>();
        for (Move moves : legalMoves) {
            if (!node.children.containsKey(moves)) {
                untriedMoves.add(moves);
            }
        }

        if (untriedMoves.isEmpty()) {
            return node;
        }

        //  pruner applied
        List<Move> prunedUntried = untriedMoves;
        if (pruner != null) {
            List<Move> pruned = pruner.pruneMoves(currentState, untriedMoves);
            if (!pruned.isEmpty()) {
                prunedUntried = pruned;
            }
        }

        // pick a random untried move
        Move chosenMove = prunedUntried.get(random.nextInt(prunedUntried.size()));
        Player toMove = currentState.getToMove();
        Node child = new Node(chosenMove, node, toMove.id);

        if (heuristic != null && biasScale != 0.0) {
            double h = heuristic.score(currentState, chosenMove);
            child.heuristicBias = biasScale * h;
        }
        
        node.children.put(chosenMove, child);

        // return the newly created child
        return child;
    }

    /**
     * Returns the MovePruner associated with this expansion strategy.
     *
     * @return the MovePruner instance, or null if no pruner is configured
     */
    public MovePruner getPruner() {
        return pruner;
    }
}

