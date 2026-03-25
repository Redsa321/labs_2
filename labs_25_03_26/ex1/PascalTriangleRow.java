package labs_25_03_26.ex1;

class PascalTriangleRow {
    private int[] row;

    public PascalTriangleRow(int n) throws InvalidRowNumberException {
        if (n < 0) {
            throw new InvalidRowNumberException("Nieprawidłowy numer wiersza");
        }
        generateRow(n);
    }

    private void generateRow(int n) {
        row = new int[n + 1];
        row[0] = 1;
        for (int k = 1; k <= n; k++) {
            row[k] = (int) ((long) row[k - 1] * (n - k + 1) / k);
        }
    }

    // Metoda zwracająca wartość m-tego elementu
    public int getElement(int m) throws OutOfBoundsException {
        if (m < 0 || m >= row.length) {
            throw new OutOfBoundsException("liczba spoza zakresu");
        }
        return row[m];
    }
}