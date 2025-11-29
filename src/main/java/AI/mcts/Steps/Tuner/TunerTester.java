package AI.mcts.Steps.Tuner;

public class TunerTester {
    public static void main(String[] args) {
        int boardSize = 7;
        int iterations = 1000;
        int gamesPerSide = 10; // 20 games per config (10 as RED, 10 as BLACK)

        MCTSTuner tuner = new MCTSTuner(boardSize, iterations, gamesPerSide);
        tuner.randomSearch(20);  // try 20 random configs
    }
}
