#pragma once
#include "figure.hpp"

class Hexagon : public Figure {
  double side;

public:
  Hexagon(double s);
  double calculateArea() const override;
  double calculatePerimeter() const override;
  std::string getName() const override;
};