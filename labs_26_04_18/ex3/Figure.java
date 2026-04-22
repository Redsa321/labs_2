package labs_26_04_18.ex3;

public class Figure {

    // INTERFEJSY

    public interface IOneParamFigure {
        double calculateArea(double a);

        double calculatePerimeter(double a);

        String getName();
    }

    // Interfejs dla figur wymagających dwóch parametrów
    public interface ITwoParamFigure {
        double calculateArea(double a, double b);

        double calculatePerimeter(double a, double b);

        String getName();
    }

    // ENUMY

    // Enum dla figur z jednym parametrem
    public enum OneParamType implements IOneParamFigure {
        CIRCLE {
            @Override
            public double calculateArea(double r) {
                return Math.PI * r * r;
            }

            @Override
            public double calculatePerimeter(double r) {
                return 2 * Math.PI * r;
            }

            @Override
            public String getName() {
                return "Koło";
            }
        },
        SQUARE {
            @Override
            public double calculateArea(double a) {
                return a * a;
            }

            @Override
            public double calculatePerimeter(double a) {
                return 4 * a;
            }

            @Override
            public String getName() {
                return "Kwadrat";
            }
        },
        PENTAGON {
            @Override
            public double calculateArea(double a) {
                return (Math.sqrt(5 * (5 + 2 * Math.sqrt(5))) * a * a) / 4.0;
            }

            @Override
            public double calculatePerimeter(double a) {
                return 5 * a;
            }

            @Override
            public String getName() {
                return "Pięciokąt foremny";
            }
        },
        HEXAGON {
            @Override
            public double calculateArea(double a) {
                return (3 * Math.sqrt(3) * a * a) / 2.0;
            }

            @Override
            public double calculatePerimeter(double a) {
                return 6 * a;
            }

            @Override
            public String getName() {
                return "Sześciokąt foremny";
            }
        }
    }

    // Enum dla figur z dwoma parametrami
    public enum TwoParamType implements ITwoParamFigure {
        RECTANGLE {
            @Override
            public double calculateArea(double a, double b) {
                return a * b;
            }

            @Override
            public double calculatePerimeter(double a, double b) {
                return 2 * a + 2 * b;
            }

            @Override
            public String getName() {
                return "Prostokąt";
            }
        },
        DIAMOND { // Romb
            @Override
            public double calculateArea(double a, double angle) {
                return a * a * Math.sin(Math.toRadians(angle));
            }

            @Override
            public double calculatePerimeter(double a, double angle) {
                return 4 * a;
            }

            @Override
            public String getName() {
                return "Romb";
            }
        }
    }
}