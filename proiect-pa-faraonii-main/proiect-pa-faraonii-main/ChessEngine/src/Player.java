import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Player {
    /*
        Vom retine pozitiile pieselor pentru jucator, pentru fiecare tip de
        piesa. Folosim un ArrayList si pentru regine pentru ca putem ajunge
        intr-o situatie in care avem mai multe regine (cand se transforma un
        pion).
    */
    public ArrayList<Position> pawns;
    public ArrayList<Position> knights;
    public ArrayList<Position> rooks;
    public ArrayList<Position> bishops;
    public ArrayList<Position> queens;
    public Position king;
    public boolean canBeEnPassant;
    public Position lastPieceMoved;
    public boolean queenSideCastleAvailable;
    public boolean kingSideCastleAvailable;
    public int numberOfChecks;
    public boolean castled;

    Player() {
        pawns = new ArrayList<>();
        knights = new ArrayList<>();
        rooks = new ArrayList<>();
        bishops = new ArrayList<>();
        queens = new ArrayList<>();
        canBeEnPassant = false;
        queenSideCastleAvailable = true;
        kingSideCastleAvailable = true;
        numberOfChecks = 0;
        castled = false;
    }

    public Player(Player player) {
        pawns = new ArrayList<>(player.pawns);
        knights = new ArrayList<>(player.knights);
        rooks = new ArrayList<>(player.rooks);
        bishops = new ArrayList<>(player.bishops);
        queens = new ArrayList<>(player.queens);
        king = player.king;
        lastPieceMoved = player.lastPieceMoved;
        canBeEnPassant = player.canBeEnPassant;
        queenSideCastleAvailable = player.queenSideCastleAvailable;
        kingSideCastleAvailable = player.kingSideCastleAvailable;
        numberOfChecks = player.numberOfChecks;
        castled = player.castled;
    }

    /*
    Metoda folosita pentru debugger.
     */
    public void printPositions(FileWriter debugFile) {
        try {
            debugFile.write("Positions of Pawns: ");
            for (Position pos : pawns) {
                if (pos != null)
                    debugFile.write(pos.toString() + " ");
            }

            debugFile.write("\nPositions of Knights: ");
            for (Position pos : knights) {
                if (pos != null)
                    debugFile.write(pos.toString() + " ");
            }

            debugFile.write("\nPositions of Rooks: ");
            for (Position pos : rooks) {
                if (pos != null)
                    debugFile.write(pos.toString() + " ");
            }

            debugFile.write("\nPositions of Bishops: ");
            for (Position pos : bishops) {
                if (pos != null)
                    debugFile.write(pos.toString() + " ");
            }

            debugFile.write("\nPositions of Knights: ");
            for (Position pos : knights) {
                if (pos != null)
                    debugFile.write(pos.toString() + " ");
            }

            debugFile.write("\nPositions of Queens: ");
            for (Position pos : queens) {
                if (pos != null)
                    debugFile.write(pos.toString() + " ");
            }

            debugFile.write("\nPosition of King: " + king.toString() + "\n");

            debugFile.write("NumberOfChecks: " + numberOfChecks + "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
