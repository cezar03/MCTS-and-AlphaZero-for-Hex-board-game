package AI.mcts.Optimazation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import game.core.Board;
import game.core.Color;

public final class ShortestPath {

<<<<<<< HEAD
    /** Utility class, no instances. */
    private ShortestPath() {
        // utility class, no instances
    }
=======
    private ShortestPath() {}
>>>>>>> 1165bedc5af5867e936278ee2626c1ff7663bbd5

    /**
     * 0–1 BFS shortest path:
     * cost 0 to move through player's stones,
     * cost 1 to move through empty cells,
     * opponent stones are blocked.
     *
     * RED:   top (row 0)    -> bottom (row n-1)
     * BLACK: left (col 0)   -> right (col n-1)
     *
     * @return minimal number of empty cells that must be filled to connect,
     *         or a large number (INF) if no path exists.
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
                Color cell = board.getCell(0, col);
                if (cell == player) {
                    dist[0][col] = 0;
                } else if (cell == Color.EMPTY) {
                    dist[0][col] = 1;
                } else {
                    continue;
                }
                queue.add(new int[]{0, col});
            }
        } else {
            for (int row = 0; row < n; row++) {
                Color cell = board.getCell(row, 0);
                if (cell == player) {
                    dist[row][0] = 0;
                } else if (cell == Color.EMPTY) {
                    dist[row][0] = 1;
                } else {
                    continue;
                }
                queue.add(new int[]{row, 0});
            }
        }

        while (head < queue.size()) {
            int[] cur = queue.get(head++);
            int row = cur[0];
            int column = cur[1];
            int currentDist = dist[row][column];

            for (int[] nb : board.neighbors(row, column)) {
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











