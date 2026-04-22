#pragma once
#include "quadrangle.hpp"

class Square : public Quadrangle {
  double side;

public:
  Square(double s);
  double calculateArea() const override;
  double calculatePerimeter() const override;
  std::string getName() const override;
};