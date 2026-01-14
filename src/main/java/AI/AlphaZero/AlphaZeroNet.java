package AI.AlphaZero;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType; // This is used for branching networks, which we need for AlphaZero.
import org.deeplearning4j.nn.conf.layers.BatchNormalization;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer; // This is used for optimization.
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Neural network architecture for the AlphaZero algorithm applied to Hex.
 * This network uses a convolutional architecture with two output heads:
 * one for policy (move probabilities) and one for value (position evaluation).
 * 
 * <p>Architecture overview:
 * <ul>
 *   <li><strong>Input:</strong> 3-plane board representation (Red pieces, Black pieces, current player)</li>
 *   <li><strong>Shared trunk:</strong> Convolutional layers that extract spatial features from the board</li>
 *   <li><strong>Policy head:</strong> Outputs probability distribution over all possible moves</li>
 *   <li><strong>Value head:</strong> Outputs a single scalar estimating the win probability</li>
 * </ul>
 * 
 * <p>The network uses ResNet-style architecture with batch normalization for stable training.
 * It is implemented as a ComputationGraph to support the dual-head output structure.
*/
public class AlphaZeroNet {
    // The actual neural network model. 
    // It is called a ComputationGraph instead of a simple Network, because it has a branching structure with two heads. (For policy and value outputs)
    private ComputationGraph model;

    // Size of the Hex board.
    private final int boardSize;

    /**
     * Constructs and initializes a new AlphaZero neural network for the specified board size.
     * The network is immediately initialized with random weights ready for training.
     * 
     * @param boardSize the size of the Hex board (e.g., 11 for an 11×11 board)
    */
    public AlphaZeroNet(int boardSize) {
        this.boardSize = boardSize;
        initModel(); // Method to build the neural network structure.
    }

    /**
     * Initializes the neural network architecture with convolutional layers and dual output heads.
     * 
     * <p>Network structure:
     * <ol>
     *   <li><strong>Shared convolutional trunk:</strong>
     *       <ul>
     *         <li>Conv layer 1: 3×3 filters, 64 channels, ReLU activation</li>
     *         <li>Batch normalization</li>
     *         <li>Conv layer 2: 3×3 filters, 64 channels, ReLU activation</li>
     *         <li>Batch normalization</li>
     *       </ul>
     *   </li>
     *   <li><strong>Policy head:</strong>
     *       <ul>
     *         <li>1×1 convolution to 2 channels</li>
     *         <li>Flatten to dense layer</li>
     *         <li>Softmax output of size boardSize² (one probability per board position)</li>
     *         <li>Loss function: KL-divergence (measures difference from MCTS policy)</li>
     *       </ul>
     *   </li>
     *   <li><strong>Value head:</strong>
     *       <ul>
     *         <li>1×1 convolution to 1 channel</li>
     *         <li>Flatten to dense layer with 64 neurons</li>
     *         <li>Tanh output of size 1 (win probability from -1 to 1)</li>
     *         <li>Loss function: MSE (mean squared error from actual game outcome)</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p>Training configuration:
     * <ul>
     *   <li>Optimizer: Adam with learning rate 3×10⁻⁴</li>
     *   <li>L2 regularization: 3×10⁻⁴ to prevent overfitting</li>
     * </ul>
    */
    private void initModel() {
        int outputSize = boardSize * boardSize; // The total number of moves. The policy head will need to output this amount of probabilities.

        ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
            .updater(new Adam(3e-4)) // Learning Rate
            .l2(3e-4) // Regularization to prevent overfitting
            .graphBuilder() // Allows for creating split paths in the network (policy and value).
            .addInputs("input") // Input layer

            // Shared "Torso" (Convolutional Layers). These layers look at the board to understand the state and is shared by both the policy and the value heads.
            .addLayer("conv1", new ConvolutionLayer.Builder(3, 3) // Adds the first convolutional layer and creates a 'filter' of size 3x3 that scans the board and looks for patterns.
                .nIn(3) // 3 Input Planes (Red, Black, Turn)
                /** 
                 * .stride(1,1) means the filter moves 1 step at a time.
                 * .padding(1,1) means we add a border of zeros around the input to keep the output size the same as input size. Otherwise the board would shrink after convolution.
                 * Since we use a 3x3 filter, padding of 1 makes sure that for example the corners are still processed correctly. Otherwise, the result of 1 convolutional layer would be a 9x9 board instead of a 11x11 board, and after multiple convolutional layers the board could disappear.
                 * .nOut(64) means we have 64 different filters looking for different patterns.
                 * Activation.RELU adds non-linearity, allowing the network to learn complex patterns. It applies ReLU logic and turns negative numbers to 0.
                 * Finally the layer gets connected to the input node defined earlier.
                 */
                // TODO: Choose the best number for nOut (number of filters)
                .stride(1,1).padding(1,1).nOut(64).activation(Activation.RELU).build(), "input")
            .addLayer("bn1", new BatchNormalization(), "conv1") // Normalizes the output of conv1 to stabilize and speed up training.
            
            // Add a second convolutional layer for deeper feature extraction and to combine the simple patterns from conv1 into more complex patterns.
            .addLayer("conv2", new ConvolutionLayer.Builder(3, 3)
                .stride(1,1).padding(1,1).nOut(64).activation(Activation.RELU).build(), "bn1")
            .addLayer("bn2", new BatchNormalization(), "conv2")

            // Policy Head, which is a branch that splits off to decide which move is best.
            .addLayer("p_conv", new ConvolutionLayer.Builder(1, 1).nOut(2).activation(Activation.RELU).build(), "bn2") // 1x1 convolution to reduce the features from the torso to just 2 features.
            .addLayer("p_flat", new DenseLayer.Builder().nOut(outputSize).build(), "p_conv") // Flattens the 2D grid information into a single list of numbers. The output size is the number of board positions/moves and the output represents raw scores for every possible move.
            /**
             * OutputLayer for the policy head.
             * KL_DIVERGENCE is the loss function and measures how different two probability distributions are. In this case it compares the prediction from the network to what the MCTS visit counts were.
             * Activation.SOFTMAX converts the raw scores into probabilities that sum to 1.
            */
            .addLayer("policy", new OutputLayer.Builder(LossFunctions.LossFunction.KL_DIVERGENCE)
                .activation(Activation.SOFTMAX).nOut(outputSize).build(), "p_flat")

            // Value Head which is a branch that splits off to decide who is winning.
            .addLayer("v_conv", new ConvolutionLayer.Builder(1, 1).nOut(1).activation(Activation.RELU).build(), "bn2") // 1x1 convolution to reduce the features from the torso to just 1 feature.
            .addLayer("v_flat", new DenseLayer.Builder().nOut(64).activation(Activation.RELU).build(), "v_conv") // A small Dense layer with 64 neurons to process the board evaluation logic.
            /**
             * OutputLayer for the value head.
             * MSE (Mean Squared Error) is the loss function and measures the average squared difference between the predicted value and the actual game outcome.
             * Activation.TANH squashes the output to be between -1 and 1, representing the game outcome from the current player's perspective (-1 = loss, 0 = draw, 1 = win).
             * nOut(1) means the output is a single number representing the value of the board state.
             */
            .addLayer("value", new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .activation(Activation.TANH).nOut(1).build(), "v_flat")

            // Explicitly define the outputs of the network.
            .setOutputs("policy", "value")
            // Establish the input type as a convolutional input with 3 channels (planes) for the board.
            .setInputTypes(InputType.convolutional(boardSize, boardSize, 3))
            .build();
        
        // Create the network object using the blueprint 'conf' defined above.
        model = new ComputationGraph(conf);

        // Initialize the model's parameters (weights and biases) and allocates memory.
        model.init();
    }

    /**
     * Returns the underlying neural network model.
     * 
     * @return the ComputationGraph representing the dual-head neural network
    */
    public ComputationGraph getModel() {
        return model;
    }
    
    /**
     * Saves the trained neural network model to disk.
     * This preserves all learned weights and biases so training progress is not lost.
     * 
     * @param path the file path where the model should be saved (e.g., "model.zip")
     * @throws IOException if the file cannot be written
    */
    public void save(String path) throws IOException {
        model.save(new File(path), true);
    }
    
    /**
     * Loads a previously saved neural network model from disk.
     * This method creates a new AlphaZeroNet instance with the specified architecture,
     * then overwrites its parameters with the saved weights.
     * 
     * @param path the file path to the saved model
     * @param size the board size the model was trained on
     * @return a new AlphaZeroNet instance with weights loaded from the file
     * @throws IOException if the file cannot be read or is corrupted
    */
    public static AlphaZeroNet load(String path, int size) throws IOException {
        AlphaZeroNet net = new AlphaZeroNet(size);
        net.model = ComputationGraph.load(new File(path), true);
        return net;
    }
}