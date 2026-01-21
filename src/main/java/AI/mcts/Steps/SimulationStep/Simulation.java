package AI.mcts.Steps.SimulationStep;

import AI.mcts.HexGame.GameState;

/**
 * Interface for simulation strategies in Monte Carlo Tree Search.
 * Implementations of this interface define how game simulations (playouts)
 * are performed from a given game state.
 * 
 * <p>Different simulation strategies can range from completely random play
 * to sophisticated heuristic-guided approaches.</p>
 */
public interface Simulation {
    /**
     * Simulates a complete game from the given starting state until a terminal
     * state is reached.
     *
     * @param start the game state from which to begin simulation
     * @return the ID of the winning player (0 for no winner, 1 for RED, 2 for BLACK)
     */
    int simulate(GameState start);
}












