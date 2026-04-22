#include "../../inc/rectangle.hpp"

Rectangle::Rectangle(double w, double h) : width(w), height(h) {}
double Rectangle::calculateArea() const { return width * height; }
double Rectangle::calculatePerimeter() const { return 2 * width + 2 * height; }
std::string Rectangle::getName() const { return "Prostokat"; }