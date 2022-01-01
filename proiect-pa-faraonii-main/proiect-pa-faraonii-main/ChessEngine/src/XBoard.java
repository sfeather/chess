import java.util.HashMap;

public class XBoard {
    public static void main(String[] args) {
        MyScanner sc = new MyScanner();
        String currentCommand;
        Game currentGame = new Game();
        HashMap<CharMatrix, Integer> hm = new HashMap<>();
        int countMoves = 0;
        while (true) {
            // daca este randul engine-ului, muta
            if (currentGame.currentTurn == Turn.ENGINE) {
                if (countMoves == 0 && currentGame.currentColor == 1) {
                    countMoves++;
                    Position before = new Position(6, 4);
                    Position after = new Position(5, 4);
                    Move move = new Move(before, after);

                    System.out.println(currentGame.makeMove(move));
                    System.out.flush();
                } else if (countMoves == 1 && currentGame.currentColor == 1) {
                    countMoves++;
                    Position before = new Position(7, 6);
                    Position after = new Position(6, 4);
                    Move move = new Move(before, after);

                    System.out.println(currentGame.makeMove(move));
                    System.out.flush();
                } else {
                    MinimaxState choice;
                    countMoves++;
                    if (countMoves < 30) {
                        choice = currentGame.maxValue(4, currentGame.currentColor, -2000000, 2000000, hm);
                    } else {
                        choice = currentGame.maxValue(5, currentGame.currentColor, -2000000, 2000000, hm);
                    }

                    currentGame = choice.game;
                    System.out.println(choice.command);
                    System.out.flush();
                    currentGame.currentColor = 1 - currentGame.currentColor;
                }

                currentGame.condition3Repetitions();
                currentGame.changeTurn();
                currentGame.checkConditions();
                currentGame.conditionMoves();
                continue;
            }
            // primeste comanda de la xboard
            currentCommand = sc.nextLine();
            if (currentCommand.compareTo("xboard") == 0) {
                currentGame.initGame();

                continue;
            }
            if (currentCommand.startsWith("protover")) {
                System.out.println("feature sigint=0 san=0 myname=\"Faraonii\" usermove=1");
                System.out.flush();
                continue;
            }
            if (currentCommand.compareTo("new") == 0) {
                currentGame.initGame();
                countMoves = 0;
                continue;
            }
            // in modul force, engine-ul este dezactivat
            if (currentCommand.compareTo("force") == 0) {
                currentGame.isEngineOn = false;
                currentGame.currentTurn = Turn.OPPONENT;
                continue;
            }
            // in modul go, reintra engine-ul in joc
            // muta pentru culoarea curenta
            if (currentCommand.compareTo("go") == 0) {
                currentGame.isEngineOn = true;
                currentGame.currentTurn = Turn.ENGINE;
                continue;
            }
            // inchiderea jocului
            if (currentCommand.compareTo("quit") == 0) {
                return;
            }
            // mutarile jucatorului (primite de la xboard)
            // de forma "usermove e2e4"
            if (currentCommand.startsWith("usermove")) {
                String move = currentCommand.split(" ")[1];
                currentGame.moveAsPlayer(move);
                currentGame.changeTurn();
                currentGame.checkConditions();
                currentGame.conditionMoves();
            }
        }
    }
}
