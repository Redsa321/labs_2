#include "primenumbers.hpp"
#include <cstdlib>
#include <stdexcept>
#include <vector>

PrimeNumbers::PrimeNumbers(int n) {
  int *isSieve = (int *)calloc(n + 1, sizeof(int));

  for (int i = 2; i * i <= n; i++) {
    if (!isSieve[i]) {
      for (int j = i * i; j <= n; j += i) {
        isSieve[j] = 1;
      }
    }
  }

  std::vector<int> primesList;
  for (int i = 2; i <= n; i++) {
    if (!isSieve[i]) {
      primesList.push_back(i);
    }
  }

  free(isSieve);
  size = primesList.size();
  primes = (int *)malloc(size * sizeof(int));
  for (int i = 0; i < size; i++) {
    primes[i] = primesList[i];
  }
}

int PrimeNumbers::getNumber(int m) {
  if (m < 0 || m >= size) {
    throw std::out_of_range("liczba spoza zakresu");
  }
  return primes[m];
}

PrimeNumbers::~PrimeNumbers() { free(primes); };