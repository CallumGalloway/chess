package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static ui.EscapeSequences.*;

public class ServerFacade{

    //board elements
    private static final String LIGHT_SQUARE_COLOR = "";
    private static final String LIGHT_PIECE_COLOR = "";
    private static final String DARK_SQUARE_COLOR = "";
    private static final String DARK_PIECE_COLOR = "";

    public static void main(String[] args){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);

        printCurrentBoard(out, "observer", new ChessGame());
        //drawPlainBoard(out);

    }

    public static void printCurrentBoard(PrintStream out, String color, ChessGame game){
        List<String> rows = Arrays.asList(" 8\u2003"," 7\u2003"," 6\u2003"," 5\u2003"," 4\u2003"," 3\u2003"," 2\u2003"," 1\u2003");
        List<String> cols = Arrays.asList(" a\u2003"," b\u2003"," c\u2003"," d\u2003"," e\u2003"," f\u2003"," g\u2003"," h\u2003");
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
                drawRow(out,row-1, game);
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

    public static void drawRow(PrintStream out, int row, ChessGame game){
        var board = game.getBoard();
        ChessPosition position = null;
        ChessPiece.PieceType piece = null;
        ChessGame.TeamColor color = null;
        var squareColor = row % 2 == 0 ? "light" : "dark";
        for (int col = 1; col <= 8; col++) {
            squareColor = swapColor(squareColor);
            switch (squareColor){
                case "dark" -> out.print(SET_BG_COLOR_CHERRY);
                case "light" -> out.print(SET_BG_COLOR_MIDWOOD);
            }
            position = new ChessPosition(row, col);
            if (board.getPiece(position)==null) {
                out.print(EMPTY);
            } else {
                piece = board.getPiece(position).getPieceType();
                color = board.getPiece(position).getTeamColor();
                if (color == ChessGame.TeamColor.BLACK) {
                    out.print(SET_TEXT_COLOR_BLACK);
                } else {
                    out.print(SET_TEXT_COLOR_WHITE);
                }
                switch (piece) {
                    case PAWN -> out.print(BLACK_PAWN);
                    case KNIGHT -> out.print(BLACK_KNIGHT);
                    case ROOK -> out.print(BLACK_ROOK);
                    case BISHOP -> out.print(BLACK_BISHOP);
                    case KING -> out.print(BLACK_KING);
                    case QUEEN -> out.print(BLACK_QUEEN);
                }
            }
        }
    }

    static String swapColor(String color){
        return color.equals("dark") ? "light" : "dark";
    }

    //public static void drawHighlightedBoard(PrintStream out, ?)

}
