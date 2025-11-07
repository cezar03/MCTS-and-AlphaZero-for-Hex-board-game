package AI.mcts;
public class Move {
    public String coordinate; // move coordinate

    // constructor 
    public Move(String coordinate){
        this.coordinate = coordinate; 
    }

    // getter 
    public String getCoordinate(){
        return coordinate; 
    }
}
