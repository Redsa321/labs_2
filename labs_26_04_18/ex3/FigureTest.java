package labs_26_04_18.ex3;

import java.util.ArrayList;
import java.util.List;

public class FigureTest {

    // Klasa pomocnicza do przechowywania obliczonych wyników dla zestawienia
    static class FigureResult {
        String name;
        double perimeter;
        double area;

        FigureResult(String name, double perimeter, double area) {
            this.name = name;
            this.perimeter = perimeter;
            this.area = area;
        }
    }

    public static void main(String[] args) {
        List<FigureResult> results = new ArrayList<>();
        int i = 0;

        try {
            while (i < args.length) {
                String type = args[i].toLowerCase();
                i++;

                switch (type) {
                    case "c": // Koło
                        double r = Double.parseDouble(args[i++]);
                        addOneParamResult(results, Figure.OneParamType.CIRCLE, r);
                        break;
                    case "p": // Pięciokąt
                        double pSide = Double.parseDouble(args[i++]);
                        addOneParamResult(results, Figure.OneParamType.PENTAGON, pSide);
                        break;
                    case "h": // Sześciokąt
                        double hSide = Double.parseDouble(args[i++]);
                        addOneParamResult(results, Figure.OneParamType.HEXAGON, hSide);
                        break;
                    case "q": // Czworokąt
                        List<Double> qArgs = new ArrayList<>();
                        // Zbieranie parametrów (elastyczne dla 2 lub 5)
                        while (i < args.length && !args[i].matches("[a-zA-Z]+")) {
                            qArgs.add(Double.parseDouble(args[i++]));
                        }

                        if (qArgs.size() == 5) {
                            processQuadrangle(results, qArgs.get(0), qArgs.get(1), qArgs.get(2), qArgs.get(3),
                                    qArgs.get(4));
                        } else if (qArgs.size() == 2) {
                            // Skrócony zapis (bok, kąt)
                            processQuadrangle(results, qArgs.get(0), qArgs.get(0), qArgs.get(0), qArgs.get(0),
                                    qArgs.get(1));
                        } else {
                            throw new IllegalArgumentException("Oczekiwano 2 lub 5 parametrów dla 'q'.");
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
        System.out.println("Zestawienie figur");
        for (FigureResult res : results) {
            System.out.printf("Figura: %-20s | Obwód: %-8.2f | Pole: %-8.2f%n",
                    res.name, res.perimeter, res.area);
        }
    }

    // Metoda pomocnicza dla figur jednoparametrowych
    private static void addOneParamResult(List<FigureResult> results, Figure.IOneParamFigure figureEnum, double param) {
        if (param <= 0)
            throw new IllegalArgumentException("Parametr wymiaru musi być większy od zera.");
        results.add(new FigureResult(figureEnum.getName(), figureEnum.calculatePerimeter(param),
                figureEnum.calculateArea(param)));
    }

    // Metoda analizująca czworokąt i delegująca obliczenia do odpowiedniego enuma
    private static void processQuadrangle(List<FigureResult> results, double s1, double s2, double s3, double s4,
            double angle) {
        if (s1 <= 0 || s2 <= 0 || s3 <= 0 || s4 <= 0 || angle <= 0 || angle >= 180) {
            throw new IllegalArgumentException("Nieprawidłowe wymiary lub kąt czworokąta.");
        }

        // Sprawdzanie czy to kwadrat / romb
        if (s1 == s2 && s2 == s3 && s3 == s4) {
            if (angle == 90) {
                // Kwadrat używa OneParamType
                addOneParamResult(results, Figure.OneParamType.SQUARE, s1);
            } else {
                // Romb używa TwoParamType
                results.add(new FigureResult(
                        Figure.TwoParamType.DIAMOND.getName(),
                        Figure.TwoParamType.DIAMOND.calculatePerimeter(s1, angle),
                        Figure.TwoParamType.DIAMOND.calculateArea(s1, angle)));
            }
            return;
        }

        // Sprawdzanie czy to prostokąt
        if (angle == 90) {
            if (s1 == s3 && s2 == s4) {
                addRectangle(results, s1, s2);
                return;
            } else if (s1 == s2 && s3 == s4) {
                addRectangle(results, s1, s3);
                return;
            } else if (s1 == s4 && s2 == s3) {
                addRectangle(results, s1, s2);
                return;
            }
        }

        throw new IllegalArgumentException("Z podanych parametrów nie można utworzyć obsługiwanego czworokąta.");
    }

    // Metoda pomocnicza skracająca zapis dla prostokąta
    private static void addRectangle(List<FigureResult> results, double w, double h) {
        results.add(new FigureResult(
                Figure.TwoParamType.RECTANGLE.getName(),
                Figure.TwoParamType.RECTANGLE.calculatePerimeter(w, h),
                Figure.TwoParamType.RECTANGLE.calculateArea(w, h)));
    }
}