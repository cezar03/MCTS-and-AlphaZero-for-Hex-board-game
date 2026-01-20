package ai.mcts.Steps;

import ai.mcts.Node;
import ai.mcts.HexGame.GameState;

/**
 * Implements the selection phase of Monte Carlo Tree Search (MCTS).
 * This class traverses the tree from the root to find a node that is either
 * not fully expanded (has untried moves) or is a terminal node (game over).
 * 
 * <p>Selection uses the Upper Confidence Bound for Trees (UCT) formula to balance
 * exploration and exploitation:</p>
 * <pre>
 * UCT = (w_i / n_i) + C * sqrt(ln(N) / n_i) + bias
 * </pre>
 * where:
 * <ul>
 *   <li>w_i: number of wins for the child</li>
 *   <li>n_i: number of visits for the child</li>
 *   <li>N: number of visits for the parent node</li>
 *   <li>C: exploration constant (balances exploration vs exploitation)</li>
 *   <li>bias: optional heuristic bias</li>
 * </ul>
 */
public class Selection{
    
    // Exploration constant (C) used in UCT formula.
    // sqrt(2) is commonly used to balance exploration and exploitation.
    private final double c;
    
    /**
     * Constructs a Selection object with the specified exploration constant.
     *
     * @param c the exploration constant used in the UCT formula (typically sqrt(2))
     */
    public Selection(double c) {
        this.c = c;
    }
    
    /**
     * Selects a path through the tree from the given node until reaching a node
     * that is either not fully expanded or is terminal. The game state is updated
     * along the path to reflect the moves taken.
     *
     * @param node the starting node for selection (typically the root)
     * @param state the game state at the starting node
     * @return the selected node where expansion or simulation should occur
     */
    public Node select(Node node, GameState state){
        while (!state.isTerminal() && isFullyExpanded(node, state)) { // while the game is not over and the node is fully expanded
            node = bestChild(node); // choose the child that gives the best UCT. See the method below
            state.doMove(node.move); // apply the move of the selected child to the game state
            }

        return node; // return the nodes where the expansion will happen

    }

    /**
     * Checks if a node is fully expanded by comparing the number of children
     * to the number of legal moves available.
     *
     * @param node the node to check
     * @param state the current game state
     * @return true if all legal moves have been tried (node is fully expanded), false otherwise
     */
    private boolean isFullyExpanded(Node node, GameState state) {
        int legal = state.getLegalMoves().size();
        return !state.isTerminal() && node.children.size() >= legal;
    }

    /**
     * Selects the best child of a node according to the UCT formula.
     * Children with zero visits are prioritized with infinite UCT value.
     * The UCT calculation includes the win rate, exploration bonus, and heuristic bias.
     *
     * @param node the parent node whose best child is to be selected
     * @return the child node with the highest UCT value
     */
    private Node bestChild(Node node) {
        Node best = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Node child : node.children.values()) {
            double uctValue;

            if (child.visits == 0) {
                uctValue = Double.POSITIVE_INFINITY;
            } else {
                double eps = 1e-9;
                double winRate = child.wins / (child.visits + eps);
                double explore = c * Math.sqrt(Math.log(node.visits + 1.0) / (child.visits + eps));
                double bias = child.heuristicBias;

                uctValue = winRate + explore + bias;
            }

            if (uctValue > bestValue) {
                bestValue = uctValue;
                best = child;
            }
        }
        return best;
    }
}












