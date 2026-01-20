package ai.alphazero.mcts;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.math3.distribution.GammaDistribution;

import ai.alphazero.batch.Batcher;
import ai.alphazero.batch.NeuralNetBatcher;
import ai.alphazero.config.AlphaZeroConfig;
import ai.alphazero.net.BoardEncoder;
import ai.mcts.Node;
import game.core.Board;
import game.core.Color;
import game.core.Move;

public class AlphaZeroMCTS {
    private final Batcher batcher;
    private final int boardSize;

    private final double cPuct;
    private final double dirEps;
    private final double dirAlpha;

    private final GammaDistribution gammaDist;

    public AlphaZeroMCTS(Batcher batcher, AlphaZeroConfig cfg) {
        this.batcher = batcher;
        this.boardSize = cfg.getBoardSize();

        this.cPuct = cfg.getCpuct();
        this.dirEps = 0.25;
        this.dirAlpha = 0.10;

        this.gammaDist = new GammaDistribution(dirAlpha, 1.0);
    }

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
            for (int j = 0; j < movesMade; j++) {
                workingBoard.undoMove();
            }
        }

        return root;
    }

    private void backpropagate(Node node, double value) {
        while (node != null) {
            node.visits++;
            node.wins += value; 
            value = -value;
            node = node.parent;
        }
    }

    private double expandAndEvaluate(Node node, Board board, Color player) {
        float[] input = BoardEncoder.encode(board, player);
        if (node.parent == null && node.cachedEncoding == null) {
            node.cachedEncoding = input.clone();
        }
        NeuralNetBatcher.Output output;
        try {
            output = batcher.predict(input).get();
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
                idx = row * (int)boardSize + col;
            } else {
                idx = col * (int)boardSize + row;
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










