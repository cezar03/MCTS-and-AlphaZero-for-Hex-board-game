package AI.tools;

import AI.api.AIAgent;
import AI.api.AIBoardAdapter;
import AI.mcts.MCTSPlayer;
import AI.random.RandomPlayer;
import bridge.BoardAdapter;
import game.core.Board;
import game.core.Move;
import game.core.Player;

/**
 * Framework for testing AI agents against each other.
 * Runs multiple games and collects statistics on win rates.
 */
public class AITester {
    /**
     * Represents the result of a testing session.
     */
    public static class TestResult {
        public final String redAgentName;
        public final String blackAgentName;
        public final int totalGames;
        public final int redWins;
        public final int blackWins;
        public final int draws;

        /**
         * Constructs a TestResult instance.
         * @param redAgentName the name of the red agent
         * @param blackAgentName the name of the black agent
         * @param totalGames the total number of games played
         * @param redWins the number of games won by the red agent
         * @param blackWins the number of games won by the black agent
         * @param draws the number of drawn games
         */
        public TestResult(String redAgentName, String blackAgentName, int totalGames, int redWins, int blackWins, int draws) {
            this.redAgentName = redAgentName;
            this.blackAgentName = blackAgentName;
            this.totalGames = totalGames;
            this.redWins = redWins;
            this.blackWins = blackWins;
            this.draws = draws;
        }

        /**
         * Calculates the win rate of the red agent as a percentage.
         * @return the red agent's win rate
         */
        public double getRedWinRate() {
            return totalGames > 0 ? (redWins * 100.0 / totalGames) : 0;
        }

        /**
         * Calculates the win rate of the black agent as a percentage.
         * @return the black agent's win rate
         */
        public double getBlackWinRate() {
            return totalGames > 0 ? (blackWins * 100.0 / totalGames) : 0;
        }

        /**
         * Prints a summary of the test results to the console.
         */
        public void printResults() {
            System.out.println("\n========== TEST RESULTS ==========");
            System.out.println("RED:   " + redAgentName);
            System.out.println("BLACK: " + blackAgentName);
            System.out.println("----------------------------------");
            System.out.println("Total games: " + totalGames);
            System.out.println("RED wins:    " + redWins + " (" + String.format("%.2f%%", getRedWinRate()) + ")");
            System.out.println("BLACK wins:  " + blackWins + " (" + String.format("%.2f%%", getBlackWinRate()) + ")");
            System.out.println("Draws:       " + draws);
            System.out.println("==================================\n");
        }
    }

    /**
     * Runs a match between two AI agents for a specified number of games.
     * @param redAgent the agent playing as RED
     * @param blackAgent the agent playing as BLACK
     * @param numGames the number of games to play
     * @param boardSize the size of the game board
     * @param extensivePrints whether to print detailed game results
     * @return a TestResult object containing the match statistics
     */
    public static TestResult runMatch(AIAgent redAgent, AIAgent blackAgent,
                                      int numGames, int boardSize, boolean extensivePrints) {
        int redWins = 0;
        int blackWins = 0;
        int draws = 0;
        String redName = getAgentName(redAgent);
        String blackName = getAgentName(blackAgent);
        System.out.println("\nStarting match: " + redName + " vs " + blackName);
        System.out.println("Playing " + numGames + " games on " + boardSize + "x" + boardSize + " board\n");

        for (int gameNum = 1; gameNum <= numGames; gameNum++) {
            if (extensivePrints) { System.out.println("Playing game " + gameNum + "/" + numGames + "..."); }

            Player winner = playGame(redAgent, blackAgent, boardSize);

            if (winner == null) {
                draws++;
                if (extensivePrints) System.out.println("  Result: DRAW/UNKNOWN");
            } else {
                switch (winner) {
                    case RED -> { redWins++; if (extensivePrints) System.out.println("  Result: RED wins"); }
                    case BLACK -> { blackWins++; if (extensivePrints) System.out.println("  Result: BLACK wins"); }
                }
            }

            if (!extensivePrints && gameNum % 10 == 0) { System.out.println("Progress: " + gameNum + "/" + numGames + " games completed"); }
        }

        TestResult result = new TestResult(redName, blackName, numGames, redWins, blackWins, draws);
        result.printResults();

        if (redAgent instanceof MCTSPlayer p) { printMctsPruningConfig("RED", p); }
        if (blackAgent instanceof MCTSPlayer p) { printMctsPruningConfig("BLACK", p); }

        return result;
    }

    /**
     * Prints the MCTS pruning configuration of the given player.
     * @param label the label for the player (e.g., "RED" or "BLACK")
     * @param p the MCTSPlayer whose configuration to print
     */
    private static void printMctsPruningConfig(String label, MCTSPlayer p) {
        double t  = p.getThreshold();
        double cw = p.getCentralityWeight();
        double connw = p.getConnectivityWeight();
        boolean usesPruning = (t != 0.0) || (cw != 0.0) || (connw != 0.0);

        if (!usesPruning) {
            System.out.println(label + " pruning threshold: (no pruning)");
            System.out.println(label + " centrality weight: (no pruning)");
            System.out.println(label + " connectivity weight: (no pruning)");
        } else {
            System.out.println(label + " pruning threshold: " + t);
            System.out.println(label + " centrality weight: " + cw);
            System.out.println(label + " connectivity weight: " + connw);
        }
    }

    /**
     * Plays a single game between two AI agents.
     * @param redAgent the agent playing as RED
     * @param blackAgent the agent playing as BLACK
     * @param boardSize the size of the game board
     * @return the winning Player, or null for a draw/unknown
     */
    private static Player playGame(AIAgent redAgent, AIAgent blackAgent, int boardSize) {
        Board board = new Board(boardSize);
        AIBoardAdapter adapter = new BoardAdapter(board);

        Player currentPlayer = Player.RED;
        int maxMoves = boardSize * boardSize;
        int moveCount = 0;

        while (!adapter.isTerminal() && moveCount < maxMoves) {
            AIAgent currentAgent = (currentPlayer == Player.RED) ? redAgent : blackAgent;
            Move move = currentAgent.getBestMove(adapter, currentPlayer);

            if (move == null) return null;

            boolean success = adapter.makeMove(move.row, move.col, currentPlayer);
            if (!success) {
                System.err.println("Warning: Invalid move returned by " + getAgentName(currentAgent));
                return null;
            }

            currentPlayer = currentPlayer.other();
            moveCount++;
        }

        if (adapter.redWins()) return Player.RED;
        if (adapter.blackWins()) return Player.BLACK;
        return null;
    }

    /**
     * Retrieves a human-readable name for the given AI agent.
     * @param agent the AI agent
     * @return the agent's name
     */
    private static String getAgentName(AIAgent agent) {
        if (agent == null) {
            return "Unknown";
        }
        if (agent instanceof MCTSPlayer mctsPlayer) {
            int iters = mctsPlayer.getIterations();
            double t  = mctsPlayer.getThreshold();
            double cw = mctsPlayer.getCentralityWeight();
            double connw = mctsPlayer.getConnectivityWeight();
            boolean usesPruning = (t != 0.0) || (cw != 0.0) || (connw != 0.0);

            if (usesPruning) {
                return "MCTS(pruned," + iters + " iters)";
            } else {
                return "MCTS(base," + iters + " iters)";
            }
        } else if (agent instanceof RandomPlayer) {
            return "RandomPlayer";
        } else {
            return agent.getClass().getSimpleName();
        }
    }

    /**
     * Runs a tournament between two AI agents, with each playing both sides.
     * @param agent1 the first AI agent
     * @param agent2 the second AI agent
     * @param gamesPerSide the number of games each agent plays as RED and BLACK
     * @param boardSize the size of the game board
     * @param extensivePrints whether to print detailed game results
     */
    public static void runTournament(AIAgent agent1, AIAgent agent2,
                                     int gamesPerSide, int boardSize,
                                     boolean extensivePrints) {
        System.out.println("\n########## TOURNAMENT ##########");
        System.out.println("Each agent will play " + gamesPerSide + " games as RED and BLACK");
        TestResult round1 = runMatch(agent1, agent2, gamesPerSide, boardSize, extensivePrints);
        TestResult round2 = runMatch(agent2, agent1, gamesPerSide, boardSize, extensivePrints);
        String agent1Name = getAgentName(agent1);
        String agent2Name = getAgentName(agent2);
        int agent1TotalWins = round1.redWins + round2.blackWins;
        int agent2TotalWins = round1.blackWins + round2.redWins;
        int totalGames = round1.totalGames + round2.totalGames;
        System.out.println("\n========== TOURNAMENT SUMMARY ==========");
        System.out.println(agent1Name + " total wins: " + agent1TotalWins + "/" + totalGames +
                " (" + String.format("%.2f%%", agent1TotalWins * 100.0 / totalGames) + ")");
        System.out.println(agent2Name + " total wins: " + agent2TotalWins + "/" + totalGames +
                " (" + String.format("%.2f%%", agent2TotalWins * 100.0 / totalGames) + ")");
        System.out.println("========================================\n");
    }
}











