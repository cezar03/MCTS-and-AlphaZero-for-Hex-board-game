package AI.alphazero.net;

import game.core.Board;
import game.core.Color;

public class BoardEncoder {

    public static float[] encode(Board board, Color currentPlayer) {
        int size = board.getSize();
        int planeSize = size * size;
        
        // Allocate flat array: 3 planes * width * height
        float[] flatData = new float[3 * planeSize];

        int offsetRed = 0;
        int offsetBlack = planeSize;
        int offsetTurn = 2 * planeSize;

        // Fill array using standard Java loops (Extremely fast L1 cache access)
        if (currentPlayer == Color.RED) {
            // RED Perspective: Standard
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    Color cell = board.getCell(row, col);
                    int idx = row * size + col;

                    if (cell == Color.RED) {
                        flatData[offsetRed + idx] = 1.0f;
                    } else if (cell == Color.BLACK) {
                        flatData[offsetBlack + idx] = 1.0f;
                    }
                    flatData[offsetTurn + idx] = 1.0f; // Always 1.0 for "Current" (Red)
                }
            }
        } else {
            // BLACK Perspective: Canonicalize to RED
            // Transpose board: (row, col) -> (col, row)
            // Swap colors: My Stone (Black) -> Plane 0 (Red), Opponent (Red) -> Plane 1 (Black)
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    Color cell = board.getCell(row, col);
                    
                    // Transposed index for the output buffer
                    // We are mapping board(row,col) to canonical(col,row)
                    // So we write to idx = col * size + row
                    int idx = col * size + row;

                    if (cell == Color.BLACK) {
                        // My stone (Black) becomes "Red" (Plane 0) in canonical view
                        flatData[offsetRed + idx] = 1.0f;
                    } else if (cell == Color.RED) {
                        // Opponent (Red) becomes "Black" (Plane 1) in canonical view
                        flatData[offsetBlack + idx] = 1.0f;
                    }
                    flatData[offsetTurn + idx] = 1.0f; // Always 1.0 for "Current"
                }
            }
        }
        
        return flatData;
    }
}










