package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn = null;
    public ChessBoard board = new ChessBoard();

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece piece = this.board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }



        if (moves.size() != 0) {
            return moves;
        }
        else return null;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = this.board.getPiece(move.getStartPosition());

        if (piece == null || validMoves(move.getStartPosition()) == null) {
            throw new InvalidMoveException();
        }



        board.emptyPiece(move.getStartPosition());
        board.addPiece(move.getEndPosition(), piece);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // find king of given color and note position
        // while we go, add all enemy's moves to a list
        // check list for an endPosition that matches the king's position
        // if so, return true. else, false.
        ChessPiece[][] squares = this.board.squares;
        ChessPiece piece;
        ArrayList<ChessMove> enemyMoves = new ArrayList<>();
        enemyMoves.addAll(getEnemyMoves(teamColor));

        for (ChessMove move : enemyMoves) {
            if (move.getEndPosition() == findKing(teamColor)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // find king of given color
        // for all it's valid moves, run isInCheck algorithm on square
        // if any moves are not in check, return false. else, true
        ChessBoard board = this.board;
        ChessPosition kingPos = findKing(teamColor);
        ChessPiece king = board.getPiece(kingPos);
        ArrayList<ChessMove> kingMoves = new ArrayList<>();
        ArrayList<ChessMove> enemyMoves = new ArrayList<>();
        kingMoves.addAll(king.pieceMoves(board, kingPos));
        enemyMoves.addAll(getEnemyMoves(teamColor));
        int possible = kingMoves.size();
        int inCheck = 0;

        if (isInCheck(teamColor)) {
            for (ChessMove kingMove : kingMoves) {
                for (ChessMove enemyMove : enemyMoves) {
                    if (kingMove.getEndPosition() == enemyMove.getEndPosition()) {
                        inCheck += 1;
                        if (inCheck == possible) return true;
                        }
                    }
                }
            }
        return false;
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    /**
     * finds the location of the king of a given color for use in other functions
     *
     * @return the king's position
     */
    private ChessPosition findKing(TeamColor teamColor) {
        ChessPiece[][] squares = this.board.squares;
        ChessPiece piece;
        ChessPosition king = null;

        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                if (squares[row][col] != null) {
                    piece = squares[row][col];
                    if (piece.getTeamColor() == teamColor) {
                        if (piece.getPieceType() == ChessPiece.PieceType.KING){
                            king = new ChessPosition(row,col);
                            break;
                        }
                    }
                }
            }
        }
        return king;
    }

    private Collection<ChessMove> getEnemyMoves(TeamColor teamColor) {
        ChessPiece[][] squares = this.board.squares;
        ChessPiece piece;
        ArrayList<ChessMove> enemyMoves = new ArrayList<>();

        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                if (squares[row][col] != null) {
                    piece = squares[row][col];
                    if (piece.getTeamColor() != teamColor) {
                        enemyMoves.addAll(piece.pieceMoves(this.board, new ChessPosition(row,col)));
                    }
                }
            }
        }

        return enemyMoves;
    }

}
