package ai.mcts.Steps.Tuner;

/**
 * Represents a complete configuration of hyperparameters for MCTS with
 * move pruning and heuristic enhancements.
 * 
 * <p>This immutable class encapsulates all tunable parameters:</p>
 * <ul>
 *   <li>threshold: the pruning threshold for move filtering</li>
 *   <li>centralityWeight: weight for centrality in move evaluation</li>
 *   <li>connectivityWeight: weight for connectivity in move evaluation</li>
 *   <li>biasScale: scaling factor for heuristic bias in node selection</li>
 *   <li>spWeight: weight for shortest path heuristic</li>
 *   <li>cExploration: exploration constant in the UCT formula</li>
 * </ul>
 */
public class PrunerConfig {
    public final double threshold;
    public final double centralityWeight;
    public final double connectivityWeight;
    public final double biasScale;
    public final double spWeight;
    public final double cExploration;

    /**
     * Constructs a PrunerConfig with all hyperparameters specified.
     *
     * @param threshold the pruning threshold for move filtering
     * @param centralityWeight the weight for centrality in move evaluation
     * @param connectivityWeight the weight for connectivity in move evaluation
     * @param biasScale the scaling factor for heuristic bias
     * @param spWeight the weight for shortest path heuristic
     * @param cExploration the exploration constant for UCT
     */
    public PrunerConfig(double threshold, double centralityWeight, double connectivityWeight, double biasScale, double spWeight, double cExploration) {
        this.threshold = threshold;
        this.centralityWeight = centralityWeight;
        this.connectivityWeight = connectivityWeight;
        this.biasScale = biasScale;
        this.spWeight = spWeight;
        this.cExploration = cExploration;
    }

    /**
     * Returns a formatted string representation of this configuration showing
     * all parameter values with three decimal places.
     *
     * @return a string representation of the configuration
     */
    @Override
    public String toString() {
        return String.format(
            "thr=%.3f, cent=%.3f, conn=%.3f, bias=%.3f, sp=%.3f, c=%.3f",
            threshold, centralityWeight, connectivityWeight,
            biasScale, spWeight, cExploration
        );
    }
}











