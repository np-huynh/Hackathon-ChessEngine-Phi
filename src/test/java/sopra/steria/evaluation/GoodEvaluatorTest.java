package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BPiece;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GoodEvaluatorTest {

    @Test
    void test() {
        // https://lichess.org/editor/8/8/4b3/8/1R6/5B2/8/8_w_-_-_0_1?color=white
        // witte loper en toren en zwarte loper (8 material voor wit, 3 voor zwart)
        BBoard board = new BBoard("8/8/4b3/8/1R6/5B2/8/8 w - - 0 1");

        int actual = new GoodEvaluator().evaluate(board);

        assertThat(actual).isEqualTo(5);
    }

    @Test
    void should_correspond_with_PST_white(){
        // Source: https://lichess.org/editor/8/8/1B6/8/8/7N/8/8_w_-_-_0_1?color=white
        // -30 knight, 10 for bishop

        BBoard board = new BBoard("8/8/1B6/8/8/7N/8/8 w - - 0 1");

        int whitePoints = new GoodEvaluator().determinePointsPosition(board, BPiece::isWhite);

        assertEquals(-20, whitePoints);
    }

}
