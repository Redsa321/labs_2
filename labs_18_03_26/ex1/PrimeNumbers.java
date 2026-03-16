import java.util.ArrayList;
import java.util.List;

public class PrimeNumbers {

    private int[] primes;

    public PrimeNumbers(int n) {

        boolean[] isSieve = new boolean[n + 1];

        // Set all values to be prime numbers
        for (int i = 2; i <= n; i++) {
            isSieve[i] = true;
        }

        // Sieve algorithm
        for (int i = 2; i * i <= n; i++) {
            if (isSieve[i]) {
                for (int j = i * i; j <= n; j += i) {
                    isSieve[j] = false;
                }
            }
        }

        List<Integer> primesList = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            if (isSieve[i]) {
                primesList.add(i);
            }
        }

        primes = new int[primesList.size()];
        for (int i = 0; i < primesList.size(); i++) {
            primes[i] = primesList.get(i);
        }
    }

    public int getNumber(int m) {
        if (m < 0 || m >= primes.length) {
            throw new IndexOutOfBoundsException("liczba spoza zakresu");
        }

        return primes[m];
    }
}
