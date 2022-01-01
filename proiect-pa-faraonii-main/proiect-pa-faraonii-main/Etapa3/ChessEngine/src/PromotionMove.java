public class PromotionMove extends Move {
    private char type;

    PromotionMove(Position before, Position after, char type) {
        super(before, after);
        this.type = type;
    }

    public char getType() {
        return type;
    }
}
