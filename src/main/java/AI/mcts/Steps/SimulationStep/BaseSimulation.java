package AI.mcts.Steps.SimulationStep;

import java.util.*;
import AI.mcts.HexGame.GameState;
import game.core.Move;

/**
 * Implements a basic random simulation strategy for Monte Carlo Tree Search.
 * This class performs game simulations by randomly selecting legal moves
 * until a terminal state is reached.
 * 
 * <p>This is the simplest simulation strategy and serves as a baseline
 * for more sophisticated approaches.</p>
 */
public class BaseSimulation implements Simulation {
    private final Random random = new Random();

    /**
     * Simulates a game from the given starting state by randomly playing moves
     * until the game ends. The simulation is performed on a copy of the state
     * to avoid modifying the original.
     *
     * @param start the game state from which to begin simulation
     * @return the ID of the winning player (0 for no winner, 1 for RED, 2 for BLACK)
     */
    @Override
    public int simulate(GameState start) {
        GameState state = start.copy();
        while (!state.isTerminal()) {
            List<Move> legal = state.getLegalMoves();
            if (legal.isEmpty()) break;

            Move chosen = legal.get(random.nextInt(legal.size()));
            state.doMove(chosen);
        }
        return state.getWinnerId();
    }
}










