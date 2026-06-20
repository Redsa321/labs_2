import java.awt.Color;
import javax.swing.SwingUtilities;

/**
 * Wątek reprezentujący pojedyncze pole siatki symulacji kolorów.
 *
 * <p>
 * Każde pole planszy jest osobnym wątkiem działającym w nieskończonej pętli.
 * Co losowy czas z przedziału {@code [0.5k, 1.5k]} ms wątek:
 * <ul>
 * <li>z prawdopodobieństwem {@code p} - zmienia kolor na losowy,</li>
 * <li>z prawdopodobieństwem {@code 1-p} - ustawia kolor jako średnią RGB
 * czterech aktywnych sąsiadów (plansza jako torus 2D).</li>
 * </ul>
 *
 * <p>
 * Każda iteracja jest otoczona globalnym lockiem
 * ({@link SimulationPanel#globalLock}),
 * co gwarantuje sekwencyjność wypisywanych komunikatów
 * {@code "Start: X"} / {@code "End: X"} i brak konfliktów przy odczycie kolorów
 * sąsiadów.
 *
 * <p>
 * Wątek można zawiesić ({@link #suspendThread()}) i wznowić
 * ({@link #resumeThread()}) kliknięciem na odpowiednie pole w GUI.
 * Zawieszony wątek nie zmienia koloru i nie jest brany pod uwagę
 * przez sąsiadów przy obliczaniu średniej.
 *
 * @author Serhii Onopriienko
 * @see SimulationPanel
 */
public final class CellThread extends Thread {

    /** Numer wątku wypisywany w komunikatach Start/End. */
    private final int index;

    /** Wiersz pola na planszy (0-indeksowany). */
    private final int row;

    /** Kolumna pola na planszy (0-indeksowany). */
    private final int col;

    /**
     * Aktualny kolor pola.
     * Zadeklarowany {@code volatile} dla bezpiecznego odczytu z EDT podczas
     * renderowania ({@link SimulationPanel#paintComponent}).
     */
    private volatile Color color;

    /** Referencja do panelu - dostęp do globalLock, siatki komórek i parametrów. */
    private final SimulationPanel panel;

    /**
     * Flaga zawieszenia wątku.
     * Ustawiana przez {@link #suspendThread()} i kasowana przez
     * {@link #resumeThread()}.
     * {@code volatile} zapewnia widoczność zmiany między wątkami.
     */
    private volatile boolean suspended = false;

    /**
     * Flaga działania pętli głównej.
     * {@code false} kończy pętlę {@link #run()}.
     */
    private volatile boolean running = true;

    /**
     * Monitor używany do zawieszania wątku przez {@code wait()} /
     * {@code notifyAll()}.
     * Oddzielny obiekt od {@code globalLock}, aby zawieszony wątek nie blokował
     * reszty symulacji.
     */
    private final Object suspendLock = new Object();

    /**
     * Tworzy nowy wątek komórki z podanymi parametrami.
     *
     * @param row   wiersz pola na planszy
     * @param col   kolumna pola na planszy
     * @param color początkowy kolor pola (losowy)
     * @param panel panel symulacji (źródło globalLock i siatki)
     * @param index unikalny numer wątku do komunikatów Start/End
     */
    public CellThread(int row, int col, Color color, SimulationPanel panel, int index) {
        this.row = row;
        this.col = col;
        this.color = color;
        this.panel = panel;
        this.index = index;
        setDaemon(true);
    }

    /**
     * Główna pętla wątku komórki.
     *
     * <p>
     * Schemat jednej iteracji:
     * <ol>
     * <li>Losowy uśpienie {@code [0.5k, 1.5k]} ms.</li>
     * <li>Jeśli wątek jest zawieszony - {@code wait()} aż do wznowienia.</li>
     * <li>Wejście w sekcję krytyczną ({@code globalLock}):
     * <ul>
     * <li>wypisanie {@code "Start: index"},</li>
     * <li>obliczenie nowego koloru,</li>
     * <li>wypisanie {@code "End: index"}.</li>
     * </ul>
     * </li>
     * <li>Odświeżenie widoku przez {@link SwingUtilities#invokeLater}.</li>
     * </ol>
     */
    @Override
    public void run() {
        int k = panel.getK();
        double p = panel.getP();

        while (running) {
            long delay = (long) (0.5 * k + SimulationPanel.random.nextDouble() * k);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            synchronized (suspendLock) {
                while (suspended && running) {
                    try {
                        suspendLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        running = false;
                        break;
                    }
                }
            }

            if (!running)
                break;

            synchronized (panel.globalLock) {
                System.out.println("Start: " + index);

                if (SimulationPanel.random.nextDouble() < p) {
                    color = SimulationPanel.randomColor();
                } else {
                    color = computeNeighborAverage();
                }

                System.out.println("End: " + index);
            }

            SwingUtilities.invokeLater(panel::repaint);
        }
    }

    /**
     * Oblicza kolor jako średnią RGB czterech sąsiadów (topologia torusa).
     *
     * <p>
     * Sąsiedzi zawieszeni ({@link #isSuspended()}) są pomijani. Jeśli wszyscy
     * czterej sąsiedzi są zawieszeni, metoda zwraca aktualny kolor bez zmiany.
     *
     * <p>
     * Wywoływana wyłącznie pod {@code globalLock} - kolory sąsiadów nie mogą
     * się zmienić w trakcie odczytu.
     *
     * @return uśredniony kolor sąsiadów lub bieżący kolor, gdy brak aktywnych
     *         sąsiadów
     */
    private Color computeNeighborAverage() {
        CellThread[][] cells = panel.getCells();
        int rows = panel.getRows();
        int cols = panel.getCols();

        int[] dr = { -1, 1, 0, 0 };
        int[] dc = { 0, 0, -1, 1 };

        int totalR = 0, totalG = 0, totalB = 0, count = 0;
        for (int d = 0; d < 4; d++) {
            int nr = ((row + dr[d]) + rows) % rows;
            int nc = ((col + dc[d]) + cols) % cols;
            CellThread neighbor = cells[nr][nc];
            if (!neighbor.isSuspended()) {
                Color nc2 = neighbor.color;
                totalR += nc2.getRed();
                totalG += nc2.getGreen();
                totalB += nc2.getBlue();
                count++;
            }
        }

        if (count == 0)
            return color;
        return new Color(totalR / count, totalG / count, totalB / count);
    }

    /**
     * Zawiesza wątek.
     *
     * <p>
     * Ustawia flagę {@code suspended}. Wątek dokończy bieżący sleep, po czym
     * zatrzyma się przed wejściem w sekcję krytyczną i będzie czekał na
     * {@link #resumeThread()}.
     */
    public void suspendThread() {
        suspended = true;
    }

    /**
     * Wznawia zawieszony wątek.
     *
     * <p>
     * Kasuje flagę {@code suspended} i wybudza wątek czekający na
     * {@code suspendLock}.
     */
    public void resumeThread() {
        synchronized (suspendLock) {
            suspended = false;
            suspendLock.notifyAll();
        }
    }

    /**
     * Zatrzymuje pętlę główną wątku.
     *
     * <p>
     * Ustawia {@code running = false} i wybudza wątek z ewentualnego
     * zawieszenia, aby mógł sprawdzić flagę i zakończyć działanie.
     * Po wywołaniu tej metody należy wywołać {@link #join()}, aby poczekać
     * na faktyczne zakończenie.
     */
    public void stopThread() {
        running = false;
        resumeThread();
        interrupt();
    }

    /**
     * Zwraca aktualny kolor pola.
     *
     * <p>
     * Dostęp jest bezpieczny z EDT dzięki deklaracji {@code volatile} na polu
     * {@code color}. Przy odczycie przez sąsiadów należy trzymać
     * {@code globalLock}.
     *
     * @return aktualny kolor pola
     */
    public Color getColor() {
        return color;
    }

    /**
     * Ustawia kolor pola.
     *
     * @param color nowy kolor
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Sprawdza, czy wątek jest aktualnie zawieszony.
     *
     * @return {@code true}, jeśli wątek jest zawieszony
     */
    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Zwraca numer wątku używany w komunikatach Start/End.
     *
     * @return unikalny numer wątku
     */
    public int getIndex() {
        return index;
    }

    /**
     * Zwraca wiersz pola na planszy.
     *
     * @return numer wiersza (0-indeksowany)
     */
    public int getRow() {
        return row;
    }

    /**
     * Zwraca kolumnę pola na planszy.
     *
     * @return numer kolumny (0-indeksowany)
     */
    public int getCol() {
        return col;
    }
}
