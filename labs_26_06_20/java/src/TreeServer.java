import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/** Multi-client server operating on four typed trees. */
public class TreeServer {
    public static final int DEFAULT_PORT = 5050;

    private final BinarySearchTree<Integer> integers = new BinarySearchTree<>();
    private final BinarySearchTree<Double> doubles = new BinarySearchTree<>();
    private final BinarySearchTree<String> strings = new BinarySearchTree<>();
    private final BinarySearchTree<Person> people = new BinarySearchTree<>();

    /** Starts the server. An optional argument selects the port. */
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
        new TreeServer().start(port);
    }

    /** Accepts clients and handles each connection in a separate thread. */
    public void start(int port) throws IOException {
        ExecutorService clients = Executors.newCachedThreadPool();
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("BST server is listening on port " + port + '.');
            while (true) {
                Socket socket = server.accept();
                clients.submit(() -> handleClient(socket));
            }
        } finally {
            clients.shutdown();
        }
    }

    private void handleClient(Socket socket) {
        System.out.println("Connected: " + socket.getRemoteSocketAddress());
        try (socket;
             DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            while (true) {
                String type = input.readUTF();
                String operation = input.readUTF();
                String value = input.readUTF();

                Result result = execute(type, operation, value);
                output.writeBoolean(result.success());
                output.writeUTF(result.message());
                output.writeUTF(result.tree());
                output.flush();
            }
        } catch (EOFException ignored) {
            // The client closed the connection.
        } catch (IOException exception) {
            System.err.println("Client error: " + exception.getMessage());
        } finally {
            System.out.println("Disconnected: " + socket.getRemoteSocketAddress());
        }
    }

    private Result execute(String type, String operation, String value) {
        try {
            return switch (type) {
                case "Integer" -> executeOn(integers, operation, value, Integer::valueOf);
                case "Double" -> executeOn(doubles, operation, value, TreeServer::parseDouble);
                case "String" -> executeOn(strings, operation, value, text -> text);
                case "Person" -> executeOn(people, operation, value, Person::parse);
                default -> Result.error("Unknown tree type.");
            };
        } catch (IllegalArgumentException exception) {
            return Result.error("Invalid value: " + exception.getMessage());
        }
    }

    private <T extends Comparable<T>> Result executeOn(
            BinarySearchTree<T> tree, String operation, String text, Function<String, T> parser) {
        if (operation.equals("DRAW")) {
            return Result.ok("Current tree.", tree.draw());
        }
        if (text.isBlank()) {
            return Result.error("Enter a value.");
        }

        T value = parser.apply(text);
        return switch (operation) {
            case "SEARCH" -> tree.search(value)
                    ? Result.ok("Found: " + value, "")
                    : Result.ok("Not found: " + value, "");
            case "INSERT" -> tree.insert(value)
                    ? Result.ok("Inserted: " + value, tree.draw())
                    : Result.ok("Value already exists: " + value, tree.draw());
            case "DELETE" -> tree.delete(value)
                    ? Result.ok("Deleted: " + value, tree.draw())
                    : Result.ok("Not found: " + value, tree.draw());
            default -> Result.error("Unknown operation.");
        };
    }

    private static Double parseDouble(String text) {
        double value = Double.parseDouble(text);
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("the number must be finite");
        }
        return value;
    }

    private record Result(boolean success, String message, String tree) {
        static Result ok(String message, String tree) {
            return new Result(true, message, tree);
        }

        static Result error(String message) {
            return new Result(false, message, "");
        }
    }
}
