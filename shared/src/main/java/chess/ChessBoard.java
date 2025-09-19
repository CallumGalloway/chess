package chess;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int y = 1; y < 9; y++) {
            for (int x = 1; x < 9; x++) {
                switch (y) {
                    case 1://y is 1, add white pieces in pairs except royalty depending on x
                        switch (x) {
                            case 1, 8 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
                            case 2, 7 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
                            case 3, 6 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
                            case 4 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
                            case 5 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
                        }
                        break;
                    case 2://y is 2, add all white pawns
                        addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
                        break;
                    case 7://y is 7, add all black pawns
                        addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
                        break;
                    case 8://y is 8, add black pieces in pairs except royalty depending on x
                        switch (x) {
                            case 1, 8 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
                            case 2, 7 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
                            case 3, 6 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
                            case 4 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
                            case 5 -> addPiece(new ChessPosition(x, y), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
                        }
                        break;
                    default://y is 3-6, set null
                        addPiece(new ChessPosition(x, y), null);
                        break;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "squares=" + Arrays.toString(squares) +
                '}';
    }
}
