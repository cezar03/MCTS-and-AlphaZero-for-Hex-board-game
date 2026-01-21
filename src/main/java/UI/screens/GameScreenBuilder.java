package UI.screens;

import bridge.BoardAdapter;
import game.core.Board;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import UI.controller.GameController;
import UI.session.ScoreBoard;
import UI.view.Buttons;
import UI.view.HexBoardView;

public final class GameScreenBuilder {
    private GameScreenBuilder() {}

    public static GameScreen build(int size,
                                   double hexSize,
                                   ScoreBoard scoreBoard,
                                   Runnable onBackToMenu) {
        Board board = new Board(size);
        BoardAdapter adapter = new BoardAdapter(board);
        HexBoardView boardView = new HexBoardView(size, hexSize);
        GameController controller = new GameController(adapter, boardView);

        Label turnLabel = new Label();
        turnLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label scoreLabel = new Label(scoreBoard.toString());
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        boardView.setTurnLabel(turnLabel);
        boardView.update(adapter);
        boardView.updateTurnDisplay(controller.getCurrentPlayer());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #DEB887, #D2A679, #C8A882);");
        root.setCenter(boardView);

        Button backBtn = Buttons.primary("<- Back to Menu");
        backBtn.setOnAction(e -> {
            controller.removeAllAIAgents();
            onBackToMenu.run();
        });

        HBox topBar = new HBox(12, backBtn, turnLabel, scoreLabel);
        topBar.setPadding(new Insets(12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        return new GameScreen(root, controller, scoreLabel);
    }

    public record GameScreen(Parent root, GameController controller, Label scoreLabel) {}
}
