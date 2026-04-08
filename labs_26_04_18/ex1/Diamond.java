package labs_26_04_18.ex1;

// Klasa dla Rombu
public class Diamond extends Quadrangle {
    private final double side;
    private final double angle;

    public Diamond(double side, double angle) {
        this.side = side;
        this.angle = angle;
    }

    @Override
    public double calculateArea() {
        return side * side * Math.sin(Math.toRadians(angle));
    }

    @Override
    public double calculatePerimeter() {
        return 4 * side;
    }

    @Override
    public String getName() {
        return "Romb";
    }
}
