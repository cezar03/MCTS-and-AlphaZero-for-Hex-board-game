public class Backpropagation {
    /*
    This method is in charge of updating the statistics (like wins and visits) for each node and bring them up to the root. 
     */
    public void backpropagate(Node node, int winner){
        Node current = node; 
        while (current != null){
            current.visits += 1; // Each time this node is visited during backpropagation, the visits increase
            if (winner == 0) {
                current.wins += 0.5;
            }               
            if (current.playerThatMoved == winner){
                current.wins += 1.0; 
            }
            // else: if the player lost 0 is added. 
            current = current.parent; // move one level up to the parent node. 
        }
    }
}
