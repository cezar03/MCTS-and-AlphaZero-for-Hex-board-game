package ai.alphazero.net;

public class TrainingExampleData {
    public float[] inputBoard;
    public float[] targetPolicy;
    public float[] targetValue;

    public TrainingExampleData(float[] input, float[] policy, float value) {
        this.inputBoard = input;
        this.targetPolicy = policy;
        this.targetValue = new float[]{ value };
    }
}










