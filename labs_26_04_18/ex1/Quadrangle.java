package labs_26_04_18.ex1;

// Abstrakcyjna klasa Czworokąt dziedzicząca po Figure
public abstract class Quadrangle extends Figure {
    // Metoda fabrykująca do weryfikacji i tworzenia konkretnego czworokąta
    public static Quadrangle create(double s1, double s2, double s3, double s4, double angle) {
        if (s1 <= 0 || s2 <= 0 || s3 <= 0 || s4 <= 0 || angle <= 0 || angle >= 180) {
            throw new IllegalArgumentException("Nieprawidłowe wymiary lub kąt czworokąta.");
        }

        // Sprawdzenie, czy wszystkie boki są równe
        if (s1 == s2 && s2 == s3 && s3 == s4) {
            if (angle == 90) {
                return new Square(s1);
            } else {
                return new Diamond(s1, angle);
            }
        }

        // Sprawdzenie, czy to prostokąt (zakładamy kolejność podawania boków obwodowo:
        // a, b, a, b)
        if (s1 == s3 && s2 == s4 && angle == 90) {
            return new Rectangle(s1, s2);
        }

        throw new IllegalArgumentException(
                "Z podanych 5 parametrów nie można utworzyć obsługiwanego czworokąta (Kwadrat, Prostokąt, Romb).");
    }
}