public class Board {
    //this creates the game board and initializez it
    int[][] board = new int[11][21];
    public void initialize(int[][] board) {
        this.board = board;
        for (int i = 0; i<11; i++) {
            for (int j = 0; j < 21; j++) {
                if (i > j || j - i > 10) {
                    board[i][j] = -1;
                }
                else {
                    board[i][j] = 0;
                }}
        }
    }
    public static int getcell(int[][] board, int i, int j){

        return board[i][j];

    }
    // this displays our board without the values out of the bounds of the game board(-1)
    public static void display(int[][] board){

        for (int i = 0; i<11; i++) {
            System.out.println();
            for (int j = 0; j<21; j++) {
                if ((i > j || j - i > 10)== false){
                    System.out.print(getcell(board, i, j)+ " ");
                } else {
                    System.out.print(" ");
}}}}}
