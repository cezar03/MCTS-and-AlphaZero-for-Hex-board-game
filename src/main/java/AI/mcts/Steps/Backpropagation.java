package ai.mcts.Steps;

import ai.mcts.Node;

/**
 * Implements the backpropagation phase of Monte Carlo Tree Search (MCTS).
 * This class is responsible for updating win statistics and visit counts
 * for all nodes along the path from a leaf node back to the root.
 * 
 * <p>During backpropagation, each node on the path has its visit count incremented,
 * and nodes whose player matches the winner have their win count incremented.</p>
*/
public class Backpropagation {
    /**
     * Backpropagates the result of a simulation from the given node up to the root.
     * Updates visit counts for all nodes along the path, and increments win counts
     * for nodes where the player who moved to reach that node is the winner.
     *
     * @param node the leaf node from which to start backpropagation
     * @param winner the ID of the winning player (0 for no winner, 1 for RED, 2 for BLACK)
     */
    public void backpropagate(Node node, int winner){
        Node current = node; 
        while (current != null){
            current.visits++; // Each time this node is visited during backpropagation, the visits increase              
            if (current.playerThatMoved == winner && winner != 0){ // if the player that made the move to reach this node is the winner
                current.wins += 1.0; 
            }
            // else: if the player lost 0 is added. 
            current = current.parent; // move one level up to the parent node. 
        }
    }
}











