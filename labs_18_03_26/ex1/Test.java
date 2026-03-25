public class Test {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Brak argumentów");
            return;
        }

        int n;
        try {
            n = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println(args[0] + " - Nieprawidłowy zakres");
            return;
        }

        if (n < 2) {
            System.out.println(args[0] + " - Nieprawidłowy zakres");
            return;
        }

        PrimeNumbers primeNumbers = new PrimeNumbers(n);

        for (int i = 1; i < args.length; i++) {
            int m;
            try {
                m = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                System.out.println(args[i] + " - nieprawidłowa dana");
                continue;
            }

            try {
                int result = primeNumbers.getNumber(m);
                System.out.println(m + " - " + result);
            } catch (IndexOutOfBoundsException e) {
                System.out.println(m + " - liczba spoza zakresu");
            }
        }
    }
}