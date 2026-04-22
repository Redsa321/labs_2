#pragma once
#include "figure.hpp"
class Pentagon : public Figure {
  double side;

public:
  Pentagon(double s);
  double calculateArea() const override;
  double calculatePerimeter() const override;
  std::string getName() const override;
};