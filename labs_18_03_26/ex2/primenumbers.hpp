#pragma once

class PrimeNumbers {
private:
  int *primes;
  int size;

public:
  PrimeNumbers(int n);
  ~PrimeNumbers();

  int getNumber(int m);
};