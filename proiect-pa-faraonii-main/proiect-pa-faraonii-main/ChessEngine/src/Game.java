import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

enum Turn {
    ENGINE, OPPONENT
}

public class Game {
    // players[0] -> alb, players[1] -> negru
    public Player[] players;
    // tabla
    public char[][] table;
    // folosita pentru modul force (spune daca engine-ul e pornit)
    public boolean isEngineOn;
    // randul engine-ului sau al oponentului
    public Turn currentTurn;
    // 0 -> alb, 1 -> negru
    public int currentColor;
    public int nrMovesForDraw;
    public ArrayList<State> history;

    public Game() {

    }

    public Game(Game game) {
        players = new Player[2];
        players[0] = new Player(game.players[0]);
        players[1] = new Player(game.players[1]);
        table = new char[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                table[i][j] = game.table[i][j];
            }
        }
        isEngineOn = game.isEngineOn;
        currentTurn = game.currentTurn;
        currentColor = game.currentColor;
        nrMovesForDraw = game.nrMovesForDraw;
        history = new ArrayList<>(game.history);
    }

    // initializare joc
    public void initGame() {
        // incepe oponentul, avand culoarea alb
        currentTurn = Turn.OPPONENT;
        currentColor = 0;
        isEngineOn = true;
        players = new Player[2];
        players[0] = new Player();
        players[1] = new Player();
        table = new char[8][8];
        nrMovesForDraw = 0;
        history = new ArrayList<>();
        // pune piesele in pozitiile initiale
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                table[i][j] = Pieces.EMPTY_TILE;
            }
        }

        for (int j = 0; j < 8; j++) {
            table[1][j] = Pieces.WHITE_PAWN;
            players[0].pawns.add(new Position(1, j));
            table[6][j] = Pieces.BLACK_PAWN;
            players[1].pawns.add(new Position(6, j));
        }

        table[0][0] = table[0][7] = Pieces.WHITE_ROOK;
        players[0].rooks.add(new Position(0, 0));
        players[0].rooks.add(new Position(0, 7));

        table[0][1] = table[0][6] = Pieces.WHITE_KNIGHT;
        players[0].knights.add(new Position(0, 1));
        players[0].knights.add(new Position(0, 6));

        table[0][2] = table[0][5] = Pieces.WHITE_BISHOP;
        players[0].bishops.add(new Position(0, 2));
        players[0].bishops.add(new Position(0, 5));

        table[0][3] = Pieces.WHITE_QUEEN;
        players[0].queens.add(new Position(0, 3));

        table[0][4] = Pieces.WHITE_KING;
        players[0].king = new Position(0, 4);

        table[7][0] = table[7][7] = Pieces.BLACK_ROOK;
        players[1].rooks.add(new Position(7, 0));
        players[1].rooks.add(new Position(7, 7));

        table[7][1] = table[7][6] = Pieces.BLACK_KNIGHT;
        players[1].knights.add(new Position(7, 1));
        players[1].knights.add(new Position(7, 6));

        table[7][2] = table[7][5] = Pieces.BLACK_BISHOP;
        players[1].bishops.add(new Position(7, 2));
        players[1].bishops.add(new Position(7, 5));

        table[7][3] = Pieces.BLACK_QUEEN;
        players[1].queens.add(new Position(7, 3));

        table[7][4] = Pieces.BLACK_KING;
        players[1].king = new Position(7, 4);

        history.add(new State(table, currentColor, players[1 - currentColor].canBeEnPassant,
                players[0].queenSideCastleAvailable, players[1].queenSideCastleAvailable,
                players[0].kingSideCastleAvailable, players[1].kingSideCastleAvailable));
    }

    // schimba culoarea si comuta intre engine si oponent
    // daca nu suntem in force
    public void changeTurn() {
        currentColor = 1 - currentColor;
        if (isEngineOn) {
            if (currentTurn == Turn.ENGINE)
                currentTurn = Turn.OPPONENT;
            else
                currentTurn = Turn.ENGINE;
        }
    }

    // sterge piesa de la pozitia pos din piesele jucatorului care nu muta acum
    public void deletePiece(Position pos) {
        // iau piesa din matrice
        char pieceToDelete = table[pos.getLine()][pos.getColumn()];
        // daca piesa reprezinta o celula libera, nu facem nimic
        if (pieceToDelete == Pieces.EMPTY_TILE)
            return;
        // daca piesa este pion
        // liniile comentate din if-urile astea o sa le folosim la etapele
        // urmatoare
        history.clear();
        if (pieceToDelete == Pieces.BLACK_PAWN + 32 * currentColor) {
            // "stergem" pionul din ArrayList (la etapele urmatoare probabil o
            // sa folosim remove si add pentru chestii din astea)
            players[1 - currentColor].pawns.remove(pos);
            nrMovesForDraw = 0;
        } else if (pieceToDelete == Pieces.BLACK_BISHOP + 32 * currentColor) {
            // de aici e similar pentru celelalte tipuri de piese
            players[1 - currentColor].bishops.remove(pos);
            nrMovesForDraw = 0;
        } else if (pieceToDelete == Pieces.BLACK_KNIGHT + 32 * currentColor) {
            players[1 - currentColor].knights.remove(pos);
            nrMovesForDraw = 0;
        } else if (pieceToDelete == Pieces.BLACK_ROOK + 32 * currentColor) {
            players[1 - currentColor].rooks.remove(pos);
            nrMovesForDraw = 0;
            if (players[1 - currentColor].queenSideCastleAvailable &&
                    pos.equals(new Position(7 - 7 * currentColor, 0))) {
                players[currentColor].queenSideCastleAvailable = false;
            }
            if (players[1 - currentColor].kingSideCastleAvailable &&
                    pos.equals(new Position(7 - 7 * currentColor, 7))) {
                players[currentColor].kingSideCastleAvailable = false;
            }
        } else if (pieceToDelete == Pieces.BLACK_QUEEN + 32 * currentColor) {
            players[1 - currentColor].queens.remove(pos);
            nrMovesForDraw = 0;
        }
    }

    // functie care realizeaza mutarea in ArrayListii jucatorilor, intre 2 pozitii
    public void changePosition(Move move) {
        // ia piesa din matrice
        Position before = move.getBefore();
        Position after = move.getAfter();
        char pieceToChange = table[before.getLine()][before.getColumn()];
        // liniile comentate vor fi folosite in etapele urmatoare
        // daca piesa pe care o mutam este pion, editam pozitia sa din
        // ArrayListul de pozitii ale pionilor
        if (pieceToChange == Pieces.WHITE_PAWN - 32 * currentColor) {
            players[currentColor].pawns.remove(before);
            nrMovesForDraw = 0;
            // cazul de promovare
            if (move instanceof PromotionMove) {
                char type = ((PromotionMove) move).getType();
                if (currentColor == 0) {
                    if (type == 'q') {
                        table[before.getLine()][before.getColumn()] = Pieces.WHITE_QUEEN;
                        players[currentColor].queens.add(after);
                    } else if (type == 'b') {
                        table[before.getLine()][before.getColumn()] = Pieces.WHITE_BISHOP;
                        players[currentColor].bishops.add(after);
                    } else if (type == 'r') {
                        table[before.getLine()][before.getColumn()] = Pieces.WHITE_ROOK;
                        players[currentColor].rooks.add(after);
                    } else if (type == 'n') {
                        table[before.getLine()][before.getColumn()] = Pieces.WHITE_KNIGHT;
                        players[currentColor].knights.add(after);
                    }
                } else {
                    if (type == 'q') {
                        table[before.getLine()][before.getColumn()] = Pieces.BLACK_QUEEN;
                        players[currentColor].queens.add(after);
                    } else if (type == 'b') {
                        table[before.getLine()][before.getColumn()] = Pieces.BLACK_BISHOP;
                        players[currentColor].bishops.add(after);
                    } else if (type == 'r') {
                        table[before.getLine()][before.getColumn()] = Pieces.BLACK_ROOK;
                        players[currentColor].rooks.add(after);
                    } else if (type == 'n') {
                        table[before.getLine()][before.getColumn()] = Pieces.BLACK_KNIGHT;
                        players[currentColor].knights.add(after);
                    }
                }
            } else {
                // daca e o mutare normala
                // editam pozitia
                if (players[1 - currentColor].canBeEnPassant &&
                        table[after.getLine()][after.getColumn()] == Pieces.EMPTY_TILE &&
                        after.getColumn() != before.getColumn()) {
                    Position pawnEnPassant = players[1 - currentColor].lastPieceMoved;
                    players[1 - currentColor].pawns.remove(pawnEnPassant);
                    table[pawnEnPassant.getLine()][pawnEnPassant.getColumn()] = Pieces.EMPTY_TILE;
                }
                players[currentColor].pawns.add(after);
            }
        } else if (pieceToChange == Pieces.WHITE_BISHOP - 32 * currentColor) {
            // similar pentru celelalte piese
            players[currentColor].bishops.remove(before);
            players[currentColor].bishops.add(after);
        } else if (pieceToChange == Pieces.WHITE_KNIGHT - 32 * currentColor) {
            players[currentColor].knights.remove(before);
            players[currentColor].knights.add(after);
        } else if (pieceToChange == Pieces.WHITE_ROOK - 32 * currentColor) {
            players[currentColor].rooks.remove(before);
            players[currentColor].rooks.add(after);
            if (players[currentColor].queenSideCastleAvailable &&
                    before.equals(new Position(7 * currentColor, 0))) {
                players[currentColor].queenSideCastleAvailable = false;
            }
            if (players[currentColor].kingSideCastleAvailable &&
                    before.equals(new Position(7 * currentColor, 7))) {
                players[currentColor].kingSideCastleAvailable = false;
            }
        } else if (pieceToChange == Pieces.WHITE_QUEEN - 32 * currentColor) {
            players[currentColor].queens.remove(before);
            players[currentColor].queens.add(after);
        } else {
            players[currentColor].king = after;
            players[currentColor].kingSideCastleAvailable = false;
            players[currentColor].queenSideCastleAvailable = false;
        }
    }

    // realizeaza o mutare primita de la xboard
    public void moveAsPlayer(String move) {
        // rocadele
        nrMovesForDraw++;
        ArrayList<String> castlings = new ArrayList<>();
        castlings.add("e1g1");
        castlings.add("e1c1");
        castlings.add("e8g8");
        castlings.add("e8c8");
        ArrayList<String> complementary = new ArrayList<>();
        complementary.add("h1f1");
        complementary.add("a1d1");
        complementary.add("h8f8");
        complementary.add("a8d8");
        // determina coordonatele
        int line1 = move.charAt(1) - '1';
        int col1 = move.charAt(0) - 'a';
        int line2 = move.charAt(3) - '1';
        int col2 = move.charAt(2) - 'a';
        Position before = new Position(line1, col1);
        Position after = new Position(line2, col2);
        // caz rocada
        if (table[line1][col1] == Pieces.WHITE_KING - 32 * currentColor) {
            for (int i = 0; i < 4; i++) {
                if (castlings.get(i).equals(move)) {
                    changePosition(new Move(before, after));
                    table[line2][col2] = table[line1][col1];
                    table[line1][col1] = Pieces.EMPTY_TILE;
                    String compl = complementary.get(i);
                    line1 = compl.charAt(1) - '1';
                    col1 = compl.charAt(0) - 'a';
                    line2 = compl.charAt(3) - '1';
                    col2 = compl.charAt(2) - 'a';
                    before = new Position(line1, col1);
                    after = new Position(line2, col2);
                    changePosition(new Move(before, after));
                    table[line2][col2] = table[line1][col1];
                    table[line1][col1] = Pieces.EMPTY_TILE;
                    players[currentColor].canBeEnPassant = false;
                    return;
                }
            }
        }
        if (table[line1][col1] == Pieces.WHITE_PAWN - 32 * currentColor &&
                line1 == 1 + 5 * currentColor && line2 == 3 + currentColor) {
            players[currentColor].canBeEnPassant = true;
            players[currentColor].lastPieceMoved = after;
        } else {
            players[currentColor].canBeEnPassant = false;
        }
        // muta piesa
        if (move.length() == 5) {
            char type = move.charAt(4);
            changePosition(new PromotionMove(before, after, type));
        } else {
            changePosition(new Move(before, after));
        }
        // sterge piesa care se afla dinainte pe pozitia de final
        deletePiece(after);
        // editam si in matrice
        char pieceToMove = table[line1][col1];
        table[line2][col2] = pieceToMove;
        table[line1][col1] = Pieces.EMPTY_TILE;
        condition3Repetitions();
    }

    public boolean isPawn(char piece) {
        return (piece == Pieces.BLACK_PAWN + 32 * currentColor);
    }

    public boolean isRook(char piece) {
        return (piece == Pieces.BLACK_ROOK + 32 * currentColor);
    }

    public boolean isBishop(char piece) {
        return (piece == Pieces.BLACK_BISHOP + 32 * currentColor);
    }

    public boolean isKnight(char piece) {
        return (piece == Pieces.BLACK_KNIGHT + 32 * currentColor);
    }

    public boolean isQueen(char piece) {
        return (piece == Pieces.BLACK_QUEEN + 32 * currentColor);
    }

    public boolean isKing(char piece) {
        return (piece == Pieces.BLACK_KING + 32 * currentColor);
    }

    public boolean notInCheck(int line, int column, char[][] copy) {
        int k;
        if (line + 1 <= 7) {
            char piece = copy[line + 1][column];
            if (isKing(piece) || isQueen(piece) || isRook(piece)) {
                return false;
            }

            if (piece == Pieces.EMPTY_TILE) {
                k = 2;
                while (line + k <= 7 && copy[line + k][column] == '_') {
                    k++;
                }

                if (line + k <= 7) {
                    piece = copy[line + k][column];
                    if (isQueen(piece) || isRook(piece))
                        return false;
                }
            }

            if (column + 1 <= 7) {
                piece = copy[line + 1][column + 1];
                if (isKing(piece) || isQueen(piece) || isBishop(piece))
                    return false;

                if (isPawn(piece) && currentColor == 0)
                    return false;

                if (piece == Pieces.EMPTY_TILE) {
                    k = 2;
                    while (line + k <= 7 && column + k <= 7 && copy[line + k][column + k] == '_') {
                        k++;
                    }

                    if (line + k <= 7 && column + k <= 7) {
                        piece = copy[line + k][column + k];
                        if (isQueen(piece) || isBishop(piece)) {
                            return false;
                        }
                    }
                }
            }

            if (column - 1 >= 0) {
                piece = copy[line + 1][column - 1];
                if (isKing(piece) || isQueen(piece) || isBishop(piece))
                    return false;

                if (isPawn(piece) && currentColor == 0)
                    return false;

                if (piece == Pieces.EMPTY_TILE) {
                    k = 2;
                    while (line + k <= 7 && column - k >= 0 && copy[line + k][column - k] == '_') {
                        k++;
                    }

                    if (line + k <= 7 && column - k >= 0) {
                        piece = copy[line + k][column - k];
                        if (isQueen(piece) || isBishop(piece)) {
                            return false;
                        }
                    }
                }
            }
        }

        if (line - 1 >= 0) {
            char piece = copy[line - 1][column];
            if (isKing(piece) || isQueen(piece) || isRook(piece)) {
                return false;
            }

            if (piece == Pieces.EMPTY_TILE) {
                k = 2;
                while (line - k >= 0 && copy[line - k][column] == '_') {
                    k++;
                }

                if (line - k >= 0) {
                    piece = copy[line - k][column];
                    if (isQueen(piece) || isRook(piece))
                        return false;
                }
            }

            if (column + 1 <= 7) {
                piece = copy[line - 1][column + 1];
                if (isKing(piece) || isQueen(piece) || isBishop(piece))
                    return false;

                if (isPawn(piece) && currentColor == 1)
                    return false;

                if (piece == Pieces.EMPTY_TILE) {
                    k = 2;
                    while (line - k >= 0 && column + k <= 7 && copy[line - k][column + k] == '_') {
                        k++;
                    }

                    if (line - k >= 0 && column + k <= 7) {
                        piece = copy[line - k][column + k];
                        if (isQueen(piece) || isBishop(piece)) {
                            return false;
                        }
                    }
                }
            }

            if (column - 1 >= 0) {
                piece = copy[line - 1][column - 1];
                if (isKing(piece) || isQueen(piece) || isBishop(piece))
                    return false;

                if (isPawn(piece) && currentColor == 1)
                    return false;

                if (piece == Pieces.EMPTY_TILE) {
                    k = 2;
                    while (line - k >= 0 && column - k >= 0 && copy[line - k][column - k] == '_') {
                        k++;
                    }

                    if (line - k >= 0 && column - k >= 0) {
                        piece = copy[line - k][column - k];
                        if (isQueen(piece) || isBishop(piece)) {
                            return false;
                        }
                    }
                }
            }
        }

        if (column + 1 <= 7) {
            char piece = copy[line][column + 1];
            if (isKing(piece) || isQueen(piece) || isRook(piece)) {
                return false;
            }

            if (piece == Pieces.EMPTY_TILE) {
                k = 2;
                while (column + k <= 7 && copy[line][column + k] == '_') {
                    k++;
                }

                if (column + k <= 7) {
                    piece = copy[line][column + k];
                    if (isQueen(piece) || isRook(piece))
                        return false;
                }
            }
        }

        if (column - 1 >= 0) {
            char piece = copy[line][column - 1];
            if (isKing(piece) || isQueen(piece) || isRook(piece)) {
                return false;
            }

            if (piece == Pieces.EMPTY_TILE) {
                k = 2;
                while (column - k >= 0 && copy[line][column - k] == '_') {
                    k++;
                }

                if (column - k >= 0) {
                    piece = copy[line][column - k];
                    if (isQueen(piece) || isRook(piece))
                        return false;
                }
            }
        }
        // cal
        if (column + 2 <= 7) {
            if (line - 1 >= 0 && isKnight(copy[line - 1][column + 2]))
                return false;
            if (line + 1 <= 7 && isKnight(copy[line + 1][column + 2]))
                return false;
        }

        if (column - 2 >= 0) {
            if (line - 1 >= 0 && isKnight(copy[line - 1][column - 2]))
                return false;
            if (line + 1 <= 7 && isKnight(copy[line + 1][column - 2]))
                return false;
        }

        if (column + 1 <= 7) {
            if (line - 2 >= 0 && isKnight(copy[line - 2][column + 1]))
                return false;
            if (line + 2 <= 7 && isKnight(copy[line + 2][column + 1]))
                return false;
        }

        if  (column - 1 >= 0) {
            if (line - 2 >= 0 && isKnight(copy[line - 2][column - 1]))
                return false;
            if (line + 2 <= 7 && isKnight(copy[line + 2][column - 1]))
                return false;
        }
        return true;
    }

    public boolean notInCheckCurrent() {
        return notInCheck(players[currentColor].king.getLine(),
                players[currentColor].king.getColumn(), table);
    }

    public void checkConditions() {
        if (!notInCheckCurrent()) {
            if (checkQueenSideCastle() == false && checkKingSideCastle() == false &&
                    generateAllMoves().size() == 0) {
                if (currentColor == 0) {
                    System.out.println("0-1 {Black mates}");
                    System.out.flush();
                } else {
                    System.out.println("1-0 {White mates}");
                    System.out.flush();
                }
                return;
            }
            players[currentColor].numberOfChecks++;
            if (players[currentColor].numberOfChecks == 3) {
                if (currentColor == 0) {
                    System.out.println("0-1 {Black wins by 3 checks}");
                    System.out.flush();
                } else {
                    System.out.println("1-0 {White wins by 3 checks}");
                    System.out.flush();
                }
                return;
            }
        } else {
            if (checkQueenSideCastle() == false && checkKingSideCastle() == false &&
                    generateAllMoves().size() == 0) {
                System.out.println("1/2-1/2 {Stalemate}");
                System.out.flush();
            }
        }
    }

    public void conditionMoves() {
        if (nrMovesForDraw == 50) {
            System.out.println("1/2-1/2 {50 moves rule}");
            System.out.flush();
        }
    }

    public void condition3Repetitions() {
        State currentState = new State(table, 1 - currentColor, players[currentColor].canBeEnPassant,
                players[0].queenSideCastleAvailable, players[1].queenSideCastleAvailable,
                players[0].kingSideCastleAvailable, players[1].kingSideCastleAvailable);

        int index = history.indexOf(currentState);
        if (index < 0) {
            history.add(currentState);
        } else {
            int rep = history.get(index).repetition;
            if (rep == 2) {
                System.out.println("1/2-1/2 {threefold repetition rule}");
                System.out.flush();
            } else {
                history.remove(index);
                currentState.repetition = rep + 1;
                history.add(currentState);
            }
        }
    }

    public boolean isValidMove(Move move) {
        // Copie a matricei in care vedem daca putem face mutarea (daca nu suntem in sah
        // dupa ce facem mutarea)
        Position before = move.getBefore();
        Position after = move.getAfter();
        Player currentPlayer = players[currentColor];

        int lineBefore = before.getLine();
        int columnBefore = before.getColumn();
        int lineAfter = after.getLine();
        int columnAfter = after.getColumn();
        // Fac "copie" a matricei, pentru starea after
        char[][] copy = new char[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                copy[i][j] = table[i][j];
            }
        }
        char pieceToMove = copy[lineBefore][columnBefore];

        if (pieceToMove == Pieces.WHITE_PAWN - 32 * currentColor) {
            if (move instanceof PromotionMove) {
                char type = ((PromotionMove) move).getType();
                if (currentColor == 0) {
                    if (type == 'q') {
                        copy[lineAfter][columnAfter] = Pieces.WHITE_QUEEN;
                    } else if (type == 'b') {
                        copy[lineAfter][columnAfter] = Pieces.WHITE_BISHOP;
                    } else if (type == 'r') {
                        copy[lineAfter][columnAfter] = Pieces.WHITE_ROOK;
                    } else if (type == 'n') {
                        copy[lineAfter][columnAfter] = Pieces.WHITE_KNIGHT;
                    }
                } else {
                    if (type == 'q') {
                        copy[lineAfter][columnAfter] = Pieces.BLACK_QUEEN;
                    } else if (type == 'b') {
                        copy[lineAfter][columnAfter] = Pieces.BLACK_BISHOP;
                    } else if (type == 'r') {
                        copy[lineAfter][columnAfter] = Pieces.BLACK_ROOK;
                    } else if (type == 'n') {
                        copy[lineAfter][columnAfter] = Pieces.BLACK_KNIGHT;
                    }
                }
            } else {
                if (table[lineAfter][columnAfter] == Pieces.EMPTY_TILE &&
                        columnAfter != columnBefore) {
                    Position pawnEnPassant = players[1 - currentColor].lastPieceMoved;
                    copy[pawnEnPassant.getLine()][pawnEnPassant.getColumn()] = Pieces.EMPTY_TILE;
                }
                copy[lineAfter][columnAfter] = pieceToMove;
            }
        } else {
            copy[lineAfter][columnAfter] = pieceToMove;
        }
        copy[lineBefore][columnBefore] = '_';
        // pozitia regelui
        int line, column;
        if (pieceToMove == Pieces.WHITE_KING - 32 * currentColor) {
            line = lineAfter;
            column = columnAfter;
        } else {
            Position king = currentPlayer.king;
            line = king.getLine();
            column = king.getColumn();
        }
        return notInCheck(line, column, copy);
    }

    public boolean checkQueenSideCastle() {
        if (!players[currentColor].queenSideCastleAvailable)
            return false;
        if (table[7 * currentColor][1] != Pieces.EMPTY_TILE)
            return false;
        if (table[7 * currentColor][2] != Pieces.EMPTY_TILE)
            return false;
        if (table[7 * currentColor][3] != Pieces.EMPTY_TILE)
            return false;
        Position king = players[currentColor].king;
        int kingLine = king.getLine();
        int kingCol = 4;
        if (!notInCheck(kingLine, kingCol, table))
            return false;
        table[kingLine][kingCol - 1] = table[kingLine][kingCol];
        table[kingLine][kingCol] = Pieces.EMPTY_TILE;
        if (!notInCheck(kingLine, kingCol - 1, table)) {
            table[kingLine][kingCol] = table[kingLine][kingCol - 1];
            table[kingLine][kingCol - 1] = Pieces.EMPTY_TILE;
            return false;
        }
        table[kingLine][kingCol - 2] = table[kingLine][kingCol - 1];
        table[kingLine][kingCol - 1] = Pieces.EMPTY_TILE;
        if (!notInCheck(kingLine, kingCol - 2, table)) {
            table[kingLine][kingCol] = table[kingLine][kingCol - 2];
            table[kingLine][kingCol - 2] = Pieces.EMPTY_TILE;
            return false;
        }
        table[kingLine][kingCol] = table[kingLine][kingCol - 2];
        table[kingLine][kingCol - 2] = Pieces.EMPTY_TILE;
        return true;
    }

    public boolean checkKingSideCastle() {
        if (!players[currentColor].kingSideCastleAvailable)
            return false;
        if (table[7 * currentColor][5] != Pieces.EMPTY_TILE)
            return false;
        if (table[7 * currentColor][6] != Pieces.EMPTY_TILE)
            return false;
        Position king = players[currentColor].king;
        int kingLine = king.getLine();
        int kingCol = 4;
        if (!notInCheck(kingLine, kingCol, table))
            return false;
        table[kingLine][kingCol + 1] = table[kingLine][kingCol];
        table[kingLine][kingCol] = Pieces.EMPTY_TILE;
        if (!notInCheck(kingLine, kingCol + 1, table)) {
            table[kingLine][kingCol] = table[kingLine][kingCol + 1];
            table[kingLine][kingCol + 1] = Pieces.EMPTY_TILE;
            return false;
        }
        table[kingLine][kingCol + 2] = table[kingLine][kingCol + 1];
        table[kingLine][kingCol + 1] = Pieces.EMPTY_TILE;
        if (!notInCheck(kingLine, kingCol + 2, table)) {
            table[kingLine][kingCol] = table[kingLine][kingCol + 2];
            table[kingLine][kingCol + 2] = Pieces.EMPTY_TILE;
            return false;
        }
        table[kingLine][kingCol] = table[kingLine][kingCol + 2];
        table[kingLine][kingCol + 2] = Pieces.EMPTY_TILE;
        return true;
    }

    public void generateAllPawnMoves(ArrayList<Move> all,
                                     Position pawn) {
        int line = pawn.getLine();
        int column = pawn.getColumn();
        Position after;
        if (currentColor == 0) {
            if (players[1 - currentColor].canBeEnPassant) {
                Position pawnEnPassant = players[1 - currentColor].lastPieceMoved;
                int columnPawn = pawnEnPassant.getColumn();
                if (line == 4) {
                    if (column - 1 == columnPawn && table[line + 1][column - 1] == '_') {
                        after = new Position(line + 1, column - 1);
                        Move move = new Move(pawn, after);
                        if (isValidMove(move))
                            all.add(move);
                    }
                    if (column + 1 == columnPawn && table[line + 1][column + 1] == '_') {
                        after = new Position(line + 1, column + 1);
                        Move move = new Move(pawn, after);
                        if (isValidMove(move))
                            all.add(move);
                    }
                }
            }
            if (column > 0 && table[line + 1][column - 1] <= 'Z') {
                after = new Position(line + 1, column - 1);
                if (line == 6) {
                    PromotionMove move = new PromotionMove(pawn, after, 'q');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'b');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'r');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'n');
                    if (isValidMove(move))
                        all.add(move);
                } else {
                    Move move = new Move(pawn, after);
                    if (isValidMove(move)) {
                        all.add(move);
                    }
                }
            }
            if (column < 7 && table[line + 1][column + 1] <= 'Z') {
                after = new Position(line + 1, column + 1);
                if (line == 6) {
                    PromotionMove move = new PromotionMove(pawn, after, 'q');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'b');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'r');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'n');
                    if (isValidMove(move))
                        all.add(move);
                } else {
                    Move move = new Move(pawn, after);
                    if (isValidMove(move)) {
                        all.add(move);
                    }
                }
            }
            if (table[line + 1][column] == Pieces.EMPTY_TILE) {
                if (line == 1 && table[line + 2][column] == Pieces.EMPTY_TILE) {
                    after = new Position(line + 2, column);
                    Move move = new Move(pawn, after);
                    if (isValidMove(move)) {
                        all.add(move);
                    }
                }
                after = new Position(line + 1, column);
                if (line == 6) {
                    PromotionMove move = new PromotionMove(pawn, after, 'q');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'b');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'r');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'n');
                    if (isValidMove(move))
                        all.add(move);
                } else {
                    Move move = new Move(pawn, after);
                    if (isValidMove(move)) {
                        all.add(move);
                    }
                }
            }
        } else {
            if (players[1 - currentColor].canBeEnPassant) {
                Position pawnEnPassant = players[1 - currentColor].lastPieceMoved;
                int columnPawn = pawnEnPassant.getColumn();
                if (line == 3) {
                    if (column - 1 == columnPawn && table[line - 1][column - 1] == '_') {
                        after = new Position(line - 1, column - 1);
                        Move move = new Move(pawn, after);
                        if (isValidMove(move))
                            all.add(move);
                    }
                    if (column + 1 == columnPawn && table[line - 1][column + 1] == '_') {
                        after = new Position(line - 1, column + 1);
                        Move move = new Move(pawn, after);
                        if (isValidMove(move))
                            all.add(move);
                    }
                }
            }
            if (column > 0 && table[line - 1][column - 1] >= 'a') {
                after = new Position(line - 1, column - 1);
                if (line == 1) {
                    PromotionMove move = new PromotionMove(pawn, after, 'q');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'b');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'r');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'n');
                    if (isValidMove(move))
                        all.add(move);
                } else {
                    Move move = new Move(pawn, after);
                    if (isValidMove(move)) {
                        all.add(move);
                    }
                }
            }
            if (column < 7 && table[line - 1][column + 1] >= 'a') {
                after = new Position(line - 1, column + 1);
                if (line == 1) {
                    PromotionMove move = new PromotionMove(pawn, after, 'q');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'b');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'r');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'n');
                    if (isValidMove(move))
                        all.add(move);
                } else {
                    Move move = new Move(pawn, after);
                    if (isValidMove(move)) {
                        all.add(move);
                    }
                }
            }
            if (table[line - 1][column] == Pieces.EMPTY_TILE) {
                if (line == 6 && table[line - 2][column] == Pieces.EMPTY_TILE) {
                    after = new Position(line - 2, column);
                    Move move = new Move(pawn, after);
                    if (isValidMove(move)) {
                        all.add(move);
                    }
                }
                after = new Position(line - 1, column);
                if (line == 1) {
                    PromotionMove move = new PromotionMove(pawn, after, 'q');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'b');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'r');
                    if (isValidMove(move))
                        all.add(move);

                    move = new PromotionMove(pawn, after, 'n');
                    if (isValidMove(move))
                        all.add(move);
                } else {
                    Move move = new Move(pawn, after);
                    if (isValidMove(move)) {
                        all.add(move);
                    }
                }
            }
        }
    }

    public void generateAllRookMoves(ArrayList<Move> all,
                                     Position rook) {
        int line = rook.getLine();
        int column = rook.getColumn();
        Position after;
        int k = 1;
        // piese negre <= Z | ciurecolor 0 deci alb
        // piese albe >= 'a' | curentColor 1 deci sunt negru

        //la noi <= _

        // negru e sus linia 7 6
        // alb e jos linia 0 1
        if (currentColor == 0) {

            // in sus
            while (line + k <= 7 && table[line + k][column] == Pieces.EMPTY_TILE) {
                after = new Position(line + k, column);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (line + k <= 7 && table[line + k][column] <= 'Z') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line + k, column);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }


            // dreapta
            k = 1;
            while (column + k <= 7 && table[line][column + k] == Pieces.EMPTY_TILE) {
                after = new Position(line, column + k);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (column + k <= 7 && table[line][column + k] <= 'Z') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line, column + k);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            // jos
            k = 1;
            while (line - k >= 0 && table[line - k][column] == Pieces.EMPTY_TILE) {
                after = new Position(line - k, column);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (line - k >= 0 && table[line - k][column] <= 'Z') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line - k, column);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            // stanga
            k = 1;
            while (column - k >= 0 && table[line][column - k] == Pieces.EMPTY_TILE) {
                after = new Position(line, column - k);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (column - k >= 0 && table[line][column - k] <= 'Z') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line, column - k);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

        } else {
            // in sus
            while (line + k <= 7 && table[line + k][column] == Pieces.EMPTY_TILE) {
                after = new Position(line + k, column);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (line + k <= 7 && table[line + k][column] >= 'a') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line + k, column);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }


            // dreapta
            k = 1;
            while (column + k <= 7 && table[line][column + k] == Pieces.EMPTY_TILE) {
                after = new Position(line, column + k);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (column + k <= 7 && table[line][column + k] >= 'a') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line, column + k);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            // jos
            k = 1;
            while (line - k >= 0 && table[line - k][column] == Pieces.EMPTY_TILE) {
                after = new Position(line - k, column);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (line - k >= 0 && table[line - k][column] >= 'a') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line - k, column);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            // stanga
            k = 1;
            while (column - k >= 0 && table[line][column - k] == Pieces.EMPTY_TILE) {
                after = new Position(line, column - k);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (column - k >= 0 && table[line][column - k] >= 'a') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line, column - k);
                Move move = new Move(rook, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }
        }

    }

    public void generateAllKnightMoves(ArrayList<Move> all,
                                       Position knight) {
        int line = knight.getLine();
        int column = knight.getColumn();
        Position after;

        if (currentColor == 0) {
            if (column + 2 <= 7 && line + 1 <= 7 && table[line + 1][column + 2] <= '_') {
                after = new Position(line + 1, column + 2);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 2 <= 7 && line - 1 >= 0 && table[line - 1][column + 2] <= '_') {
                after = new Position(line - 1, column + 2);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 2 >= 0 && line + 1 <= 7 && table[line + 1][column - 2] <= '_') {
                after = new Position(line + 1, column - 2);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 2 >= 0 && line - 1 >= 0 && table[line - 1][column - 2] <= '_') {
                after = new Position(line - 1, column - 2);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 1 <= 7 && line + 2 <= 7 && table[line + 2][column + 1] <= '_') {
                after = new Position(line + 2, column + 1);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 1 <= 7 && line - 2 >= 0 && table[line - 2][column + 1] <= '_') {
                after = new Position(line - 2, column + 1);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 1 >= 0 && line + 2 <= 7 && table[line + 2][column - 1] <= '_') {
                after = new Position(line + 2, column - 1);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }


            if (column - 1 >= 0 && line - 2 >= 0 && table[line - 2][column - 1] <= '_') {
                after = new Position(line - 2, column - 1);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

        } else {
            if (column + 2 <= 7 && line + 1 <= 7 && table[line + 1][column + 2] >= '_') {
                after = new Position(line + 1, column + 2);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 2 <= 7 && line - 1 >= 0 && table[line - 1][column + 2] >= '_') {
                after = new Position(line - 1, column + 2);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 2 >= 0 && line + 1 <= 7 && table[line + 1][column - 2] >= '_') {
                after = new Position(line + 1, column - 2);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 2 >= 0 && line - 1 >= 0 && table[line - 1][column - 2] >= '_') {
                after = new Position(line - 1, column - 2);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 1 <= 7 && line + 2 <= 7 && table[line + 2][column + 1] >= '_') {
                after = new Position(line + 2, column + 1);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 1 <= 7 && line - 2 >= 0 && table[line - 2][column + 1] >= '_') {
                after = new Position(line - 2, column + 1);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 1 >= 0 && line + 2 <= 7 && table[line + 2][column - 1] >= '_') {
                after = new Position(line + 2, column - 1);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }


            if (column - 1 >= 0 && line - 2 >= 0 && table[line - 2][column - 1] >= '_') {
                after = new Position(line - 2, column - 1);
                Move move = new Move(knight, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }
        }
    }

    public void generateAllBishopMoves(ArrayList<Move> all,
                                       Position bishop) {
        int line = bishop.getLine();
        int column = bishop.getColumn();
        Position after;
        int k;

        if (currentColor == 0) {
            // dreapta-sus
            k = 1;
            while (line + k <= 7 && column + k <= 7 && table[line + k][column + k] == Pieces.EMPTY_TILE) {
                after = new Position(line + k, column + k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (line + k <= 7 && column + k <= 7 && table[line + k][column + k] <= 'Z') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line + k, column + k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }


            // dreapta-jos
            k = 1;
            while (column + k <= 7 && line - k >= 0 && table[line - k][column + k] == Pieces.EMPTY_TILE) {
                after = new Position(line - k, column + k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (column + k <= 7 && line - k >= 0 && table[line - k][column + k] <= 'Z') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line - k, column + k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            // stanga-jos
            k = 1;
            while (line - k >= 0 && column - k >= 0 && table[line - k][column - k] == Pieces.EMPTY_TILE) {
                after = new Position(line - k, column - k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (line - k >= 0 && column - k >= 0 && table[line - k][column - k] <= 'Z') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line - k, column - k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            // stanga-sus
            k = 1;
            while (column - k >= 0 && line + k <= 7 && table[line + k][column - k] == Pieces.EMPTY_TILE) {
                after = new Position(line + k, column - k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (column - k >= 0 && line + k <= 7 && table[line + k][column - k] <= 'Z') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line + k, column - k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

        } else {
            k = 1;
            while (line + k <= 7 && column + k <= 7 && table[line + k][column + k] == Pieces.EMPTY_TILE) {
                after = new Position(line + k, column + k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (line + k <= 7 && column + k <= 7 && table[line + k][column + k] >= 'a') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line + k, column + k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }


            // dreapta-jos
            k = 1;
            while (column + k <= 7 && line - k >= 0 && table[line - k][column + k] == Pieces.EMPTY_TILE) {
                after = new Position(line - k, column + k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (column + k <= 7 && line - k >= 0 && table[line - k][column + k] >= 'a') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line - k, column + k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            // stanga-jos
            k = 1;
            while (line - k >= 0 && column - k >= 0 && table[line - k][column - k] == Pieces.EMPTY_TILE) {
                after = new Position(line - k, column - k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (line - k >= 0 && column - k >= 0 && table[line - k][column - k] >= 'a') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line - k, column - k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            // stanga-sus
            k = 1;
            while (column - k >= 0 && line + k <= 7 && table[line + k][column - k] == Pieces.EMPTY_TILE) {
                after = new Position(line + k, column - k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
                k++;
            }

            if (column - k >= 0 && line + k <= 7 && table[line + k][column - k] >= 'a') {
                //daca e piesa de a lui o iau si mut acolo
                after = new Position(line + k, column - k);
                Move move = new Move(bishop, after);
                if (isValidMove(move)) {
                    all.add(move);
                }
            }
        }
    }

    public void generateAllQueenMoves(ArrayList<Move> all,
                                      Position queen) {
        generateAllBishopMoves(all, queen);
        generateAllRookMoves(all, queen);
    }

    public void generateAllKingMoves(ArrayList<Move> all,
                                     Position king) {
        int line = king.getLine();
        int column = king.getColumn();
        Position after;

        if (currentColor == 0) {
            if (column + 1 <= 7 && table[line][column + 1] <= '_') {
                after = new Position(line, column + 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 1 <= 7 && line + 1 <= 7 && table[line + 1][column + 1] <= '_') {
                after = new Position(line + 1, column + 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 1 <= 7 && line - 1 >= 0 && table[line - 1][column + 1] <= '_') {
                after = new Position(line - 1, column + 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 1 >= 0 && table[line][column - 1] <= '_') {
                after = new Position(line, column - 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 1 >= 0 && line + 1 <= 7 && table[line + 1][column - 1] <= '_') {
                after = new Position(line + 1, column - 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 1 >= 0 && line - 1 >= 0 && table[line - 1][column - 1] <= '_') {
                after = new Position(line - 1, column - 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (line - 1 >= 0 && table[line - 1][column] <= '_') {
                after = new Position(line - 1, column);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (line + 1 <= 7 && table[line + 1][column] <= '_') {
                after = new Position(line + 1, column);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }
        } else {
            if (column + 1 <= 7 && table[line][column + 1] >= '_') {
                after = new Position(line, column + 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 1 <= 7 && line + 1 <= 7 && table[line + 1][column + 1] >= '_') {
                after = new Position(line + 1, column + 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column + 1 <= 7 && line - 1 >= 0 && table[line - 1][column + 1] >= '_') {
                after = new Position(line - 1, column + 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 1 >= 0 && table[line][column - 1] >= '_') {
                after = new Position(line, column - 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 1 >= 0 && line + 1 <= 7 && table[line + 1][column - 1] >= '_') {
                after = new Position(line + 1, column - 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (column - 1 >= 0 && line - 1 >= 0 && table[line - 1][column - 1] >= '_') {
                after = new Position(line - 1, column - 1);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (line - 1 >= 0 && table[line - 1][column] >= '_') {
                after = new Position(line - 1, column);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }

            if (line + 1 <= 7 && table[line + 1][column] >= '_') {
                after = new Position(line + 1, column);
                Move move = new Move(king, after);

                if (isValidMove(move)) {
                    all.add(move);
                }
            }
        }
    }

    public ArrayList<Move> generateAllMoves() {
        Player currentPlayer = players[currentColor];
        ArrayList<Move> all = new ArrayList<>();

        if (checkQueenSideCastle()) {
            all.add(new QueenSideCastleMove());
        }

        if (checkKingSideCastle()) {
            all.add(new KingSideCastleMove());
        }

        for (Position pawn : currentPlayer.pawns) {
            generateAllPawnMoves(all, pawn);
        }
        for (Position rook : currentPlayer.rooks) {
            generateAllRookMoves(all, rook);
        }
        for (Position knight : currentPlayer.knights) {
            generateAllKnightMoves(all, knight);
        }
        for (Position bishop : currentPlayer.bishops) {
            generateAllBishopMoves(all, bishop);
        }
        for (Position queen : currentPlayer.queens) {
            generateAllQueenMoves(all, queen);
        }
        generateAllKingMoves(all, currentPlayer.king);
        return all;
    }

    public int isolatedPawns(int color) {
        int result = 0;
        Player player = players[color];

        if (color == 0) {
            for (Position pawn : player.pawns) {
                int line = pawn.getLine();
                int col = pawn.getColumn();
                if (col > 0 && table[line - 1][col - 1] == Pieces.WHITE_PAWN) {
                    continue;
                }
                if (col < 7 && table[line - 1][col + 1] == Pieces.WHITE_PAWN) {
                    continue;
                }
                result++;
            }
        } else {
            for (Position pawn : player.pawns) {
                int line = pawn.getLine();
                int col = pawn.getColumn();
                if (col > 0 && table[line + 1][col - 1] == Pieces.BLACK_PAWN) {
                    continue;
                }
                if (col < 7 && table[line + 1][col + 1] == Pieces.BLACK_PAWN) {
                    continue;
                }
                result++;
            }
        }

        return result;
    }

    public int doubledPawns(int color) {
        int result = 0;
        for (int j = 0; j < 8; j++) {
            int colorNr = 0;
            int othersNr = 0;
            for (int i = 0; i < 8; i++) {
                if (table[i][j] == Pieces.WHITE_PAWN - 32 * color) {
                    colorNr++;
                } else if (table[i][j] == Pieces.BLACK_PAWN + 32 * color) {
                    othersNr++;
                }
            }
            if (colorNr > 1) {
                result += colorNr;
            }
            if (othersNr > 1) {
                result -= othersNr;
            }
        }
        return result;
    }

    public int blockedPawns(int color) {
        Player player = players[color];
        int result = 0;
        if (color == 0) {
            for (Position pawn : player.pawns) {
                int line = pawn.getLine();
                int col = pawn.getColumn();
                if (table[line + 1][col] != Pieces.EMPTY_TILE) {
                    result++;
                }
            }
        } else {
            for (Position pawn : player.pawns) {
                int line = pawn.getLine();
                int col = pawn.getColumn();
                if (table[line - 1][col] != Pieces.EMPTY_TILE) {
                    result++;
                }
            }
        }
        return result;
    }

    public int pawnsScore(int color) {
        int result = 0;
        int centralPawns = 0;
        if (table[3][3] == Pieces.WHITE_PAWN - 32 * color) {
            centralPawns++;
        }
        if (table[3][4] == Pieces.WHITE_PAWN - 32 * color) {
            centralPawns++;
        }
        if (table[4][3] == Pieces.WHITE_PAWN - 32 * color) {
            centralPawns++;
        }
        if (table[4][4] == Pieces.WHITE_PAWN - 32 * color) {
            centralPawns++;
        }
        result -= 8 * centralPawns;
        return result;
    }

    public int mobilityScore() {
        int result = 0;
        Player currentPlayer = players[currentColor];
        ArrayList<Move> all = new ArrayList<>();

        if (checkQueenSideCastle()) {
            all.add(new QueenSideCastleMove());
        }
        if (checkKingSideCastle()) {
            all.add(new KingSideCastleMove());
        }
        for (Position rook : currentPlayer.rooks) {
            generateAllRookMoves(all, rook);
        }
        result += 9 * all.size();

        all = new ArrayList<>();
        for (Position knight : currentPlayer.knights) {
            generateAllKnightMoves(all, knight);
        }
        result += 14 * all.size();

        all = new ArrayList<>();
        for (Position bishop : currentPlayer.bishops) {
            generateAllBishopMoves(all, bishop);
        }
        result += 13 * all.size();

        all = new ArrayList<>();
        for (Position queen : currentPlayer.queens) {
            generateAllQueenMoves(all, queen);
        }
        result += 3 * all.size();

        all = new ArrayList<>();
        for (Position pawn : currentPlayer.pawns) {
            generateAllPawnMoves(all, pawn);
        }
        generateAllKingMoves(all, currentPlayer.king);
        result += all.size();

        return result;
    }

    public int knightScores(int color) {
        int result = 0;
        Player player = players[color];
        if (color == 0) {
            for (Position knight : player.knights) {
                int line = knight.getLine();
                int col = knight.getColumn();
                if (line == 0 || line == 7 || col == 0 || col == 7) {
                    result -= 51;
                } else if (line == 1 || line == 6 || col == 1 || col == 6) {
                    result -= 18;
                } else if (line == 2 || line == 5 || col == 2 || col == 5) {
                    result += 45;
                } else {
                    result--;
                }
                if (line > 0) {
                    if (col > 0 && table[line - 1][col - 1] == Pieces.WHITE_PAWN) {
                        result += 40;
                        if (line == 3 || line == 4 || line == 5 || line == 6) {
                            if (table[line + 1][col - 1] != Pieces.BLACK_PAWN &&
                                col < 7 && table[line + 1][col + 1] != Pieces.BLACK_PAWN) {
                                result += 39;
                            }
                        }
                        continue;
                    }
                    if (col < 7 && table[line - 1][col + 1] == Pieces.WHITE_PAWN) {
                        result += 40;
                        if (line == 3 || line == 4 || line == 5 || line == 6) {
                            if (col > 0 && table[line + 1][col - 1] != Pieces.BLACK_PAWN
                                    && table[line + 1][col + 1] != Pieces.BLACK_PAWN) {
                                result += 39;
                            }
                        }
                        continue;
                    }
                }
            }
        } else {
            for (Position knight : player.knights) {
                int line = knight.getLine();
                int col = knight.getColumn();
                if (line == 0 || line == 7 || col == 0 || col == 7) {
                    result -= 51;
                } else if (line == 1 || line == 6 || col == 1 || col == 6) {
                    result -= 18;
                } else if (line == 2 || line == 5 || col == 2 || col == 5) {
                    result += 45;
                } else {
                    result--;
                }
                if (line < 7) {
                    if (col > 0 && table[line + 1][col - 1] == Pieces.BLACK_PAWN) {
                        result += 40;
                        if (line == 1 || line == 2 || line == 3 || line == 4) {
                            if (table[line - 1][col - 1] != Pieces.WHITE_PAWN &&
                                    col < 7 && table[line - 1][col + 1] != Pieces.WHITE_PAWN) {
                                result += 39;
                            }
                        }
                        continue;
                    }
                    if (col < 7 && table[line + 1][col + 1] == Pieces.BLACK_PAWN) {
                        result += 40;
                        if (line == 1 || line == 2 || line == 3 || line == 4) {
                            if (col > 0 && table[line - 1][col - 1] != Pieces.WHITE_PAWN
                                    && table[line - 1][col + 1] != Pieces.WHITE_PAWN) {
                                result += 39;
                            }
                        }
                        continue;
                    }
                }
            }
        }

        return result;
    }

    public int rookScores(int color) {
        int result = 0;
        Player player = players[color];
        for (Position rook : player.rooks) {
            int line = rook.getLine();
            if (line == 6 - 5 * color) {
                result += 41;
            }
            int col = rook.getColumn();
            int ok1 = 1, ok2 = 1;
            for (int i = 0; i < 7; i++) {
                if (table[i][col] == Pieces.WHITE_PAWN - 32 * color) {
                    ok1 = 0;
                    ok2 = 0;
                    break;
                } else if (Character.toLowerCase(table[i][col]) == 'p') {
                    ok1 = 0;
                }
            }
            if (ok1 == 1) {
                result += 27;
            } else {
                if (ok2 == 1) {
                    result += 57;
                } else {
                    result -= 46;
                }
            }
        }
        return result;
    }

    public int rookCon(int color) {
        Player player = players[color];
        if (player.rooks.size() > 1) {
            Position rook1 = player.rooks.get(0);
            Position rook2 = player.rooks.get(1);
            int line1 = rook1.getLine();
            int col1 = rook1.getColumn();
            int line2 = rook2.getLine();
            int col2 = rook2.getColumn();
            if (line1 == line2) {
                int min = Math.min(col1, col2);
                int max = Math.max(col1, col2);
                for (int j = min + 1; j < max; j++) {
                    if (table[line1][j] != Pieces.EMPTY_TILE) {
                        return 0;
                    }
                }
                return 11;
            } else if (col1 == col2) {
                int min = Math.min(line1, line2);
                int max = Math.max(line1, line2);
                for (int i = min + 1; i < max; i++) {
                    if (table[i][col1] != Pieces.EMPTY_TILE) {
                        return 0;
                    }
                }
                return 11;
            }
        }
        return 0;
    }

    public int evaluate(int color) {
        int score = 0;
        Player engine = players[color];
        Player opponent = players[1 - color];
        score += 911 * (engine.queens.size() - opponent.queens.size());
        score += 530 * (engine.rooks.size() - opponent.rooks.size());
        score += 374 * (engine.bishops.size() - opponent.bishops.size());
        if (engine.bishops.size() > 1) {
            score += 5;
        }
        if (opponent.bishops.size() > 1) {
            score -= 5;
        }
        score += 342 * (engine.knights.size() - opponent.knights.size());
        score += 100 * (engine.pawns.size() - opponent.pawns.size());
        score -= 7 * doubledPawns(color);
        score += 3 * isolatedPawns(1 - color) - 3 * isolatedPawns(color);
        score += 23 * blockedPawns(1 - color) - 23 * blockedPawns(color);
        score += pawnsScore(color) - pawnsScore(1 - color);
        score += knightScores(color) - knightScores(1 - color);
        score += rookScores(color) - rookScores(1 - color);
        score += rookCon(color) - rookCon(1 - color);

        if (engine.castled == true) {
            score += 60;
        }
        if (opponent.castled == true) {
            score -= 60;
        }

        int currentMobScore = mobilityScore();
        currentColor = 1 - currentColor;
        int nextMobScore = mobilityScore();
        currentColor = 1 - currentColor;
        if (color == currentColor) {
            score += currentMobScore - nextMobScore;
            if (!notInCheckCurrent()) {
                if (currentMobScore == 0) {
                    score -= 100000;
                } else {
                    if (engine.numberOfChecks == 2) {
                        score -= 100000;
                    } else if (engine.numberOfChecks == 1) {
                        score -= 10000;
                    } else {
                        score -= 4000;
                    }
                }
            }
        } else {
            score += nextMobScore - currentMobScore;
            if (!notInCheckCurrent()) {
                if (currentMobScore == 0) {
                    score += 100000;
                } else {
                    if (opponent.numberOfChecks == 2) {
                        score += 100000;
                    } else if (opponent.numberOfChecks == 1) {
                        score += 10000;
                    } else {
                        score += 4000;
                    }
                }
            }
        }
        return score;
    }

    public MinimaxState maxValue(int depth, int color, int alfa, int beta,
                                 HashMap<CharMatrix, Integer> hm) {
        if (depth == 0) {
            MinimaxState state;
            CharMatrix tableWrapper = new CharMatrix(table);
            if (hm.containsKey(tableWrapper)) {
                state = new MinimaxState(hm.get(tableWrapper), this, "");
            } else {
                int stateScore = evaluate(color);
                hm.put(tableWrapper, stateScore);
                state = new MinimaxState(stateScore, this, "");
            }
            return state;
        }
        ArrayList<Move> allMoves = generateAllMoves();
        PriorityQueue<MinimaxState> pq = new PriorityQueue<>(new Comparator<MinimaxState>() {
            @Override
            public int compare(MinimaxState o1, MinimaxState o2) {
                return o2.score - o1.score;
            }
        });
        for (Move move : allMoves) {
            Game newGame = new Game(this);
            String command = newGame.makeMove(move);
            newGame.currentColor = 1 - currentColor;
            CharMatrix tableWrapper = new CharMatrix(newGame.table);
            MinimaxState currState;
            if (hm.containsKey(tableWrapper)) {
                int stateScore = hm.get(tableWrapper);
                currState = new MinimaxState(stateScore, newGame, command);
            } else {
                currState = new MinimaxState(newGame.evaluate(color), newGame, command);
            }
            pq.add(currState);
        }

        MinimaxState choice = new MinimaxState();
        int maxim = -2000000;
        while (!pq.isEmpty()) {
            MinimaxState curr = pq.poll();
            MinimaxState aux = curr.game.minValue(depth - 1, color, alfa, beta, hm);
            alfa = Math.max(alfa, aux.score);
            if (aux.score > maxim) {
                maxim = aux.score;
                choice = curr;
                choice.score = aux.score;
            }
            if (alfa >= beta) {
                break;
            }
        }
        return choice;
    }

    public MinimaxState minValue(int depth, int color, int alfa, int beta,
                                 HashMap<CharMatrix, Integer> hm) {
        if (depth == 0) {
            MinimaxState state;
            CharMatrix tableWrapper = new CharMatrix(table);
            if (hm.containsKey(tableWrapper)) {
                state = new MinimaxState(hm.get(tableWrapper), this, "");
            } else {
                int stateScore = evaluate(color);
                hm.put(tableWrapper, stateScore);
                state = new MinimaxState(stateScore, this, "");
            }
            return state;
        }
        ArrayList<Move> allMoves = generateAllMoves();
        PriorityQueue<MinimaxState> pq = new PriorityQueue<>(new Comparator<MinimaxState>() {
            @Override
            public int compare(MinimaxState o1, MinimaxState o2) {
                return o1.score - o2.score;
            }
        });
        for (Move move : allMoves) {
            Game newGame = new Game(this);
            String command = newGame.makeMove(move);
            newGame.currentColor = 1 - currentColor;
            CharMatrix tableWrapper = new CharMatrix(newGame.table);
            MinimaxState currState;
            if (hm.containsKey(tableWrapper)) {
                int stateScore = hm.get(tableWrapper);
                currState = new MinimaxState(stateScore, newGame, command);
            } else {
                currState = new MinimaxState(newGame.evaluate(color), newGame, command);
            }
            pq.add(currState);
        }
        MinimaxState choice = new MinimaxState();
        int minim = 2000000;
        while (!pq.isEmpty()) {
            MinimaxState curr = pq.poll();
            MinimaxState aux = curr.game.maxValue(depth - 1, color, alfa, beta, hm);
            beta = Math.min(beta, aux.score);
            if (aux.score < minim) {
                minim = aux.score;
                choice = curr;
                choice.score = aux.score;
            }
            if (beta <= alfa) {
                break;
            }
        }
        return choice;
    }

    public String makeMove(Move move) {
        Position before, after;
        String beforeString, afterString;
        String result = "";

        if (move instanceof QueenSideCastleMove) {
            players[currentColor].castled = true;
            before = players[currentColor].king;
            after = new Position(7 * currentColor, 2);
            beforeString = before.encode();
            afterString = after.encode();
            result = "move " + beforeString.concat(afterString);
            changePosition(new Move(before, after));
            before = new Position(7 * currentColor, 0);
            after = new Position(7 * currentColor, 3);
            changePosition(new Move(before, after));
            table[7 * currentColor][3] = table[7 * currentColor][0];
            table[7 * currentColor][0] = Pieces.EMPTY_TILE;
            table[7 * currentColor][2] = table[7 * currentColor][4];
            table[7 * currentColor][4] = Pieces.EMPTY_TILE;
            nrMovesForDraw++;
            return result;
        }

        if (move instanceof KingSideCastleMove) {
            players[currentColor].castled = true;
            before = players[currentColor].king;
            after = new Position(7 * currentColor, 6);
            beforeString = before.encode();
            afterString = after.encode();
            result = "move " + beforeString.concat(afterString);
            changePosition(new Move(before, after));
            before = new Position(7 * currentColor, 7);
            after = new Position(7 * currentColor, 5);
            changePosition(new Move(before, after));
            table[7 * currentColor][5] = table[7 * currentColor][7];
            table[7 * currentColor][7] = Pieces.EMPTY_TILE;
            table[7 * currentColor][6] = table[7 * currentColor][4];
            table[7 * currentColor][4] = Pieces.EMPTY_TILE;
            nrMovesForDraw++;
            return result;
        }
        before = move.getBefore();
        after = move.getAfter();

        beforeString = before.encode();
        afterString = after.encode();

        if (table[before.getLine()][before.getColumn()] == Pieces.WHITE_PAWN -
                32 * currentColor) {
            if (move instanceof PromotionMove) {
                char type = ((PromotionMove) move).getType();
                result = "move " + beforeString.concat(afterString) + type;
                changePosition(new PromotionMove(before, after, type));
                deletePiece(after);
                players[currentColor].canBeEnPassant = false;
            } else {
                result = "move " + beforeString.concat(afterString);
                changePosition(new Move(before, after));
                deletePiece(after);
                if (before.getLine() == 1 + 5 * currentColor &&
                        after.getLine() == 3 + currentColor) {
                    players[currentColor].canBeEnPassant = true;
                    players[currentColor].lastPieceMoved = after;
                }
            }
        } else {
            result = "move " + beforeString.concat(afterString);
            changePosition(new Move(before, after));
            deletePiece(after);
            players[currentColor].canBeEnPassant = false;
        }

        char pieceToMove = table[before.getLine()][before.getColumn()];
        table[after.getLine()][after.getColumn()] = pieceToMove;
        table[before.getLine()][before.getColumn()] = Pieces.EMPTY_TILE;
        return result;
    }
}
