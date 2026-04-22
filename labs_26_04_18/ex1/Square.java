package labs_26_04_18.ex1;

public class Square extends Quadrangle {
    private final double side;

    public Square(double side) {
        this.side = side;
    }

    @Override
    public double calculateArea() {
        return side * side;
    }

    @Override
    public double calculatePerimeter() {
        return 4 * side;
    }

    @Override
    public String getName() {
        return "Kwadrat";
    }
}