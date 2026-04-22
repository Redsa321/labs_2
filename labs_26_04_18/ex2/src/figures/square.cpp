#include "../../inc/square.hpp"

Square::Square(double s) : side(s) {}
double Square::calculateArea() const { return side * side; }
double Square::calculatePerimeter() const { return 4 * side; }
std::string Square::getName() const { return "Kwadrat"; }