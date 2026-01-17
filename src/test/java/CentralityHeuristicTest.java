import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.Optimazation.Heuristic.CentralityHeuristic;
import Game.Board;
import Game.BoardAdapter;
import Game.Player;

class CentralityHeuristicTest {

    @Test
    void scoreCenterMoveTest() {
        Board b = new Board(5); // Center = (2,2)
        GameState state = new GameState(new BoardAdapter(b), Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double s = h.score(state, new Move(2, 2));
        assertEquals(1.0, s, 1e-12);
    }

    @Test
    void scoreNearCornerZeroTest() {
        Board b = new Board(5); // Center = (2,2)
        GameState state = new GameState(new BoardAdapter(b), Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double s = h.score(state, new Move(0, 0));

        assertTrue(s >= 0.0);
        assertTrue(s < 1e-6, "Corner score should be =0");
    }

    @Test
    void scoreCloseCenterTest() {
        Board b = new Board(5);
        GameState state = new GameState(new BoardAdapter(b), Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double center = h.score(state, new Move(2, 2));
        double near   = h.score(state, new Move(2, 1)); 
        double far    = h.score(state, new Move(0, 0)); 

        assertTrue(center > near);
        assertTrue(near > far);
    }

    @Test
    void scoreOnSymmetricBoardTest() {
        Board b = new Board(5); // Center (2,2)
        GameState state = new GameState(new BoardAdapter(b), Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double top = h.score(state, new Move(0, 2));
        double bot = h.score(state, new Move(4, 2));

        assertEquals(top, bot, 1e-12);
    }

    @Test
    void scoreEvenBoardTest() {
        Board b = new Board(4); // Center = (1.5, 1.5)
        GameState state = new GameState(new BoardAdapter(b), Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        
        double s11 = h.score(state, new Move(1, 1));
        double s22 = h.score(state, new Move(2, 2));

        assertEquals(s11, s22, 1e-12);
        assertTrue(s11 < 1.0, "On even board if any cell is at center then score < 1");
    }

    @Test
    void scoreTest() {
        Board b = new Board(6);
        GameState state = new GameState(new BoardAdapter(b), Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double s1 = h.score(state, new Move(0, 0));
        double s2 = h.score(state, new Move(5, 5));
        double s3 = h.score(state, new Move(3, 2));

        assertTrue(s1 >= 0.0 && s1 <= 1.0);
        assertTrue(s2 >= 0.0 && s2 <= 1.0);
        assertTrue(s3 >= 0.0 && s3 <= 1.0);
    }

    @Test
    void outOfBoundsTest() {
        Board b = new Board(5);
        GameState state = new GameState(new BoardAdapter(b), Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

  
        double s = h.score(state, new Move(-10, -10));
        assertTrue(s < 0.0, "Out of bounds move can give negative score");
    }
}
