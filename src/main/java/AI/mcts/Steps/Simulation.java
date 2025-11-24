package AI.mcts.Steps;

import Game.*;
import java.util.*;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.MovePruner;

public class Simulation {
    private Random random = new Random();

    // added pruner
    private final MovePruner pruner = new MovePruner(0.25);

    public int simulate(GameState start) {
        GameState state = start.copy();
        while (!state.isTerminal()) {
            List<Move> legal = state.getLegalMoves();
            if (legal.isEmpty()) {
                break;
            }

            //  prune rollout moves
            List<Move> pruned = pruner.pruneMoves(state, legal);
            if (pruned.isEmpty()) pruned = legal;

            Collections.shuffle(pruned, random);
            state.doMove(pruned.getFirst());
        }
        return state.getWinnerId();
    }
}

