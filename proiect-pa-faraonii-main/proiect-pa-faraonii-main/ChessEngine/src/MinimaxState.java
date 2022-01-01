public class MinimaxState {
    public int score;
    public Game game;
    public String command;

    public MinimaxState() {

    }

    public MinimaxState(int score, Game game, String command) {
        this.score = score;
        this.game = game;
        this.command = command;
    }
}
