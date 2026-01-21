package game.core;

import java.util.Objects;

/**
 * An immutable representation of a move coordinate (row, column) in the game.
 * <p>
 * This class utilizes a cache for commonly used coordinates (0-19) to reduce 
 * memory allocation overhead during intensive operations like AI search.
 */
public class Move {
    public final int row;
    public final int col;

    private static final Move[][] CACHE = new Move[20][20];

    static {
        for (int r = 0; r < 20; r++) {
            for (int c = 0; c < 20; c++) { CACHE[r][c] = new Move(r, c);}
        }
    }

    /**
     * Factory method to obtain a Move instance.
     * <p>
     * Returns a cached instance if the coordinates are within the range [0, 19],
     * otherwise creates a new instance.
     * * @param row the row coordinate
     * @param col the column coordinate
     * @return a Move object corresponding to the coordinates
     */
    public static Move get(int row, int col) {
        if (row >= 0 && row < 20 && col >= 0 && col < 20) { return CACHE[row][col];}
        return new Move(row, col);
    }
    
    /*
     * Private constructor to enforce use of the factory method.
     * @param row the row coordinate
     * @param col the column coordinate
     */
    private Move(int row, int col){
        this.row = row;
        this.col = col;
    }

    /**
     * Returns a string representation of the coordinates in the format "(row,col)".
     * * @return the coordinate string
     */
    public String getCoordinate(){
        return "(" + row + "," + col + ")";
    }

    /**
     * Returns the string representation of this move.
     * * @return same as {@link #getCoordinate()}
     */
    @Override
    public String toString(){
        return getCoordinate();
    }
    
    /**
     * Checks for equality based on row and column indices.
     * * @param o the object to compare
     * @return true if the object is a Move with the same coordinates
     */
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move m = (Move) o;
        return row == m.row && col == m.col;
    }

    /**
     * Generates a hash code based on the row and column.
     * * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}










