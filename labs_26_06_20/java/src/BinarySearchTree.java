import java.util.Arrays;

/**
 * Simple generic binary search tree.
 * Duplicate values are not inserted.
 *
 * @param <T> type of values stored in the tree
 */
public class BinarySearchTree<T extends Comparable<T>> {
    private Node<T> root;

    /** Inserts a value into the tree. */
    public synchronized boolean insert(T value) {
        if (root == null) {
            root = new Node<>(value);
            return true;
        }

        Node<T> current = root;
        while (true) {
            int comparison = value.compareTo(current.value);
            if (comparison == 0) {
                return false;
            }
            if (comparison < 0) {
                if (current.left == null) {
                    current.left = new Node<>(value);
                    return true;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new Node<>(value);
                    return true;
                }
                current = current.right;
            }
        }
    }

    /** Searches for a value in the tree. */
    public synchronized boolean search(T value) {
        Node<T> current = root;
        while (current != null) {
            int comparison = value.compareTo(current.value);
            if (comparison == 0) {
                return true;
            }
            current = comparison < 0 ? current.left : current.right;
        }
        return false;
    }

    /** Deletes a value from the tree. */
    public synchronized boolean delete(T value) {
        if (!search(value)) {
            return false;
        }
        root = deleteNode(root, value);
        return true;
    }

    private Node<T> deleteNode(Node<T> node, T value) {
        int comparison = value.compareTo(node.value);
        if (comparison < 0) {
            node.left = deleteNode(node.left, value);
        } else if (comparison > 0) {
            node.right = deleteNode(node.right, value);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }

            Node<T> successor = node.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            node.value = successor.value;
            node.right = deleteNode(node.right, successor.value);
        }
        return node;
    }

    /** Draws left children on the left and right children on the right. */
    public synchronized String draw() {
        if (root == null) {
            return "(empty tree)";
        }

        int spacing = Math.max(6, longestValue(root) + 4);
        int rows = height(root) * 2 - 1;
        int columns = countNodes(root) * spacing;
        char[][] canvas = new char[rows][columns];
        for (char[] row : canvas) {
            Arrays.fill(row, ' ');
        }

        int[] nextColumn = { spacing / 2 };
        setPositions(root, 0, spacing, nextColumn);
        drawNodes(root, canvas);
        drawBranches(root, canvas);
        return canvasToString(canvas);
    }

    private void setPositions(Node<T> node, int depth, int spacing, int[] nextColumn) {
        if (node == null) {
            return;
        }
        setPositions(node.left, depth + 1, spacing, nextColumn);
        node.column = nextColumn[0];
        node.depth = depth;
        nextColumn[0] += spacing;
        setPositions(node.right, depth + 1, spacing, nextColumn);
    }

    private void drawNodes(Node<T> node, char[][] canvas) {
        if (node == null) {
            return;
        }
        String text = node.value.toString();
        int start = node.column - text.length() / 2;
        for (int i = 0; i < text.length(); i++) {
            canvas[node.depth * 2][start + i] = text.charAt(i);
        }
        drawNodes(node.left, canvas);
        drawNodes(node.right, canvas);
    }

    private void drawBranches(Node<T> node, char[][] canvas) {
        if (node == null) {
            return;
        }
        int row = node.depth * 2 + 1;
        if (node.left != null) {
            canvas[row][node.left.column] = '/';
            canvas[row][node.column] = '+';
            for (int column = node.left.column + 1; column < node.column; column++) {
                canvas[row][column] = '-';
            }
        }
        if (node.right != null) {
            canvas[row][node.column] = '+';
            canvas[row][node.right.column] = '\\';
            for (int column = node.column + 1; column < node.right.column; column++) {
                canvas[row][column] = '-';
            }
        }
        drawBranches(node.left, canvas);
        drawBranches(node.right, canvas);
    }

    private String canvasToString(char[][] canvas) {
        String[] lines = new String[canvas.length];
        int commonIndent = Integer.MAX_VALUE;
        for (int row = 0; row < canvas.length; row++) {
            lines[row] = new String(canvas[row]).stripTrailing();
            commonIndent = Math.min(commonIndent, lines[row].length() - lines[row].stripLeading().length());
        }

        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line.substring(commonIndent)).append('\n');
        }
        return result.toString().stripTrailing();
    }

    private int countNodes(Node<T> node) {
        return node == null ? 0 : 1 + countNodes(node.left) + countNodes(node.right);
    }

    private int height(Node<T> node) {
        return node == null ? 0 : 1 + Math.max(height(node.left), height(node.right));
    }

    private int longestValue(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return Math.max(node.value.toString().length(),
                Math.max(longestValue(node.left), longestValue(node.right)));
    }

    private static class Node<E> {
        E value;
        Node<E> left;
        Node<E> right;
        int column;
        int depth;

        Node(E value) {
            this.value = value;
        }
    }
}
