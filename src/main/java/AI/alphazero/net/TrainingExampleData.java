package AI.alphazero.net;

/**
 * A data container representing a single training example generated during self-play.
 * <p>
 * Each example consists of:
 * <ul>
 * <li><b>Input</b>: The encoded board state.</li>
 * <li><b>Target Policy</b>: The MCTS visit probabilities calculated during search.</li>
 * <li><b>Target Value</b>: The actual outcome of the game (-1, 0, or 1) from the perspective of the player.</li>
 * </ul>
 */
public class TrainingExampleData {
    public float[] inputBoard;
    public float[] targetPolicy;
    public float[] targetValue;

    /**
     * Creates a new training example.
     * * @param input the encoded board input
     * @param policy the target policy distribution (MCTS visits)
     * @param value the target value (game outcome)
     */
    public TrainingExampleData(float[] input, float[] policy, float value) {
        this.inputBoard = input;
        this.targetPolicy = policy;
        this.targetValue = new float[]{ value };
    }
}










