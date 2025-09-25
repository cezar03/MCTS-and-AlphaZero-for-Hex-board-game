package UI;

import java.util.Objects;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class NavigationService {
    private final Stage stage;

    public NavigationService(Stage stage) {
        this.stage = stage;
    }

    //Builds the menu
    public void showMenu() {
        Parent root = MainMenu.createRoot(this);
        Scene scene = new Scene(root, 720, 480);
        attachCss(scene);
        stage.setTitle("Connections — Main Menu");
        stage.setScene(scene);
    }

    //GAMBLIIIIIIIIIIIIING
    public void showSkins() {
        Parent root = new CaseOpeningView().createContent();
        Scene scene = new Scene(root, 832, 400);
        attachCss(scene);
        stage.setTitle("Case Opening");
        stage.setScene(scene);
    }

    //About button
    public void info(String title, String header, String content) {
        Alert a = new Alert(AlertType.INFORMATION);
        a.initOwner(stage);
        a.initModality(Modality.WINDOW_MODAL);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        styleDialog(a);
        a.showAndWait();
    }

    //CSS methods that helps to style our UI
    private void attachCss(Scene scene) {
        scene.getStylesheets().add(
            Objects.requireNonNull(MainMenu.class.getResource("/app.css")).toExternalForm()
        );
    }

    private void styleDialog(Alert a) {
        var dp = a.getDialogPane();
        dp.getStylesheets().add(
            Objects.requireNonNull(MainMenu.class.getResource("/app.css")).toExternalForm()
        );
        dp.getStyleClass().add("themed-dialog");
    }
}
