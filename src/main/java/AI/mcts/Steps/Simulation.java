package AI.mcts.Steps;

import Game.*;
import java.util.*;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.MovePruner;

public class Simulation {
    private Random random = new Random();

    // pruner passed from MCTS instead of hardcode
    private final MovePruner pruner;

    // constructor for agent-specific pruner
    public Simulation(MovePruner pruner) {
        this.pruner = pruner;
    }

    public int simulate(GameState start) {
        GameState state = start.copy();
        while (!state.isTerminal()) {
            List<Move> legal = state.getLegalMoves();
            if (legal.isEmpty()) {
                break;
            }

            //  agent-specific pruner
            List<Move> pruned = pruner.pruneMoves(state, legal);
            if (pruned.isEmpty()) pruned = legal;

            Collections.shuffle(pruned, random);
            state.doMove(pruned.getFirst());
        }
        return state.getWinnerId();
    }
}


