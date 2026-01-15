package AI.mcts;
import java.util.HashMap;
import java.util.Map;

import AI.mcts.HexGame.Move;

/**
 * Represents a node in the Monte Carlo Tree Search (MCTS) game tree.
 * <p>
 * Each node stores information about a game state reached by a particular move,
 * along with statistics accumulated during the MCTS process. Nodes maintain
 * parent-child relationships to form the search tree structure.
 * </p>
 * <p>
 * The node tracks visit counts and win statistics used by selection policies
 * (such as UCT) to balance exploration and exploitation. Optional fields support
 * enhancements like heuristic biases and neural network priors.
 * </p>
 */
public final class Node { // this is not a final version. it's just a skeleton to compile and try the selection class
    // fields 
    public Move move; // the move that led to this node. null for the root node
    public Node parent; // parent node 
    public Map<Move, Node> children = new HashMap<>(); // map of all the children of this node
    public int visits = 0; // how many times has this node been visited 
    public double wins = 0.0; // how many times did it win
    public int playerThatMoved; //Player 1 or player 2. 
    public double heuristicBias =0.0; // Default bias
    public double priorProbability = 0.0; // Prior probability from neural network. 

    /**
     * Constructs a new node with the specified move, parent, and player.
     * <p>
     * This is the basic constructor for standard MCTS implementations.
     * Heuristic bias and prior probability are initialized to their default
     * values of 0.0.
     * </p>
     *
     * @param move the move that led to this node (null for root)
     * @param parent the parent node (null for root)
     * @param playerThatMoved the player identifier (1 or 2) who made the move
     */
    public Node(Move move, Node parent, int playerThatMoved){
        this.move = move; 
        this.parent = parent; 
        this.playerThatMoved = playerThatMoved;
    }

    /**
     * Constructs a new node with the specified move, parent, player, and prior probability.
     * <p>
     * This constructor is intended for use with neural network-guided MCTS variants,
     * where a policy network provides prior probabilities to inform the search.
     * The prior probability influences the selection policy to favor moves that
     * the neural network considers more promising.
     * </p>
     *
     * @param move the move that led to this node (null for root)
     * @param parent the parent node (null for root)
     * @param playerThatMoved the player identifier (1 or 2) who made the move
     * @param priorProbability the prior probability from a neural network policy (typically between 0 and 1)
     */
    public Node(Move move, Node parent, int playerThatMoved, double priorProbability){
        this.move = move; 
        this.parent = parent; 
        this.playerThatMoved = playerThatMoved;
        this.priorProbability = priorProbability;
    }
}
