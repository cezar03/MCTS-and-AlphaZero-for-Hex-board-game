package AI.AiPlayer;

import Game.Board;
import Game.BoardAdapter;
import Game.Player;
import AI.mcts.HexGame.Move;

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
        public final int draws; // In Hex, there should be no draws, but this variable can also help detect errors if it shows that there are draws.

        public TestResult(String redAgentName, String blackAgentName,
                          int totalGames, int redWins, int blackWins, int draws) {
            this.redAgentName = redAgentName;
            this.blackAgentName = blackAgentName;
            this.totalGames = totalGames;
            this.redWins = redWins;
            this.blackWins = blackWins;
            this.draws = draws;
        }

        /**
         * Calculates the win percentage for the agent that represents player RED.
         * @return Win rate as a double between 0 and 100 to represent a percentage.
         */
        public double getRedWinRate() {
            return totalGames > 0 ? (redWins * 100.0 / totalGames) : 0;
        }

        /**
         * Calculates the win percentage for the agent that represents player BLACK.
         * @return Win rate as a double between 0 and 100 to represent a percentage.
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
     * Runs a match between two AI agents.
     * @param redAgent The agent playing as RED
     * @param blackAgent The agent playing as BLACK
     * @param numGames The number of games to play
     * @param boardSize The size of the board (typically 11)
     * @param extensivePrints If true, prints progress for each game
     * @return TestResult containing statistics
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
            if (extensivePrints) {
                System.out.println("Playing game " + gameNum + "/" + numGames + "...");
            }

            Player winner = playGame(redAgent, blackAgent, boardSize);

            if (winner == Player.RED) {
                redWins++;
                if (extensivePrints) System.out.println("  Result: RED wins");
            } else if (winner == Player.BLACK) {
                blackWins++;
                if (extensivePrints) System.out.println("  Result: BLACK wins");
            } else {
                draws++;
                if (extensivePrints) System.out.println("  Result: Draw");
            }

            if (!extensivePrints && gameNum % 10 == 0) {
                System.out.println("Progress: " + gameNum + "/" + numGames + " games completed");
            }
        }

        TestResult result = new TestResult(redName, blackName, numGames, redWins, blackWins, draws);
        result.printResults();

        // print pruning & heuristic params
        if (redAgent instanceof MCTSPlayer p) {
            System.out.println("RED pruning threshold: " + p.getPruner().getThreshold());
            System.out.println("RED centrality weight: " + p.getPruner().getCentralityWeight());
            System.out.println("RED connectivity weight: " + p.getPruner().getConnectivityWeight());
        }

        if (blackAgent instanceof MCTSPlayer p) {
            System.out.println("BLACK pruning threshold: " + p.getPruner().getThreshold());
            System.out.println("BLACK centrality weight: " + p.getPruner().getCentralityWeight());
            System.out.println("BLACK connectivity weight: " + p.getPruner().getConnectivityWeight());
        }

        return result;
    }

    /**
     * Plays a single game between two agents.
     * @return The winner (RED, BLACK, or null for draw)
     */
    private static Player playGame(AIAgent redAgent, AIAgent blackAgent, int boardSize) {
        Board board = new Board(boardSize);
        BoardAdapter adapter = new BoardAdapter(board);
        Player currentPlayer = Player.RED;

        int maxMoves = boardSize * boardSize; // Prevent infinite loops
        int moveCount = 0;

        while (!adapter.isGameOver() && moveCount < maxMoves) {
            AIAgent currentAgent = (currentPlayer == Player.RED) ? redAgent : blackAgent;
            Move move = currentAgent.getBestMove(board, currentPlayer);

            if (move == null) {
                break; // No legal moves
            }

            boolean success = adapter.makeMove(move.row, move.col, currentPlayer);
            if (!success) {
                System.err.println("Warning: Invalid move returned by " + getAgentName(currentAgent));
                break;
            }

            currentPlayer = currentPlayer.other();
            moveCount++;
        }

        return adapter.getWinner();
    }

    /**
     * Gets a descriptive name for an agent.
     */
    private static String getAgentName(AIAgent agent) {
        if (agent instanceof MCTSPlayer) {
            MCTSPlayer mctsPlayer = (MCTSPlayer) agent;
            return "MCTS(" + mctsPlayer.getIterations() + " iterations)";
        } else if (agent instanceof RandomPlayer) {
            return "RandomPlayer";
        } else {
            return agent.getClass().getSimpleName();
        }
    }

    /**
     * Runs a tournament where agents play each other with swapped colors.
     * This helps eliminate first-player advantage bias.
     */
    public static void runTournament(AIAgent agent1, AIAgent agent2,
                                     int gamesPerSide, int boardSize, boolean extensivePrints) {
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
