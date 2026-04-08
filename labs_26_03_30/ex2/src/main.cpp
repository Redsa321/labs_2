#include "../inc/InvalidRowNumberException.h"
#include "../inc/OutOfBoundsException.h"
#include "../inc/PascalTriangleRow.h"
#include <iostream>
#include <stdexcept>
#include <string>

int main(int argc, char *argv[]) {
  if (argc <= 1) {
    std::cout << "Brak argumentów wywołania." << std::endl;
    return 0;
  }

  try {
    int n = std::stoi(argv[1]);
    PascalTriangleRow pascalRow(n);

    for (int i = 2; i < argc; i++) {
      std::string arg = argv[i];
      try {
        int m = std::stoi(arg);
        int val = pascalRow.getElement(m);
        std::cout << m << " - " << val << std::endl;
      } catch (const std::invalid_argument &) {
        std::cout << arg << " - nieprawidłowa dana" << std::endl;
      } catch (const std::out_of_range &) {
        std::cout << arg << " - nieprawidłowa dana" << std::endl;
      } catch (const OutOfBoundsException &e) {
        std::cout << arg << " - " << e.what() << std::endl;
      }
    }
  } catch (const InvalidRowNumberException &e) {
    std::cout << argv[1] << " - " << e.what() << std::endl;
  } catch (const std::invalid_argument &) {
  } catch (const std::out_of_range &) {
  }

  return 0;
}