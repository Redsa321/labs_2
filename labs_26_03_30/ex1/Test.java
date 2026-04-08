package labs_25_03_26.ex1;

public class Test {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Brak argumentów wywołania.");
            return;
        }

        try {
            int n = Integer.parseInt(args[0]);
            PascalTriangleRow pascalRow = new PascalTriangleRow(n);

            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                try {
                    int m = Integer.parseInt(arg);
                    int val = pascalRow.getElement(m);
                    System.out.println(m + " - " + val);
                } catch (NumberFormatException e) {
                    System.out.println(arg + " - nieprawidłowa dana");
                } catch (OutOfBoundsException e) {
                    System.out.println(arg + " - " + e.getMessage());
                }
            }
        } catch (InvalidRowNumberException e) {
            System.out.println(args[0] + " - " + e.getMessage());
        }
    }

}
