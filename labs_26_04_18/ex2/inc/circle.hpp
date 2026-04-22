#pragma once
#include "figure.hpp"

class Circle : public Figure {
  double radius;

public:
  Circle(double r);
  double calculateArea() const override;
  double calculatePerimeter() const override;
  std::string getName() const override;
};