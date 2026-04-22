import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PascalProcessGUI extends JFrame {

    private JTextField fieldExecutable;
    private JTextField fieldRowN;
    private JTextField fieldIndicesM;
    private JTextArea displayArea;
    private JButton buttonRun;

    public PascalProcessGUI() {
        setTitle("GUI dla programu c++");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelInput = new JPanel(new GridLayout(3, 2, 5, 5));
        panelInput.setBorder(BorderFactory.createTitledBorder("Parametry uruchomienia"));

        panelInput.add(new JLabel("Ścieżka do skompilowanego C++:"));
        fieldExecutable = new JTextField("../../labs_26_03_30/ex2/src/PascalTriangleRow");
        panelInput.add(fieldExecutable);

        panelInput.add(new JLabel("Numer wiersza (n):"));
        fieldRowN = new JTextField();
        panelInput.add(fieldRowN);

        panelInput.add(new JLabel("Indeksy (m) po spacji:"));
        fieldIndicesM = new JTextField();
        panelInput.add(fieldIndicesM);

        displayArea = new JTextArea(15, 40);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Wyjście z programu C++"));

        JPanel panelBottom = new JPanel();
        buttonRun = new JButton("Uruchom kod");
        buttonRun.setFont(new Font("Arial", Font.BOLD, 14));
        panelBottom.add(buttonRun);

        add(panelInput, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);

        buttonRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCppProcess();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void runCppProcess() {
        displayArea.setText("");

        String executablePath = fieldExecutable.getText().trim();
        String rowN = fieldRowN.getText().trim();
        String indices = fieldIndicesM.getText().trim();

        List<String> command = new ArrayList<>();
        command.add(executablePath);

        if (!rowN.isEmpty()) {
            command.add(rowN);
        }

        String[] indicesArray = indices.split("\\s+");
        for (String idx : indicesArray) {
            if (!idx.isEmpty()) {
                command.add(idx);
            }
        }

        try {

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            output.append("\n[Proces zakończony z kodem: ").append(exitCode).append("]");

            // Wyświetlenie wyniku w interfejsie graficznym
            displayArea.setText(output.toString());

        } catch (Exception ex) {
            // Przechwytywanie błędów np. gdy plik nie istnieje lub brak uprawnień
            JOptionPane.showMessageDialog(this,
                    "Błąd podczas uruchamiania procesu:\n" + ex.getMessage(),
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PascalProcessGUI().setVisible(true);
        });
    }
}