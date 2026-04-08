package labs_26_04_18.ex1;

// Klasa dla Pięciokąta foremnego
public class Pentagon extends Figure {
    private final double side;

    public Pentagon(double side) {
        if (side <= 0)
            throw new IllegalArgumentException("Bok musi być większy od zera.");
        this.side = side;
    }

    @Override
    public double calculateArea() {
        return (Math.sqrt(5 * (5 + 2 * Math.sqrt(5))) * side * side) / 4.0;
    }

    @Override
    public double calculatePerimeter() {
        return 5 * side;
    }

    @Override
    public String getName() {
        return "Pięciokąt foremny";
    }
}