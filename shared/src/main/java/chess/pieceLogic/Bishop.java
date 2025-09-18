package chess.pieceLogic;

public class Bishop {
    private final int row;
    private final int col;
    public Bishop(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}
