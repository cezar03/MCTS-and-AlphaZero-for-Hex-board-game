package UI.nav;

import java.awt.Desktop;
import java.io.File;
import java.util.function.Consumer;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import AI.alphazero.config.AlphaZeroConfig;
import AI.api.AIAgent;
import AI.registry.AgentRegistry;
import game.core.Player;
import UI.MainMenu;
import UI.config.UiDefaults;
import UI.controller.GameController;
import UI.dialogs.AgentSelectionDialog;
import UI.dialogs.PlayAgainDialog;
import UI.screens.AITestScreenBuilder;
import UI.screens.GameScreenBuilder;
import UI.screens.MenuScreenBuilder;
import UI.session.ScoreBoard;
import UI.view.AppStyles;
import UI.view.CaseOpeningView;

/**
 * Service for navigating between different UI screens.
 */
public final class NavigationService {

    private final Stage stage;
    private final AgentRegistry agentRegistry;

    // -----------------------------------------
    // Navigation methods
    // -----------------------------------------
    /**
     * Creates a NavigationService for the given stage.
     * @param stage the primary Stage for the application
     */
    public NavigationService(Stage stage) {
        this.stage = stage;
        this.agentRegistry = new AgentRegistry(buildAlphaZeroConfig());
    }

    /**
     * Shows the main menu screen.
     */
    public void showMenu() {
        Parent root = MenuScreenBuilder.build(this);
        Scene scene = new Scene(root, 720, 480);
        AppStyles.apply(scene);
        stage.setTitle("Connections Main Menu");
        stage.setScene(scene);
    }

    /**
     * Shows a new game screen for human vs human play.
     * @param size the board size
     * @param hexSize the hex cell size
     */
    public void showGame(int size, double hexSize) {
        ScoreBoard scoreBoard = new ScoreBoard();
        showGameScreen(size, hexSize, scoreBoard, controller -> {});
    }

    // Human vs MCTS (old entry point)
    /**
     * Shows a new game screen for human vs MCTS AI play.
     * @param size the board size
     * @param hexSize  the hex cell size
     * @param aiIterations the number of MCTS iterations for the AI, or null for default
     */
    public void showGame(int size, double hexSize, Integer aiIterations) {
        int iters = (aiIterations == null ? UiDefaults.DEFAULT_MCTS_ITERATIONS : aiIterations);
        ScoreBoard scoreBoard = new ScoreBoard();

        showGameScreen(size, hexSize, scoreBoard, controller -> {
            AIAgent agent = agentRegistry.createAgent("MCTS", Player.BLACK, iters);
            controller.setupAIAgent(Player.BLACK, agent);
        });
    }

    // Main route: play vs selected AI
    /**
     * Shows a new game screen for human vs selected AI play.
     * @param agentType the type of AI agent to play against
     * @param iterations the number of MCTS iterations for the AI (if applicable)
     */
    public void showGameWithAI(String agentType, int iterations) {
        ScoreBoard scoreBoard = new ScoreBoard();

        showGameScreen(UiDefaults.BOARD_SIZE, UiDefaults.HEX_SIZE, scoreBoard, controller -> {
            AIAgent agent = agentRegistry.createAgent(agentType, Player.BLACK, iterations);
            controller.setupAIAgent(Player.BLACK, agent);
        });
    }

    // AI testing dialog
    /**
     * Shows the AI testing dialog to select two AIs to compete against each other.
     */
    public void showAITesting() {
        AITestScreenBuilder.showDialog(agentRegistry, selection -> {
            ScoreBoard scoreBoard = new ScoreBoard();
            showGameScreen(UiDefaults.BOARD_SIZE, UiDefaults.HEX_SIZE, scoreBoard, controller -> {
                AIAgent red = agentRegistry.createAgent(selection.redType(), Player.RED, UiDefaults.AI_TEST_MCTS_ITERATIONS);
                AIAgent black = agentRegistry.createAgent(selection.blackType(), Player.BLACK, UiDefaults.AI_TEST_MCTS_ITERATIONS);
                controller.setupAIAgent(Player.RED, red);
                controller.setupAIAgent(Player.BLACK, black);
            });
        });
    }

    // Agent selection dialog (your existing UX)
    /**
     * Shows the agent selection dialog to choose an AI agent to play against.
     */
    public void showAgentSelection() {
        AgentSelectionDialog.AgentSelection selection = AgentSelectionDialog.show(agentRegistry);
        if (selection == null) return;
        showGameWithAI(selection.type(), selection.iterations());
    }

    // -----------------------------------------
    // Core session builder (single source of UI)
    // -----------------------------------------

    /**
     * Shows the game screen with the specified configuration.
     * @param size the board size
     * @param hexSize the hex cell size
     * @param scoreBoard the ScoreBoard to track wins
     * @param configure a consumer to configure the GameController
     */
    private void showGameScreen(int size, double hexSize, ScoreBoard scoreBoard,
                                Consumer<GameController> configure) {
        GameScreenBuilder.GameScreen screen = GameScreenBuilder.build(
                size, hexSize, scoreBoard, this::showMenu
        );

        GameController controller = screen.controller();
        Label scoreLabel = screen.scoreLabel();

        Scene scene = new Scene(screen.root(), 900, 900);
        AppStyles.apply(scene);
        stage.setTitle("Hex Game");
        stage.setScene(scene);

        controller.setGameEndListener(winner -> {
            scoreBoard.recordWin(winner);
            scoreLabel.setText(scoreBoard.toString());

            PlayAgainDialog.showDialog(
                    winner,
                    scoreBoard,
                    controller::resetForNewGame,
                    () -> {
                        controller.removeAllAIAgents();
                        showMenu();
                    }
            );
        });

        configure.accept(controller);
    }

    // -----------------------------------------
    // Misc
    // -----------------------------------------

    /**
     * Shows the skins/case opening view.
     */
    public void showSkins() {
        Parent root = new CaseOpeningView().createContent();
        Scene scene = new Scene(root, 832, 400);
        AppStyles.apply(scene);
        stage.setTitle("Case Opening");
        stage.setScene(scene);
    }

    /**
     * Opens the game rules PDF in the default system viewer.
     * @param title the title of the info dialog
     * @param header the header text of the info dialog
     * @param content the content text of the info dialog
     */
    public void info(String title, String header, String content) {
        try {
            var url = MainMenu.class.getResource("/HEX_RULES.pdf");
            File pdf = new File(url.toURI());
            Desktop.getDesktop().open(pdf);
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No files was found");
            AppStyles.styleDialog(a);
            a.showAndWait();
        }
    }

    // --------------------------------------------------
    // Config builders
    // --------------------------------------------------
    /**
     * Builds the AlphaZero configuration with default UI settings.
     * @return the AlphaZeroConfig instance
     */
    private AlphaZeroConfig buildAlphaZeroConfig() {
        return new AlphaZeroConfig.Builder()
                .boardSize(UiDefaults.BOARD_SIZE)
                .mctsIterations(UiDefaults.ALPHAZERO_MCTS_ITERATIONS)
                .temperature(UiDefaults.ALPHAZERO_TEMPERATURE)
                .cpuct(UiDefaults.ALPHAZERO_CPUCT)
                .modelPath(UiDefaults.MODEL_PATH)
                .loadExistingModel(true)
                .build();
    }
}
