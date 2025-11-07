import java.util.*;
public class GameState { // just skeleton to run the selection as well
    private boolean terminal = false; // to indicate if the game is over or not

    // getter 
    public boolean isTerminal(){
        return terminal; 
    }

    // methods
    public void doMove(Move move) {
    } // in the real game this should update the board and check for the wins

    public List<Move> getLegalMoves() {
        return new ArrayList<>();
    }
}
