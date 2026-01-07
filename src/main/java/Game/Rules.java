package Game;

import AI.AiPlayer.AIBoardAdapter;

public final class Rules {
    private Rules() {}

    /* Valid cell: inside bounds. (All n×n cells are playable in Hex.) */
    public static boolean validCell(Board b, int row, int col) {
        return b.inBounds(row, col);
    }

    /* Valid move: inside bounds and EMPTY. */
    public static boolean validMove(Board b, int row, int col) {
        return validCell(b, row, col) && b.isEmpty(row, col);
    }
    
    /* Valid cell: inside bounds. (AIBoardAdapter version) */
    public static boolean validCell(AIBoardAdapter b, int row, int col) {
        return b.inBounds(row, col);
    }

    /* Valid move: inside bounds and EMPTY. (AIBoardAdapter version) */
    public static boolean validMove(AIBoardAdapter b, int row, int col) {
        return validCell(b, row, col) && b.isEmpty(row, col);
    }

    /* Pie rule hook if you want it later. */
    public static boolean pieRuleAvailable(int plyCount, Player toMove) {
        return plyCount <= 1;
    }
}

