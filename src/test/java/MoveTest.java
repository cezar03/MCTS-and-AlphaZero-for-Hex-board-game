
import AI.mcts.HexGame.Move;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    void constructorTest() {
        Move m = new Move(2, 5);
        assertEquals(2, m.row);
        assertEquals(5, m.col);
    }

    @Test
    void getCoordinatesTest() {
        Move m = new Move(0, 0);
        assertEquals("(0,0)", m.getCoordinate());

        Move m2 = new Move(10, 7);
        assertEquals("(10,7)", m2.getCoordinate());
    }

    @Test
    void toStringTest() {
        Move m = new Move(3, 4);
        assertEquals("(3,4)", m.toString());
    }

    @Test
    void hashCodeTest() {
        Move a = new Move(1, 2);
        Move b = new Move(1, 2);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentMoveTest() {
        Move a = new Move(1, 2);
        Move b = new Move(1, 3);

        assertNotEquals(a, b);
    }

    @Test
    void falseNullTest() {
        Move a = new Move(1, 2);

        assertNotEquals(a, null);
        assertNotEquals(a, "(1,2)");
    }
}
