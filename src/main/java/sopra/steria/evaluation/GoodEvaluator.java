package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BPiece;

import java.util.function.Predicate;


public class GoodEvaluator implements Evaluator {

    @Override
    public int evaluate(BBoard board) {
        int score = 0;

        // Me take opponent material good
        int whiteMaterial = getScore(board, BPiece::isWhite);
        int blackMaterial = getScore(board, piece -> !BPiece.isWhite(piece));

        int whitePosition = determinePointsPosition(board, BPiece::isWhite);
        int blackPosition = determinePointsPosition(board, piece -> !BPiece.isWhite(piece));

        score += whiteMaterial - blackMaterial;
        score += whitePosition - blackPosition;

        return board.isWhiteToMove() ? score : -score;
    }

    private int getScore(BBoard board, Predicate<Integer> predicate) {
        int[] posities = board.getPieceBoards();
        int score = 0;
        for (int bezetting : posities) {
            if (bezetting > 0) {
                if (predicate.test(bezetting)) {
                    score += getMaterialValue(bezetting);
                }
            }
        }
        return score;
    }

    public static int getMaterialValue(int piece) {
        int pieceType = BPiece.getPieceType(piece);
        return switch (pieceType) {
            case BPiece.pawn -> 1;
            case BPiece.knight -> 3;
            case BPiece.bishop -> 3;
            case BPiece.rook -> 5;
            case BPiece.queen -> 9;
            case BPiece.king -> 6;
            default -> throw new IllegalArgumentException("Invalid piece: " + pieceType);
        };
    }

    public int determinePointsPosition(BBoard board, Predicate<Integer> predicate) {
        // 1. Get the current overview of the board
        int[] pieces = board.getPieceBoards();
        int points = 0;

        // 2. Loop through the whole board, check if a piece exists on the square (= 1)
        for (int square = 0; square < 64; square++) {
            int piece = pieces[square];
            // 3. If piece exists (not 0), get the type and get the points
            if (piece != 0 && predicate.test(piece)) {
                int type = BPiece.getPieceType(piece);

                // 4. If the piece is not white, mirror the PST
                int position = BPiece.isWhite(piece) ? square : square ^ 56;

                // 5. Add the total points based on the position
                switch (type) {
                    case 1:
                        points += pawnPST[position]; break;
                    case 2:
                        points += knightPST[position]; break;
                    case 3:
                        points += bishopPST[position]; break;
                    case 4:
                        points += rookPST[position]; break;
                    case 5:
                        points += queenPST[position]; break;
                    case 6:
                        points += kingPST[position]; break; // Points based on phase of chess game
                }
            }
        }
        return points;
    }

    // King has different PST's depending on the phase of the board, therefore evaluate with the no. of pieces
    public int determinePointsKing(int position) {
        // TODO - Points of king differ per phase
        return 0;
    }

    /**
     * Create generic PST's for each piece:
     * 1: Pion → Pawn
     * 2: Paard → Knight
     * 3: Loper → Bishop
     * 4: Toren → Rook
     * 5: Dame → Queen
     * 6: Koning → King
     */
    // 1: Pawns are encouraged to stay in the center and advance forward
    public int[] pawnPST = {
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 27, 27, 10, 5, 5,
            0, 0, 0, 25, 25, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -25, -25, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    // 2: Knights are encouraged to control the center and stay away from edges to increase mobility
    public static int[] knightPST = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -20, -30, -30, -20, -40, -50,
    };

    // 3: Bishops are also encouraged to control the center and stay away from edges and corners
    public int[] bishopPST = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -40, -10, -10, -40, -10, -20,
    };

    // 4: Rook gets bonus on the 7th row, less dependent from the centre compared to other pieces
    public int[] rookPST = {
            0, 0, 5, 10, 10, 5, 0, 0,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            5, 10, 10, 10, 10, 10, 10, 5,
            0, 0, 0, 5, 5, 0, 0, 0
    };

    // 5: Queen gets penalty at the border, less bonus in the middle (more mobility)
    public int[] queenPST = {
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 5, 5, 5, 5, 5, 5, -10,
            -5, 5, 5, 5, 5, 5, 5, -5,
            0, 5, 5, 5, 5, 5, 5, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
    };

    // 6A: PST for middle of the game (needs to be safeguarded)
    public int[] kingPST = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, 0, 0, 0, 0, 20, 20,
            20, 30, 10, 0, 0, 10, 30, 20
    };

    // 6B: PST for the end of the game (needs to be active)
    public int[] kingPSTEndGame = {
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50
    };
}
