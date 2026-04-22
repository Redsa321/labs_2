#pragma once
#include "figure.hpp"
#include <memory>

class Quadrangle : public Figure {
public:
  // Deklaracja metody fabrykującej
  static std::unique_ptr<Quadrangle> create(double s1, double s2, double s3,
                                            double s4, double angle);
};