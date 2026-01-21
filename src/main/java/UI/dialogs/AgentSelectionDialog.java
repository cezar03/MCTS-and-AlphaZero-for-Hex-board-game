package UI.dialogs;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import AI.registry.AgentRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import UI.view.AppStyles;

/**
 * Dialog for selecting an AI agent type and configuration.
 */
public final class AgentSelectionDialog {
    private AgentSelectionDialog() {}

    public record AgentSelection(String type, int iterations) {}

    /**
     * Displays the agent selection dialog.
     * @param registry the AgentRegistry to retrieve available agent types
     * @return the selected AgentSelection, or null if cancelled
     */
    public static AgentSelection show(AgentRegistry registry) {
        Objects.requireNonNull(registry, "AgentRegistry cannot be null");

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Agent Selection");
        dialog.setHeaderText("Select AI Agent");
        dialog.setContentText("Choose which AI agent to play against:");
        AppStyles.styleDialog(dialog);

        AtomicReference<AgentSelection> result = new AtomicReference<>();

        Button mctsBtn = new Button("MCTS");
        Button randomBtn = new Button("Random");
        Button alphaZeroBtn = new Button("AlphaZero");
        Button cancelBtn = new Button("Cancel");

        mctsBtn.setOnAction(e -> {
            Integer iterations = DifficultyDialog.show();
            if (iterations != null) {
                result.set(new AgentSelection("MCTS", iterations));
            }
            dialog.close();
        });
        randomBtn.setOnAction(e -> { result.set(new AgentSelection("Random", 0)); dialog.close(); });
        alphaZeroBtn.setOnAction(e -> { result.set(new AgentSelection("AlphaZero", 0)); dialog.close(); });
        cancelBtn.setOnAction(e -> dialog.close());

        VBox buttonBox = new VBox(10, mctsBtn, randomBtn, alphaZeroBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(buttonBox);
        dialog.showAndWait();
        return result.get();
    }
}
