#include <cctype>
#include <iomanip>
#include <iostream>
#include <memory>
#include <string>
#include <vector>

// Dołączamy wszystkie potrzebne klasy
#include "../inc/circle.hpp"
#include "../inc/figure.hpp"
#include "../inc/hexagon.hpp"
#include "../inc/pentagon.hpp"
#include "../inc/quadrangle.hpp"

bool isLetter(const std::string &str) {
  return (str.length() == 1 && std::isalpha(str[0]));
}

int main(int argc, char *argv[]) {
  std::vector<std::unique_ptr<Figure>> figures;
  int i = 1;

  try {
    while (i < argc) {
      std::string type = argv[i++];
      type[0] = std::tolower(type[0]);

      if (type == "c") {
        figures.push_back(std::make_unique<Circle>(std::stod(argv[i++])));
      } else if (type == "p") {
        figures.push_back(std::make_unique<Pentagon>(std::stod(argv[i++])));
      } else if (type == "h") {
        figures.push_back(std::make_unique<Hexagon>(std::stod(argv[i++])));
      } else if (type == "q") {
        std::vector<double> qArgs;
        while (i < argc && !isLetter(argv[i])) {
          qArgs.push_back(std::stod(argv[i++]));
        }

        if (qArgs.size() == 5) {
          figures.push_back(Quadrangle::create(qArgs[0], qArgs[1], qArgs[2],
                                               qArgs[3], qArgs[4]));
        } else if (qArgs.size() == 2) {
          figures.push_back(Quadrangle::create(qArgs[0], qArgs[0], qArgs[0],
                                               qArgs[0], qArgs[1]));
        } else {
          throw std::invalid_argument("Oczekiwano 2 lub 5 parametrow dla 'q'.");
        }
      } else {
        throw std::invalid_argument("Nieznany typ figury: " + type);
      }
    }
  } catch (const std::exception &e) {
    std::cerr << "Blad: " << e.what() << std::endl;
    return 1;
  }

  std::cout << "\tZestawienie figur" << std::endl;
  for (const auto &fig : figures) {
    std::cout << "Figura: " << std::left << std::setw(20) << fig->getName()
              << " | Obwod: " << std::left << std::setw(8) << std::fixed
              << std::setprecision(2) << fig->calculatePerimeter()
              << " | Pole: " << std::left << std::setw(8)
              << fig->calculateArea() << std::endl;
  }

  return 0;
}