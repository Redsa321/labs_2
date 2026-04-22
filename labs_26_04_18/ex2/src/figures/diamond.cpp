#include "../../inc/diamond.hpp"
#include <cmath>

Diamond::Diamond(double s, double a) : side(s), angle(a) {}
double Diamond::calculateArea() const {
  return side * side * std::sin(angle * std::acos(-1.0) / 180.0);
}
double Diamond::calculatePerimeter() const { return 4 * side; }
std::string Diamond::getName() const { return "Romb"; }