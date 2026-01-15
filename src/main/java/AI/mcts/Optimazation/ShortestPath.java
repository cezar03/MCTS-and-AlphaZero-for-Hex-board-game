package AI.mcts.Optimazation;

import Game.Board;
import Game.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for computing shortest path distances in Hex game boards
 * using a 0-1 BFS (Breadth-First Search) algorithm.
 * <p>
 * The algorithm calculates the minimum number of empty cells that need to be
 * filled to create a winning connection between the player's goal edges:
 * <ul>
 * <li>RED player: connects top edge (row 0) to bottom edge (row n-1)</li>
 * <li>BLACK player: connects left edge (col 0) to right edge (col n-1)</li>
 * </ul>
 * <p>
 * The path cost model:
 * <ul>
 * <li>Cost 0 to traverse cells containing the player's stones</li>
 * <li>Cost 1 to traverse empty cells (these need to be filled)</li>
 * <li>Opponent's stones are impassable (blocked)</li>
 * </ul>
 * <p>
 * This is a utility class and cannot be instantiated.
 */
public final class ShortestPath {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ShortestPath() {
        // utility class, no instances
    }

    /**
     * Computes the shortest path distance for the specified player using 0-1 BFS.
     * The distance represents the minimum number of empty cells that must be filled
     * to create a winning connection between the player's goal edges.
     * <p>
     * The algorithm uses a deque-like structure to efficiently process edges with
     * different costs, ensuring optimal shortest path computation.
     * 
     * 0–1 BFS shortest path:
     * cost 0 to move through player's stones,
     * cost 1 to move through empty cells,
     * opponent stones are blocked.
     *
     * RED:   top (row 0)    -> bottom (row n-1)
     * BLACK: left (col 0)   -> right (col n-1)
     * 
     * @param board the game board to analyze
     * @param player the player (RED or BLACK) for whom to compute the shortest path
     * @return the minimum number of empty cells needed to complete a winning path,
     *         or a large number (1,000,000) if no path exists (board is blocked)
     */
    public static int shortestPath(Board board, Color player) {
        final int n = board.getSize();
        final int INF = 1_000_000;

        int[][] dist = new int[n][n];
        for (int r = 0; r < n; r++) {
            Arrays.fill(dist[r], INF);
        }

        List<int[]> queue = new ArrayList<>();
        int head = 0;

        if (player == Color.RED) {
            for (int col = 0; col < n; col++) {
                Color c = board.getCell(0, col);
                if (c == player) {
                    dist[0][col] = 0;
                } else if (c == Color.EMPTY) {
                    dist[0][col] = 1;
                } else {
                    continue;
                }
                queue.add(new int[]{0, col});
            }
        } else {
            for (int row = 0; row < n; row++) {
                Color c = board.getCell(row, 0);
                if (c == player) {
                    dist[row][0] = 0;
                } else if (c == Color.EMPTY) {
                    dist[row][0] = 1;
                } else {
                    continue;
                }
                queue.add(new int[]{row, 0});
            }
        }

        while (head < queue.size()) {
            int[] cur = queue.get(head++);
            int r = cur[0];
            int c = cur[1];
            int currentDist = dist[r][c];

            for (int[] nb : board.neighbors(r, c)) {
                int nr = nb[0];
                int nc = nb[1];
                Color cellColor = board.getCell(nr, nc);
                int cost;

                if (cellColor == player) {
                    cost = 0;
                } else if (cellColor == Color.EMPTY) {
                    cost = 1;
                } else {
                    continue;
                }

                int newDist = currentDist + cost;

                if (newDist < dist[nr][nc]) {
                    dist[nr][nc] = newDist;
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        int best = INF;
        if (player == Color.RED) {
            for (int col = 0; col < n; col++) {
                best = Math.min(best, dist[n - 1][col]);
            }
        } else {
            for (int row = 0; row < n; row++) {
                best = Math.min(best, dist[row][n - 1]);
            }
        }

        return best;
    }
}
