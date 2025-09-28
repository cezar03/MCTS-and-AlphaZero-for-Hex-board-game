import java.util.Random;
import java.util.Scanner;

public class Player{
    String p_name1;
    String p_name2;
    int color1,color2;
    Scanner in = new Scanner(System.in);

    public String inputname(String name1, int x){
    System.out.println("Player "+x+" input your name");
    name1=in.nextLine();
    return name1;}

    public void color(String name1, String name2, int color1, int color2){
        Random rand = new Random();
        int x=rand.nextInt(2);
        if(x==0){
            System.out.println(name1 + "`s color is red");
            System.out.println(name2 + " `s color is white");
            color1=1;
            color2=2;
        }
        else {
            System.out.println(name1 + "`s color is white");
            System.out.println(name2 + " `s color is red");
            color1=2;
            color2=1;
        }

    }
    public void getMove(String name1, String name2, int[][] board){
        this.p_name1 = name1;
        this.p_name2 = name2;

        System.out.println( name1+ " select where to place your marble, row+column");
        int x=0;
        int i,j;
        while(x==0){
            i=in.nextInt();
            j=in.nextInt();
            if(Rules.checkBoard(i,j)){
                board[i][j]=1;
                Board.display(board);
                x=1;}
            else {
                System.out.println("Move is out of bounds,try again");
            }} x=0;
System.out.println();
        System.out.println(name2+ " select where to place your marble, row+column");

        while(x==0){
            i=in.nextInt();
            j=in.nextInt();
            if(Rules.checkBoard(i,j)){
                board[i][j]=1;
                Board.display(board);
                x=1;}
            else {
                System.out.println("Move is out of bounds,try again");
            }}

    }}

