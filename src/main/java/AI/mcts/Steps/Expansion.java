package AI.mcts.Steps;
import java.util.*;

import AI.mcts.Node;
import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;

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
        for (Move moves : legalMoves) {
            if (!node.children.containsKey(moves)) {
                untriedMoves.add(moves);
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
