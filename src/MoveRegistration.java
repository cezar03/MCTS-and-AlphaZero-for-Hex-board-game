import java.util.ArrayList;
import java.util.Scanner;

public class MoveRegistration {
private ArrayList<String> moves = new ArrayList<>(); // List to save all the movements done.
private int currentPlayer = 1; // Variable to know who is playing. 1 is red, 2 is blue.

/*
Method that registers the movement in the board.
It returns true when the movement is valid, false when it's not
 */
public boolean registerMove (int[][] board, int row, int col){ // verify that the position is in the limits of the board
    if (row < 0 || col < 0 || row >= board.length || col >= board[0].length){
        System.out.println("This movement is out of the board!");
        return false; // not valid movement 
    }
    if (row > col || col - row > 10){ // check of the cell follows the hexagonal board shape rules
        System.out.println("Not valid cell!");
        return false; // not valid movement 
    }
    if (board[row][col] != 0){ // verify that the cell is empty. 
        System.out.println("This cell is already taken!");
        return false; // not valid movement  
    } 
    board[row][col] = currentPlayer; // put the player's mark on the board 
    moves.add("Player" + currentPlayer + ":(" + row + "," + col + ")"); // save the move in the list. (for instance; Player1:(2,3)).

    // change player turn
    if (currentPlayer == 1){
        currentPlayer = 2;
    }
    else {
        currentPlayer = 1;
    }
    return true; // move is valid 
}
/*
Method that asks the player to input a move. 
 */
public void getMove(int[][] board){
    Scanner in = new Scanner (System.in);
    int row;
    int col;
    boolean check = false; 
    while (!check){ // loop to repeat until we get a valid input move
        System.out.print("Player" + currentPlayer + "introduce the row number selected: ");
        row = in.nextInt();
        System.out.print("Introduce the column number selected: ");
        col = in.nextInt();

        check = registerMove(board,row,col); // try to register the move and check if it's valid 

        if (!check){ // if it's an invalid movement error message will be displayed and user will have to try again 
            System.out.println("Movement invalid, try again");
        }
    }
in.close();
}
public ArrayList<String> getMoves(){
    return moves;
}
}
