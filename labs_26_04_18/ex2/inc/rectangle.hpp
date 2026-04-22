#pragma once
#include "quadrangle.hpp"

class Rectangle : public Quadrangle {
  double width, height;

public:
  Rectangle(double w, double h);
  double calculateArea() const override;
  double calculatePerimeter() const override;
  std::string getName() const override;
};