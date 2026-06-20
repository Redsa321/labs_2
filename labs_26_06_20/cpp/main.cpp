#include "BinarySearchTree.hpp"

#include <algorithm>
#include <cctype>
#include <cmath>
#include <iostream>
#include <stdexcept>
#include <string>

// Custom data type ordered by age and then by name.
struct Person {
  std::string name;
  int age;

  bool operator<(const Person &other) const {
    return age != other.age ? age < other.age : name < other.name;
  }
};

std::ostream &operator<<(std::ostream &output, const Person &person) {
  return output << person.name << " (" << person.age << ')';
}

void trim(std::string &text) {
  const std::size_t first = text.find_first_not_of(" \t\r\n");
  if (first == std::string::npos) {
    text.clear();
    return;
  }
  const std::size_t last = text.find_last_not_of(" \t\r\n");
  text = text.substr(first, last - first + 1);
}

int parseInteger(const std::string &text) {
  std::size_t position;
  int value = std::stoi(text, &position);
  if (position != text.size()) {
    throw std::invalid_argument("Invalid Integer value");
  }
  return value;
}

double parseDouble(const std::string &text) {
  std::size_t position;
  double value = std::stod(text, &position);
  if (position != text.size() || !std::isfinite(value)) {
    throw std::invalid_argument("Invalid Double value");
  }
  return value;
}

Person parsePerson(const std::string &text) {
  const std::size_t comma = text.rfind(',');
  if (comma == std::string::npos) {
    throw std::invalid_argument("Enter a person as name,age");
  }
  std::string name = text.substr(0, comma);
  trim(name);
  int age = parseInteger(text.substr(comma + 1));
  if (name.empty() || age < 0 || age > 150) {
    throw std::invalid_argument("Invalid name or age");
  }
  return {name, age};
}

template <typename T, typename Parser> void run(Parser parse) {
  BinarySearchTree<T> tree;
  std::cout << "Commands: insert, search, delete, draw, quit\n";

  std::string line;
  while (std::cout << "> " && std::getline(std::cin, line)) {
    trim(line);
    const std::size_t space = line.find_first_of(" \t");
    std::string command = line.substr(0, space);
    std::string argument =
        space == std::string::npos ? "" : line.substr(space + 1);
    trim(argument);
    std::transform(
        command.begin(), command.end(), command.begin(),
        [](unsigned char character) { return std::tolower(character); });

    try {
      if (command == "quit") {
        return;
      }
      if (command == "draw") {
        std::cout << tree.draw() << '\n';
        continue;
      }
      if (argument.empty()) {
        std::cout << "Enter a value\n";
        continue;
      }

      T value = parse(argument);
      if (command == "insert") {
        std::cout << (tree.insert(value) ? "Value inserted\n"
                                         : "Value already exists\n");
        std::cout << tree.draw() << '\n';
      } else if (command == "search") {
        std::cout << (tree.search(value) ? "Value found\n"
                                         : "Value not found\n");
      } else if (command == "delete") {
        std::cout << (tree.remove(value) ? "Value deleted\n"
                                         : "Value not found\n");
        std::cout << tree.draw() << '\n';
      } else {
        std::cout << "Unknown command.\n";
      }
    } catch (const std::exception &exception) {
      std::cout << "Error: " << exception.what() << '\n';
    }
  }
}

int main() {
  std::cout << "Choose a type: 1-Integer, 2-Double, 3-String, 4-Person\n> ";
  std::string choice;
  std::getline(std::cin, choice);

  if (choice == "1") {
    run<int>(parseInteger);
  } else if (choice == "2") {
    run<double>(parseDouble);
  } else if (choice == "3") {
    run<std::string>([](const std::string &text) { return text; });
  } else if (choice == "4") {
    run<Person>(parsePerson);
  } else {
    std::cout << "Invalid choice.\n";
  }
}
