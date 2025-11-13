import java.util.*;

public class Expansion {

     //Expands a node by creating one new child for an untried move
    public Node expand(Node node, GameState currentState) {
        // in case  the game is over there is nothing to expand
        if (currentState.isTerminal()) {
            return node;
        }

        // getting all legal moves from the current state
        List<Move> legalMoves = currentState.getLegalMoves();

        // find moves that have not been tried yet
        List<Move> untriedMoves = new ArrayList<>();
        for (Move m : legalMoves) {
            if (!node.children.containsKey(m)) {
                untriedMoves.add(m);
            }
        }

        // if all moves are already tried, the node is fully expanded
        if (untriedMoves.isEmpty()) {
            return node;
        }

        // pick a random untried move
        Move chosenMove = untriedMoves.get(new Random().nextInt(untriedMoves.size()));

        // switch the player (1 ↔ 2)
        int nextPlayer = 3 - node.playerThatMoved;

        // create a new child node with the selected move
        Node child = new Node(chosenMove, node, nextPlayer);
        node.children.put(chosenMove, child);

        // return the newly created child
        return child;
    }
}
