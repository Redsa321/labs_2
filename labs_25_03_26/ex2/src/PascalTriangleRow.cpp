#include "../inc/PascalTriangleRow.h"

void PascalTriangleRow::generateRow(int n) {
  row.resize(n + 1);
  row[0] = 1;
  for (int k = 1; k <= n; k++) {
    row[k] =
        static_cast<int>(static_cast<long long>(row[k - 1]) * (n - k + 1) / k);
  }
}

PascalTriangleRow::PascalTriangleRow(int n) {
  if (n < 0) {
    throw InvalidRowNumberException("Nieprawidłowy numer wiersza");
  }
  generateRow(n);
}

int PascalTriangleRow::getElement(int m) {
  if (m < 0 || static_cast<size_t>(m) >= row.size()) {
    throw OutOfBoundsException("liczba spoza zakresu");
  }
  return row[m];
}