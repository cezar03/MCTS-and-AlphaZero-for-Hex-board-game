package game.core;

/**
 * Represents the possible states of a cell on the Hex game board.
 * <p>
 * In the game of Hex, each cell can be empty or occupied by one of two players'
 * stones (RED or BLACK). This enum is used throughout the game logic to represent
 * and check cell states.
 */
public enum Color {
    EMPTY, /** Represents an unoccupied cell on the board. */
    RED, /** Represents a cell occupied by the Red player (connects Top to Bottom). */
    BLACK /** Represents a cell occupied by the Black player (connects Left to Right). */
}











