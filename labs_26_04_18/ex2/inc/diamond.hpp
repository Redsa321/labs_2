#pragma once
#include "quadrangle.hpp"

class Diamond : public Quadrangle {
  double side, angle;

public:
  Diamond(double s, double a);
  double calculateArea() const override;
  double calculatePerimeter() const override;
  std::string getName() const override;
};