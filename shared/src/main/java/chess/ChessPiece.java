package chess;

import java.util.ArrayList;
import java.util.Collection;
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
    private transient ChessBoard board;
    private ChessPosition myPosition;
    private int row;
    private int col;
    private int x;
    private int y;

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
        return String.format("%s %s",pieceColor, type);
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
        this.board = board;
        this.myPosition = myPosition;
        ArrayList<ChessMove> moves = new ArrayList<>();

        ChessPiece piece = board.getPiece(myPosition);

        switch (piece.getPieceType()){
            case BISHOP:
                int bishCoords[][] = {{1,1},{1,-1},{-1,-1},{-1,1}};
                for (int[] coord : bishCoords){
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(getBishopRookQueenMoves(board, myPosition, myPosition.getRow(), myPosition.getColumn(), x, y));
                }
                return moves;
            case ROOK:
                int rookCoords[][] = {{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] coord : rookCoords){
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(getBishopRookQueenMoves(board, myPosition, myPosition.getRow(), myPosition.getColumn(), x, y));
                }
                return moves;
            case QUEEN:
                int queenCoords[][] = {{1,1},{1,-1},{-1,-1},{-1,1},{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] coord : queenCoords){
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(getBishopRookQueenMoves(board, myPosition, myPosition.getRow(), myPosition.getColumn(), x, y));
                }
                return moves;
            case KING:
                int kingCoords[][] = {{1,1},{1,-1},{-1,-1},{-1,1},{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] coord : kingCoords){
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(getKingKnightMoves(board, myPosition, x, y));
                }
                return moves;
            case KNIGHT:
                int knightCoords[][] = {{1,2},{1,-2},{-1,2},{-1,-2},{2,1},{2,-1},{-2,1},{-2,-1}};
                for (int[] coord : knightCoords){
                    int x = coord[0];
                    int y = coord[1];
                    moves.addAll(getKingKnightMoves(board, myPosition, x, y));
                }
                return moves;
            case PAWN:
                moves.addAll(getPawnMoves(board,myPosition));
                return moves;
            default:
                return null;
        }
    }

    private Collection<ChessMove> getBishopRookQueenMoves(ChessBoard board, ChessPosition myPosition, int row, int col, int x, int y) {

        ChessPosition newPosition = new ChessPosition(row+y, col+x);

        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);

        if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 && newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8){
            ChessPiece newPiece = board.getPiece(newPosition);
            if (newPiece != null) {
                if (newPiece.getTeamColor() != piece.getTeamColor()){
                    moves.add(new ChessMove(myPosition,newPosition,null));
                    return moves;
                }
                else {
                    return moves;
                }
            }
            else {
                moves.add(new ChessMove(myPosition,newPosition,null));
                moves.addAll(piece.getBishopRookQueenMoves(board, myPosition, row+y, col+x, x, y));
                return moves;
            }
        }
        else {
            return moves;
        }
    }

    private Collection<ChessMove> getKingKnightMoves(ChessBoard board, ChessPosition myPosition, int x, int y) {
        ChessPosition newPosition = new ChessPosition(myPosition.getRow() + y, myPosition.getColumn() + x);

        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);

        if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 && newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8){
            ChessPiece newPiece = board.getPiece(newPosition);
            if (newPiece != null) {
                if (newPiece.getTeamColor() != piece.getTeamColor()){
                    moves.add(new ChessMove(myPosition,newPosition,null));
                    return moves;
                }
                else {
                    return moves;
                }
            }
            else {
                moves.add(new ChessMove(myPosition,newPosition,null));
                return moves;
            }
        }
        else {
            return moves;
        }
    }

    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {
        int direction = pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
        boolean starting = (myPosition.getRow() == 2 && direction == 1) || (myPosition.getRow() == 7 && direction == -1);
        ArrayList<ChessMove> moves = new ArrayList<>();

        ArrayList<PieceType> promotions = new ArrayList<>();
        promotions.add(PieceType.QUEEN);
        promotions.add(PieceType.BISHOP);
        promotions.add(PieceType.KNIGHT);
        promotions.add(PieceType.ROOK);

        if (starting) {
            ChessPosition newPositionOne = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
            ChessPiece newPieceOne = board.getPiece(newPositionOne);
            ChessPosition newPositionTwo = new ChessPosition(myPosition.getRow() + 2*direction, myPosition.getColumn());
            ChessPiece newPieceTwo = board.getPiece(newPositionTwo);
            if (newPieceOne == null && newPieceTwo == null){
                moves.add(new ChessMove(myPosition,newPositionTwo,null));
            }
        }
        int[] threeCols = {-1,0,1};
        for (int col : threeCols) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + col);
            boolean promote = (myPosition.getRow() + direction == 8) || (myPosition.getRow() + direction == 1);
            if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 && newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8){
                ChessPiece newPiece = board.getPiece(newPosition);
                if (newPiece != null && col != 0 && pieceColor != newPiece.getTeamColor()) {
                    if (promote) {
                        for (PieceType promotion : promotions){
                            moves.add(new ChessMove(myPosition, newPosition, promotion));
                        }
                    }
                    else {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
                if (newPiece == null && col == 0) {
                    if (promote) {
                        for (PieceType promotion : promotions){
                            moves.add(new ChessMove(myPosition, newPosition, promotion));
                        }
                    }
                    else {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }
        return moves;
    }

    public ChessPiece copy() {
        return new ChessPiece(this.pieceColor, this.type);
    }

}
