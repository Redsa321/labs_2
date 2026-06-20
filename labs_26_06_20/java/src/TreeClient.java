import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** JavaFX client communicating with the BST server. */
public class TreeClient extends Application {
    private final ComboBox<String> type = new ComboBox<>();
    private final TextField value = new TextField();
    private final TextArea tree = new TextArea("(not connected)");
    private final Label status = new Label("Connecting to the server...");
    private final Button search = new Button("Search");
    private final Button insert = new Button("Insert");
    private final Button delete = new Button("Delete");
    private final Button draw = new Button("Draw");
    private final List<Button> operationButtons = List.of(search, insert, delete, draw);
    private final ExecutorService network = Executors.newSingleThreadExecutor();

    private volatile Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    /** Builds the client window and connects to localhost:5050. */
    @Override
    public void start(Stage stage) {
        type.getItems().addAll("Integer", "Double", "String", "Person");
        type.setValue("Integer");
        value.setPromptText("Enter a value");
        tree.setEditable(false);
        tree.setStyle("-fx-font-family: monospace; -fx-font-size: 15px;");

        search.setOnAction(event -> send("SEARCH"));
        insert.setOnAction(event -> send("INSERT"));
        delete.setOnAction(event -> send("DELETE"));
        draw.setOnAction(event -> send("DRAW"));
        value.setOnAction(event -> send("INSERT"));
        type.setOnAction(event -> {
            if (socket != null) {
                send("DRAW");
            }
        });

        Button reconnect = new Button("Reconnect");
        reconnect.setOnAction(event -> connect());

        HBox buttons = new HBox(8, search, insert, delete, draw, reconnect);
        VBox controls = new VBox(10,
                new Label("Tree type:"), type,
                new Label("Value (Person: name,age):"), value,
                buttons);
        controls.setPadding(new Insets(15));

        status.setPadding(new Insets(10));
        BorderPane root = new BorderPane(tree, controls, null, status, null);
        BorderPane.setMargin(tree, new Insets(0, 15, 10, 15));

        stage.setTitle("Generic BST — Client");
        stage.setScene(new Scene(root, 720, 580));
        stage.show();

        setButtonsDisabled(true);
        connect();
    }

    private void connect() {
        status.setText("Connecting to localhost:" + TreeServer.DEFAULT_PORT + "...");
        setButtonsDisabled(true);
        network.submit(() -> {
            closeConnection();
            try {
                socket = new Socket("localhost", TreeServer.DEFAULT_PORT);
                input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                Platform.runLater(() -> {
                    status.setText("Connected to the server.");
                    setButtonsDisabled(false);
                    send("DRAW");
                });
            } catch (IOException exception) {
                Platform.runLater(() -> status.setText(
                        "Cannot connect to the server: " + exception.getMessage()));
            }
        });
    }

    private void send(String operation) {
        if (socket == null || socket.isClosed()) {
            status.setText("Not connected to the server.");
            return;
        }
        String selectedType = type.getValue();
        String enteredValue = operation.equals("DRAW") ? "" : value.getText();
        setButtonsDisabled(true);

        network.submit(() -> {
            try {
                output.writeUTF(selectedType);
                output.writeUTF(operation);
                output.writeUTF(enteredValue);
                output.flush();

                boolean success = input.readBoolean();
                String message = input.readUTF();
                String drawing = input.readUTF();
                Platform.runLater(() -> {
                    status.setText((success ? "" : "Error: ") + message);
                    if (!drawing.isEmpty()) {
                        tree.setText(drawing);
                    }
                    setButtonsDisabled(false);
                });
            } catch (IOException exception) {
                closeConnection();
                Platform.runLater(() -> {
                    status.setText("Connection lost: " + exception.getMessage());
                    setButtonsDisabled(true);
                });
            }
        });
    }

    private void setButtonsDisabled(boolean disabled) {
        operationButtons.forEach(button -> button.setDisable(disabled));
    }

    private void closeConnection() {
        Socket current = socket;
        socket = null;
        if (current != null) {
            try {
                current.close();
            } catch (IOException ignored) {
                // The connection is already being closed.
            }
        }
    }

    /** Closes the connection when the application exits. */
    @Override
    public void stop() {
        closeConnection();
        network.shutdownNow();
    }

    /** Launches the JavaFX application. */
    public static void main(String[] args) {
        launch(args);
    }
}
