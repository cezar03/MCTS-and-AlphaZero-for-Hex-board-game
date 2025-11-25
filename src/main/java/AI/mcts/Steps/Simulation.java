package AI.mcts.Steps;

import Game.*;
import java.util.*;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;

public class Simulation {
    private Random random = new Random();
    private static final double EPSILON = 0.1;

    public int simulate(GameState start) {
        GameState state = start.copy();
        while (!state.isTerminal()) {
            List<Move> legal = state.getLegalMoves();
            if (legal.isEmpty()) {
                break;
            }
            Move chosen = chooseMove(state, legal);
            state.doMove(chosen);
        }
        return state.getWinnerId();
    }
    
    //epsilon-greedy move selection
    private Move chooseMove(GameState state, List<Move> legal) {
        if (random.nextDouble() < EPSILON) {
            return legal.get(random.nextInt(legal.size()));
        }
        return bestHeuristicMove(state, legal);
    }

    //choose move that minimizes the estimated shortest path 
    private Move bestHeuristicMove(GameState state, List<Move> legal) {
        Move best = null;
        int bestScore = Integer.MAX_VALUE;  // lower = better

        for (Move m : legal) {
            int score = state.estimateAfterMove(m);  
            if (score < bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return best;
    }
}
