package AI.mcts.Steps.Tuner;

public class PrunerConfig {
        public final double threshold;
        public final double centralityWeight;
        public final double connectivityWeight;

        public PrunerConfig(double threshold, double centralityWeight, double connectivityWeight) {
            this.threshold = threshold;
            this.centralityWeight = centralityWeight;
            this.connectivityWeight = connectivityWeight;
        }

        @Override
        public String toString() {
            return String.format("thr=%.3f, cent=%.3f, conn=%.3f",
                    threshold, centralityWeight, connectivityWeight);
        }
    }
