package UI.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import UI.config.UiDefaults;
import UI.nav.NavigationService;

/**
 * Builder for the Menu screen.
 */
public final class MenuScreenBuilder {
    private MenuScreenBuilder() {}

    /**
     * Builds the Menu screen.
     *
     * @param nav The navigation service for screen transitions.
     * @return The constructed Menu screen as a Parent node.
     */
    public static Parent build(NavigationService nav) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1f3b7c, #111827);");

        Text title = new Text("Connections");
        title.setFill(javafx.scene.paint.Color.WHITE);
        title.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 42));

        VBox header = new VBox(6, title);
        header.setAlignment(Pos.CENTER_LEFT);
        Button playBtn  = new Button("PLAY");  playBtn.getStyleClass().add("btn-primary");
        Button aiBtn    = new Button("VS COMPUTER"); aiBtn.getStyleClass().add("btn-primary");
        Button testAIvsAIBtn = new Button("AI vs AI game"); testAIvsAIBtn.getStyleClass().add("btn-tertiary");
        Button aboutBtn = new Button("ABOUT"); aboutBtn.getStyleClass().add("btn-ghost");

        playBtn.setOnAction(e -> nav.showGame(UiDefaults.BOARD_SIZE, UiDefaults.HEX_SIZE));
        aiBtn.setOnAction(e -> nav.showAgentSelection());
        testAIvsAIBtn.setOnAction(e -> nav.showAITesting());
        aboutBtn.setOnAction(e -> nav.info("About Connections", "About the Game","Put the link to the game's rules here."));

        VBox buttons = new VBox(12, playBtn, aiBtn, testAIvsAIBtn, aboutBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(20, 0, 0, 0));

        HBox footer = new HBox(12, spacer());
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(24, 0, 0, 0));

        VBox content = new VBox(8, header, buttons, footer);
        content.setAlignment(Pos.TOP_LEFT);
        root.setCenter(content);

        return root;
    }

    /**
     * Creates a horizontal spacer region.
     * @return The spacer region.
     */
    private static Region spacer() {
        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS); return r;
    }
}
