import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Główna klasa aplikacji — okno Swing zawierające symulację kolorów.
 *
 * <p>
 * Aplikacja symuluje planszę n×m pól, gdzie każde pole jest osobnym wątkiem
 * ({@link CellThread}). Wątki co losowy czas zmieniają kolor pola:
 * <ul>
 * <li>z prawdopodobieństwem {@code p} — na losowy kolor,</li>
 * <li>z prawdopodobieństwem {@code 1-p} — na średnią kolorów czterech sąsiadów
 * (plansza traktowana jako torus 2D).</li>
 * </ul>
 *
 * <p>
 * Parametry można podać jako argumenty wiersza poleceń:
 * {@code java ColorSimulation n m k p}
 * (np. {@code java ColorSimulation 10 10 500 0.3}).
 *
 * @author Serhii Onopriienko
 * @version 1.0
 * @see SimulationPanel
 * @see CellThread
 */
public class ColorSimulation extends JFrame {

    /** Pole tekstowe dla liczby wierszy planszy. */
    private final JTextField nField = new JTextField("10", 4);

    /** Pole tekstowe dla liczby kolumn planszy. */
    private final JTextField mField = new JTextField("10", 4);

    /**
     * Pole tekstowe dla parametru szybkości {@code k}.
     * Opóźnienie wątku losowane z {@code [0.5k, 1.5k]} ms.
     */
    private final JTextField kField = new JTextField("500", 5);

    /**
     * Pole tekstowe dla prawdopodobieństwa {@code p}.
     * Wartość z przedziału 0.0–1.0.
     */
    private final JTextField pField = new JTextField("0.3", 4);

    /**
     * Aktualnie wyświetlany panel symulacji. Null przed pierwszym uruchomieniem.
     */
    private SimulationPanel simPanel = null;

    /**
     * Przewijalny kontener na panel symulacji — umożliwia obsługę dużych siatek
     * bez zmiany rozmiaru okna.
     */
    private final JScrollPane scrollPane = new JScrollPane();

    /**
     * Tworzy okno aplikacji i buduje jego zawartość.
     */
    public ColorSimulation() {
        super("Symulacja kolorów — Lista 6");
        buildUI();
    }

    /**
     * Buduje i rozmieszcza wszystkie elementy interfejsu użytkownika.
     *
     * <p>
     * Struktura:
     * <ul>
     * <li>NORTH — panel parametrów z przyciskiem Start,</li>
     * <li>CENTER — {@link JScrollPane} na panel symulacji,</li>
     * <li>SOUTH — przyciski "Dodaj wiersz" i "Dodaj kolumnę".</li>
     * </ul>
     */
    private void buildUI() {
        setLayout(new BorderLayout(4, 4));

        // --- Panel parametrów (góra) ---
        JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        paramPanel.add(new JLabel("Wiersze (n):"));
        paramPanel.add(nField);
        paramPanel.add(new JLabel("Kolumny (m):"));
        paramPanel.add(mField);
        paramPanel.add(new JLabel("Szybkość k (ms):"));
        paramPanel.add(kField);
        paramPanel.add(new JLabel("Prawdop. p (0–1):"));
        paramPanel.add(pField);

        JButton startBtn = new JButton("Start");
        startBtn.addActionListener(e -> startSimulation());
        paramPanel.add(startBtn);

        JButton stopBtn = new JButton("Stop");
        stopBtn.addActionListener(e -> stopSimulation());
        paramPanel.add(stopBtn);

        add(paramPanel, BorderLayout.NORTH);

        // --- Środek: przewijalna siatka ---
        scrollPane.setPreferredSize(new Dimension(620, 520));
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel dolny: dynamiczne dodawanie ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        JButton addRowBtn = new JButton("Dodaj wiersz");
        addRowBtn.addActionListener(e -> {
            if (simPanel != null) {
                simPanel.addRow();
                pack();
            }
        });
        JButton addColBtn = new JButton("Dodaj kolumnę");
        addColBtn.addActionListener(e -> {
            if (simPanel != null) {
                simPanel.addColumn();
                pack();
            }
        });
        bottomPanel.add(addRowBtn);
        bottomPanel.add(addColBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Bezpieczne zamknięcie: zatrzymaj wszystkie wątki przed wyjściem
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (simPanel != null)
                    simPanel.stopAll();
                dispose();
                System.exit(0);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Odczytuje parametry z pól tekstowych, waliduje je i uruchamia nową symulację.
     *
     * <p>
     * Przed uruchomieniem nowej symulacji zatrzymuje poprzednią
     * ({@link SimulationPanel#stopAll()}).
     * Rozmiar komórki jest dobierany automatycznie tak, aby siatka mieściła się
     * w widocznym obszarze (maks. 60 px, min. 10 px).
     *
     * <p>
     * W przypadku błędnych danych wyświetlane jest okno dialogowe z komunikatem.
     */
    private void startSimulation() {
        int n, m, k;
        double p;
        try {
            n = Integer.parseInt(nField.getText().trim());
            m = Integer.parseInt(mField.getText().trim());
            k = Integer.parseInt(kField.getText().trim());
            p = Double.parseDouble(pField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Nieprawidłowe wartości parametrów.\n" +
                            "n, m, k — liczby całkowite; p — liczba dziesiętna.",
                    "Błąd parametrów", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (n < 1 || n > 100 || m < 1 || m > 100) {
            JOptionPane.showMessageDialog(this,
                    "Wymiary planszy muszą być w przedziale [1, 100].",
                    "Błąd parametrów", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (k <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Parametr k musi być większy od zera.",
                    "Błąd parametrów", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (p < 0.0 || p > 1.0) {
            JOptionPane.showMessageDialog(this,
                    "Prawdopodobieństwo p musi być w przedziale [0.0, 1.0].",
                    "Błąd parametrów", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (simPanel != null)
            simPanel.stopAll();

        int cellSize = Math.max(10, Math.min(60, 600 / Math.max(n, m)));
        simPanel = new SimulationPanel(n, m, k, p, cellSize);
        scrollPane.setViewportView(simPanel);
        pack();
        repaint();
    }

    /**
     * Zatrzymuje działającą symulację i usuwa panel siatki z widoku.
     *
     * <p>
     * Wywołuje {@link SimulationPanel#stopAll()}, co przerywa wszystkie wątki
     * komórek i czeka na ich zakończenie. Po zatrzymaniu panel jest usuwany
     * z {@link JScrollPane}. Symulację można ponownie uruchomić przyciskiem Start.
     *
     * <p>
     * Jeśli symulacja nie jest uruchomiona, metoda nie wykonuje żadnej akcji.
     */
    private void stopSimulation() {
        if (simPanel == null)
            return;
        simPanel.stopAll();
        simPanel = null;
        scrollPane.setViewportView(null);
        repaint();
    }

    /**
     * Punkt wejścia aplikacji.
     *
     * <p>
     * Przyjmuje opcjonalne argumenty wiersza poleceń w kolejności:
     * {@code n m k p}. Jeśli podano cztery argumenty, są one wpisywane do pól
     * tekstowych i symulacja startuje automatycznie.
     *
     * @param args argumenty wiersza poleceń: {@code n m k p} (opcjonalne)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ColorSimulation app = new ColorSimulation();

            if (args.length == 4) {
                app.nField.setText(args[0]);
                app.mField.setText(args[1]);
                app.kField.setText(args[2]);
                app.pField.setText(args[3]);
                app.startSimulation();
            }

            app.setVisible(true);
        });
    }
}
