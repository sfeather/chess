public class State {
    public char[][] table;
    public int color;
    public boolean isEnpassant;
    public boolean queenCastleWhite;
    public boolean queenCastleBlack;
    public boolean kingCastleWhite;
    public boolean kingCastleBlack;
    public int repetition;

    public State(char[][] table, int color, boolean isEnpassant, boolean queenCastleWhite,
                 boolean queenCastleBlack, boolean kingCastleWhite, boolean kingCastleBlack) {
        this.table = new char[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                this.table[i][j] = table[i][j];
            }
        }
        this.color = color;
        this.isEnpassant = isEnpassant;
        this.queenCastleWhite = queenCastleWhite;
        this.queenCastleBlack = queenCastleBlack;
        this.kingCastleBlack = kingCastleBlack;
        this.kingCastleWhite = kingCastleWhite;
        repetition = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (state.table[i][j] != table[i][j])
                    return false;
            }
        }
        return color == state.color && isEnpassant == state.isEnpassant && queenCastleWhite == state.queenCastleWhite &&
                queenCastleBlack == state.queenCastleBlack && kingCastleWhite == state.kingCastleWhite &&
                kingCastleBlack == state.kingCastleBlack;
    }
}
