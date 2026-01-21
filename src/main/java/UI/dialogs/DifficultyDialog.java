package UI.dialogs;

import java.util.concurrent.atomic.AtomicReference;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import UI.config.UiDefaults;
import UI.view.AppStyles;

/**
 * Dialog for selecting the difficulty level for MCTS AI.
 */
public final class DifficultyDialog {
    private DifficultyDialog() {}

    /**
     * Displays the difficulty selection dialog.
     * @return the number of iterations for the selected difficulty, or null if cancelled
     */
    public static Integer show() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("AI Difficulty");
        dialog.setHeaderText("Select Difficulty Level");
        dialog.setContentText("Choose how strong the MCTS AI opponent should be:");
        AppStyles.styleDialog(dialog);

        AtomicReference<Integer> result = new AtomicReference<>();

        Button easyBtn = new Button("Easy (" + UiDefaults.MCTS_EASY_ITERATIONS + " iterations)");
        Button mediumBtn = new Button("Medium (" + UiDefaults.MCTS_MEDIUM_ITERATIONS + " iterations)");
        Button hardBtn = new Button("Hard (" + UiDefaults.MCTS_HARD_ITERATIONS + " iterations)");
        Button expertBtn = new Button("Expert (" + UiDefaults.MCTS_EXPERT_ITERATIONS + " iterations)");
        Button cancelBtn = new Button("Cancel");

        easyBtn.setOnAction(e -> { result.set(UiDefaults.MCTS_EASY_ITERATIONS); dialog.close(); });
        mediumBtn.setOnAction(e -> { result.set(UiDefaults.MCTS_MEDIUM_ITERATIONS); dialog.close(); });
        hardBtn.setOnAction(e -> { result.set(UiDefaults.MCTS_HARD_ITERATIONS); dialog.close(); });
        expertBtn.setOnAction(e -> { result.set(UiDefaults.MCTS_EXPERT_ITERATIONS); dialog.close(); });
        cancelBtn.setOnAction(e -> dialog.close());

        VBox buttonBox = new VBox(10, easyBtn, mediumBtn, hardBtn, expertBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(buttonBox);
        dialog.showAndWait();
        return result.get();
    }
}
