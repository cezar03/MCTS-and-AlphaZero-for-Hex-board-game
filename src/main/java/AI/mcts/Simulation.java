package AI.mcts;

import Game.*;
import java.util.*;

public class Simulation {
    private Random random;

    public Simulation() {
        this.random = new Random();
    }

    public Player simulate(Board board, Player currentPlayer) {
        // Create a copy of the board.
        Board simBoard = board.copyBoard(board);
        
        // Get all empty positions
        List<int[]> emptyPositions = simBoard.legalMoves();
        
        // Shuffle the empty positions
        Collections.shuffle(emptyPositions, random);
        
        // Alternate between players and fill positions
        Player currentSimPlayer = currentPlayer;
        for (int[] position : emptyPositions) {
            int row = position[0];
            int col = position[1];
            
            // Place stone for current player
            if (currentSimPlayer == Player.RED) {
                simBoard.getMoveRed(row, col, Color.RED);
            } else {
                simBoard.getMoveBlack(row, col, Color.BLACK);
            }
            
            // Switch player
            currentSimPlayer = currentSimPlayer.other();
        }
        
        // Determine winner using the Union Find algorithm.
        if (simBoard.redWins()) {
            return Player.RED;
        } else if (simBoard.blackWins()) {
            return Player.BLACK;
        }
        
        // This shouldn't happen in Hex (no draws), but handle it
        return null;
    }
}
