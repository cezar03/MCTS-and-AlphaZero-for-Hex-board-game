package UI;

import javafx.application.Application;
import javafx.stage.Stage;

import UI.nav.NavigationService;

/**
 * JavaFX application entry point for the Connections game.
 *
 * <p>This class bootstraps the JavaFX runtime and initializes the
 * {@link NavigationService}, which is responsible for screen management
 * and navigation throughout the application.</p>
 *
 * <p>On startup, the main menu screen is shown immediately.</p>
 */
public class MainMenu extends Application {

    /**
     * Called by the JavaFX runtime to start the application.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Creates a {@link NavigationService} bound to the primary stage</li>
     *   <li>Displays the main menu screen</li>
     *   <li>Shows the primary stage</li>
     * </ul>
     *
     * @param primaryStage the primary window provided by the JavaFX runtime
     */
    @Override
    public void start(Stage primaryStage) {
        var nav = new NavigationService(primaryStage);
        nav.showMenu();
        primaryStage.show();
    }

    /**
     * Standard Java entry point.
     *
     * <p>Delegates application startup to the JavaFX runtime via
     * {@link #launch(String...)}.</p>
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}











