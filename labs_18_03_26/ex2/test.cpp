#include "primenumbers.hpp"
#include <cstdlib>
#include <iostream>
#include <stdexcept>

int main(int argc, char *argv[]) {
  if (argc < 2) {
    std::cout << "Brak argumentów" << std::endl;
    return 1;
  }

  char *end;
  int n = strtol(argv[1], &end, 10);
  if (*end != '\0' || n < 2) {
    std::cout << argv[1] << " - Nieprawidłowy zakres" << std::endl;
    return 1;
  }

  PrimeNumbers primeNumbers(n);

  for (int i = 2; i < argc; i++) {
    int m = strtol(argv[i], &end, 10);

    if (*end != '\0') {
      std::cout << argv[i] << " - nieprawidłowa dana" << std::endl;
      continue;
    }

    try {
      std::cout << m << " - " << primeNumbers.getNumber(m) << std::endl;
    } catch (const std::out_of_range &e) {
      std::cout << m << " - " << e.what() << std::endl;
    }
  }

  return 0;
}