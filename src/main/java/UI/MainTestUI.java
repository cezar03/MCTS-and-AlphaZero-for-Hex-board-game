package UI;

import Game.Color;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainTestUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        int boardSize = 11;
        double hexSize = 60;

        // Create your BoardView
        BoardView boardView = new BoardView(boardSize, hexSize);

        // (Optional) Create a mock adapter that pretends to color some cells
        MockBoardAdapter mockAdapter = new MockBoardAdapter(boardSize);
        mockAdapter.setCell(2, 2, Color.RED);
        mockAdapter.setCell(1, 3, Color.BLACK);
        
        // (Optional) Create a mock controller to print clicks
        boardView.setController((row, col) -> {
            System.out.println("Clicked on: (" + row + ", " + col + ")");
            mockAdapter.setCell(row, col, Color.RED);
            boardView.update(mockAdapter);
        });

        

        boardView.update(mockAdapter);

        // Create a scene and display
        Scene scene = new Scene(boardView, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hex Board Test");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
