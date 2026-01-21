package AI.alphazero.mcts;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.math3.distribution.GammaDistribution;

import AI.alphazero.batch.Batcher;
import AI.alphazero.batch.NeuralNetBatcher;
import AI.alphazero.config.AlphaZeroConfig;
import AI.alphazero.net.BoardEncoder;
import AI.mcts.Node;
import game.core.Board;
import game.core.Color;
import game.core.Move;

/**
 * Implements a Monte Carlo Tree Search (MCTS) specifically designed for AlphaZero.
 * <p>
 * Unlike standard MCTS, this implementation uses a neural network to guide the search.
 * The network provides:
 * <ul>
 * <li><b>Prior probabilities (Policy)</b>: Used to bias selection towards promising moves.</li>
 * <li><b>Value evaluation</b>: Used to estimate the win probability of leaf nodes without random rollouts.</li>
 * </ul>
 * This class also supports adding Dirichlet noise to the root node during training
 * to encourage exploration.
 */

public class AlphaZeroMCTS {
    private final Batcher batcher;
    private final int boardSize;

    private final double cPuct;
    private final double dirEps;
    private final double dirAlpha;

    private final GammaDistribution gammaDist;

    /**
     * Constructs an AlphaZero MCTS instance.
     * * @param batcher the batching service used to query the neural network
     * @param cfg the configuration containing hyperparameters like CPUCT
     */
    public AlphaZeroMCTS(Batcher batcher, AlphaZeroConfig cfg) {
        this.batcher = batcher;
        this.boardSize = cfg.getBoardSize();

        this.cPuct = cfg.getCpuct();
        this.dirEps = 0.25;
        this.dirAlpha = 0.10;

        this.gammaDist = new GammaDistribution(dirAlpha, 1.0);
    }

    /**
     * Performs the MCTS search process.
     * <p>
     * If {@code training} is true, Dirichlet noise is added to the root node's priors
     * to ensure diverse move selection during self-play.
     * * @param rootBoard the current state of the board
     * @param rootPlayer the player whose turn it is
     * @param iterations the number of MCTS simulations to perform
     * @param training true if training (adds noise), false if playing/evaluating (deterministic)
     * @return the root {@link Node} of the search tree after simulations are complete
     */
    public Node search(Board rootBoard, Color rootPlayer, int iterations, boolean training) {
        Node root = new Node(null, null, rootPlayer == Color.RED ? 1 : 2);
        expandAndEvaluate(root, rootBoard, rootPlayer);
        if (training) {
            addDirichletNoiseToRoot(root);
        }
        Board workingBoard = rootBoard.fastCopy();

        for (int i = 0; i < iterations; i++) {
            Node node = root;
            Color currentPlayer = rootPlayer;
            int movesMade = 0;

            while (!node.children.isEmpty()) {
                node = selectBestChild(node);
                if (currentPlayer == Color.RED) {
                    workingBoard.getMoveRed(node.move.row, node.move.col, null);
                } else {
                    workingBoard.getMoveBlack(node.move.row, node.move.col, null);
                }
                movesMade++;
                currentPlayer = (currentPlayer == Color.RED) ? Color.BLACK : Color.RED;
            }
            double value;
            if (!workingBoard.isTerminal()) {
                value = expandAndEvaluate(node, workingBoard, currentPlayer);
            } else {
                boolean redWon = workingBoard.redWins();
                if (redWon) {
                    value = (currentPlayer == Color.RED) ? 1.0 : -1.0;
                } else {
                    value = (currentPlayer == Color.BLACK) ? 1.0 : -1.0; 
                }
            }
            backpropagate(node, value);
            for (int j = 0; j < movesMade; j++) { workingBoard.undoMove(); }
        }

        return root;
    }

    /**
     * Backpropagates the evaluation value from a leaf node up to the root.
     * @param node the leaf node from which to start backpropagation
     * @param value the evaluation value to propagate (from the perspective of the current player at the leaf)
     */
    private void backpropagate(Node node, double value) {
        while (node != null) {
            node.visits++;
            node.wins += value; 
            value = -value;
            node = node.parent;
        }
    }

    /**
     * Expands the given node by querying the neural network for policy and value.
     * Creates child nodes for each legal move with prior probabilities from the policy.
     * @param node the node to expand
     * @param board the current board state at this node
     * @param player the player to move at this node
     * @return the value evaluation from the neural network
     */
    private double expandAndEvaluate(Node node, Board board, Color player) {
        float[] inputBoard = BoardEncoder.encode(board, player);
        if (node.parent == null && node.cachedEncoding == null) {
            node.cachedEncoding = inputBoard.clone();
        }
        NeuralNetBatcher.Output output;
        try {
            output = batcher.predict(inputBoard).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error during Neural Net Inference", e);
        }
        float[] policy = output.policy;
        double value = output.value;
        List<int[]> legalMoves = board.legalMoves();
        double policySum = 0;
        for (int[] move : legalMoves) {
            int row = move[0];
            int col = move[1];
            int idx;
            if (player == Color.RED) {
                idx = row * boardSize + col;
            } else {
                idx = col * boardSize + row;
            }
            
            double prob = policy[idx];
            
            Move newMove = Move.get(row, col);
            Node child = new Node(newMove, node, 0);
            child.priorProbability = prob;
            
            node.children.put(newMove, child);
            policySum += prob;
        }

        if (policySum > 0) {
            for (Node child : node.children.values()) {
                child.priorProbability /= policySum;
            }
        } else {
            double uniform = 1.0 / Math.max(1, node.children.size());
            for (Node child : node.children.values()) child.priorProbability = uniform;
        }

        return value;
    }

    /**
     * Selects the best child node using the PUCT formula.
     * @param parent the parent node from which to select a child
     * @return the selected child node
     */
    private Node selectBestChild(Node parent) {
        Node bestChild = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double sqrtParentVisits = Math.sqrt(parent.visits);

        for (Node child : parent.children.values()) {
            double Q = (child.visits > 0) ? -(child.wins / child.visits) : 0;
            double U = cPuct * child.priorProbability * (sqrtParentVisits / (1 + child.visits));
            double score = Q + U; 

            if (score > bestScore) {
                bestScore = score;
                bestChild = child;
            }
        }
        return bestChild;
    }

    /**
     * Generates a probability distribution (policy) over all possible moves based on visit counts.
     * <p>
     * The temperature parameter controls the exploration/exploitation balance:
     * <ul>
     * <li><b>High temperature (~1.0)</b>: Probability is proportional to visit counts (used in early training).</li>
     * <li><b>Low temperature (~0.0)</b>: Probability concentrates on the most visited move (used in competitive play).</li>
     * </ul>
     * * @param root the root node of the search tree
     * @param temperature the temperature parameter controlling distribution sharpness
     * @return an array of probabilities corresponding to the flattened board indices
     */
    public double[] getSearchPolicy(Node root, double temperature) {
        int size = boardSize * boardSize;
        double[] policy = new double[size];

        if (temperature < 0.01) {
            int bestIdx = -1;
            int maxVisits = -1;
            for (var entry : root.children.entrySet()) {
                if (entry.getValue().visits > maxVisits) {
                    maxVisits = entry.getValue().visits;
                    Move m = entry.getKey();
                    bestIdx = m.row * boardSize + m.col;
                }
            }
            if (bestIdx != -1) policy[bestIdx] = 1.0;
            return policy;
        }

        double sum = 0;
        for (var entry : root.children.entrySet()) {
            Move m = entry.getKey();
            Node child = entry.getValue();

            double visits = Math.pow(child.visits, 1.0 / temperature);
            int idx = m.row * boardSize + m.col;
            policy[idx] = visits;
            sum += visits;
        }

        if (sum > 0) {
            for (int i = 0; i < size; i++) policy[i] /= sum;
        }
        return policy;
    }

    /**
     * Adds Dirichlet noise to the root node's prior probabilities to encourage exploration.
     * @param root the root node of the search tree
     */
    private void addDirichletNoiseToRoot(Node root) {
        int k = root.children.size();
        if (k == 0) return;
        double[] noise = new double[k];
        double sum = 0.0;

        for (int i = 0; i < k; i++) {
            noise[i] = gammaDist.sample();
            sum += noise[i];
        }

        int i = 0;
        double priorSum = 0.0;
        for (Node child : root.children.values()) {
            double n = noise[i] / sum; // Normalize noise
            child.priorProbability = (1 - dirEps) * child.priorProbability + dirEps * n;
            priorSum += child.priorProbability;
            i++;
        }
        
        if (priorSum > 0) {
            for (Node child : root.children.values()) child.priorProbability /= priorSum;
        }
    }
}










