#pragma once

#include <algorithm>
#include <memory>
#include <sstream>
#include <string>
#include <vector>

/**
 * Simple generic binary search tree.
 * Type T must support operator < and stream output.
 */
template <typename T>
class BinarySearchTree {
public:
    /** Inserts a value. Duplicate values are not inserted. */
    bool insert(const T& value) {
        return insert(root_, value);
    }

    /** Searches for a value. */
    bool search(const T& value) const {
        return search(root_.get(), value);
    }

    /** Removes a value. */
    bool remove(const T& value) {
        if (!search(value)) {
            return false;
        }
        remove(root_, value);
        return true;
    }

    /** Draws left children on the left and right children on the right. */
    std::string draw() const {
        if (!root_) {
            return "(empty tree)";
        }

        const std::size_t spacing = std::max<std::size_t>(6, longestValue(root_.get()) + 4);
        const std::size_t rows = height(root_.get()) * 2 - 1;
        const std::size_t columns = countNodes(root_.get()) * spacing;
        std::vector<std::string> canvas(rows, std::string(columns, ' '));

        std::size_t nextColumn = spacing / 2;
        setPositions(root_.get(), 0, spacing, nextColumn);
        drawNodes(root_.get(), canvas);
        drawBranches(root_.get(), canvas);
        return canvasToString(canvas);
    }

private:
    struct Node {
        explicit Node(const T& value) : value(value) {}

        T value;
        std::unique_ptr<Node> left;
        std::unique_ptr<Node> right;
        std::size_t column{};
        std::size_t depth{};
    };

    std::unique_ptr<Node> root_;

    bool equal(const T& first, const T& second) const {
        return !(first < second) && !(second < first);
    }

    bool insert(std::unique_ptr<Node>& node, const T& value) {
        if (!node) {
            node = std::make_unique<Node>(value);
            return true;
        }
        if (equal(value, node->value)) {
            return false;
        }
        return value < node->value
                ? insert(node->left, value)
                : insert(node->right, value);
    }

    bool search(const Node* node, const T& value) const {
        if (!node) {
            return false;
        }
        if (equal(value, node->value)) {
            return true;
        }
        return value < node->value
                ? search(node->left.get(), value)
                : search(node->right.get(), value);
    }

    void remove(std::unique_ptr<Node>& node, const T& value) {
        if (value < node->value) {
            remove(node->left, value);
        } else if (node->value < value) {
            remove(node->right, value);
        } else if (!node->left) {
            node = std::move(node->right);
        } else if (!node->right) {
            node = std::move(node->left);
        } else {
            Node* successor = node->right.get();
            while (successor->left) {
                successor = successor->left.get();
            }
            node->value = successor->value;
            remove(node->right, successor->value);
        }
    }

    void setPositions(Node* node, std::size_t depth, std::size_t spacing,
                      std::size_t& nextColumn) const {
        if (!node) {
            return;
        }
        setPositions(node->left.get(), depth + 1, spacing, nextColumn);
        node->column = nextColumn;
        node->depth = depth;
        nextColumn += spacing;
        setPositions(node->right.get(), depth + 1, spacing, nextColumn);
    }

    void drawNodes(const Node* node, std::vector<std::string>& canvas) const {
        if (!node) {
            return;
        }
        std::ostringstream stream;
        stream << node->value;
        const std::string text = stream.str();
        canvas[node->depth * 2].replace(
                node->column - text.size() / 2, text.size(), text);
        drawNodes(node->left.get(), canvas);
        drawNodes(node->right.get(), canvas);
    }

    void drawBranches(const Node* node, std::vector<std::string>& canvas) const {
        if (!node) {
            return;
        }
        const std::size_t row = node->depth * 2 + 1;
        if (node->left) {
            canvas[row][node->left->column] = '/';
            canvas[row][node->column] = '+';
            for (std::size_t column = node->left->column + 1;
                 column < node->column; ++column) {
                canvas[row][column] = '-';
            }
        }
        if (node->right) {
            canvas[row][node->column] = '+';
            canvas[row][node->right->column] = '\\';
            for (std::size_t column = node->column + 1;
                 column < node->right->column; ++column) {
                canvas[row][column] = '-';
            }
        }
        drawBranches(node->left.get(), canvas);
        drawBranches(node->right.get(), canvas);
    }

    std::string canvasToString(std::vector<std::string>& canvas) const {
        std::size_t commonIndent = canvas.front().size();
        for (std::string& line : canvas) {
            line.erase(line.find_last_not_of(' ') + 1);
            commonIndent = std::min(commonIndent, line.find_first_not_of(' '));
        }

        std::ostringstream result;
        for (std::size_t row = 0; row < canvas.size(); ++row) {
            result << canvas[row].substr(commonIndent);
            if (row + 1 < canvas.size()) {
                result << '\n';
            }
        }
        return result.str();
    }

    std::size_t countNodes(const Node* node) const {
        return node ? 1 + countNodes(node->left.get()) + countNodes(node->right.get()) : 0;
    }

    std::size_t height(const Node* node) const {
        return node ? 1 + std::max(height(node->left.get()), height(node->right.get())) : 0;
    }

    std::size_t longestValue(const Node* node) const {
        if (!node) {
            return 0;
        }
        std::ostringstream stream;
        stream << node->value;
        return std::max(stream.str().size(),
                std::max(longestValue(node->left.get()), longestValue(node->right.get())));
    }
};
