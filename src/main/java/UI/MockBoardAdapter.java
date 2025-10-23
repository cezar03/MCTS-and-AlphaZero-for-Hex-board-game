package UI;

import Game.Color;

public class MockBoardAdapter {
    private final Color[][] mockCells;

    public MockBoardAdapter(int size) {
        mockCells = new Color[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                mockCells[i][j] = Color.EMPTY;
            }
        }
    }

    public void setCell(int row, int col, Color color) {
        mockCells[row][col] = color;
    }

    public Color getCellColor(int row, int col) {
        return mockCells[row][col];
    }
}
