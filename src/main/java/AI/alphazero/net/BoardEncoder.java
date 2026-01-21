package AI.alphazero.net;

import game.core.Board;
import game.core.Color;

public class BoardEncoder {

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










