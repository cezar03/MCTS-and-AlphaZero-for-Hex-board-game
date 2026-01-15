package AI.mcts.HexGame;

import java.util.Objects;

/**
 * Represents a move in the Hex game, defined by row and column coordinates.
 * This class is immutable and provides proper equals, hashCode, and toString implementations
 * for use in collections and maps.
*/
public class Move {
    public final int row;
    public final int col;

    /**
     * Constructs a Move with the specified row and column coordinates.
     *
     * @param row the row position of the move
     * @param col the column position of the move
    */
    public Move(int row, int col){
        this.row = row;
        this.col = col;
    }

    /**
     * Returns a string representation of the move's coordinates in the format "(row,col)".
     *
     * @return a string containing the coordinate representation
    */
    public String getCoordinate(){
        return "(" + row + "," + col + ")";
    }

    /**
     * Compares this move to another object for equality.
     * Two moves are equal if they have the same row and column values.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o){
        if (this == o) {
            return true;
        }
        if (!(o instanceof Move)) {
            return false;
        }
        Move m = (Move) o;
        return row == m.row && col == m.col;
    }

    /**
     * Generates a hash code for this move based on its row and column values.
     *
     * @return the hash code value for this move
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Returns a string representation of this move using its coordinates.
     *
     * @return a string representation of the move
     */
    @Override
    public String toString(){
        return getCoordinate();
    }
}
