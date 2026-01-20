package ui;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.nav.NavigationService;

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) {
        var nav = new NavigationService(primaryStage);
        nav.showMenu();
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}











