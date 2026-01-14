package AI.AlphaZero;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Represents a single training example for the AlphaZero neural network.
 * Each example consists of a board state paired with target outputs for both network heads.
 * 
 * <p>Components:
 * <ul>
 *   <li><strong>Input:</strong> Encoded board state (3-plane tensor)</li>
 *   <li><strong>Target policy:</strong> Improved move probabilities from MCTS search</li>
 *   <li><strong>Target value:</strong> Actual game outcome from this position's player's perspective</li>
 * </ul>
 * 
 * <p>During training, the network learns to:
 * <ul>
 *   <li>Predict policies that match the MCTS-improved policies (which are generally
 *       better than the raw network policy)</li>
 *   <li>Predict values that match the actual game outcomes (win/loss)</li>
 * </ul>
*/
public class TrainingExampleData {
    INDArray inputBoard; // The 3-plane tensor
    INDArray targetPolicy; // The probabilities from MCTS
    INDArray targetValue; // The actual winner (-1 or 1)

    /**
     * Creates a training example with the given board state and target outputs.
     * 
     * @param inputBoard the encoded board state as a 3-plane tensor [1, 3, size, size]
     * @param targetPolicy the target policy as a probability distribution over moves [1, size²]
     *                     (typically derived from MCTS visit counts)
     * @param value the target value representing the game outcome from the current player's perspective
     *              (+1.0 for a win, -1.0 for a loss, 0.0 for a draw - though Hex has no draws)
    */
    public TrainingExampleData(INDArray inputBoard, INDArray targetPolicy, double value) {
        this.inputBoard = inputBoard;
        this.targetPolicy = targetPolicy;
        // ND4J expects the value as an array, even if it's a single number. Convert it accordingly.
        this.targetValue = org.nd4j.linalg.factory.Nd4j.create(new double[]{value}, new int[]{1, 1});
    }
}