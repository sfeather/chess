public class Position {
    // clasa folosita pentru a retine pozitiile pieselor din matrice
    private int line, column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public void setPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    // suprascrisa pentru a fi folosita de metode din ArrayList
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Position))
            return false;

        Position o = (Position) obj;
        return (line == o.line && column == o.column);
    }

    // transforma coordonatele din matrice in coordonate pentru xboard
    // (1, 4) -> (e2)
    public String encode() {
        char col = (char) ('a' + column);
        return "" + col + (line + 1);
    }

    public String toString() {
        return "[" + line + ", " + column + "]";
    }
}
