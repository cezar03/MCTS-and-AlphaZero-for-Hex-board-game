package AI.mcts.Steps;

import Game.*;
import java.util.*;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;

public class Simulation {
    private Random random = new Random();

    public int simulate(GameState start) {
        GameState state = start.copy();
        while (!state.isTerminal()) {
            List<Move> legal = state.getLegalMoves();
            if (legal.isEmpty()) {
                break;
            }
            Collections.shuffle(legal, random);
            state.doMove(legal.getFirst());
        }
        return state.getWinnerId();
    }
}
