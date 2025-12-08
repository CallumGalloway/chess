package ui;

import chess.*;
import datamodel.GameData;
import datamodel.JoinData;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static ui.EscapeSequences.*;

public class BoardDisplay {

    public static void main(String[] args){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);

        ChessGame game = new ChessGame();

        try {
            game.makeMove(new ChessMove(new ChessPosition(2, 2), new ChessPosition(3, 2), null));
        } catch (Exception ex) {

        }
        printCurrentBoard(out, "BLACK", game, null);
    }

    public static void printCurrentBoard(PrintStream out, String color, ChessGame game, ChessPosition target) {
        List<String> rows = Arrays.asList(" 8\u2003"," 7\u2003"," 6\u2003"," 5\u2003"," 4\u2003"," 3\u2003"," 2\u2003"," 1\u2003");
        List<String> cols = Arrays.asList(" a\u2003"," b\u2003"," c\u2003"," d\u2003"," e\u2003"," f\u2003"," g\u2003"," h\u2003");
        out.print("\n");
        out.print(SET_BG_COLOR_BIRCH);
        out.print(SET_TEXT_BOLD);
        out.print(SET_TEXT_COLOR_BLACK);
        if (color.equals("BLACK")){
            rows = rows.reversed();
            cols = cols.reversed();
        }
        for (int row = 0; row <= 11; row++){
            //set board background and label text
            out.print(SET_BG_COLOR_BIRCH);
            out.print(SET_TEXT_COLOR_BLACK);
            out.print(EMPTY.repeat(1));
            if (row ==0 || row ==11) {
                out.print(EMPTY.repeat(10));
            }
            else if (row ==1 || row ==10){
                out.print(EMPTY);
                out.print(SET_TEXT_BOLD);
                for(int col = 0; col < cols.size(); col++){
                    out.print(cols.get(col));
                }
                out.print(RESET_TEXT_BOLD_FAINT);
                out.print(EMPTY);
            }
            else {
                out.print(SET_TEXT_BOLD);
                out.print(rows.get(row-2));
                out.print(RESET_TEXT_BOLD_FAINT);
                drawRow(out,row-1, game, color, target);
                out.print(SET_BG_COLOR_BIRCH);
                out.print(SET_TEXT_COLOR_BLACK);
                out.print(SET_TEXT_BOLD);
                out.print(rows.get(row-2));
                out.print(RESET_TEXT_BOLD_FAINT);
            }
            out.print(EMPTY.repeat(1));
            out.print(RESET_BG_COLOR);
            out.println();
        }
    }

    public static void drawRow(PrintStream out, int row, ChessGame game, String color, ChessPosition target) {
        var board = game.getBoard();
        ChessPosition position = null;
        ChessPiece.PieceType piece = null;
        ChessGame.TeamColor pieceColor = null;

        Collection<ChessMove> valid = new ArrayList<>();


        if (target != null) {
            valid = game.validMoves(target);
        }
        if (!color.equals("BLACK")) {
            board = switchView(board);
            valid = invertMoves(valid);
            if (target != null) {
                var inverseTarget = new ChessPosition(9 - target.getRow(), 9 - target.getColumn());
                target = inverseTarget;
            }
        }

        var squareColor = row % 2 == 0 ? "light" : "dark";
        for (int col = 1; col <= 8; col++) {
            squareColor = swapColor(squareColor);
            position = new ChessPosition(row, 9-col);
            ChessMove highlight = new ChessMove(target, position, null);

            var darkColor = SET_BG_COLOR_CHERRY;
            var lightColor = SET_BG_COLOR_MIDWOOD;

            if (target != null && position.equals(target)) {
                darkColor = SET_BG_COLOR_DARK_GREEN;
                lightColor = SET_BG_COLOR_LIGHT_GREEN;
            }
            if (!valid.isEmpty()) {
                for (ChessMove move : valid) {
                    if (move.equals(highlight)) {
                        darkColor = SET_BG_COLOR_DARK_GREEN;
                        lightColor = SET_BG_COLOR_LIGHT_GREEN;
                    }
                }
            }

            switch (squareColor){
                case "dark" -> out.print(darkColor);
                case "light" -> out.print(lightColor);
            }

            if (board.getPiece(position)==null) {
                out.print(EMPTY);
            } else {
                piece = board.getPiece(position).getPieceType();
                pieceColor = board.getPiece(position).getTeamColor();
                if (pieceColor == ChessGame.TeamColor.BLACK) {
                    out.print(SET_TEXT_COLOR_BLACK);
                    switch (piece) {
                        case PAWN -> out.print(BLACK_PAWN);
                        case KNIGHT -> out.print(BLACK_KNIGHT);
                        case ROOK -> out.print(BLACK_ROOK);
                        case BISHOP -> out.print(BLACK_BISHOP);
                        case KING -> out.print(BLACK_KING);
                        case QUEEN -> out.print(BLACK_QUEEN);
                    }
                } else {
                    out.print(SET_TEXT_COLOR_WHITE);
                    switch (piece) {
                        case PAWN -> out.print(WHITE_PAWN);
                        case KNIGHT -> out.print(WHITE_KNIGHT);
                        case ROOK -> out.print(WHITE_ROOK);
                        case BISHOP -> out.print(WHITE_BISHOP);
                        case KING -> out.print(WHITE_KING);
                        case QUEEN -> out.print(WHITE_QUEEN);
                    }
                }
            }
        }
    }

    static String swapColor(String color) {
        return color.equals("dark") ? "light" : "dark";
    }

    static ChessBoard switchView(ChessBoard board) {
        ChessBoard newBoard = new ChessBoard();
        for (int row = 1; row <=8; row++){
            for (int col = 1; col <=8; col++){
                ChessPosition switched = new ChessPosition(9-row,9-col);
                ChessPiece piece = board.getPiece(new ChessPosition(row,col));
                newBoard.addPiece(switched,piece);
            }
        }
        return newBoard;
    }

    static Collection<ChessMove> invertMoves(Collection<ChessMove> moves) {
        ArrayList<ChessMove> newMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessPosition start = move.getStartPosition();
            ChessPosition end = move.getEndPosition();
            ChessPosition newStart = new ChessPosition(9-start.getRow(), 9-start.getColumn());
            ChessPosition newEnd = new ChessPosition(9- end.getRow(), 9-end.getColumn());
            ChessMove newMove = new ChessMove(newStart, newEnd, null);
            newMoves.add(newMove);
        }
        return newMoves;
    }

}
