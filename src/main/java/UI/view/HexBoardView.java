package UI.view;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;

import UI.controller.GameController;
import bridge.BoardAdapter;
import game.core.Color;
import game.core.Player;

/**
 * JavaFX view component that renders an interactive Hex board.
 *
 * <p>This class is responsible for:</p>
 * <ul>
 *   <li>Drawing a {@code boardSize x boardSize} grid of hexagonal cells</li>
 *   <li>Updating cell fills based on the current {@link BoardAdapter} state</li>
 *   <li>Forwarding mouse clicks on cells to a {@link GameController}</li>
 *   <li>Displaying turn and win information through an attached {@link Label}</li>
 *   <li>Drawing colored border lines indicating the goal edges for each player</li>
 * </ul>
 *
 * <p>Cells are stored as {@link Polygon} nodes in a 2D array ({@code hexCells})
 * so they can be updated efficiently without redrawing the whole board.</p>
 */
public class HexBoardView extends Pane {

    /**
     * Logical board dimension (number of rows/columns).
     */
    private final int boardSize;

    /**
     * Size parameter controlling the rendered hexagon width.
     * Internally, the hexagon radius is computed as {@code hexSize / 2}.
     */
    private final double hexSize;

    /**
     * JavaFX polygons representing the visual hexagonal cells.
     * Indexed as {@code hexCells[row][col]}.
     */
    private final Polygon[][] hexCells;

    /**
     * Controller that receives user interaction events (cell clicks).
     * May be {@code null} if not wired.
     */
    private GameController controller;

    /**
     * Optional UI label used to show turn/win status.
     * May be {@code null} if not set.
     */
    private Label turnLabel;

    /**
     * Creates a Hex board view and immediately draws an empty board.
     *
     * @param boardSize the board dimension (rows and columns)
     * @param hexSize the rendered size of each hex cell (used to derive radius and spacing)
     */
    public HexBoardView(int boardSize, double hexSize) {
        this.boardSize = boardSize;
        this.hexSize = hexSize;
        this.hexCells = new Polygon[boardSize][boardSize];
        drawEmptyBoard();
    }

    /**
     * Sets the controller that will handle user interactions on the board.
     *
     * <p>When a user clicks a cell, {@link GameController#handleCellClick(int, int)}
     * will be called with the clicked {@code (row, col)}.</p>
     *
     * @param controller the controller to notify on cell clicks
     */
    public void setController(GameController controller) { this.controller = controller; }

    /**
     * Sets the label used to display current turn and winner messages.
     *
     * @param turnLabel the label to update; may be {@code null} to disable text updates
     */
    public void setTurnLabel(Label turnLabel) { this.turnLabel = turnLabel; }

    /**
     * Updates the board cell colors from the given adapter.
     *
     * <p>This does not recreate nodes; it only updates fills of the existing
     * polygons for performance.</p>
     *
     * @param adapter the board adapter providing the current cell colors
     */
    public void update(BoardAdapter adapter) {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Color cellColor = adapter.getCell(row, col);
                hexCells[row][col].setFill(paintForColor(cellColor));
            }
        }
    }

    /**
     * Updates the attached label to show which player is currently to move.
     *
     * <p>If no label is attached via {@link #setTurnLabel(Label)}, this method is a no-op.</p>
     *
     * @param currentPlayer the player whose turn it is
     */
    public void updateTurnDisplay(Player currentPlayer) {
        if (turnLabel == null) return;

        String playerName = (currentPlayer == Player.RED) ? "RED" : "BLACK";
        String color = (currentPlayer == Player.RED) ? "#ef4444" : "#000000ff";
        turnLabel.setText("Player " + playerName + " is in turn");
        turnLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 16px;");
    }

    /**
     * Updates the attached label to show the winning player.
     *
     * <p>If no label is attached via {@link #setTurnLabel(Label)}, this method is a no-op.</p>
     *
     * @param winner the player who won the game
     */
    public void updateWinDisplay(Player winner) {
        if (turnLabel == null) return;

        String playerName = (winner == Player.RED) ? "RED" : "BLACK";
        String color = (winner == Player.RED) ? "#ef4444" : "#1f2937";
        turnLabel.setText("Player " + playerName + " has won!");
        turnLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 18px;");
    }

    /**
     * Draws a fresh empty board: clears children, creates hexagon polygons,
     * installs click handlers, and draws border lines.
     */
    private void drawEmptyBoard() {
        getChildren().clear();

        double radius = hexSize / 2;
        double width = hexSize;
        double height = Math.sqrt(3) * radius;

        // Padding ensures the board is not glued to the top-left corner of the Pane.
        double padding = 2 * radius;

        // Create all cell polygons and attach click handlers.
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                // Coordinate layout places hexes in a skewed grid appropriate for Hex.
                double x = padding + col * width * Math.sqrt(3) / 2 + row * (width / 2) * Math.sqrt(3) / 2;
                double y = padding + row * height * Math.sqrt(3) / 2;

                Polygon hex = createHexagon(x, y, radius);
                hexCells[row][col] = hex;

                int finalRow = row;
                int finalCol = col;
                hex.setOnMouseClicked((MouseEvent event) -> {
                    if (controller != null) controller.handleCellClick(finalRow, finalCol);
                });

                getChildren().add(hex);
            }
        }

        // Draw the colored outer borders representing each player's connection goal.
        drawBoardBorders(padding, radius, width, height);
    }

    /**
     * Creates a single flat-topped hexagon polygon centered at {@code (centerX, centerY)}.
     *
     * <p>The vertices are generated in 60-degree steps with a -30 degree offset,
     * producing a flat-topped hex orientation.</p>
     *
     * @param centerX the x-coordinate of the hexagon center
     * @param centerY the y-coordinate of the hexagon center
     * @param radius the distance from center to each vertex
     * @return a configured {@link Polygon} representing one hex cell
     */
    private Polygon createHexagon(double centerX, double centerY, double radius) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            double xPos = centerX + radius * Math.cos(angle);
            double yPos = centerY + radius * Math.sin(angle);
            hex.getPoints().addAll(xPos, yPos);
        }
        hex.setStroke(Paint.valueOf("BLACK"));
        hex.setFill(paintForColor(Color.EMPTY));
        return hex;
    }

    /**
     * Maps game {@link Color} values to JavaFX {@link Paint} objects for rendering.
     *
     * @param color the game color enum value
     * @return the JavaFX paint used to fill the cell
     */
    private Paint paintForColor(Color color) {
        return switch (color) {
            case RED -> Paint.valueOf("RED");
            case BLACK -> Paint.valueOf("BLACK");
            default -> Paint.valueOf("LIGHTGRAY");
        };
    }

    /**
     * Draws the outer boundary lines of the board to visually indicate the goal edges.
     *
     * <p>In Hex, each player aims to connect opposing sides:
     * this method draws two red borders and two black borders to represent those sides.</p>
     *
     * @param padding the padding offset applied to all board coordinates
     * @param radius the hex radius (center to vertex)
     * @param width the nominal hex width (equals {@code hexSize})
     * @param height the nominal hex height component (derived from radius)
     */
    private void drawBoardBorders(double padding, double radius, double width, double height) {

        // ---- TOP BORDER (RED) ----
        Polyline topBorder = new Polyline();
        for (int col = 0; col < boardSize; col++) {
            double x = padding + col * width * Math.sqrt(3) / 2;
            double y = padding;

            double angle1 = Math.toRadians(60 * 4 - 30);
            double xPos1 = x + radius * Math.cos(angle1);
            double yPos1 = y + radius * Math.sin(angle1);
            topBorder.getPoints().addAll(xPos1, yPos1);

            double angle2 = Math.toRadians(60 * 5 - 30);
            double xPos2 = x + radius * Math.cos(angle2);
            double yPos2 = y + radius * Math.sin(angle2);
            topBorder.getPoints().addAll(xPos2, yPos2);

            if (col == boardSize - 1) {
                double angle3 = Math.toRadians(60 * 0 - 30);
                double xPos3 = xPos2 + radius * Math.cos(angle3);
                double yPos3 = y + radius * Math.sin(angle3);
                topBorder.getPoints().addAll(xPos3, yPos3);
            }
        }
        topBorder.setStroke(Paint.valueOf("RED"));
        topBorder.setStrokeWidth(3);
        getChildren().add(topBorder);

        // ---- LEFT BORDER (BLACK) ----
        Polyline leftBorder = new Polyline();
        double x0 = padding;
        double y0 = padding;
        double angle0 = Math.toRadians(60 * 4 - 30);
        double xPos0 = x0 + radius * Math.cos(angle0);
        double yPos0 = y0 + radius * Math.sin(angle0);
        leftBorder.getPoints().addAll(xPos0, yPos0);

        for (int row = 0; row < boardSize; row++) {
            double x = padding + row * (width / 2) * Math.sqrt(3) / 2;
            double y = padding + row * height * Math.sqrt(3) / 2;

            double angle1 = Math.toRadians(60 * 3 - 30);
            double xPos1 = x + radius * Math.cos(angle1);
            double yPos1 = y + radius * Math.sin(angle1);
            leftBorder.getPoints().addAll(xPos1, yPos1);

            double angle2 = Math.toRadians(60 * 2 - 30);
            double xPos2 = x + radius * Math.cos(angle2);
            double yPos2 = y + radius * Math.sin(angle2);
            leftBorder.getPoints().addAll(xPos2, yPos2);
        }
        leftBorder.setStroke(Paint.valueOf("BLACK"));
        leftBorder.setStrokeWidth(3);
        getChildren().add(leftBorder);

        // ---- BOTTOM BORDER (RED) ----
        Polyline bottomBorder = new Polyline();
        for (int col = boardSize - 1; col >= 0; col--) {
            int row = boardSize - 1;
            double x = padding + col * width * Math.sqrt(3) / 2 + row * (width / 2) * Math.sqrt(3) / 2;
            double y = padding + row * height * Math.sqrt(3) / 2;

            double angle1 = Math.toRadians(60 * 1 - 30);
            double xPos1 = x + radius * Math.cos(angle1);
            double yPos1 = y + radius * Math.sin(angle1);
            bottomBorder.getPoints().addAll(xPos1, yPos1);

            double angle2 = Math.toRadians(60 * 2 - 30);
            double xPos2 = x + radius * Math.cos(angle2);
            double yPos2 = y + radius * Math.sin(angle2);
            bottomBorder.getPoints().addAll(xPos2, yPos2);
        }
        bottomBorder.setStroke(Paint.valueOf("RED"));
        bottomBorder.setStrokeWidth(3);
        getChildren().add(bottomBorder);

        // ---- RIGHT BORDER (BLACK) ----
        Polyline rightBorder = new Polyline();
        for (int row = 0; row < boardSize; row++) {
            int col = boardSize - 1;
            double x = padding + col * width * Math.sqrt(3) / 2 + row * (width / 2) * Math.sqrt(3) / 2;
            double y = padding + row * height * Math.sqrt(3) / 2;

            double angle1 = Math.toRadians(60 * 0 - 30);
            double xPos1 = x + radius * Math.cos(angle1);
            double yPos1 = y + radius * Math.sin(angle1);
            rightBorder.getPoints().addAll(xPos1, yPos1);

            double angle2 = Math.toRadians(60 * 1 - 30);
            double xPos2 = x + radius * Math.cos(angle2);
            double yPos2 = y + radius * Math.sin(angle2);
            rightBorder.getPoints().addAll(xPos2, yPos2);
        }
        rightBorder.setStroke(Paint.valueOf("BLACK"));
        rightBorder.setStrokeWidth(3);
        getChildren().add(rightBorder);
    }
}










