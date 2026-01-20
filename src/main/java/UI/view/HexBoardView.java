package ui.view;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;

import bridge.BoardAdapter;
import game.core.Color;
import game.core.Player;
import ui.controller.GameController;

public class HexBoardView extends Pane {
    private final int boardSize;
    private final double hexSize;
    private final Polygon[][] hexCells;

    private GameController controller;
    private Label turnLabel;

    public HexBoardView(int boardSize, double hexSize) {
        this.boardSize = boardSize;
        this.hexSize = hexSize;
        this.hexCells = new Polygon[boardSize][boardSize];
        drawEmptyBoard();
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public void setTurnLabel(Label turnLabel) {
        this.turnLabel = turnLabel;
    }

    public void update(BoardAdapter adapter) {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Color cellColor = adapter.getCell(row, col); // FIX: was getCellColor
                hexCells[row][col].setFill(paintForColor(cellColor));
            }
        }
    }

    public void updateTurnDisplay(Player currentPlayer) {
        if (turnLabel == null) return;

        String playerName = (currentPlayer == Player.RED) ? "RED" : "BLACK";
        String color = (currentPlayer == Player.RED) ? "#ef4444" : "#000000ff";
        turnLabel.setText("Player " + playerName + " is in turn");
        turnLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 16px;");
    }

    public void updateWinDisplay(Player winner) {
        if (turnLabel == null) return;

        String playerName = (winner == Player.RED) ? "RED" : "BLACK";
        String color = (winner == Player.RED) ? "#ef4444" : "#1f2937";
        turnLabel.setText("Player " + playerName + " has won!");
        turnLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 18px;");
    }

    private void drawEmptyBoard() {
        getChildren().clear();

        double radius = hexSize / 2;
        double width = hexSize;
        double height = Math.sqrt(3) * radius;
        double padding = 2 * radius;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
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

        drawBoardBorders(padding, radius, width, height);
    }

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

    private Paint paintForColor(Color color) {
        return switch (color) {
            case RED -> Paint.valueOf("RED");
            case BLACK -> Paint.valueOf("BLACK");
            default -> Paint.valueOf("LIGHTGRAY");
        };
    }

    private void drawBoardBorders(double padding, double radius, double width, double height) {
        // Top border
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

        // Left border
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

        // Bottom border
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

        // Right border
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











