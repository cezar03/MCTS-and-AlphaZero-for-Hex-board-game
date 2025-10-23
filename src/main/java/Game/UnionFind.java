package Game;

import java.util.Arrays;

public class UnionFind {
    private int[] parent; // Parent[i] points to the parent of i, or to itself if i is the root.
    private int[] rank; // rank[i] is an estimate of the tree height for root i.

    // Constructor to initialize Union-Find structure.
    public UnionFind(int size) { // size is the total number of places on the board.
        parent = new int[size];
        rank = new int[size];
        // Initially, every node is its own parent, and ranks are zero.
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    // FIND: returns the root of the set containing x
    // with path compression to make future finds faster
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Recursively find the root and compress the path
        }
        return parent[x];
    }

    // UNION: merges the sets containing a and b
    public void union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) return; // They are already in the same set

        // Union by rank. A smaller tree gets attached to a larger tree.
        if (rank[rootA] < rank[rootB]) {
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            parent[rootB] = rootA;
        } else {
            parent[rootB] = rootA;
            rank[rootA]++;
        }
    }

    // CONNECTED: checks if a and b are in the same set. If that is the case, the method will return True.
    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    public void reset() {
        Arrays.fill(rank, 0);
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }
    }
}

