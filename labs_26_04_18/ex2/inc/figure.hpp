#pragma once
#include <string>

// Abstrakcyjna klasa bazowa
class Figure {
public:
  virtual ~Figure() = default;
  virtual double calculateArea() const = 0;
  virtual double calculatePerimeter() const = 0;
  virtual std::string getName() const = 0;
};