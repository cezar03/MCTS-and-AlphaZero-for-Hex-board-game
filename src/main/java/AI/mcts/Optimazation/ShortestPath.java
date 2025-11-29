package AI.mcts.Optimazation;

import Game.Board;
import Game.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ShortestPath {

    private ShortestPath() {
        // utility class, no instances
    }

    private static Color getCellByPlayer(Board board, Color player, int i) {
        if (player == Color.RED) {
            return board.getCell(i, 0);
        } else {
            return board.getCell(0, i);
        }
    }

    /**
     * 0–1 BFS / SPFA-style shortest path:
     * cost 0 to move through player's stones,
     * cost 1 to move through empty cells,
     * opponent stones are blocked.
     * <p>
     * RED:  top -> bottom
     * BLACK: left -> right
     *
     * @return minimal number of empty cells that must be filled to connect,
     * or a large number (INF) if no path exists.
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

        // initialize start edge
        for (int i = 0; i < n; i++) {
            Color c = getCellByPlayer(board, player, i);
            if (c == player) {
                dist[0][i] = 0;
            } else if (c == Color.EMPTY) {
                dist[0][i] = 1;
            } else {
                continue; // opponent stone blocks
            }
            queue.add(new int[]{0, i});
        }

        // BFS / SPFA over 0–1 edge costs
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
                    continue; // opponent stone blocks this neighbor
                }

                int newDist = currentDist + cost;
                if (newDist < dist[nr][nc]) {
                    dist[nr][nc] = newDist;
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        // Check target edge
        int best = INF;
        // bottom row
        for (int i = 0; i < n; i++) {
            if (player == Color.RED) {
                best = Math.min(best, dist[n - 1][i]);
            } else {
                best = Math.min(best, dist[i][n - 1]);
            }
        }

        return best;
    }
}