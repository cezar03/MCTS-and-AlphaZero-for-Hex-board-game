package UI;

import Game.Board;
import UI.MockBoardAdapter;
import UI.GameController;
import Game.Color;
import javafx.scene.shape.Line;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.paint.Paint;
import javafx.scene.input.MouseEvent;

public class BoardView extends Pane{
    private final int boardSize; // Number of hexagons along one edge of the board
    private final double hexSize; // Size of each hexagon
    private final Polygon[][] hexCells; // 2D array to hold hexagon shapes
    private GameController controller; // reference for click events.

    public BoardView(int boardSize, double hexSize) {
        this.boardSize = boardSize;
        this.hexSize = hexSize;
        this.hexCells = new Polygon[boardSize][boardSize];
        drawEmptyBoard(); // Draw the initial empty board
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public void update(MockBoardAdapter adapter){
        for(int row = 0; row < boardSize; row++){
            for(int col = 0; col < boardSize; col++){
                Color cellColor = adapter.getCellColor(row, col);
                hexCells[row][col].setFill(paintForColor(cellColor));
            }
        }
    }

    // Helper methods

    private void drawEmptyBoard(){
        getChildren().clear();
        double radius = hexSize / 2;
        double width = hexSize;
        double height = Math.sqrt(3) * radius; // Height of one hexagon
        double padding = 2*radius; // Padding from the edges

        for(int row = 0; row < boardSize; row++){
            for(int col = 0; col < boardSize; col++){
                double x = padding + col * width * Math.sqrt(3)/2 + row * (width/2)*Math.sqrt(3)/2;
                double y = padding + row * height*Math.sqrt(3)/2;

                Polygon hex = createHexagon(x, y, radius); // Create hexagon centered at (x, y)
                hexCells[row][col] = hex;

                int finalRow = row;
                int finalCol = col;
                hex.setOnMouseClicked((MouseEvent event) -> {
                    if(controller != null){
                        controller.handleCellClick(finalRow, finalCol);

                    }
                });

                getChildren().add(hex);
            }
        }
        drawBoardBorders(padding, radius, width, height);
    }

    private Polygon createHexagon(double centerX, double centerY, double radius){
        Polygon hex = new Polygon();
        for(int i = 0; i < 6; i++){
            double angle = Math.toRadians(60 * i - 30); // Pointy-top hexagon
            double xPos = centerX + radius * Math.cos(angle);
            double yPos = centerY + radius * Math.sin(angle);
            hex.getPoints().addAll(xPos, yPos);
        }
        hex.setStroke(Paint.valueOf("BLACK"));
        hex.setFill(paintForColor(Color.EMPTY));
        return hex;
    }

    private Paint paintForColor(Color color){
        switch(color){
            case RED:
                return Paint.valueOf("RED");
            case BLACK:
                return Paint.valueOf("BLACK");
            default:
                return Paint.valueOf("LIGHTGRAY");
        }
    }

    private void drawBoardBorders(double padding, double radius, double width, double height) {
        // Top border (horizontal edge at the top)
        Polyline topBorder = new Polyline();
        for (int col = 0; col < boardSize; col++) {
            double x = padding + col * width * Math.sqrt(3) / 2;
            double y = padding;
            
            // Add the top-left vertex of each hexagon along the top edge
            double angle1 = Math.toRadians(60 * 4 - 30); // 210 degrees
            double xPos1 = x + radius * Math.cos(angle1);
            double yPos1 = y + radius * Math.sin(angle1);
            topBorder.getPoints().addAll(xPos1, yPos1);
            
            // Add the top-right vertex
            double angle2 = Math.toRadians(60 * 5 - 30); // 270 degrees
            double xPos2 = x + radius * Math.cos(angle2);
            double yPos2 = y + radius * Math.sin(angle2);
            topBorder.getPoints().addAll(xPos2, yPos2);

            if(col == boardSize - 1) {
                // Add the last top-right vertex of the last hexagon
                double angle3 = Math.toRadians(60 * 0 - 30); // -30 degrees (330)
                double xPos3 = xPos2 + radius * Math.cos(angle3);
                double yPos3 = y + radius * Math.sin(angle3);
                topBorder.getPoints().addAll(xPos3, yPos3);
            }
        }
        topBorder.setStroke(Paint.valueOf("RED"));
        topBorder.setStrokeWidth(3);
        getChildren().add(topBorder);
        
        // Left border (diagonal edge on the left)
        Polyline leftBorder = new Polyline();

        // Start with the top-left vertex of the first hexagon (90 degrees)
        double x0 = padding;
        double y0 = padding;
        double angle0 = Math.toRadians(60 * 4 - 30); // 210 degrees
        double xPos0 = x0 + radius*Math.cos(angle0);
        double yPos0 = y0 + radius*Math.sin(angle0);
        leftBorder.getPoints().addAll(xPos0, yPos0);

        // Now add the left vertex (150 degrees) for each row
        for (int row = 0; row < boardSize; row++) {
            double x = padding + row * (width / 2) * Math.sqrt(3) / 2;
            double y = padding + row * height * Math.sqrt(3) / 2;
            
            // Add the left vertex of each hexagon along the left edge
            double angle1 = Math.toRadians(60 * 3 - 30); // 150 degrees
            double xPos1 = x + radius * Math.cos(angle1);
            double yPos1 = y + radius * Math.sin(angle1);
            leftBorder.getPoints().addAll(xPos1, yPos1);
            
            // Add the bottom-left vertex
            double angle2 = Math.toRadians(60 * 2 - 30); // 90 degrees
            double xPos2 = x + radius * Math.cos(angle2);
            double yPos2 = y + radius * Math.sin(angle2);
            leftBorder.getPoints().addAll(xPos2, yPos2);
        }

        leftBorder.setStroke(Paint.valueOf("BLACK"));
        leftBorder.setStrokeWidth(3);
        getChildren().add(leftBorder);
        
        // Bottom border (horizontal edge at the bottom)
        Polyline bottomBorder = new Polyline();
        for (int col = boardSize - 1; col >= 0; col--) {
            int row = boardSize - 1;
            double x = padding + col * width * Math.sqrt(3) / 2 + row * (width / 2) * Math.sqrt(3) / 2;
            double y = padding + row * height * Math.sqrt(3) / 2;
            
            // Add the bottom-right vertex
            double angle1 = Math.toRadians(60 * 1 - 30); // 30 degrees
            double xPos1 = x + radius * Math.cos(angle1);
            double yPos1 = y + radius * Math.sin(angle1);
            bottomBorder.getPoints().addAll(xPos1, yPos1);
            
            // Add the bottom-left vertex
            double angle2 = Math.toRadians(60 * 2 - 30); // 90 degrees
            double xPos2 = x + radius * Math.cos(angle2);
            double yPos2 = y + radius * Math.sin(angle2);
            bottomBorder.getPoints().addAll(xPos2, yPos2);
        }
        bottomBorder.setStroke(Paint.valueOf("RED"));
        bottomBorder.setStrokeWidth(3);
        getChildren().add(bottomBorder);
        
        // Right border (diagonal edge on the right)
        Polyline rightBorder = new Polyline();

        // Now add vertices for each row going down
        for (int row = 0; row < boardSize; row++) {
            int col = boardSize - 1;
            double x = padding + col * width * Math.sqrt(3) / 2 + row * (width / 2) * Math.sqrt(3) / 2;
            double y = padding + row * height * Math.sqrt(3) / 2;
            
            // Add the right vertex
            double angle1 = Math.toRadians(60 * 0 - 30); // -30 degrees (330)
            double xPos1 = x + radius * Math.cos(angle1);
            double yPos1 = y + radius * Math.sin(angle1);
            rightBorder.getPoints().addAll(xPos1, yPos1);
            
            // Add the bottom-right vertex
            double angle2 = Math.toRadians(60 * 1 - 30); // 30 degrees
            double xPos2 = x + radius * Math.cos(angle2);
            double yPos2 = y + radius * Math.sin(angle2);
            rightBorder.getPoints().addAll(xPos2, yPos2);
        }

        rightBorder.setStroke(Paint.valueOf("BLACK"));
        rightBorder.setStrokeWidth(3);
        getChildren().add(rightBorder);
    }
}
