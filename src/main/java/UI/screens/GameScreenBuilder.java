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

/**
 * Builder for the Game screen.
 */
public final class GameScreenBuilder {
    private GameScreenBuilder() {}

    /**
     * Builds the Game screen.
     *
     * @param size         The size of the board.
     * @param hexSize      The size of each hexagon.
     * @param scoreBoard   The scoreboard to display scores.
     * @param onBackToMenu The handler to invoke when returning to the main menu.
     * @return The constructed GameScreen.
     */
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

    /**
     * Container for the Game screen components.
     *
     * @param root       The root node of the screen.
     * @param controller The game controller.
     * @param scoreLabel The label displaying the score.
     */
    public record GameScreen(Parent root, GameController controller, Label scoreLabel) {}
}
