package labs_26_04_18.ex1;

import java.util.ArrayList;
import java.util.List;

// Główna klasa programu
public class FigureTest {
    public static void main(String[] args) {
        List<Figure> figures = new ArrayList<>();
        int i = 0;

        try {
            while (i < args.length) {
                String type = args[i].toLowerCase();
                i++;

                switch (type) {
                    case "c": // koło
                        figures.add(new Circle(Double.parseDouble(args[i++])));
                        break;
                    case "p": // pieciokąt
                        figures.add(new Pentagon(Double.parseDouble(args[i++])));
                        break;
                    case "h": // szesciokąt
                        figures.add(new Hexagon(Double.parseDouble(args[i++])));
                        break;
                    case "q": // czworokąt (2 lub 5 parametrów)
                        List<Double> qArgs = new ArrayList<>();

                        // Pobiera parametry, dopoki nie natrafi na litere
                        while (i < args.length && !args[i].matches("[a-zA-Z]+")) {
                            qArgs.add(Double.parseDouble(args[i++]));
                        }

                        if (qArgs.size() == 5) {
                            // Pełny zapis
                            figures.add(Quadrangle.create(
                                    qArgs.get(0), qArgs.get(1), qArgs.get(2), qArgs.get(3), qArgs.get(4)));
                        } else if (qArgs.size() == 2) {
                            // Skrocony zapis dla
                            double side = qArgs.get(0);
                            double angle = qArgs.get(1);
                            figures.add(Quadrangle.create(side, side, side, side, angle));
                        } else {
                            throw new IllegalArgumentException(
                                    "Oczekiwano 2 (bok, kąt) lub 5 (4 boki, kąt) parametrów dla 'q'. Podano: "
                                            + qArgs.size());
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Nieznany typ figury: " + type);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Błąd: Zbyt mało parametrów dla podanej figury.");
            return;
        } catch (NumberFormatException e) {
            System.err.println("Błąd: Parametry figury muszą być liczbami.");
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("Błąd: " + e.getMessage());
            return;
        }

        // Wypisywanie informacji o figurach
        System.out.println("\tZestawienie figur");
        for (Figure fig : figures) {
            System.out.printf("Figura: %-20s | Obwód: %-8.2f | Pole: %-8.2f%n",
                    fig.getName(), fig.calculatePerimeter(), fig.calculateArea());
        }
    }
}