package AI;

import Game.Player;

/**
 * Main class for running AI tests.
 * This class can be modified to quickly test different agent configurations.
 */
public class TestRunner {
    
    public static void main(String[] args) {
        // Example 1: MCTS vs Random
        testMCTSvsRandom();
        
        // Example 2: Different MCTS configurations
        testMCTSvsMCTS();
        
        // Example 3: Tournament format (eliminates first-player bias)
        AIAgent mctsAgent1 = new MCTSPlayer(Player.RED, 1000);
        AIAgent randomAgent = new RandomPlayer(Player.BLACK);
        runTournament(mctsAgent1, randomAgent, 50, 11, false);
    }
    
    /**
     * Test MCTS agent against random baseline.
     */
    private static void testMCTSvsRandom() {
        AIAgent mcts = new MCTSPlayer(Player.RED, 1000);
        AIAgent random = new RandomPlayer(Player.BLACK);
        
        AITester.runMatch(mcts, random, 3, 11, false);
    }
    
    /**
     * Test two MCTS agents with different iteration counts.
     */
    private static void testMCTSvsMCTS() {
        AIAgent mcts1 = new MCTSPlayer(Player.RED, 1000);
        AIAgent mcts2 = new MCTSPlayer(Player.BLACK, 2000);
        
        AITester.runMatch(mcts1, mcts2, 30, 11, false);
    }
    
    /**
     * Run a full tournament to eliminate first-player advantage.
     */
    private static void runTournament(AIAgent agent1, AIAgent agent2, 
                                    int gamesPerSide, int boardSize, boolean extensivePrints) {
        AITester.runTournament(agent1, agent2, gamesPerSide, boardSize, extensivePrints);
    }
    
    /**
     * Quick test with verbose output to see individual game results.
     */
    private static void quickTest(AIAgent agent1, AIAgent agent2) {
        AITester.runMatch(agent1, agent2, 5, 11, true); // extensive prints for debugging purposes
    }
}