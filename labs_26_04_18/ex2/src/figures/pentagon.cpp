#include "../../inc/pentagon.hpp"
#include <cmath>
#include <stdexcept>

Pentagon::Pentagon(double s) : side(s) {
  if (s <= 0)
    throw std::invalid_argument("Bok musi byc wiekszy od zera.");
}
double Pentagon::calculateArea() const {
  return (std::sqrt(5 * (5 + 2 * std::sqrt(5))) * side * side) / 4.0;
}
double Pentagon::calculatePerimeter() const { return 5 * side; }
std::string Pentagon::getName() const { return "Pieciokat foremny"; }