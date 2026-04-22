#include "figures.hpp"
#include <cmath>
#include <stdexcept>

const double PI = std::acos(-1.0);

// --- UKRYTE IMPLEMENTACJE KALKULATORÓW ---

class CircleCalc : public IOneParamFigure {
public:
  double calculateArea(double r) const override { return PI * r * r; }
  double calculatePerimeter(double r) const override { return 2 * PI * r; }
  std::string getName() const override { return "Kolo"; }
};

class SquareCalc : public IOneParamFigure {
public:
  double calculateArea(double a) const override { return a * a; }
  double calculatePerimeter(double a) const override { return 4 * a; }
  std::string getName() const override { return "Kwadrat"; }
};

class PentagonCalc : public IOneParamFigure {
public:
  double calculateArea(double a) const override {
    return (std::sqrt(5 * (5 + 2 * std::sqrt(5))) * a * a) / 4.0;
  }
  double calculatePerimeter(double a) const override { return 5 * a; }
  std::string getName() const override { return "Pieciokat foremny"; }
};

class HexagonCalc : public IOneParamFigure {
public:
  double calculateArea(double a) const override {
    return (3 * std::sqrt(3) * a * a) / 2.0;
  }
  double calculatePerimeter(double a) const override { return 6 * a; }
  std::string getName() const override { return "Szesciokat foremny"; }
};

class RectangleCalc : public ITwoParamFigure {
public:
  double calculateArea(double a, double b) const override { return a * b; }
  double calculatePerimeter(double a, double b) const override {
    return 2 * a + 2 * b;
  }
  std::string getName() const override { return "Prostokat"; }
};

class DiamondCalc : public ITwoParamFigure {
public:
  double calculateArea(double a, double angle) const override {
    return a * a * std::sin(angle * PI / 180.0);
  }
  double calculatePerimeter(double a, double angle) const override {
    return 4 * a;
  }
  std::string getName() const override { return "Romb"; }
};

// --- IMPLEMENTACJA FUNKCJI DOSTĘPOWYCH ---

const IOneParamFigure &getCalculator(OneParamType type) {
  static CircleCalc circle;
  static SquareCalc square;
  static PentagonCalc pentagon;
  static HexagonCalc hexagon;

  switch (type) {
  case OneParamType::CIRCLE:
    return circle;
  case OneParamType::SQUARE:
    return square;
  case OneParamType::PENTAGON:
    return pentagon;
  case OneParamType::HEXAGON:
    return hexagon;
  }
  throw std::invalid_argument("Nieznany typ jednoparametrowy.");
}

const ITwoParamFigure &getCalculator(TwoParamType type) {
  static RectangleCalc rectangle;
  static DiamondCalc diamond;

  switch (type) {
  case TwoParamType::RECTANGLE:
    return rectangle;
  case TwoParamType::DIAMOND:
    return diamond;
  }
  throw std::invalid_argument("Nieznany typ dwuparametrowy.");
}