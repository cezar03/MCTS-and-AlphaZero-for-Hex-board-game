package AI.mcts;
import java.util.HashMap;
import java.util.Map;

import game.core.Move;

/**
 * Represents a node in the Monte Carlo Tree Search (MCTS) game tree.
 * <p>
 * Each node corresponds to a specific game state resulting from a move.
 * It stores statistics necessary for the MCTS algorithm, such as the number
 * of visits, win count, and references to parent and child nodes.
 */
public final class Node { // this is not a final version. it's just a skeleton to compile and try the selection class
    // fields 
    public Move move; // the move that led to this node. null for the root node
    public Node parent; // parent node 
    public Map<Move, Node> children = new HashMap<>(); // map of all the children of this node
    public int visits = 0; // how many times has this node been visited 
    public double wins = 0.0; // how many times did it win
    public int playerThatMoved; //Player 1 or player 2. 
    public double heuristicBias = 0.0; // Default bias
    public double priorProbability = 0.0; // Prior probability from neural network.
    public float[] cachedEncoding = null; // Cached board encoding for training data reuse 

    /**
     * Constructs a standard MCTS node.
     * * @param move the move that led to this state (null for root)
     * @param parent the parent node (null for root)
     * @param playerThatMoved the ID of the player who made the move
     */
    public Node(Move move, Node parent, int playerThatMoved){
        this.move = move; 
        this.parent = parent; 
        this.playerThatMoved = playerThatMoved;
    }

    /**
     * Constructs an MCTS node with a prior probability (used by AlphaZero).
     * * @param move the move that led to this state
     * @param parent the parent node
     * @param playerThatMoved the ID of the player who made the move
     * @param priorProbability the probability of selecting this move assigned by the neural network
     */
    public Node(Move move, Node parent, int playerThatMoved, double priorProbability){
        this.move = move; 
        this.parent = parent; 
        this.playerThatMoved = playerThatMoved;
        this.priorProbability = priorProbability;
    }
}











