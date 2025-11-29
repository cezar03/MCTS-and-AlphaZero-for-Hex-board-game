package AI.mcts.Steps;
import java.util.*;

import AI.mcts.Node;
import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.Optimazation.*;
import Game.Player;

public class Expansion {

    // pruner
    private final MovePruner pruner;
    private final Random random = new Random();

    // constructor for pruner
    public Expansion(MovePruner pruner) {
        this.pruner = pruner;
    }

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

        if (untriedMoves.isEmpty()) {
            return node;
        }

        //  pruner applied
        List<Move> prunedUntried = untriedMoves;
        if (pruner != null) {
            List<Move> pruned = pruner.pruneMoves(currentState, prunedUntried);
            if (!pruned.isEmpty()) {
                prunedUntried = pruned;
            }
        }

        // pick a random untried move
        Move chosenMove = prunedUntried.get(random.nextInt(prunedUntried.size()));
        Player toMove = currentState.getToMove();
        Node child = new Node(chosenMove, node, toMove.id);
        node.children.put(chosenMove, child);

        // return the newly created child
        return child;
    }

    // accessor
    public MovePruner getPruner() {
        return pruner;
    }
}

