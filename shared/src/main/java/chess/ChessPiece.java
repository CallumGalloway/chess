package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        var moves = new ArrayList<ChessMove>();
        switch (this.type) {
            case BISHOP:
                int[][] bishCoords = {{1,1},{-1,1},{-1,-1},{1,-1}};
                for (int[] coord : bishCoords) {
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(piece.getBishopRookQueenMoves(board, myPosition, myPosition.getRow(), myPosition.getColumn(),x,y));
                }
                return moves;
            case ROOK:
                int[][] rookCoords = {{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] coord : rookCoords) {
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(piece.getBishopRookQueenMoves(board, myPosition, myPosition.getRow(), myPosition.getColumn(),x,y));
                }
                return moves;
            case QUEEN:
                int[][] queenCoords = {{1,1},{-1,1},{-1,-1},{1,-1},{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] coord : queenCoords) {
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(piece.getBishopRookQueenMoves(board, myPosition, myPosition.getRow(), myPosition.getColumn(),x,y));
                }
                return moves;
            case KING:
                moves.addAll(piece.getKingMoves(board, myPosition));
                return moves;
            case KNIGHT:
                moves.addAll(piece.getKnightMoves(board, myPosition));
                return moves;
            case PAWN:
                moves.addAll(piece.getPawnMoves(board, myPosition));
                return moves;
            default:
                return null;
        }
    }

   private Collection<ChessMove> getBishopRookQueenMoves(ChessBoard board, ChessPosition myPosition, int row, int col, int x, int y) {
        //function that can be called recursively to check each line in a specified x/y
        var moves = new ArrayList<ChessMove>();
        var newPosition = new ChessPosition(row,col);

        if (((row > 8) || (col > 8)) || ((row < 1) || (col < 1))){
            return moves;
        }

       ChessPiece piece = board.getPiece(myPosition);
       ChessPiece newPiece = board.getPiece(newPosition);

        if (row == myPosition.getRow() & col == myPosition.getColumn()){
            moves.addAll(getBishopRookQueenMoves(board,myPosition,row+x, col+y, x, y));
            return moves;
        }
        if (newPiece != null){
            if (newPiece.getTeamColor()==piece.getTeamColor()){
                return moves;
            }
            else{
                moves.add(new ChessMove(myPosition,new ChessPosition(row,col),null));
                return moves;
            }
        }

        moves.add(new ChessMove(myPosition,new ChessPosition(row,col),null));
        moves.addAll(getBishopRookQueenMoves(board,myPosition,row+x, col+y, x, y));
        return moves;
    }

    private Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(myPosition);
        int[][] kingCoords = {{1,1},{-1,1},{-1,-1},{1,-1},{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] coord : kingCoords) {
            int x = coord[0];
            int y = coord[1];
            int newRow = myPosition.getRow()+y;
            int newCol = myPosition.getColumn()+x;
            var newPosition = new ChessPosition(newRow,newCol);
            if (newRow < 8 && newRow > 1 && newCol < 8 && newCol > 1) {
                // if in board range, then we have to check for pieces
                ChessPiece newPiece = board.getPiece(newPosition);
                if (newPiece != null) {
                    if (newPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(newRow, newCol), null));
                    }
                }
                else {
                    moves.add(new ChessMove(myPosition, new ChessPosition(newRow, newCol), null));
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(myPosition);
        return moves;
    }

    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(myPosition);
        return moves;
    }

}
