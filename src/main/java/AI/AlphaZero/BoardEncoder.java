package AI.AlphaZero;

import Game.Board;
import Game.Color;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Encodes Hex board states into a tensor format suitable for neural network input.
 * The encoding uses a 3-plane representation to capture all relevant game state information.
 * 
 * <p>Plane representation:
 * <ul>
 *   <li><strong>Plane 0:</strong> Red piece positions (1.0 where Red has a piece, 0.0 elsewhere)</li>
 *   <li><strong>Plane 1:</strong> Black piece positions (1.0 where Black has a piece, 0.0 elsewhere)</li>
 *   <li><strong>Plane 2:</strong> Current player indicator (1.0 everywhere if Red to move, 0.0 if Black to move)</li>
 * </ul>
 * 
 * <p>This representation allows the neural network to:
 * <ul>
 *   <li>Identify piece positions for both players</li>
 *   <li>Determine whose turn it is</li>
 *   <li>Process spatial patterns using convolutional layers</li>
 * </ul>
*/
public class BoardEncoder {
    
    /**
     * Converts a Hex board and current player into a 3-plane tensor representation.
     * 
     * <p>The output tensor has shape [1, 3, boardSize, boardSize] where:
     * <ul>
     *   <li>Dimension 0: Batch size (always 1 for single board encoding)</li>
     *   <li>Dimension 1: Channels (3 planes as described above)</li>
     *   <li>Dimensions 2-3: Spatial dimensions matching the board size</li>
     * </ul>
     * 
     * @param board the current Hex board state to encode
     * @param currentPlayer the player whose turn it is (Color.RED or Color.BLACK)
     * @return an INDArray tensor of shape [1, 3, boardSize, boardSize] representing the encoded board
    */
    public static INDArray encode(Board board, Color currentPlayer) {
        int size = board.getSize();

        // Shape: [BatchSize, Channels, Height, Width] -> [1, 3, size, size]
        // BatchSize is 1 since we are encoding a single board state.
        // Channels is 3 for the three planes described above.
        // Height and Width are both equal to the board size.
        INDArray convertedBoard = Nd4j.zeros(1, 3, size, size);

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Color cell = board.getCell(row, col); // Get the color of the cell at (row, col)

                if (cell == Color.RED) {
                    // Batch is 0 since we only encode 1 board state, and plane 0 indicates Red stones.
                    convertedBoard.putScalar(0, 0, row, col, 1.0);
                } else if (cell == Color.BLACK) {
                    // Plane 1 indicates Black stones.
                    convertedBoard.putScalar(0, 1, row, col, 1.0);
                }
                
                // Plane 2 indicates whose turn it is
                // If it is RED's turn, fill the whole plane with 1s.
                double turnValue = (currentPlayer == Color.RED) ? 1.0 : 0.0;
                convertedBoard.putScalar(0, 2, row, col, turnValue);
            }
        }
        return convertedBoard;
    }
}
