#pragma once
#include "../inc/InvalidRowNumberException.h"
#include "../inc/OutOfBoundsException.h"
#include <vector>

class PascalTriangleRow {
private:
  std::vector<int> row;
  void generateRow(int n);

public:
  PascalTriangleRow(int n);
  int getElement(int m);
};