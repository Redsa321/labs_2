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
                    case "c": // koło (circle)
                        double radius = Double.parseDouble(args[i++]);
                        figures.add(new Circle(radius));
                        break;
                    case "p": // pięciokąt (pentagon)
                        double pSide = Double.parseDouble(args[i++]);
                        figures.add(new Pentagon(pSide));
                        break;
                    case "h": // sześciokąt (hexagon)
                        double hSide = Double.parseDouble(args[i++]);
                        figures.add(new Hexagon(hSide));
                        break;
                    case "q": // czworokąt (quadrangle)
                        double s1 = Double.parseDouble(args[i++]);
                        double s2 = Double.parseDouble(args[i++]);
                        double s3 = Double.parseDouble(args[i++]);
                        double s4 = Double.parseDouble(args[i++]);
                        double angle = Double.parseDouble(args[i++]);
                        figures.add(Quadrangle.create(s1, s2, s3, s4, angle));
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
        System.out.println("--- Zestawienie figur ---");
        for (Figure fig : figures) {
            System.out.printf("Figura: %-20s | Obwód: %-8.2f | Pole: %-8.2f%n",
                    fig.getName(), fig.calculatePerimeter(), fig.calculateArea());
        }
    }
}