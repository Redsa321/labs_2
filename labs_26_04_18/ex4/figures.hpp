#pragma once
#include <string>

// INTERFEJSY
class IOneParamFigure {
public:
  virtual ~IOneParamFigure() = default;
  virtual double calculateArea(double a) const = 0;
  virtual double calculatePerimeter(double a) const = 0;
  virtual std::string getName() const = 0;
};

class ITwoParamFigure {
public:
  virtual ~ITwoParamFigure() = default;
  virtual double calculateArea(double a, double b) const = 0;
  virtual double calculatePerimeter(double a, double b) const = 0;
  virtual std::string getName() const = 0;
};

// ENUMY
enum class OneParamType { CIRCLE, SQUARE, PENTAGON, HEXAGON };

enum class TwoParamType { RECTANGLE, DIAMOND };

// STRUKTURA WYNIKOWA
struct FigureResult {
  std::string name;
  double perimeter;
  double area;
};

const IOneParamFigure &getCalculator(OneParamType type);
const ITwoParamFigure &getCalculator(TwoParamType type);