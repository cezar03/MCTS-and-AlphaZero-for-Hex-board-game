package AI.mcts;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import Game.Board;
import Game.Color;
import Game.Player;

import java.util.*;


public class MovePruner {

    //set thershold to represent how many moves we want to keep (smaller=fewer)
    private final double threshold;

    // heuristics
    private final double centralityWeight;
    private final double connectivityWeight;

    //  constructor to allow different agents to use different pruning settings (used for testing)
    public MovePruner(double threshold, double centralityWeight, double connectivityWeight) {
        this.threshold = threshold;
        this.centralityWeight = centralityWeight;
        this.connectivityWeight = connectivityWeight;
    }

    //scores each move based on the heuristic, find max among legal (possible) moves and creates a minimum score for being kept
    public List<Move> pruneMoves(GameState state, List<Move> legalMoves) {
        if (legalMoves.isEmpty()) return legalMoves;

        Map<Move, Double> scores = new HashMap<>();
        double maxScore = Double.NEGATIVE_INFINITY;

        // scoring of each move
        for (Move m : legalMoves) {
            double score = heuristic(state, m);
            scores.put(m, score);
            if (score > maxScore) {
                maxScore = score;
            }
        }

        //  keep moves based on our thershold, here is where we decide how many nodes we want to prune
        double min = maxScore - threshold;
        List<Move> pruned = new ArrayList<>();
        for (Move m : legalMoves) {
            if (scores.get(m) >= min) {
                pruned.add(m);
            }
        }

        // if we pruned everything, return moves
        return pruned.isEmpty() ? legalMoves : pruned;
    }

    // our heuristic based on centrality and how connected a move is to other friendly stones
    private double heuristic(GameState state, Move move) {
        Board board = state.getBoard();
        int n = board.getSize();

        // centrality variable for calculating the score based on centrality of our move
        double centerRow = (n - 1) / 2.0;
        double centerCol = (n - 1) / 2.0;
        double distCenter = Math.hypot(move.row - centerRow, move.col - centerCol);
        double maxDist = Math.hypot(centerRow, centerCol);
        double centrality = 1.0 - distCenter / (maxDist + 1e-9); // 1e-9 prevents division by 0

//identify which color agent is playing as
        Player toMove = state.getToMove();
        Color myColor;

        if (toMove == Player.RED) {
            myColor = Color.RED;
        } else {
            myColor = Color.BLACK;
        }

// variable to represent how many neighbours of same color are among neighbours of the stone placed by possible move
        int friendlyNeighbors = 0;
        int totalNeighbors = 0;
        for (int[] rc : board.neighbors(move.row, move.col)) {
            int r = rc[0];
            int c = rc[1];
            if (!board.inBounds(r, c)) continue;
            totalNeighbors++;
            if (board.getCell(r, c) == myColor) {
                friendlyNeighbors++;
            }
        }
        double connection = (totalNeighbors == 0) ? 0.0 : (double) friendlyNeighbors / totalNeighbors;

        // final score based on centrality and connection
        return centralityWeight * centrality + connectivityWeight * connection;
    }

    // getters (unchanged)
    public double getThreshold() {
        return threshold;
    }

    public double getCentralityWeight() {
        return centralityWeight;
    }

    public double getConnectivityWeight() {
        return connectivityWeight;
    }
}
