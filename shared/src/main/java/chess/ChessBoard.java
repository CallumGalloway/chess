package chess;

import java.util.Arrays;
import java.util.Objects;

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
        squares[position.getRow()-1][position.getColumn()-1] = piece;
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
     * Empties a square on the chess board
     *
     * Used to make a space vacant after a move is made
     */
    public void emptyPiece(ChessPosition position) {
        squares[position.getRow()-1][position.getColumn()-1] = null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int r = 1 ; r <= 8 ; r++){
            for (int c = 1 ; c <= 8 ; c++){
                ChessPiece.PieceType pieceToPut = null;
                //pawns
                if (r == 2) {
                    addPiece(new ChessPosition(r,c), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
                }
                else if (r == 7) {
                    addPiece(new ChessPosition(r,c), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
                }
                //empty rows
                else if (r == 3 || r == 4 || r == 5) {
                    addPiece(new ChessPosition(r,c), null);
                }
                //back rows
                else if (r == 1 || r == 8) {
                    switch (c) {
                        case 1, 8 -> pieceToPut = ChessPiece.PieceType.ROOK;
                        case 2, 7 -> pieceToPut = ChessPiece.PieceType.KNIGHT;
                        case 3, 6 -> pieceToPut = ChessPiece.PieceType.BISHOP;
                        case 4    -> pieceToPut = ChessPiece.PieceType.QUEEN;
                        case 5    -> pieceToPut = ChessPiece.PieceType.KING;
                    }
                    if (r == 1){
                        addPiece(new ChessPosition(r,c), new ChessPiece(ChessGame.TeamColor.WHITE, pieceToPut));
                    }
                    else {
                        addPiece(new ChessPosition(r,c), new ChessPiece(ChessGame.TeamColor.BLACK, pieceToPut));
                    }
                }
            }
        }
    }

    public ChessBoard copy() {
        ChessBoard copy = new ChessBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col ++) {
                ChessPiece piece = this.squares[row][col];
                if (piece != null) {
                    ChessPiece copiedPiece = piece.copy();
                    copy.squares[row][col] = copiedPiece;
                }
                else copy.squares[row][col] = null;
            }
        }

        return copy;
    }

    @Override
    public String toString() {
        String finalString = "Board:";
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                if (squares[row][col] != null) {
                    finalString += squares[row][col].toString();
                }
            }
        }
        return finalString;
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
}
