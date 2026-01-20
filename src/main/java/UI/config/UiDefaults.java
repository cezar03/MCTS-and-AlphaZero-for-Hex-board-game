package ui.config;

public final class UiDefaults {
    private UiDefaults() {}

    public static final int BOARD_SIZE = 11;
    public static final double HEX_SIZE = 55;
    public static final String MODEL_PATH = "src/main/resources/models/hex_model_correct.zip";

    public static final int DEFAULT_MCTS_ITERATIONS = 1000;
    public static final int AI_TEST_MCTS_ITERATIONS = 2000;

    public static final int MCTS_EASY_ITERATIONS = 500;
    public static final int MCTS_MEDIUM_ITERATIONS = 1000;
    public static final int MCTS_HARD_ITERATIONS = 2000;
    public static final int MCTS_EXPERT_ITERATIONS = 5000;

    public static final int ALPHAZERO_MCTS_ITERATIONS = 100;
    public static final double ALPHAZERO_TEMPERATURE = 0.01;
    public static final double ALPHAZERO_CPUCT = Math.sqrt(2);
}
