package ai.tools;

import java.io.File;

import ai.alphazero.batch.Batcher;
import ai.alphazero.batch.DirectBatcher;
import ai.alphazero.config.AlphaZeroConfig;
import ai.alphazero.mcts.AlphaZeroMCTS;
import ai.alphazero.net.AlphaZeroNet;
import ai.alphazero.player.AlphaZeroPlayer;
import ai.api.AIAgent;
import ai.api.AIBoardAdapter;
import ai.mcts.MCTSPlayer;
import ai.random.RandomPlayer;
import bridge.BoardAdapter;
import game.core.Board;
import game.core.Move;
import game.core.Player;

public class RunExperimentCSV {
    // Simulation configurations
    private static final int BOARD_SIZE = 11;
    private static final int GAMES = 10;
    private static final boolean ALTERNATE_COLORS = true;
    private static int lastPlyCount = 0;
    private static final int SIMULATIONS = 5;
    private static final double[] cValues = {0.5, 0.8, 1.0, Math.sqrt(2), 2.0};

    private static final String MODEL_PATH = "src/main/resources/models/hex_model_correct.zip";

    // Optimized variables (MCTS)
    private static final double THR  = 0.9;
    private static final double CENT = 0.5;
    private static final double CONN = 0.5;
    private static final double BIAS = 0.046;
    private static final double SP   = 0.039;
    private static final double C_EXPL = Math.sqrt(2);

    public static void main(String[] args) {
        // runMctsVsRandomSweep(BOARD_SIZE, GAMES, ALTERNATE_COLORS);
        // compareCValues(BOARD_SIZE, GAMES, ALTERNATE_COLORS);
        // runBaseVsOpt(BOARD_SIZE, GAMES, ALTERNATE_COLORS);

        testAlphaZerovsRandom();
        // testMCTSvsAlphaZero();
    }

    private static void runMctsVsRandomSweep(int boardSize, int gamesPerSetting, boolean alternateColors) {
        System.out.println("simId,iters,games_played,mcts_wins,random_wins,winrate");

        int[] iterSettings = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
            22, 24, 26, 28, 30, 32, 34, 36, 38, 40,
            45, 50, 55, 60, 70, 80, 90, 100,
            120, 140, 160, 180, 200, 240, 280, 320, 360, 400, 500
        };

        for (int simId = 1; simId <= SIMULATIONS; simId++) {
            for (int iters : iterSettings) {
                AIAgent mctsRed = new MCTSPlayer(Player.RED, iters);
                AIAgent mctsBlack = new MCTSPlayer(Player.BLACK, iters);
                AIAgent randRed = new RandomPlayer(Player.RED);
                AIAgent randBlack = new RandomPlayer(Player.BLACK);

                int mctsWins = 0;
                int randWins = 0;
                int gamesPlayed = 0;

                for (int game = 1; game <= gamesPerSetting; game++) {
                    boolean mctsIsRed = !alternateColors || (game % 2 == 1);
                    AIAgent redAgent = mctsIsRed ? mctsRed : randRed;
                    AIAgent blackAgent = mctsIsRed ? randBlack : mctsBlack;

                    Player winner = playSingleGame(redAgent, blackAgent, boardSize);
                    if (winner == null) continue;

                    gamesPlayed++;
                    if (winner == Player.RED) {
                        if (mctsIsRed) mctsWins++; else randWins++;
                    } else {
                        if (mctsIsRed) randWins++; else mctsWins++;
                    }
                }

                double winrate = (gamesPlayed > 0) ? (double) mctsWins / gamesPlayed : 0.0;
                System.out.printf("%d,%d,%d,%d,%d,%.4f%n", simId, iters, gamesPlayed, mctsWins, randWins, winrate);
            }
        }
    }

    // ---------------- Base vs Optimized ----------------

    private static void runBaseVsOpt(int boardSize, int gamesPerSim, boolean alternateColors) {
        System.out.println("simId,game,base_is_red,winner,ply_count,base_cum,opt_cum");

        for (int simId = 1; simId <= SIMULATIONS; simId++) {
            AIAgent baseRed = new MCTSPlayer(Player.RED, 2000);
            AIAgent baseBlack = new MCTSPlayer(Player.BLACK, 2000);
            AIAgent optRed = new MCTSPlayer(Player.RED, 2000, THR, CENT, CONN, BIAS, SP, C_EXPL);
            AIAgent optBlack = new MCTSPlayer(Player.BLACK, 2000, THR, CENT, CONN, BIAS, SP, C_EXPL);

            int baseCum = 0;
            int optCum = 0;

            for (int game = 1; game <= gamesPerSim; game++) {
                boolean baseIsRed = !alternateColors || (game % 2 == 1);
                AIAgent redAgent = baseIsRed ? baseRed : optRed;
                AIAgent blackAgent = baseIsRed ? optBlack : baseBlack;

                Player winner = playSingleGame(redAgent, blackAgent, boardSize);
                int plyCount = lastPlyCount;
                if (winner == null) continue;

                String winnerLabel;
                if (winner == Player.RED) winnerLabel = baseIsRed ? "BASE" : "OPT";
                else winnerLabel = baseIsRed ? "OPT" : "BASE";

                if ("BASE".equals(winnerLabel)) baseCum++;
                else optCum++;

                System.out.printf("%d,%d,%s,%s,%d,%d,%d%n", simId, game, baseIsRed, winnerLabel, plyCount, baseCum, optCum);
            }
        }
    }

    // ---------------- Compare C values ----------------

    private static void compareCValues(int boardSize, int gamesPerC, boolean alternateColors) {
        System.out.println("simId,c_value,games_played,base_wins,opt_wins,opt_winrate");

        for (int simId = 1; simId <= SIMULATIONS; simId++) {
            for (double cValue : cValues) {
                AIAgent baseRed = new MCTSPlayer(Player.RED, 1000);
                AIAgent baseBlack = new MCTSPlayer(Player.BLACK, 1000);
                AIAgent optRed = new MCTSPlayer(Player.RED, 1000, THR, CENT, CONN, BIAS, SP, cValue);
                AIAgent optBlack = new MCTSPlayer(Player.BLACK, 1000, THR, CENT, CONN, BIAS, SP, cValue);

                int baseWins = 0;
                int optWins = 0;
                int gamesPlayed = 0;

                for (int game = 1; game <= gamesPerC; game++) {
                    boolean baseIsRed = !alternateColors || (game % 2 == 1);
                    AIAgent redAgent = baseIsRed ? baseRed : optRed;
                    AIAgent blackAgent = baseIsRed ? optBlack : baseBlack;

                    Player winner = playSingleGame(redAgent, blackAgent, boardSize);
                    if (winner == null) continue;

                    gamesPlayed++;
                    if (winner == Player.RED) {
                        if (baseIsRed) baseWins++; else optWins++;
                    } else {
                        if (baseIsRed) optWins++; else baseWins++;
                    }
                }

                double optWinrate = (gamesPlayed > 0) ? (double) optWins / gamesPlayed : 0.0;
                System.out.printf("%d,%f,%d,%d,%d,%.4f%n", simId, cValue, gamesPlayed, baseWins, optWins, optWinrate);
            }
        }
    }

    // ---------------- Shared game loop with ply counting ----------------

    private static Player playSingleGame(AIAgent redAgent, AIAgent blackAgent, int boardSize) {
        Board board = new Board(boardSize);
        AIBoardAdapter adapter = new BoardAdapter(board);

        Player currentPlayer = Player.RED;
        int maxMoves = boardSize * boardSize;
        int moveCount = 0;

        while (!adapter.isTerminal() && moveCount < maxMoves) {
            AIAgent currentAgent = (currentPlayer == Player.RED) ? redAgent : blackAgent;
            Move move = currentAgent.getBestMove(adapter, currentPlayer);
            if (move == null) break;

            boolean success = adapter.makeMove(move.row, move.col, currentPlayer);
            if (!success) break;

            currentPlayer = currentPlayer.other();
            moveCount++;
        }

        lastPlyCount = moveCount;

        if (adapter.redWins()) return Player.RED;
        if (adapter.blackWins()) return Player.BLACK;
        return null;
    }

    // ---------------- Tests ----------------

    private static void testMCTSvsAlphaZero() {
        AIAgent mcts = new MCTSPlayer(Player.RED, 1000, THR, CENT, CONN, BIAS, SP, C_EXPL);

        AlphaZeroConfig cfg = new AlphaZeroConfig.Builder()
                .boardSize(BOARD_SIZE)
                .mctsIterations(100)
                .temperature(0.01)
                .modelPath(MODEL_PATH)
                .loadExistingModel(true)
                .build();

        AIAgent alphaZero = createAlphaZeroAgent(Player.BLACK, cfg);

        AITester.runMatch(mcts, alphaZero, GAMES, BOARD_SIZE, false);
    }

    private static void testAlphaZerovsRandom() {
        AlphaZeroConfig cfg = new AlphaZeroConfig.Builder()
                .boardSize(BOARD_SIZE)
                .mctsIterations(100)
                .temperature(0.01)
                .modelPath(MODEL_PATH)
                .loadExistingModel(true)
                .build();

        AIAgent alphaZero = createAlphaZeroAgent(Player.RED, cfg);
        AIAgent random = new RandomPlayer(Player.BLACK);

        AITester.runMatch(alphaZero, random, GAMES, BOARD_SIZE, false);
    }

    private static AIAgent createAlphaZeroAgent(Player player, AlphaZeroConfig cfg) {
        AlphaZeroNet net = loadOrCreateNet(cfg);
        Batcher batcher = new DirectBatcher(net);
        AlphaZeroMCTS mcts = new AlphaZeroMCTS(batcher, cfg);
        return new AlphaZeroPlayer(player, mcts, cfg);
    }

    private static AlphaZeroNet loadOrCreateNet(AlphaZeroConfig cfg) {
        if (cfg.isLoadExistingModel()) {
            try {
                File f = new File(cfg.getModelPath());
                if (f.exists()) {
                    System.out.println("Loading AlphaZero model: " + f.getPath());
                    return AlphaZeroNet.load(f.getPath(), cfg.getBoardSize());
                } else {
                    System.out.println("Model path not found: " + f.getPath() + " (creating fresh net)");
                }
            } catch (Exception e) {
                System.out.println("Failed to load model: " + e.getMessage() + " (creating fresh net)");
            }
        }
        return new AlphaZeroNet(cfg.getBoardSize());
    }
}











