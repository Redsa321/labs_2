package labs_26_04_18.ex1;

// Klasa dla Prostokąta
class Rectangle extends Quadrangle {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double calculateArea() {
        return width * height;
    }

    @Override
    public double calculatePerimeter() {
        return 2 * width + 2 * height;
    }

    @Override
    public String getName() {
        return "Prostokąt";
    }
}