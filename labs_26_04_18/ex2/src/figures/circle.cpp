#include "../../inc/circle.hpp"
#include <cmath>
#include <stdexcept>

Circle::Circle(double r) : radius(r) {
  if (r <= 0)
    throw std::invalid_argument("Promien musi byc wiekszy od zera.");
}
double Circle::calculateArea() const {
  return std::acos(-1.0) * radius * radius;
}
double Circle::calculatePerimeter() const {
  return 2 * std::acos(-1.0) * radius;
}
std::string Circle::getName() const { return "Kolo"; }