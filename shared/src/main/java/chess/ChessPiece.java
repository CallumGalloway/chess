package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

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

    @Override
    public String toString() {
        return "ChessPiece{" + pieceColor + type + '}';
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
                int[][] kingCoords = {{1,1},{-1,1},{-1,-1},{1,-1},{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] coord : kingCoords) {
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(piece.getKingKnightMoves(board, myPosition, x, y));
                }
                return moves;
            case KNIGHT:
                int[][] knightCoords = {{1,2},{1,-2},{-1, 2},{-1,-2},{2,1},{2,-1},{-2,1},{-2,-1}};
                for (int[] coord : knightCoords) {
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(piece.getKingKnightMoves(board, myPosition, x, y));
                }
                return moves;
            case PAWN:
                boolean startingPos = myPosition.getRow() == 2 || myPosition.getRow() == 7;
                moves.addAll(piece.getPawnMoves(board, myPosition, startingPos));
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
                return moves;
            }
        }

        moves.add(new ChessMove(myPosition,new ChessPosition(row,col),null));
        moves.addAll(getBishopRookQueenMoves(board,myPosition,row+x, col+y, x, y));
        return moves;
    }

    private Collection<ChessMove> getKingKnightMoves(ChessBoard board, ChessPosition myPosition, int x, int y) {
        var moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(myPosition);

        int newRow = myPosition.getRow()+y;
        int newCol = myPosition.getColumn()+x;
        var newPosition = new ChessPosition(newRow,newCol);
        if (newRow <= 8 && newRow >= 1 && newCol <= 8 && newCol >= 1) {
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
        return moves;
    }

    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition, boolean startingPos) {
        var moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(myPosition);

        ArrayList<ChessPiece.PieceType> promotions = new ArrayList<>();
        promotions.add(PieceType.QUEEN);
        promotions.add(PieceType.BISHOP);
        promotions.add(PieceType.ROOK);
        promotions.add(PieceType.KNIGHT);

        int newRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? myPosition.getRow() + 1 : myPosition.getRow() - 1;
        int specRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? myPosition.getRow() + 2 : myPosition.getRow() - 2;
        int attackR = myPosition.getColumn() + 1;
        int attackL = myPosition.getColumn() - 1;
        var newPosition = new ChessPosition(newRow, myPosition.getColumn());
        var attackRPos = new ChessPosition(newRow, attackR);
        var attackLPos = new ChessPosition(newRow, attackL);

        if (newRow >= 1 && newRow <= 8){
            ChessPiece movePiece = board.getPiece(newPosition);
            ChessPiece attackRPiece = myPosition.getColumn() == 8 ? null : board.getPiece(attackRPos);
            ChessPiece attackLPiece = myPosition.getColumn() == 1 ? null : board.getPiece(attackLPos);
            ChessPiece.PieceType promote = null;
            if (movePiece == null){
                if (newRow == 1 || newRow == 8) {
                    for (PieceType promotion : promotions){
                        moves.add(new ChessMove(myPosition, newPosition, promotion));
                    }
                }
                else{
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
            if (((startingPos) && (piece.getTeamColor() == ChessGame.TeamColor.WHITE) && (myPosition.getRow() == 2))
            || ((startingPos) && (piece.getTeamColor() == ChessGame.TeamColor.BLACK)) && (myPosition.getRow() == 7)){
                var specPos = new ChessPosition(specRow, myPosition.getColumn());
                ChessPiece specPiece = board.getPiece(specPos);
                if (specPiece == null && movePiece == null)
                    moves.add(new ChessMove(myPosition, specPos, null));
            }
            if (attackRPiece != null && attackRPiece.getTeamColor() != piece.getTeamColor()){
                if (newRow == 1 || newRow == 8) {
                    for (PieceType promotion : promotions){
                        moves.add(new ChessMove(myPosition, attackRPos, promotion));
                    }
                }
                else{
                    moves.add(new ChessMove(myPosition, attackRPos, null));
                }
            }
            if (attackLPiece != null && attackLPiece.getTeamColor() != piece.getTeamColor()){
                if (newRow == 1 || newRow == 8) {
                    for (PieceType promotion : promotions){
                        moves.add(new ChessMove(myPosition, attackLPos, promotion));
                    }
                }
                else{
                    moves.add(new ChessMove(myPosition, attackLPos, null));
                }
            }
        }

        return moves;
    }

}
