package labs_26_04_18.ex1;

// Klasa dla Sześciokąta foremnego
public class Hexagon extends Figure {
    private final double side;

    public Hexagon(double side) {
        if (side <= 0)
            throw new IllegalArgumentException("Bok musi być większy od zera.");
        this.side = side;
    }

    @Override
    public double calculateArea() {
        return (3 * Math.sqrt(3) * side * side) / 2.0;
    }

    @Override
    public double calculatePerimeter() {
        return 6 * side;
    }

    @Override
    public String getName() {
        return "Sześciokąt foremny";
    }
}
