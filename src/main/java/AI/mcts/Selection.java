package AI.mcts;
public class Selection{
/*
 Selection Class: 
 This class implements the selection phase of the MCTS 
 In MCTS, each node represents a game state. During the selection phase, we traverse the tree from the root to find a node that is:
 - not fully expanded (still has unexplored moves), or
 - a terminal node (game over)
 
 Selection uses the UCT formula (The one that Jan explained in the first meeting):
 UCT = (w_i / n_i) + C * sqrt( ln(N) / n_i )
Where:
 - w_i: number of wins for the child
  - n_i: number of visits for the child
 - N: number of visits for the parent node
 - C: exploration constant (balances exploration vs exploitation)
 */

    // Exploration constant (C) used in UCT formula.
    // sqrt(2) is commonly used to balance exploration and exploitation.
    private double c = Math.sqrt(2); 
    
    /*
    Main Selection Method
    Goes through the tree from the given node until we reach a node that:
    - is not fully expanded (has untried moves)
    - or is terminal (end of game) -> as explained previously 
     */
    public Node select(Node node, GameState state){
        while (!state.isTerminal() && node.isFullyExpanded()) { // while the game is not over and the node is fully expanded
            node = bestChild(node); // choose the child that gives the best UCT. See the method below
            state.doMove(node.move); // apply the move of the selected child to the game state
            }
        return node; // return the nodes where the expansion will happen

    }
    private Node bestChild(Node node) { 
        Node best = null; // here we will store the child with the best uct value (highest)
        double bestValue = Double.NEGATIVE_INFINITY; // initialize with a very low value
        for (Node child : node.children.values()) { // loop through all the children of the current node
            double uctValue;
            if (child.visits == 0) { // if the child has never been visited before, we will prioritize it  with infinite value
                uctValue = Double.POSITIVE_INFINITY; 
            } else { // calculate the uct value for the child 
            uctValue = (child.wins / (child.visits + 1e-6)) + // win rate
                c * Math.sqrt(Math.log(node.visits + 1) / (child.visits + 1e-6) // the 1e-6 prevents the division by zero, to make it safe
            );
            // if this child has the highest uct value so far update the best variable
            if (uctValue > bestValue) {
                bestValue = uctValue;
                best = child;
            }
             }
             
    }
    return best; // finally return the child with the highest uct 
}
}

