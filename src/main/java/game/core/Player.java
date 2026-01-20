package game.core;

/**
 * Represents the two players in a Hex game, along with their associated
 * identifiers and stone colors.
 * <p>
 * Each player has a unique integer ID (1 for RED, 2 for BLACK) and a corresponding
 * stone color. This enum provides convenient methods for player operations such as
 * switching turns and converting between ID and Player representations.
 */
public enum Player {
    /** The red player, with ID 1 and red-colored stones. */
    RED(1, Color.RED),
    /** The black player, with ID 2 and black-colored stones. */
    BLACK(2, Color.BLACK);

    /** The numeric identifier for this player (1 for RED, 2 for BLACK). */
    public final int id;

    /** The color of stones placed by this player. */
    public final Color stone;

    /**
     * Constructs a Player enum constant with the specified ID and stone color.
     * 
     * @param id the numeric identifier for the player
     * @param stone the color of stones this player uses
     */
    Player(int id, Color stone) { this.id = id; this.stone = stone; }

    /**
     * Returns the opponent of this player.
     * 
     * @return BLACK if this player is RED, or RED if this player is BLACK
     */
    public Player other() { return this == RED ? BLACK : RED; }

    /**
     * Converts a numeric player ID to its corresponding Player enum constant.
     * 
     * @param id the player ID (1 for RED, 2 for BLACK)
     * @return the Player corresponding to the given ID
     */
    public static Player fromId(int id) { return id == 1 ? RED : BLACK; }
}













