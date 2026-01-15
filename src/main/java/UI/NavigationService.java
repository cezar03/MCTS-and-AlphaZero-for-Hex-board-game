package UI;

import java.awt.Desktop;
import java.io.File;
import java.util.Objects;

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

import Game.Board;
import Game.BoardAdapter;
import Game.Player;
import AI.AiPlayer.AIAgent;
import AI.AiPlayer.AIAgentFactory;
import AI.AiPlayer.AIAdaptationConfig;
import AI.AiPlayer.MCTSPlayer;
import AI.AiPlayer.MCTSPlayerFactory;
import AI.AiPlayer.RandomPlayer;
import AI.AiPlayer.RandomPlayerFactory;
import AI.AlphaZero.AlphaZeroConfig;
import AI.AlphaZero.AlphaZeroMCTS;
import AI.AlphaZero.AlphaZeroPlayer;
import AI.AlphaZero.AlphaZeroNet;

public final class NavigationService {
    private final Stage stage;
    public NavigationService(Stage stage) {
        this.stage = stage;
    }

    // Builds the menu
    public void showMenu() {
        Parent root = MainMenu.createRoot(this);
        Scene scene = new Scene(root, 720, 480);
        attachCss(scene);
        stage.setTitle("Connections — Main Menu");
        stage.setScene(scene);
    }

    /**
     * Shows the game when no AI is involved, so a human plays against a human.
     * @param size size of the Hex board
     * @param hexSize size of the hexagons
     */
    public void showGame(int size, double hexSize) {
        ScoreBoard scoreBoard = new ScoreBoard();
        showGameWithScoreboard(size, hexSize, null, null, scoreBoard);
    }

    /**
     * Shows the game with scoreboard tracking for human vs human games.
     * @param size size of the Hex board
     * @param hexSize size of the hexagons
     * @param scoreBoard the scoreboard to track wins
     */
    private void showGameWithScoreboard(int size, double hexSize, AIAgentFactory aiFactory, 
                                        AIAdaptationConfig aiConfig, ScoreBoard scoreBoard) {
        Board board = new Board(size);
        BoardAdapter adapter = new BoardAdapter(board);
        BoardView boardView = new BoardView(size, hexSize);
        GameController controller = new GameController(adapter, boardView);
        boardView.setController(controller); // (controller also sets itself in ctor)

        // Create turn label
        Label turnLabel = new Label();
        turnLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Create scoreboard label
        Label scoreLabel = new Label(scoreBoard.toString());
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Set the labels in the view
        boardView.setTurnLabel(turnLabel);

        // Wrap BoardView in a BorderPane to add controls
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #DEB887, #D2A679, #C8A882);");
        root.setCenter(boardView);

        // Create a back button
        Button backBtn = Buttons.primary("← Back to Menu");
        backBtn.setOnAction(e -> showMenu());

        // Add button and labels to the top
        HBox topBar = new HBox(12, backBtn, turnLabel, scoreLabel);
        topBar.setPadding(new Insets(12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        Scene scene = new Scene(root, 900, 900);
        attachCss(scene);
        stage.setTitle("Hex — Game");
        stage.setScene(scene);

        // initial paint
        boardView.update(adapter);
        boardView.updateTurnDisplay(controller.getCurrentPlayer());

        // Set up game end listener to show play again dialog
        controller.setGameEndListener(winner -> {
            scoreBoard.recordWin(winner);
            scoreLabel.setText(scoreBoard.toString());
            
            PlayAgainDialog.showDialog(winner, scoreBoard,
                () -> {
                    // Play Again
                    controller.resetForNewGame();
                },
                () -> {
                    // Back to Menu
                    if (controller.hasAnyAIAgent()) {
                        controller.removeAllAIAgents();
                    }
                    showMenu();
                }
            );
        });

        // Setup AI if applicable
        if (aiFactory != null && aiConfig != null) {
            controller.setupAIAgent(Player.BLACK, aiFactory, aiConfig);
        }
    }

    /**
     * Shows the game when one AI agent is involved, so a human plays against the computer.
     * @param size size of the Hex board
     * @param hexSize size of the hexagons
     * @param aiIterations number of iterations for the AI's Monte Carlo Tree Search
     */
    public void showGame(int size, double hexSize, Integer aiIterations) {
        ScoreBoard scoreBoard = new ScoreBoard();
        AIAdaptationConfig config = new AIAdaptationConfig.Builder(Player.BLACK)
            .iterations(aiIterations)
            .build();
        AIAgentFactory factory = new MCTSPlayerFactory();
        showGameWithScoreboard(size, hexSize, factory, config, scoreBoard);
    }

    /**
     * Shows the game with AlphaZero AI agent.
     */
    public void showGameWithAlphaZero() {
        ScoreBoard scoreBoard = new ScoreBoard();
        
        // Create AlphaZero components
        AlphaZeroNet network = new AlphaZeroNet(11);
        AlphaZeroMCTS alphaMcts = new AlphaZeroMCTS(network);
        AlphaZeroConfig alphaConfig = new AlphaZeroConfig.Builder()
            .boardSize(11)
            .mctsIterations(1000)
            .temperature(1.0)
            .build();
        
        AlphaZeroPlayer alphaAgent = new AlphaZeroPlayer(Player.BLACK, alphaMcts, alphaConfig);
        
        Board board = new Board(11);
        BoardAdapter adapter = new BoardAdapter(board);
        BoardView boardView = new BoardView(11, 55);
        GameController controller = new GameController(adapter, boardView);
        boardView.setController(controller);

        // Create turn label
        Label turnLabel = new Label();
        turnLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Create scoreboard label
        Label scoreLabel = new Label(scoreBoard.toString());
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Set the labels in the view
        boardView.setTurnLabel(turnLabel);

        // Wrap BoardView in a BorderPane to add controls
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #DEB887, #D2A679, #C8A882);");
        root.setCenter(boardView);

        // Create a back button
        Button backBtn = Buttons.primary("← Back to Menu");
        backBtn.setOnAction(e -> {
            if (controller.hasAnyAIAgent()) {
                controller.removeAllAIAgents();
            }
            showMenu();
        });

        // Add button and labels to the top
        HBox topBar = new HBox(12, backBtn, turnLabel, scoreLabel);
        topBar.setPadding(new Insets(12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        Scene scene = new Scene(root, 900, 900);
        attachCss(scene);
        stage.setTitle("Hex — Game vs AlphaZero");
        stage.setScene(scene);

        // initial paint
        boardView.update(adapter);
        boardView.updateTurnDisplay(controller.getCurrentPlayer());

        // Set up game end listener to show play again dialog
        controller.setGameEndListener(winner -> {
            scoreBoard.recordWin(winner);
            scoreLabel.setText(scoreBoard.toString());
            
            PlayAgainDialog.showDialog(winner, scoreBoard,
                () -> {
                    // Play Again
                    controller.resetForNewGame();
                },
                () -> {
                    // Back to Menu
                    if (controller.hasAnyAIAgent()) {
                        controller.removeAllAIAgents();
                    }
                    showMenu();
                }
            );
        });

        // Setup AlphaZero AI agent for BLACK player
        controller.setupAIAgent(Player.BLACK, alphaAgent);
    }

    /**
     * Shows the game with selected AI agent for human vs AI.
     */
    public void showGameWithAI(String agentType, int iterations) {
        if ("AlphaZero".equals(agentType)) {
            showGameWithAlphaZero();
            return;
        }

        ScoreBoard scoreBoard = new ScoreBoard();
        AIAgentFactory factory;
        AIAdaptationConfig config;

        if ("MCTS".equals(agentType)) {
            factory = new MCTSPlayerFactory();
            config = new AIAdaptationConfig.Builder(Player.BLACK)
                .iterations(iterations)
                .build();
        } else if ("Random".equals(agentType)) {
            factory = new RandomPlayerFactory();
            config = new AIAdaptationConfig.Builder(Player.BLACK).build();
        } else {
            throw new IllegalArgumentException("Unknown agent type: " + agentType);
        }

        showGameWithScoreboard(11, 55, factory, config, scoreBoard);
    }

    /**
     * Shows AI vs AI testing interface.
     */
    public void showAITesting() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("AI Testing");
        dialog.setHeaderText("AI Agent Testing");
        dialog.setContentText("Choose which agents to test:");
        styleDialog(dialog);

        Label redLabel = new Label("Red Player:");
        ComboBox<String> redCombo = new ComboBox<>();
        redCombo.getItems().addAll("MCTS", "Random" , "AlphaZero");
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
            AIAgent redAgent = createAgent(redType, Player.RED);
            AIAgent blackAgent = createAgent(blackType, Player.BLACK);
            showGameAIvsAI(redAgent, blackAgent);
        });

        cancelBtn.setOnAction(e -> dialog.close());

        VBox content = new VBox(10, redLabel, redCombo, blackLabel, blackCombo, startBtn, cancelBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    /**
     * Shows a game where two AI agents play against each other.
     */
    private void showGameAIvsAI(AIAgent redAgent, AIAgent blackAgent) {
        ScoreBoard scoreBoard = new ScoreBoard();
        
        Board board = new Board(11);
        BoardAdapter adapter = new BoardAdapter(board);
        BoardView boardView = new BoardView(11, 55);
        GameController controller = new GameController(adapter, boardView);
        boardView.setController(controller);

        // Setup UI
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
        stage.setTitle("Hex – AI vs AI Testing");
        stage.setScene(scene);

        boardView.update(adapter);
        boardView.updateTurnDisplay(controller.getCurrentPlayer());

        // Set up game end listener to show play again dialog
        controller.setGameEndListener(winner -> {
            scoreBoard.recordWin(winner);
            scoreLabel.setText(scoreBoard.toString());
            
            PlayAgainDialog.showDialog(winner, scoreBoard,
                () -> {
                    // Play Again
                    controller.resetForNewGame();
                },
                () -> {
                    // Back to Menu
                    controller.removeAllAIAgents();
                    showMenu();
                }
            );
        });

        // Setup BOTH AI agents
        controller.setupAIAgent(Player.RED, redAgent);
        controller.setupAIAgent(Player.BLACK, blackAgent);
        // The first agent will automatically start playing
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

    /**
     * Shows a difficulty selection dialog for playing against the AI.
     */
    public void showDifficultySelection() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("AI Difficulty");
        dialog.setHeaderText("Select AI Agent");
        dialog.setContentText("Choose which AI agent to play against:");
        styleDialog(dialog);

        Button mctsBtn = new Button("MCTS");
        Button randomBtn = new Button("Random");
        Button alphaZeroBtn = new Button("AlphaZero");
        Button cancelBtn = new Button("Cancel");

        mctsBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        randomBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        alphaZeroBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        cancelBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");

        mctsBtn.setOnAction(e -> {
            dialog.close();
            showIterationsDialog();
        });

        randomBtn.setOnAction(e -> {
            dialog.close();
            showGameWithAI("Random", 0);
        });

        alphaZeroBtn.setOnAction(e -> {
            dialog.close();
            showGameWithAI("AlphaZero", 0);
        });

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

        easyBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        mediumBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        hardBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        expertBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        cancelBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");

        easyBtn.setOnAction(e -> {
            dialog.close();
            showGameWithAI("MCTS", 500);
        });

        mediumBtn.setOnAction(e -> {
            dialog.close();
            showGameWithAI("MCTS", 1000);
        });

        hardBtn.setOnAction(e -> {
            dialog.close();
            showGameWithAI("MCTS", 2000);
        });

        expertBtn.setOnAction(e -> {
            dialog.close();
            showGameWithAI("MCTS", 5000);
        });

        cancelBtn.setOnAction(e -> dialog.close());

        VBox buttonBox = new VBox(10, easyBtn, mediumBtn, hardBtn, expertBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(buttonBox);
        dialog.showAndWait();
    }

    private AIAgent createAgent(String type, Player player) {
        if ("MCTS".equals(type)) {
            AIAdaptationConfig config = new AIAdaptationConfig.Builder(player)
                .iterations(2000)
                .threshold(0.9)
                .centralityWeight(0.5)
                .connectivityWeight(0.5)
                .biasScale(0.046)
                .shortestPathWeight(0.039)
                .explorationConstant(Math.sqrt(2))
                .build();
            return new MCTSPlayerFactory().createAgent(config);
        } else if ("Random".equals(type)) {
            AIAdaptationConfig config = new AIAdaptationConfig.Builder(player).build();
            return new RandomPlayerFactory().createAgent(config);
        } else if ("AlphaZero".equals(type)) {
            AlphaZeroNet network = new AlphaZeroNet(11);
            AlphaZeroMCTS alphaMcts = new AlphaZeroMCTS(network);
            AlphaZeroConfig alphaConfig = new AlphaZeroConfig.Builder()
                .boardSize(11)
                .mctsIterations(1000)
                .temperature(1.0)
                .build();
            return new AlphaZeroPlayer(player, alphaMcts, alphaConfig);
        }  else {
            throw new IllegalArgumentException("Unknown agent type: " + type);
        }
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
