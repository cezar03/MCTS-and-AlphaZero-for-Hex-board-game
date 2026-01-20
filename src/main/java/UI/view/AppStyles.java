package ui.view;

import java.util.Objects;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

public final class AppStyles {
    private AppStyles() {}

    public static void apply(Scene scene) {
        scene.getStylesheets().add(
                Objects.requireNonNull(AppStyles.class.getResource("/app.css")).toExternalForm()
        );
    }

    public static void styleDialog(Alert alert) {
        styleDialog(alert.getDialogPane());
    }

    public static void styleDialog(DialogPane pane) {
        pane.getStylesheets().add(
                Objects.requireNonNull(AppStyles.class.getResource("/app.css")).toExternalForm()
        );
        pane.getStyleClass().add("themed-dialog");
    }
}
