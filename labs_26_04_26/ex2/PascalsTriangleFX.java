import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class PascalsTriangleFX extends Application {

    private TextField inputField;
    private TextArea displayArea;

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Enter triangle size (number of rows):");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        inputField = new TextField();
        inputField.setPrefColumnCount(10);
        inputField.setFont(Font.font("Arial", 14));

        Button generateButton = new Button("Generate");
        generateButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        generateButton.setStyle("-fx-background-color: #4682B4; -fx-text-fill: white; -fx-cursor: hand;");

        HBox topBox = new HBox(10, label, inputField, generateButton);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-background-color: #F0F8FF;");

        displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        displayArea.setStyle("-fx-control-inner-background: #FFFAF0; -fx-text-fill: #191970;");
        displayArea.setPrefRowCount(20);
        displayArea.setPrefColumnCount(50);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(displayArea);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Pascal's Triangle Generator (JavaFX)");
        primaryStage.setScene(scene);

        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();

        generateButton.setOnAction(e -> generateAndDisplayTriangle());
    }

    private void generateAndDisplayTriangle() {
        displayArea.clear();
        String input = inputField.getText().trim();

        try {
            int size = Integer.parseInt(input);

            if (size <= 0) {
                throw new IllegalArgumentException("Triangle size must be a number greater than zero.");
            }
            if (size > 30) {
                throw new IllegalArgumentException("For readability, the maximum size is 30 rows.");
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
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
            showAlert(Alert.AlertType.ERROR, "Format Error", "Input error! Please enter a valid integer.");
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Invalid Value", ex.getMessage());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Critical Error",
                    "An unexpected application error occurred:\n" + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}