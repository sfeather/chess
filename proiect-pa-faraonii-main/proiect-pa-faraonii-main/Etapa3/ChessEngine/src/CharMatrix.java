import java.util.Arrays;

public class CharMatrix {
    char[][] table;

    public CharMatrix(char[][] matrix) {
        table = new char[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                table[i][j] = matrix[i][j];
            }
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CharMatrix that = (CharMatrix) o;
        return Arrays.deepEquals(table, that.table);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(table);
    }
}
