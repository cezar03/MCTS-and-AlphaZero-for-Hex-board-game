package AI.alphazero.net;

import game.core.Board;
import game.core.Color;

/**
 * Utility for converting game board states into neural network input tensors.
 * <p>
 * The encoding uses a multi-plane representation (typically 3 planes):
 * <ol>
 * <li>Positions occupied by the current player.</li>
 * <li>Positions occupied by the opponent.</li>
 * <li>A constant plane indicating the current player (helps the network distinguish turns).</li>
 * </ol>
 * It also handles canonicalization, ensuring the network always perceives the game
 * from the perspective of the "current" player (e.g., by rotating/swapping colors for Black).
 */
public class BoardEncoder {

    /**
     * Encodes the board state into a flattened float array suitable for the neural network.
     * * @param board the current board
     * @param currentPlayer the player whose perspective is being encoded
     * @return a flattened float array representing the 3-plane input volume
     */
    public static float[] encode(Board board, Color currentPlayer) {
        int size = board.getSize();
        int planeSize = size * size;
        float[] flatData = new float[3 * planeSize];
        int offsetRed = 0;
        int offsetBlack = planeSize;
        int offsetTurn = 2 * planeSize;

        if (currentPlayer == Color.RED) {
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    Color cell = board.getCell(row, col);
                    int idx = row * size + col;

                    if (cell == Color.RED) {
                        flatData[offsetRed + idx] = 1.0f;
                    } else if (cell == Color.BLACK) {
                        flatData[offsetBlack + idx] = 1.0f;
                    }
                    flatData[offsetTurn + idx] = 1.0f;
                }
            }
        } else {
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    Color cell = board.getCell(row, col);
                    int idx = col * size + row;

                    if (cell == Color.BLACK) {
                        flatData[offsetRed + idx] = 1.0f;
                    } else if (cell == Color.RED) {
                        flatData[offsetBlack + idx] = 1.0f;
                    }
                    flatData[offsetTurn + idx] = 1.0f;
                }
            }
        }
        
        return flatData;
    }
}










