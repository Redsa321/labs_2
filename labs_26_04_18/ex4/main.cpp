#include "figures.hpp"
#include <cctype>
#include <iomanip>
#include <iostream>
#include <stdexcept>
#include <string>
#include <vector>

bool isLetter(const std::string &str) {
  return (str.length() == 1 && std::isalpha(str[0]));
}

void addOneParamResult(std::vector<FigureResult> &results, OneParamType type,
                       double param) {
  if (param <= 0)
    throw std::invalid_argument("Parametr wymiaru musi byc wiekszy od zera.");
  const auto &calc = getCalculator(type);
  results.push_back({calc.getName(), calc.calculatePerimeter(param),
                     calc.calculateArea(param)});
}

void processQuadrangle(std::vector<FigureResult> &results, double s1, double s2,
                       double s3, double s4, double angle) {
  if (s1 <= 0 || s2 <= 0 || s3 <= 0 || s4 <= 0 || angle <= 0 || angle >= 180) {
    throw std::invalid_argument("Nieprawidlowe wymiary lub kat czworokata.");
  }

  // Kwadrat lub Romb
  if (s1 == s2 && s2 == s3 && s3 == s4) {
    if (angle == 90) {
      addOneParamResult(results, OneParamType::SQUARE, s1);
    } else {
      const auto &calc = getCalculator(TwoParamType::DIAMOND);
      results.push_back({calc.getName(), calc.calculatePerimeter(s1, angle),
                         calc.calculateArea(s1, angle)});
    }
    return;
  }

  // Prostokąt
  if (angle == 90) {
    const auto &calc = getCalculator(TwoParamType::RECTANGLE);
    if (s1 == s3 && s2 == s4) {
      results.push_back({calc.getName(), calc.calculatePerimeter(s1, s2),
                         calc.calculateArea(s1, s2)});
      return;
    } else if (s1 == s2 && s3 == s4) {
      results.push_back({calc.getName(), calc.calculatePerimeter(s1, s3),
                         calc.calculateArea(s1, s3)});
      return;
    } else if (s1 == s4 && s2 == s3) {
      results.push_back({calc.getName(), calc.calculatePerimeter(s1, s2),
                         calc.calculateArea(s1, s2)});
      return;
    }
  }

  throw std::invalid_argument(
      "Z podanych parametrow nie można utworzyc obslugiwanego czworokąta.");
}

int main(int argc, char *argv[]) {
  std::vector<FigureResult> results;
  int i = 1;

  try {
    while (i < argc) {
      std::string typeStr = argv[i++];
      typeStr[0] = std::tolower(typeStr[0]);

      if (typeStr == "c") {
        addOneParamResult(results, OneParamType::CIRCLE, std::stod(argv[i++]));
      } else if (typeStr == "p") {
        addOneParamResult(results, OneParamType::PENTAGON,
                          std::stod(argv[i++]));
      } else if (typeStr == "h") {
        addOneParamResult(results, OneParamType::HEXAGON, std::stod(argv[i++]));
      } else if (typeStr == "q") {
        std::vector<double> qArgs;
        while (i < argc && !isLetter(argv[i])) {
          qArgs.push_back(std::stod(argv[i++]));
        }

        if (qArgs.size() == 5) {
          processQuadrangle(results, qArgs[0], qArgs[1], qArgs[2], qArgs[3],
                            qArgs[4]);
        } else if (qArgs.size() == 2) {
          processQuadrangle(results, qArgs[0], qArgs[0], qArgs[0], qArgs[0],
                            qArgs[1]);
        } else {
          throw std::invalid_argument("Oczekiwano 2 lub 5 parametrow dla 'q'.");
        }
      } else {
        throw std::invalid_argument("Nieznany typ figury: " + typeStr);
      }
    }
  } catch (const std::out_of_range &e) {
    std::cerr << "Blad: Zbyt malo parametrow dla podanej figury." << std::endl;
    return 1;
  } catch (const std::invalid_argument &e) {
    std::cerr << "Blad parametrow: " << e.what() << std::endl;
    return 1;
  } catch (const std::exception &e) {
    std::cerr << "Blad nieoczekiwany: " << e.what() << std::endl;
    return 1;
  }

  // Wypisywanie zestawienia
  std::cout << "Zestawienie figur" << std::endl;
  for (const auto &res : results) {
    std::cout << "Figura: " << std::left << std::setw(20) << res.name
              << " | Obwod: " << std::left << std::setw(8) << std::fixed
              << std::setprecision(2) << res.perimeter
              << " | Pole: " << std::left << std::setw(8) << res.area
              << std::endl;
  }

  return 0;
}