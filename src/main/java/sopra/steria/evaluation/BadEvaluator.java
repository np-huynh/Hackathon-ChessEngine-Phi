package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;

public class BadEvaluator implements Evaluator {
    private static final int PIECE_VALUE = 100;

    @Override
    public int evaluate(BBoard board) {
        int score = 0;

        // Me take opponent material good
        int whiteMaterial = PIECE_VALUE * Long.bitCount(board.getColorBitboard(BBoard.whiteIndex));
        int blackMaterial = PIECE_VALUE * Long.bitCount(board.getColorBitboard(BBoard.blackIndex));
        score += whiteMaterial - blackMaterial;

        return board.isWhiteToMove() ? score : -score;
    }
}
