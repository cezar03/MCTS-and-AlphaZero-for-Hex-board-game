package Game;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Manages move registration, validation, and history tracking for a Hex game.
 * <p>
 * This class handles:
 * <ul>
 * <li>Recording all moves made during the game</li>
 * <li>Validating move legality before registration</li>
 * <li>Managing turn alternation between players</li>
 * <li>Providing move history display and undo functionality</li>
 * <li>Interactive move input from console</li>
 * </ul>
 * <p>
 * Players are represented as integers: 1 for red, 2 for black (blue).
 */
public class MoveRegistration {
    /** List storing all moves made during the game in chronological order. */
    private ArrayList<String> moves = new ArrayList<>();

    /** The current player's turn (1 for red, 2 for black). */
    private int currentPlayer = 1;

    /**
     * Attempts to register a move on the board at the specified coordinates.
     * <p>
     * This method validates that the move is within board bounds and targets an empty
     * cell. If valid, it places the current player's mark, records the move in history,
     * and switches to the other player's turn.
     * 
     * @param board the game board represented as a 2D integer array
     * @param row the row coordinate of the move
     * @param col the column coordinate of the move
     * @return true if the move was valid and successfully registered, false if invalid
     */
    public boolean registerMove (int[][] board, int row, int col){ // verify that the position is in the limits of the board
        if (row < 0 || col < 0 || row >= board.length || col >= board[0].length){
            System.out.println("This movement is out of the board!");
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
    
    /**
     * Prompts the current player to input a move via the console and registers it.
     * <p>
     * This method repeatedly prompts the player until a valid move is entered.
     * Invalid moves (out of bounds or occupied cells) display error messages and
     * request new input.
     * 
     * @param board the game board represented as a 2D integer array
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

    /**
     * Displays the complete history of all moves made during the game.
     * Each move is printed in the format "PlayerN:(row,col)".
     * If no moves have been made, a corresponding message is displayed.
     */
    public void printMoveHistory() {
        if (moves.isEmpty()) {
            System.out.println("No moves have been made yet.");
            return;
        }
        System.out.println("Move history:");
        for (String move : moves) {
            System.out.println(" - " + move);
        }
    }

    /**
     * Undoes the most recent move by removing it from history and clearing the
     * corresponding cell on the board.
     * <p>
     * This method parses the last move from the history, clears that cell on the board,
     * and switches back to the player who made the undone move (so they can make a
     * different move).
     * 
     * @param board the Board instance to modify
     * @return true if a move was successfully undone, false if there were no moves to undo
     */
    public boolean undoLastMove(Board board) {
        if (moves.isEmpty()) {
            System.out.println("No moves to undo.");
            return false;
        }

        String lastMove = moves.remove(moves.size() - 1);
        String[] parts = lastMove.replaceAll("[^0-9,]", "").split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        //clear the cell
        board.clearCell(row, col);

        //switch back to player who has undone the move
        currentPlayer = (currentPlayer == 1) ? 2 : 1;

        System.out.println("Undoing move at (" + row + ", " + col + ")");
        return true;
    }

    /**
     * Returns the complete list of moves made during the game.
     * 
     * @return an ArrayList containing all moves in chronological order, each formatted
     *         as "PlayerN:(row,col)"
     */
    public ArrayList<String> getMoves(){
        return moves;
    }
}
