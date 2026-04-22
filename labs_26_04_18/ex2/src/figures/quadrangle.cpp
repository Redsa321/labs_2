#include "../../inc/quadrangle.hpp"
#include "../../inc/diamond.hpp"
#include "../../inc/rectangle.hpp"
#include "../../inc/square.hpp"
#include <stdexcept>

std::unique_ptr<Quadrangle> Quadrangle::create(double s1, double s2, double s3,
                                               double s4, double angle) {
  if (s1 <= 0 || s2 <= 0 || s3 <= 0 || s4 <= 0 || angle <= 0 || angle >= 180) {
    throw std::invalid_argument("Nieprawidlowe wymiary lub kat czworokata.");
  }

  if (s1 == s2 && s2 == s3 && s3 == s4) {
    if (angle == 90)
      return std::make_unique<Square>(s1);
    else
      return std::make_unique<Diamond>(s1, angle);
  }

  if (angle == 90) {
    if (s1 == s3 && s2 == s4)
      return std::make_unique<Rectangle>(s1, s2);
    else if (s1 == s2 && s3 == s4)
      return std::make_unique<Rectangle>(s1, s3);
    else if (s1 == s4 && s2 == s3)
      return std::make_unique<Rectangle>(s1, s2);
  }

  throw std::invalid_argument(
      "Z podanych parametrow nie mozna utworzyc obslugiwanego czworokata.");
}