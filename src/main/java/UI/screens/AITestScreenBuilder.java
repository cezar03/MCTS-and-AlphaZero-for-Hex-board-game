package ui.screens;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ai.registry.AgentRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ui.view.AppStyles;

public final class AITestScreenBuilder {
    private AITestScreenBuilder() {}

    public record AITestSelection(String redType, String blackType) {}

    public static void showDialog(AgentRegistry registry, Consumer<AITestSelection> onStart) {
        Objects.requireNonNull(registry, "AgentRegistry cannot be null");
        Objects.requireNonNull(onStart, "Start handler cannot be null");

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("AI Testing");
        dialog.setHeaderText("AI Agent Testing");
        dialog.setContentText("Choose which agents to test:");
        AppStyles.styleDialog(dialog);

        List<String> types = registry.getAgentTypes();
        String defaultRed = types.contains("MCTS") ? "MCTS" : types.get(0);
        String defaultBlack = types.contains("Random") ? "Random" : types.get(0);

        Label redLabel = new Label("Red Player:");
        ComboBox<String> redCombo = new ComboBox<>();
        redCombo.getItems().addAll(types);
        redCombo.setValue(defaultRed);

        Label blackLabel = new Label("Black Player:");
        ComboBox<String> blackCombo = new ComboBox<>();
        blackCombo.getItems().addAll(types);
        blackCombo.setValue(defaultBlack);

        Button startBtn = new Button("Start Game");
        Button cancelBtn = new Button("Cancel");

        startBtn.setOnAction(e -> {
            dialog.close();
            String redType = redCombo.getValue();
            String blackType = blackCombo.getValue();
            if (redType != null && blackType != null) {
                onStart.accept(new AITestSelection(redType, blackType));
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        VBox content = new VBox(10, redLabel, redCombo, blackLabel, blackCombo, startBtn, cancelBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
}
