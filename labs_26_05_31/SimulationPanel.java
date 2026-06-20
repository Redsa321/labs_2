import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * Panel Swing wyświetlający siatkę komórek symulacji kolorów.
 *
 * <p>
 * Każde pole siatki n×m jest reprezentowane przez wątek {@link CellThread}.
 * Panel odpowiada za:
 * <ul>
 * <li>inicjalizację i uruchamianie wątków ({@link #initGrid()}),</li>
 * <li>renderowanie siatki ({@link #paintComponent(Graphics)}),</li>
 * <li>obsługę kliknięć myszą - zawieszanie/wznawianie wątków,</li>
 * <li>dynamiczne dodawanie wiersza ({@link #addRow()}) lub kolumny
 * ({@link #addColumn()}) wraz z nowymi wątkami,</li>
 * <li>zatrzymanie wszystkich wątków ({@link #stopAll()}).</li>
 * </ul>
 *
 * <p>
 * Jedynym generatorem liczb pseudolosowych w całej aplikacji jest statyczne
 * pole
 * {@link #random}. Wszystkie klasy korzystają z niego przez tę referencję.
 *
 * <p>
 * Synchronizacja operacji wątków jest zapewniona przez {@link #globalLock}:
 * każdy wątek komórki wchodzi w sekcję krytyczną pod tym lockiem, co gwarantuje
 * nieprzeplecione pary komunikatów {@code Start: X} / {@code End: X}.
 *
 * @author Serhii Onopriienko
 * @version 1.0
 * @see CellThread
 * @see ColorSimulation
 */
@SuppressWarnings("serial")
public final class SimulationPanel extends JPanel implements MouseListener {

    /**
     * Jeden wspólny generator liczb pseudolosowych dla całej aplikacji.
     * Używany zarówno przez {@link CellThread} (czas sleepowania, wybór koloru),
     * jak i przez {@link #randomColor()} do inicjalizacji planszy.
     */
    public static final Random random = new Random();

    /**
     * Globalny lock serializujący sekcje krytyczne wątków komórek.
     *
     * <p>
     * Każdy {@link CellThread} nabywa ten monitor przed wypisaniem
     * {@code "Start: X"} i zwalnia go po wypisaniu {@code "End: X"}.
     * Gwarantuje to, że pary Start/End nigdy się nie przeplatają.
     */
    public final Object globalLock = new Object();

    /** Dwuwymiarowa siatka wątków komórek. */
    private CellThread[][] cells;

    /** Liczba wierszy aktualnej siatki. */
    private int rows;

    /** Liczba kolumn aktualnej siatki. */
    private int cols;

    /** Parametr szybkości: opóźnienie mieści się w {@code [0.5k, 1.5k]} ms. */
    private final int k;

    /** Prawdopodobieństwo losowej zmiany koloru (0.0–1.0). */
    private final double p;

    /** Rozmiar boku pojedynczej komórki w pikselach. */
    private final int cellSize;

    /**
     * Licznik przydzielający unikalne indeksy nowym wątkom.
     * Rośnie monotonicznie przy każdym nowym {@link CellThread}.
     */
    private int nextIndex = 0;

    /**
     * Tworzy panel symulacji z podanymi parametrami i natychmiast inicjalizuje
     * siatkę wątków.
     *
     * @param rows     liczba wierszy planszy
     * @param cols     liczba kolumn planszy
     * @param k        parametr szybkości (ms)
     * @param p        prawdopodobieństwo losowej zmiany koloru
     * @param cellSize rozmiar komórki w pikselach
     */
    public SimulationPanel(int rows, int cols, int k, double p, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.k = k;
        this.p = p;
        this.cellSize = cellSize;
        setBackground(Color.WHITE);
        addMouseListener(this);
        initGrid();
    }

    /**
     * Tworzy siatkę komórek z losowymi kolorami i uruchamia wszystkie wątki.
     *
     * <p>
     * Każda komórka otrzymuje unikalny numer (pole {@code nextIndex}) rosnący
     * monotonicznie przez cały czas życia panelu - również przy dodawaniu nowych
     * wierszy/kolumn.
     */
    private void initGrid() {
        cells = new CellThread[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new CellThread(r, c, randomColor(), this, nextIndex++);
            }
        }
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c].start();
            }
        }
    }

    /**
     * Renderuje siatkę komórek.
     *
     * <p>
     * Każda komórka jest rysowana jako wypełniony prostokąt o kolorze pobieranym
     * z {@link CellThread#getColor()}. Zawieszony wątek jest oznaczony ciemnym
     * półprzezroczystym nakładką i symbolem {@code ■} na środku.
     *
     * <p>
     * Metoda wywoływana na EDT - nie wymaga synchronizacji z wątkami komórek,
     * ponieważ pole {@code color} w {@link CellThread} jest {@code volatile}.
     *
     * @param g kontekst graficzny Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        CellThread[][] snapshot = cells;
        int curRows = rows;
        int curCols = cols;
        for (int r = 0; r < curRows; r++) {
            for (int c = 0; c < curCols; c++) {
                CellThread cell = snapshot[r][c];
                int x = c * cellSize;
                int y = r * cellSize;

                g.setColor(cell.getColor());
                g.fillRect(x, y, cellSize, cellSize);

                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, cellSize, cellSize);

                if (cell.isSuspended()) {
                    g.setColor(new Color(0, 0, 0, 130));
                    g.fillRect(x, y, cellSize, cellSize);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Dialog", Font.BOLD, cellSize / 2));
                    FontMetrics fm = g.getFontMetrics();
                    String mark = "■";
                    int tx = x + (cellSize - fm.stringWidth(mark)) / 2;
                    int ty = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                    g.drawString(mark, tx, ty);
                }
            }
        }
    }

    /**
     * Obsługuje kliknięcie na komórkę: przełącza zawieszenie/wznowienie wątku.
     *
     * <p>
     * Kliknięcie na aktywną (działającą) komórkę zawiesza jej wątek.
     * Kliknięcie na zawieszoną komórkę wznawia wątek.
     *
     * @param e zdarzenie kliknięcia myszą
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        int c = e.getX() / cellSize;
        int r = e.getY() / cellSize;
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            CellThread cell = cells[r][c];
            if (cell.isSuspended()) {
                cell.resumeThread();
            } else {
                cell.suspendThread();
            }
            repaint();
        }
    }

    /** Nieużywane zdarzenie myszy - wymagane przez {@link MouseListener}. */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /** Nieużywane zdarzenie myszy - wymagane przez {@link MouseListener}. */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /** Nieużywane zdarzenie myszy - wymagane przez {@link MouseListener}. */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /** Nieużywane zdarzenie myszy - wymagane przez {@link MouseListener}. */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Dynamicznie dodaje nowy wiersz z aktywnymi wątkami do działającej symulacji.
     *
     * <p>
     * Operacja wykonywana jest pod {@link #globalLock}, aby żaden wątek nie
     * czytał starych wymiarów siatki podczas jej rozszerzania. Nowe komórki
     * otrzymują losowe kolory i są natychmiast uruchamiane.
     */
    public void addRow() {
        synchronized (globalLock) {
            int newRow = rows;
            CellThread[][] newCells = new CellThread[rows + 1][cols];
            for (int r = 0; r < rows; r++) {
                System.arraycopy(cells[r], 0, newCells[r], 0, cols);
            }
            for (int c = 0; c < cols; c++) {
                newCells[newRow][c] = new CellThread(newRow, c, randomColor(), this, nextIndex++);
            }
            cells = newCells;
            rows++;
            setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
            for (int c = 0; c < cols; c++) {
                newCells[newRow][c].start();
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Dynamicznie dodaje nową kolumnę z aktywnymi wątkami do działającej symulacji.
     *
     * <p>
     * Operacja wykonywana jest pod {@link #globalLock} - analogicznie do
     * {@link #addRow()}. Nowe komórki otrzymują losowe kolory i są natychmiast
     * uruchamiane.
     */
    public void addColumn() {
        synchronized (globalLock) {
            int newCol = cols;
            CellThread[][] newCells = new CellThread[rows][cols + 1];
            for (int r = 0; r < rows; r++) {
                System.arraycopy(cells[r], 0, newCells[r], 0, cols);
                newCells[r][newCol] = new CellThread(r, newCol, randomColor(), this, nextIndex++);
            }
            cells = newCells;
            cols++;
            setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
            for (int r = 0; r < rows; r++) {
                newCells[r][newCol].start();
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Zatrzymuje wszystkie wątki komórek i czeka na ich zakończenie.
     *
     * <p>
     * Wywoływana przed restartem symulacji z nowymi parametrami lub przy
     * zamykaniu okna aplikacji. Każdy wątek jest przerywany przez
     * {@link CellThread#stopThread()}, po czym blokująco czekamy na jego
     * zakończenie przez {@link Thread#join()}.
     */
    public void stopAll() {
        CellThread[][] snapshot = cells;
        for (CellThread[] row : snapshot) {
            for (CellThread cell : row) {
                cell.stopThread();
            }
        }
        for (CellThread[] row : snapshot) {
            for (CellThread cell : row) {
                try {
                    cell.join(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Generuje losowy kolor RGB korzystając ze wspólnego generatora
     * {@link #random}.
     *
     * @return nowy losowy kolor
     */
    public static Color randomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    /**
     * Zwraca bieżącą siatkę wątków komórek.
     *
     * <p>
     * Używane przez {@link CellThread#computeNeighborAverage()} do dostępu
     * do sąsiadów. Wywołanie odbywa się wyłącznie pod {@link #globalLock}.
     *
     * @return dwuwymiarowa tablica wątków komórek
     */
    public CellThread[][] getCells() {
        return cells;
    }

    /**
     * Zwraca aktualną liczbę wierszy planszy.
     *
     * @return liczba wierszy
     */
    public int getRows() {
        return rows;
    }

    /**
     * Zwraca aktualną liczbę kolumn planszy.
     *
     * @return liczba kolumn
     */
    public int getCols() {
        return cols;
    }

    /**
     * Zwraca parametr szybkości symulacji.
     *
     * @return wartość {@code k} w milisekundach
     */
    public int getK() {
        return k;
    }

    /**
     * Zwraca prawdopodobieństwo losowej zmiany koloru.
     *
     * @return wartość {@code p} z przedziału {@code [0.0, 1.0]}
     */
    public double getP() {
        return p;
    }
}
