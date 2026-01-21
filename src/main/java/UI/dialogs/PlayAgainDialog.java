package UI.dialogs;

import game.core.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import UI.session.ScoreBoard;
import UI.view.AppStyles;
import UI.view.Buttons;

/**
 * Dialog that appears after a game ends, showing the winner, score, and options to play again or return to menu.
 * @author Team 04
 */
public class PlayAgainDialog {
    private Stage dialogStage;
    private boolean playAgain = false;

    /**
     * Creates and displays a Play Again dialog.
     * @param winner The player who won the last game
     * @param scoreBoard The current scoreboard with win counts
     * @param onPlayAgain Callback when "Play Again" is clicked
     * @param onBackToMenu Callback when "Back to Menu" is clicked
     */
    public static void showDialog(Player winner, ScoreBoard scoreBoard, 
                                  Runnable onPlayAgain, Runnable onBackToMenu) {
        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle("Game Over");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(false);
        dialogStage.setAlwaysOnTop(true);

        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555; -fx-border-width: 2;");

        // Winner announcement
        String winnerName = (winner == Player.RED) ? "RED" : "BLACK";
        String winnerColor = (winner == Player.RED) ? "#ef4444" : "#1f2937";
        Label winnerLabel = new Label("Player " + winnerName + " Wins!");
        winnerLabel.setStyle("-fx-text-fill: " + winnerColor + "; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Scoreboard display
        Label scoreLabel = new Label(scoreBoard.toString());
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Buttons
        Button playAgainBtn = Buttons.primary("Play Again");
        playAgainBtn.setOnAction(e -> {
            dialogStage.close();
            onPlayAgain.run(); // callback to start a new game controller.resetForNewGame()
        });

        Button backToMenuBtn = Buttons.primary("Back to Menu");
        backToMenuBtn.setOnAction(e -> {
            dialogStage.close();
            onBackToMenu.run(); // I found this cool feature which calls controller.removeAllAIAgents() through lambda to runnable objects really cool
        });

        // Add all elements
        content.getChildren().addAll(
            winnerLabel,
            scoreLabel,
            playAgainBtn,
            backToMenuBtn
        );

        Scene scene = new Scene(content, 350, 250);
        AppStyles.apply(scene);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
}











