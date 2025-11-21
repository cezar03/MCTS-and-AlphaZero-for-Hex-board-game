package AI.mcts.Steps;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import Game.Board;
import Game.Color;
import Game.Player;

import java.util.*;


public class MovePruner {

//set thershold to represent how many moves we want to keep (smaller=fewer)
    private final double threshold;

    public MovePruner(double threshold) {
        this.threshold = threshold;
    }

//scores each move based on the heuristic, find max among legal moves and creates a minimum score for being kept
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

        //  keep only moves whose score is closest to the best posssible (we use threshold value to represent how close the moves have to be to the max, so basically if thershold is >=max we keep all, if it <0 we don t keep any, anything in between we prune some of them)
        double min = maxScore - threshold;
        List<Move> pruned = new ArrayList<>();
        for (Move m : legalMoves) {
            if (scores.get(m) >= min) {
                pruned.add(m);
            }
        }

        // if we pruned everything, just return all moves
        return pruned.isEmpty() ? legalMoves : pruned;
    }
// our heursitic based on centrality and how connected a move is to other friendly stones
    private double heuristic(GameState state, Move move) {
        Board board = state.getBoard();
        int n = board.getSize();

        // centrality variable for calculating the score based on centrality of our move
        double centerRow = (n - 1) / 2.0;
        double centerCol = (n - 1) / 2.0;
        double distCenter = Math.hypot(move.row - centerRow, move.col - centerCol);
        double maxDist = Math.hypot(centerRow, centerCol);
        double centrality = 1.0 - distCenter / (maxDist + 1e-9); // 1e-9 for preventing division by 0 if we are playing on a small board


//identify which color it is palying as
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

        // i ve set the importance to centrality and connection to 50/50(equal) but we can tune it to value centrality or connectivtity more, i think connectivity would be better in most situations
        return 0.5 * centrality + 0.5 * connection;
    }
}
