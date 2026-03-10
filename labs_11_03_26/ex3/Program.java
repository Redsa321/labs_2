package labs_11_03_26.ex3;

public class Program {

    public static int div(int n) {
        if (n <= 1) {
            return n;
        }

        for (int i = n / 2; i >= 1; i--) {
            if (n % i == 0) {
                return i;
            }
        }
        return 1;
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            int n = 0;
            try {
                n = Integer.parseInt(args[i]);
            } catch (NumberFormatException ex) {
                System.out.println(args[i] + " nie jest liczba calkowita");
            }
            System.out.println(n + " - " + div(n));
        }
    }
}
