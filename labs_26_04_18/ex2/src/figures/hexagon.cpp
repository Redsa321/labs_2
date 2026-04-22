#include "../../inc/hexagon.hpp"
#include <cmath>
#include <stdexcept>

Hexagon::Hexagon(double s) : side(s) {
  if (s <= 0)
    throw std::invalid_argument("Bok musi byc wiekszy od zera.");
}
double Hexagon::calculateArea() const {
  return (3 * std::sqrt(3) * side * side) / 2.0;
}
double Hexagon::calculatePerimeter() const { return 6 * side; }
std::string Hexagon::getName() const { return "Szesciokat foremny"; }