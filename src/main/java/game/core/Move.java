package game.core;

import java.util.Objects;

public class Move {
    public final int row;
    public final int col;

    private static final Move[][] CACHE = new Move[20][20];

    static {
        for (int r = 0; r < 20; r++) {
            for (int c = 0; c < 20; c++) { CACHE[r][c] = new Move(r, c);}
        }
    }

    public static Move get(int row, int col) {
        if (row >= 0 && row < 20 && col >= 0 && col < 20) { return CACHE[row][col];}
        return new Move(row, col);
    }
    
    private Move(int row, int col){
        this.row = row;
        this.col = col;
    }

    public String getCoordinate(){
        return "(" + row + "," + col + ")";
    }

    @Override
    public String toString(){
        return getCoordinate();
    }
    
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move m = (Move) o;
        return row == m.row && col == m.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}










