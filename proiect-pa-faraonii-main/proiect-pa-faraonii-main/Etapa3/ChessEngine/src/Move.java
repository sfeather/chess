public class Move {
    private Position before;
    private Position after;

    public Move() {
        before = null;
        after = null;
    }

    public Move(Position before, Position after) {
        this.before = before;
        this.after = after;
    }

    public Position getBefore() {
        return before;
    }

    public Position getAfter() {
        return after;
    }
}
