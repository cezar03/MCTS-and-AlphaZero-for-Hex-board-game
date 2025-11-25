package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Board {
    private final int n;
    private final Color[] cells;
    private final UnionFind uf;
    private final int redTop, redBottom, blackLeft, blackRight;

    public Board(int n) {
        if (n <= 0) throw new IllegalArgumentException("The board can t have less than 1 row, 1 column");
        this.n = n;
        this.cells = new Color[n * n];
        Arrays.fill(cells, Color.EMPTY);
        int unionFindSize = n * n + 4; // Union-Find arrays: one node per cell + 4 virtual edges
        this.uf = new UnionFind(unionFindSize);

        //Indices for edge nodes
        redTop = n * n;
        redBottom = n * n + 1;
        blackLeft = n * n + 2;
        blackRight = n * n + 3;
    }

    /* Get methods */

    // Gets board size
    public int getSize() {
        return n;
    }
    // Gets cell color
    public Color getCell(int row, int column) {
        return cells[idx(row, column)];
    }
    // Place red stone
    public void getMoveRed(int row, int column, Color _ignored) {
        placeStone(row, column, Color.RED);
    }
    // Place black stone
    public void getMoveBlack(int row, int column, Color _ignored) {
        placeStone(row, column, Color.BLACK);
    }

    /* Helpers for UI and future AI */

    // Check if in bounds of board
    public boolean inBounds(int row, int column) {
        return row >= 0 && column >= 0 && row < n && column < n;
    }

    // Check if cell is empty
    public boolean isEmpty(int row, int column) {
        return inBounds(row, column) && getCell(row, column) == Color.EMPTY;
    }

    /**
     * Creates a copy of the board for simulation in MCTS
    */
    public Board copyBoard(Board original) {
        int n = original.getSize();
        Board copy = new Board(n);
        
        // Copy all placed stones
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                Color cellColor = original.getCell(row, col);
                if (cellColor == Color.RED) {
                    copy.getMoveRed(row, col, Color.RED);
                } else if (cellColor == Color.BLACK) {
                    copy.getMoveBlack(row, col, Color.BLACK);
                }
            }
        }
        
        return copy;
    }
    //find shortest path with bfs
    public int shortestPath(Color player) {
    int INF = 1_000_000;
    int[][] dist = new int[n][n];

    for (int i = 0; i < n; i++)
        Arrays.fill(dist[i], INF);

    List<int[]> queue = new ArrayList<>();
    int head = 0;

    //initialize start edge
    if (player == Color.RED) {
        // top row
        for (int col = 0; col < n; col++) {
            Color c = cells[idx(0, col)];
            if (c == player) dist[0][col] = 0;
            else if (c == Color.EMPTY) dist[0][col] = 1;
            else continue;
            queue.add(new int[]{0, col});
        }
    } else {
        // left column
        for (int row = 0; row < n; row++) {
            Color c = cells[idx(row, 0)];
            if (c == player) dist[row][0] = 0;
            else if (c == Color.EMPTY) dist[row][0] = 1;
            else continue;
            queue.add(new int[]{row, 0});
        }
    }

    //BFS / dijkstra 
    while (head < queue.size()) {
        int[] cur = queue.get(head++);
        int r = cur[0], c = cur[1];
        int currentDist = dist[r][c];


        for (int[] nb : neighbors(r, c)) {
            int nr = nb[0], nc = nb[1];
            Color cellColor = cells[idx(nr, nc)];
            int cost;

            if (cellColor == player) cost = 0;
            else if (cellColor == Color.EMPTY) cost = 1;
            else continue;


            if (currentDist + cost < dist[nr][nc]) {
                dist[nr][nc] = currentDist + cost;
                queue.add(new int[]{nr, nc});
            }
        }
    }

    //check target edge
    int best = INF;

    if (player == Color.RED) {
        // bottom row
        for (int col = 0; col < n; col++)
            best = Math.min(best, dist[n - 1][col]);
    } else {
        // right col
        for (int row = 0; row < n; row++)
            best = Math.min(best, dist[row][n - 1]);
    }

    return best;
}


    // Check for win-condition for RED (top ↔ bottom connected)
    public boolean redWins() {
        return uf.find(redTop) == uf.find(redBottom);
    }

    // Check for win-condition for BLACK (left ↔ right connected)
    public boolean blackWins() {
        return uf.find(blackLeft) == uf.find(blackRight);
    }

    // Check if game is over
    public boolean isTerminal() {
        return redWins() || blackWins();
    }

    // All legal cells
    public List<int[]> legalMoves() {
        List<int[]> legalMovesList = new ArrayList<>();
        for (int row = 0; row < n; row++) {
            for (int column = 0; column < n; column++) {
                if (isEmpty(row, column)) {
                    legalMovesList.add(new int[]{row, column});
                }
            }
        }
        return legalMovesList;
    }

    /* Placing stones */

    private void placeStone(int row, int column, Color stone) {
        if (!inBounds(row, column)) {
            throw new IndexOutOfBoundsException();
        }

        int cellIndex = idx(row, column);
        if (cells[cellIndex] != Color.EMPTY) {
            throw new IllegalStateException("Cell not empty");
        }
        cells[cellIndex] = stone;
        List<int[]> list_of_neighbors = neighbors(row, column);
        // Union with same-colored neighbors
        for (var neighbor : list_of_neighbors) {
            int neighborRow    = neighbor[0];
            int neighborColumn = neighbor[1];
            int neighborIndex  = idx(neighborRow, neighborColumn);
            if (inBounds(neighborRow, neighborColumn) && cells[neighborIndex] == stone) {
                uf.union(cellIndex, neighborIndex);
            }
        }

        // Union with virtual edge nodes (for win detection)
        if (stone == Color.RED) {
            if (row == 0) {
                uf.union(cellIndex, redTop);
            }
            if (row == n - 1) {
                uf.union(cellIndex, redBottom);
            }
        } else if (stone == Color.BLACK) {
            if (column == 0) {
                uf.union(cellIndex, blackLeft);
            }
            if (column == n - 1) {
                uf.union(cellIndex, blackRight);
            }
        }
    }

    /** Neighbor coordinates for a pointy-top hex grid laid out as an n×n rhombus. */
    private List<int[]> neighbors(int row, int column) {
        int[][] neighbor_deltas = { {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
        List<int[]> neighbors = new ArrayList<>(6);
        for (var delta : neighbor_deltas) {
            int neighborRow = row + delta[0];
            int neighborColumn = column + delta[1];
            if (inBounds(neighborRow, neighborColumn)) {
                neighbors.add(new int[]{neighborRow, neighborColumn});
            }
        }
        return neighbors;
    }

    // Flatten (row, column) → linear index for the 1D cells array
    private int idx(int row, int column) {
        return row * n + column;
    }

    //Helper for undoing move (removes stone from a cell)
    public void clearCell(int row, int column) {
        if (inBounds(row, column)) {
            cells[idx(row, column)] = Color.EMPTY;
        }
    }

    public void reset() {
        Arrays.fill(cells, Color.EMPTY); // clear stones
        uf.reset();                      // clear connectivity
    }
}

