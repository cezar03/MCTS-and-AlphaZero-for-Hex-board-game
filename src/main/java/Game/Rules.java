package Game;
public class Rules {
    public static boolean checkBoard(int i, int j){
        boolean flag=false;
        if (!(i > j || j - i > 10)){
            flag = true;
            return flag;
        }

        return flag;
    }}
