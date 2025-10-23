package UI;

import java.util.Objects;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import Game.Board;
import Game.BoardAdapter;

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

    public void showGame(int size, double hexSize) {
        Board board = new Board(size);
        BoardAdapter adapter = new BoardAdapter(board);
        BoardView boardView = new BoardView(size, hexSize);
        GameController controller = new GameController(adapter, boardView);
        boardView.setController(controller); // (controller also sets itself in ctor)

        Scene scene = new Scene(boardView, 900, 900);
        attachCss(scene);
        stage.setTitle("Hex — Game");
        stage.setScene(scene);

        // initial paint
        boardView.update(adapter);
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
