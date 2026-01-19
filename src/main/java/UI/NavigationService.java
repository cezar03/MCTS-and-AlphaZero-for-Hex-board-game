package UI;

import java.awt.Desktop;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import AI.AiPlayer.AIAdaptationConfig;
import AI.AiPlayer.AIAgent;
import AI.AiPlayer.AIAgentFactory;
import AI.AiPlayer.MCTSPlayerFactory;
import AI.AiPlayer.RandomPlayerFactory;
import AI.AlphaZero.AlphaZeroConfig;
import AI.AlphaZero.AlphaZeroPlayerFactory;
import Game.Board;
import Game.BoardAdapter;
import Game.Player;

public final class NavigationService {

    private final Stage stage;

    private static final int DEFAULT_SIZE = 11;
    private static final double DEFAULT_HEX_SIZE = 55;

    private static final String MODEL_PATH = "src/main/resources/models/hex_model_correct.zip";

    public NavigationService(Stage stage) {
        this.stage = stage;
    }

    public void showMenu() {
        Parent root = MainMenu.createRoot(this);
        Scene scene = new Scene(root, 720, 480);
        attachCss(scene);
        stage.setTitle("Connections — Main Menu");
        stage.setScene(scene);
    }

    // Human vs Human
    public void showGame(int size, double hexSize) {
        ScoreBoard scoreBoard = new ScoreBoard();
        startSession(size, hexSize, scoreBoard, controller -> {});
    }

    // Human vs MCTS (old entry point)
    public void showGame(int size, double hexSize, Integer aiIterations) {
        int iters = (aiIterations == null ? 1000 : aiIterations);
        ScoreBoard scoreBoard = new ScoreBoard();

        AIAgentFactory factory = new MCTSPlayerFactory();
        AIAdaptationConfig cfg = new AIAdaptationConfig.Builder(Player.BLACK)
                .iterations(iters)
                .build();

        startSession(size, hexSize, scoreBoard, controller -> {
            controller.setupAIAgent(Player.BLACK, factory, cfg);
        });
    }

    // Main route: play vs selected AI
    public void showGameWithAI(String agentType, int iterations) {
        ScoreBoard scoreBoard = new ScoreBoard();

        startSession(DEFAULT_SIZE, DEFAULT_HEX_SIZE, scoreBoard, controller -> {
            AIAgentFactory factory = createFactory(agentType);
            AIAdaptationConfig cfg = createConfig(agentType, Player.BLACK, iterations);
            controller.setupAIAgent(Player.BLACK, factory, cfg);
        });
    }

    // AI testing dialog
    public void showAITesting() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("AI Testing");
        dialog.setHeaderText("AI Agent Testing");
        dialog.setContentText("Choose which agents to test:");
        styleDialog(dialog);

        Label redLabel = new Label("Red Player:");
        ComboBox<String> redCombo = new ComboBox<>();
        redCombo.getItems().addAll("MCTS", "Random", "AlphaZero");
        redCombo.setValue("MCTS");

        Label blackLabel = new Label("Black Player:");
        ComboBox<String> blackCombo = new ComboBox<>();
        blackCombo.getItems().addAll("MCTS", "Random", "AlphaZero");
        blackCombo.setValue("Random");

        Button startBtn = new Button("Start Game");
        Button cancelBtn = new Button("Cancel");

        startBtn.setOnAction(e -> {
            dialog.close();

            String redType = redCombo.getValue();
            String blackType = blackCombo.getValue();

            AIAgent red = createFactory(redType).createAgent(createConfig(redType, Player.RED, 2000));
            AIAgent black = createFactory(blackType).createAgent(createConfig(blackType, Player.BLACK, 2000));

            ScoreBoard scoreBoard = new ScoreBoard();
            startSession(DEFAULT_SIZE, DEFAULT_HEX_SIZE, scoreBoard, controller -> {
                controller.setupAIAgent(Player.RED, red);
                controller.setupAIAgent(Player.BLACK, black);
            });
        });

        cancelBtn.setOnAction(e -> dialog.close());

        VBox content = new VBox(10, redLabel, redCombo, blackLabel, blackCombo, startBtn, cancelBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    // Agent selection dialog (your existing UX)
    public void showAgentSelection() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Agent Selection");
        dialog.setHeaderText("Select AI Agent");
        dialog.setContentText("Choose which AI agent to play against:");
        styleDialog(dialog);

        Button mctsBtn = new Button("MCTS");
        Button randomBtn = new Button("Random");
        Button alphaZeroBtn = new Button("AlphaZero");
        Button cancelBtn = new Button("Cancel");

        mctsBtn.setOnAction(e -> { dialog.close(); showIterationsDialog(); });
        randomBtn.setOnAction(e -> { dialog.close(); showGameWithAI("Random", 0); });
        alphaZeroBtn.setOnAction(e -> { dialog.close(); showGameWithAI("AlphaZero", 0); });
        cancelBtn.setOnAction(e -> dialog.close());

        VBox buttonBox = new VBox(10, mctsBtn, randomBtn, alphaZeroBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(buttonBox);
        dialog.showAndWait();
    }

    private void showIterationsDialog() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("AI Difficulty");
        dialog.setHeaderText("Select Difficulty Level");
        dialog.setContentText("Choose how strong the MCTS AI opponent should be:");
        styleDialog(dialog);

        Button easyBtn = new Button("Easy (500 iterations)");
        Button mediumBtn = new Button("Medium (1000 iterations)");
        Button hardBtn = new Button("Hard (2000 iterations)");
        Button expertBtn = new Button("Expert (5000 iterations)");
        Button cancelBtn = new Button("Cancel");

        easyBtn.setOnAction(e -> { dialog.close(); showGameWithAI("MCTS", 500); });
        mediumBtn.setOnAction(e -> { dialog.close(); showGameWithAI("MCTS", 1000); });
        hardBtn.setOnAction(e -> { dialog.close(); showGameWithAI("MCTS", 2000); });
        expertBtn.setOnAction(e -> { dialog.close(); showGameWithAI("MCTS", 5000); });
        cancelBtn.setOnAction(e -> dialog.close());

        VBox buttonBox = new VBox(10, easyBtn, mediumBtn, hardBtn, expertBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(buttonBox);
        dialog.showAndWait();
    }

    // -----------------------------------------
    // Core session builder (single source of UI)
    // -----------------------------------------

    private void startSession(int size, double hexSize, ScoreBoard scoreBoard,
                              Consumer<GameController> configure) {

        Board board = new Board(size);
        BoardAdapter adapter = new BoardAdapter(board);

        // IMPORTANT: this must be your JavaFX UI view class (UI.BoardView).
        HexBoardView boardView = new HexBoardView(size, hexSize);

        GameController controller = new GameController(adapter, boardView);

        Label turnLabel = new Label();
        turnLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label scoreLabel = new Label(scoreBoard.toString());
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        boardView.setTurnLabel(turnLabel);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #DEB887, #D2A679, #C8A882);");
        root.setCenter(boardView);

        Button backBtn = Buttons.primary("← Back to Menu");
        backBtn.setOnAction(e -> {
            controller.removeAllAIAgents();
            showMenu();
        });

        HBox topBar = new HBox(12, backBtn, turnLabel, scoreLabel);
        topBar.setPadding(new Insets(12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        Scene scene = new Scene(root, 900, 900);
        attachCss(scene);

        stage.setTitle("Hex — Game");
        stage.setScene(scene);

        boardView.update(adapter);
        boardView.updateTurnDisplay(controller.getCurrentPlayer());

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
    // Agent wiring
    // -----------------------------------------

    private AIAgentFactory createFactory(String type) {
        return switch (type) {
            case "MCTS" -> new MCTSPlayerFactory();
            case "Random" -> new RandomPlayerFactory();
            case "AlphaZero" -> new AlphaZeroPlayerFactory(buildAlphaZeroConfig());
            default -> throw new IllegalArgumentException("Unknown agent type: " + type);
        };
    }

    private AIAdaptationConfig createConfig(String type, Player player, int iterations) {
        if ("MCTS".equals(type)) {
            return new AIAdaptationConfig.Builder(player)
                    .iterations(Math.max(1, iterations))
                    .build();
        }
        // Random + AlphaZero only need player slot
        return new AIAdaptationConfig.Builder(player).build();
    }

    private AlphaZeroConfig buildAlphaZeroConfig() {
        return new AlphaZeroConfig.Builder()
                .boardSize(DEFAULT_SIZE)
                .mctsIterations(100)
                .temperature(0.01)
                .cpuct(Math.sqrt(2))
                .modelPath(MODEL_PATH)
                .loadExistingModel(true)
                .build();
    }

    // -----------------------------------------
    // Misc
    // -----------------------------------------

    public void showSkins() {
        Parent root = new CaseOpeningView().createContent();
        Scene scene = new Scene(root, 832, 400);
        attachCss(scene);
        stage.setTitle("Case Opening");
        stage.setScene(scene);
    }

    public void info(String title, String header, String content) {
        try {
            var url = MainMenu.class.getResource("/HEX_RULES.pdf");
            File pdf = new File(url.toURI());
            Desktop.getDesktop().open(pdf);
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No files was found");
            styleDialog(a);
            a.showAndWait();
        }
    }

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
