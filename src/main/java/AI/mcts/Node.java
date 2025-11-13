package AI.mcts;
import java.util.*;
public class Node { // this is not a final version. it's just a skeleton to compile and try the selection class
    // fields 
    public Move move; // the move that led to this node. null for the root node
    public Node parent; // parent node 
    public Map<Move, Node> children = new HashMap<>(); // map of all the children of this node
    public int visits = 0; // how many times has this node been visited 
    public double wins = 0.0; // how many times did it win
    public int playerThatMoved; //Player 1 or player 2. 
    // constructor 
    public Node(Move move, Node parent, int playerThatMoved){
        this.move = move; 
        this.parent = parent; 
        this.playerThatMoved = playerThatMoved;
    }

    // methods 
    public boolean isFullyExpanded() { // checks if the node is fully expanded 
        return children.size() >= getPossibleMoves().size();
        // for now getPosibleMoves() returns an empty list, but in the real class it should return legal moves
    }

    public List<Move> getPossibleMoves() {
        return new ArrayList<>(); // empty for now 
    }
}
