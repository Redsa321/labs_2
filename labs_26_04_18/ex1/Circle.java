package labs_26_04_18.ex1;

// Klasa dla koła
public class Circle extends Figure {
    private final double radius;

    public Circle(double radius) {
        if (radius <= 0)
            throw new IllegalArgumentException("Promień musi być większy od zera.");
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }

    @Override
    public double calculatePerimeter() {
        return 2 * Math.PI * radius;
    }

    @Override
    public String getName() {
        return "Koło";
    }
}