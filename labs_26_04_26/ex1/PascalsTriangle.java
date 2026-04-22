import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PascalsTriangle extends JFrame {

    private JTextField inputField;
    private JTextArea displayArea;
    private JButton generateButton;

    public PascalsTriangle() {
        // main window
        setTitle("Pascal's Triangle Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 248, 255));

        // input
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(240, 248, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Enter triangle size (number of rows):");
        label.setFont(new Font("Arial", Font.BOLD, 14));

        inputField = new JTextField(10);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));

        generateButton = new JButton("Generate");
        generateButton.setFont(new Font("Arial", Font.BOLD, 14));
        generateButton.setBackground(new Color(70, 130, 180)); // Steel Blue
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);

        topPanel.add(label);
        topPanel.add(inputField);
        topPanel.add(generateButton);

        // display
        displayArea = new JTextArea(40, 80);
        displayArea.setEditable(false);
        // monospaced font to show triangle properly
        displayArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        displayArea.setBackground(new Color(255, 250, 240));
        displayArea.setForeground(new Color(25, 25, 112));

        // scroll
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Result"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateAndDisplayTriangle();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void generateAndDisplayTriangle() {
        // clear before displaying
        displayArea.setText("");
        String input = inputField.getText().trim();

        try {
            int size = Integer.parseInt(input);

            if (size <= 0) {
                throw new IllegalArgumentException("Triangle size must be a number greater than zero.");
            }
            if (size > 30) {
                throw new IllegalArgumentException("For readability the maximum size is 30 rows.");
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                // leading spaces to create triangle shape
                for (int j = 0; j < size - i; j++) {
                    sb.append("     ");
                }

                long number = 1;
                for (int j = 0; j <= i; j++) {
                    sb.append(String.format("%10d", number));
                    number = number * (i - j) / (j + 1);
                }
                sb.append("\n");
            }
            displayArea.setText(sb.toString());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid integer.",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Invalid Value",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unexpected error:\n" + ex.getMessage(),
                    "Unexpected Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PascalsTriangle app = new PascalsTriangle();
                app.setVisible(true);
            }
        });
    }
}