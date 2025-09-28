import java.util.Scanner;
import java.util.Random;

// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.initialize(board.board);
        Player instance = new Player();
        int x=1;
        instance.p_name1 = instance.inputname(instance.p_name1, x); x++;
         instance.p_name2= instance.inputname(instance.p_name2, x); x=0;
        instance.color(instance.p_name1,instance.p_name2, instance.color1,  instance.color2);


            instance.getMove(instance.p_name1, instance.p_name2,board.board );

    }}