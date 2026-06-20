/** Custom data type used to demonstrate the generic tree. */
public record Person(String name, int age) implements Comparable<Person> {
    /** Validates the name and age. */
    public Person {
        name = name.strip();
        if (name.isEmpty() || age < 0 || age > 150) {
            throw new IllegalArgumentException("Enter a person as name,age (for example Anna,21).");
        }
    }

    /** Creates a person from text in the name,age format. */
    public static Person parse(String text) {
        int comma = text.lastIndexOf(',');
        if (comma <= 0) {
            throw new IllegalArgumentException("Enter a person as name,age (for example Anna,21).");
        }
        try {
            return new Person(text.substring(0, comma),
                    Integer.parseInt(text.substring(comma + 1).strip()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Age must be an integer.");
        }
    }

    /** Orders people by age and then by name. */
    @Override
    public int compareTo(Person other) {
        int byAge = Integer.compare(age, other.age);
        return byAge != 0 ? byAge : name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name + " (" + age + ")";
    }
}
