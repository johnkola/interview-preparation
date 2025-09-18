package com.interview.java17;

import java.io.*;

/**
 * Java Reserved Keywords Examples
 *
 * KEY TOPICS:
 * 1. TRANSIENT - Why use? Memory vs Security trade-offs
 * 2. VOLATILE - Thread visibility, happens-before relationship
 * 3. SEALED - Controlled inheritance, pattern matching benefits
 * 4. RECORD - Immutable data carriers, when to use vs classes
 * 5. YIELD - Switch expressions vs statements
 *
 * COMMON QUESTIONS:
 * - Difference between volatile and synchronized?
 * - When would you use transient fields?
 * - Benefits of sealed classes over regular inheritance?
 * - Record vs Class - when to choose which?
 */
public class ReservedKeywordsExamples {

    // =========================
    // TRANSIENT KEYWORD
    // =========================

    /**
     * TRANSIENT KEYWORD
     *
     * PURPOSE: Exclude fields from serialization
     * COMMON USE CASES:
     * - Security: passwords, tokens (never serialize sensitive data)
     * - Performance: large objects, temporary cache
     * - Logic: calculated fields, derived values
     *
     * NOTE: "What happens to transient fields during serialization?"
     * ANSWER: Set to default values (null for objects, 0 for primitives)
     */
    static class User implements Serializable {
        private String username; // SERIALIZED: Saved to disk/network
        private transient String password; // NOT SERIALIZED: Security best practice
        private transient int loginCount;  // NOT SERIALIZED: Runtime-only data

        public User(String username, String password) {
            this.username = username;
            this.password = password;
            this.loginCount = 0; // NOTE: Transient fields need initialization
        }

        // Increment login count - transient field
        public void incrementLogin() { loginCount++; }

        @Override
        public String toString() {
            return String.format("User{username='%s', password='%s', loginCount=%d}",
                username, password, loginCount);
        }
    }

    // =========================
    // VOLATILE KEYWORD
    // =========================

    /**
     * VOLATILE KEYWORD
     *
     * PURPOSE: Thread visibility guarantee
     *
     * WHAT IT DOES:
     * - Prevents CPU caching of variable
     * - Ensures reads/writes go to main memory
     * - Provides happens-before relationship
     *
     * COMMON QUESTIONS:
     * Q: "Volatile vs Synchronized?"
     * A: Volatile = visibility only, Synchronized = visibility + atomicity
     *
     * Q: "When to use volatile?"
     * A: Flags, status variables, single-writer scenarios
     */
    static class VolatileExample {
        private volatile boolean running = true; // VOLATILE: Thread-safe visibility
        private int counter = 0; // NON-VOLATILE: May be cached, not thread-safe

        public void start() {
            Thread worker = new Thread(() -> {
                while (running) { // Volatile prevents infinite loop
                    counter++; // Race condition - not atomic operation
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                System.out.println("Worker stopped. Counter: " + counter);
            });

            worker.start(); // Start background worker

            // Stop after 1 second
            try {
                Thread.sleep(1000); // Main thread waits
                running = false; // Change immediately visible to all threads
                worker.join(); // Wait for worker to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // =========================
    // CONST & GOTO (Reserved but unused)
    // =========================

    /**
     * RESERVED BUT UNUSED KEYWORDS
     *
     * WHY RESERVED?
     * - Prevent confusion for C/C++ developers
     * - Future language evolution flexibility
     * - Compiler error prevents accidental usage
     *
     * NOTE: Shows Java's design philosophy of familiar syntax
     */
    // Example compilation errors:
    // int const = 5;  // ERROR: 'const' is reserved
    // goto label;     // ERROR: 'goto' is reserved

    // =========================
    // RECORD KEYWORD (Java 14+)
    // =========================

    /**
     * RECORD KEYWORD (Java 14+)
     *
     * PURPOSE: Immutable data carriers
     *
     * AUTO-GENERATED:
     * - All-args constructor
     * - Getter methods (no 'get' prefix)
     * - equals(), hashCode(), toString()
     *
     * COMMON QUESTIONS:
     * Q: "Record vs Class?"
     * A: Record = immutable data, Class = mutable behavior
     *
     * Q: "Can records have methods?"
     * A: Yes - instance, static, compact constructor
     *
     * WHEN TO USE: DTOs, value objects, data transfer
     */
    public record Person(String name, int age, String email) {
        // Compact constructor validates WITHOUT explicit assignment
        public Person {
            if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        }

        // Records can have business logic methods
        public boolean isAdult() {
            return age >= 18; // Check if person is 18 or older
        }
    }

    // Records also support static methods
    public record Point(double x, double y) {
        // Records support static methods like regular classes
        public static Point origin() {
            return new Point(0, 0);
        }

        // Calculate distance from origin using Pythagorean theorem
        public double distanceFromOrigin() {
            return Math.sqrt(x * x + y * y);
        }
    }

    // =========================
    // SEALED CLASSES (Java 17)
    // =========================

    /**
     * SEALED CLASSES (Java 17)
     *
     * PURPOSE: Controlled inheritance hierarchy
     *
     * BENEFITS:
     * - Exhaustive pattern matching
     * - Known subclass set at compile time
     * - Better API design and evolution
     *
     * SUBCLASS OPTIONS:
     * - final: Cannot be extended
     * - sealed: Can extend but with restrictions
     * - non-sealed: Removes restrictions
     *
     * KEY QUESTION: "Why use sealed over regular inheritance?"
     * ANSWER: Type safety, pattern matching, controlled API evolution
     */
    public abstract sealed class Shape permits Circle, Rectangle, Triangle {
        protected final String name; // All subclasses share this field

        protected Shape(String name) {
            this.name = name;
        }

        // Forces implementation in all permitted subclasses
        public abstract double area();
    }

    // FINAL - Cannot be subclassed (inheritance ends here)
    public final class Circle extends Shape {
        private final double radius; // Circle radius

        public Circle(double radius) {
            super("Circle"); // Must call sealed parent constructor
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * radius * radius; // Classic formula π × r²
        }
    }

    // SEALED - Only permits Square as subclass
    public sealed class Rectangle extends Shape permits Square {
        protected final double width, height; // Rectangle dimensions

        public Rectangle(double width, double height) {
            super("Rectangle"); // Call parent constructor
            this.width = width;
            this.height = height;
        }

        @Override
        public double area() {
            return width * height; // Simple area calculation
        }
    }

    // NON-SEALED - Anyone can extend this class
    public non-sealed class Triangle extends Shape {
        private final double base, height; // Triangle dimensions

        public Triangle(double base, double height) {
            super("Triangle"); // Call parent constructor
            this.base = base;
            this.height = height;
        }

        @Override
        public double area() {
            return 0.5 * base * height; // Triangle area formula: ½ × base × height
        }
    }

    // Square is final - specialized Rectangle
    public final class Square extends Rectangle {
        public Square(double side) {
            super(side, side); // Square IS-A Rectangle (inheritance)
        }
    }

    // =========================
    // YIELD KEYWORD (Java 14+)
    // =========================

    /**
     * YIELD KEYWORD (Java 14+)
     *
     * PURPOSE: Return values from switch expressions
     *
     * WHEN TO USE YIELD:
     * - Switch expression needs multiple statements
     * - Complex logic before returning value
     * - Side effects (logging, validation) before return
     *
     * COMMON QUESTIONS:
     * Q: "Yield vs return in switch?"
     * A: Yield = switch expressions, return = methods
     *
     * Q: "Switch expression vs switch statement?"
     * A: Expression returns value, statement executes actions
     */
    public static String processGrade(char grade) {
        return switch (grade) {
            case 'A', 'a' -> {
                System.out.println("Excellent!");
                yield "Outstanding"; // yield required for multi-statement blocks
            }
            case 'B', 'b' -> {
                System.out.println("Good job!");
                yield "Good"; // All branches must yield same type
            }
            case 'C', 'c' -> {
                System.out.println("Average");
                yield "Satisfactory"; // Return string value
            }
            case 'D', 'd' -> {
                System.out.println("Below average");
                yield "Needs improvement"; // Return string value
            }
            case 'F', 'f' -> {
                System.out.println("Failed");
                yield "Fail"; // Return string value
            }
            default -> {
                System.out.println("Invalid grade");
                yield "Unknown"; // Default case prevents compilation error
            }
        };
    }

    /**
     * Complex switch expressions with yield
     *
     * DEMONSTRATES:
     * - yield with calculations
     * - Side effects in switch expressions
     * - Type safety in switch returns
     */
    public static int calculatePoints(String operation, int a, int b) {
        return switch (operation) {
            case "add" -> {
                int result = a + b; // Perform addition
                System.out.println("Adding: " + a + " + " + b + " = " + result);
                yield result; // yield allows complex logic before return
            }
            case "multiply" -> {
                int result = a * b; // Perform multiplication
                System.out.println("Multiplying: " + a + " * " + b + " = " + result);
                yield result; // Each case must yield same type
            }
            case "max" -> {
                int result = Math.max(a, b); // Find maximum value
                System.out.println("Max of " + a + " and " + b + " is " + result);
                yield result; // yield enables expression-based switch
            }
            default -> {
                System.out.println("Unknown operation: " + operation);
                yield 0; // Default prevents non-exhaustive switch error
            }
        };
    }

    // =========================
    // DEMONSTRATION METHODS
    // =========================

    /**
     * Transient serialization behavior demonstration
     *
     * KEY POINTS TO MENTION:
     * - Security: Never serialize passwords
     * - Performance: Exclude large temporary objects
     * - Logic: Don't serialize calculated/derived fields
     *
     * EXPECTED RESULT: transient fields become null/0 after deserialization
     */
    public static void demonstrateTransient() throws IOException, ClassNotFoundException {
        System.out.println("=== TRANSIENT Example ===");

        // Create user with sensitive data
        User user = new User("john_doe", "secret123");
        user.incrementLogin(); // Modify transient field
        System.out.println("Before serialization: " + user);

        // Serialize object to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(user); // Only username serialized, not password/count
        }

        // Deserialize object from byte array
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        User deserializedUser;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserializedUser = (User) ois.readObject(); // Transient fields now null/0
        }

        System.out.println("After deserialization: " + deserializedUser);
        System.out.println("Notice: transient fields (password, loginCount) are null/0\n");
    }

    /**
     * Volatile thread visibility demonstration
     *
     * SCENARIO: Without volatile, worker thread might never see 'running' change
     * SOLUTION: volatile ensures change is immediately visible
     *
     * TIP: Mention happens-before relationship
     */
    public static void demonstrateVolatile() {
        System.out.println("=== VOLATILE Example ===");
        VolatileExample example = new VolatileExample();
        example.start(); // Demonstrates volatile preventing infinite loop
        System.out.println("Volatile ensures 'running' flag is immediately visible to worker thread\n");
    }

    /**
     * Record benefits demonstration
     *
     * HIGHLIGHTS:
     * - Boilerplate reduction (no getters/setters/equals/hashCode)
     * - Immutability by default
     * - Validation in compact constructor
     * - Can still have custom methods
     *
     * WHEN TO USE: DTOs, Value Objects, API responses
     */
    public static void demonstrateRecord() {
        System.out.println("=== RECORD Example ===");

        // Record with built-in validation
        Person person = new Person("Alice", 25, "alice@example.com");
        System.out.println("Person: " + person); // toString() auto-generated
        System.out.println("Name: " + person.name()); // Getter without 'get' prefix
        System.out.println("Is adult: " + person.isAdult()); // Custom method

        // Use static factory method
        Point origin = Point.origin();
        Point point = new Point(3, 4);
        System.out.println("Origin: " + origin);
        System.out.println("Point distance from origin: " + point.distanceFromOrigin());
        System.out.println();
    }

    /**
     * Sealed class inheritance control demonstration
     *
     * BENEFITS:
     * - Type safety: Compiler knows all possible subtypes
     * - Pattern matching: Exhaustive switch without default
     * - API evolution: Add new permitted types safely
     *
     * REAL-WORLD USE: State machines, AST nodes, result types
     */
    public static void demonstrateSealed() {
        System.out.println("=== SEALED CLASSES Example ===");

        // All possible Shape types (compiler knows this)
        ReservedKeywordsExamples examples = new ReservedKeywordsExamples();
        Shape circle = examples.new Circle(5); // final = cannot extend
        Shape rectangle = examples.new Rectangle(4, 6); // sealed = controlled extension
        Shape triangle = examples.new Triangle(8, 3); // non-sealed = open extension
        Shape square = examples.new Square(4); // Rectangle subclass

        Shape[] shapes = {circle, rectangle, triangle, square};

        // Calculate and display areas
        for (Shape shape : shapes) {
            System.out.printf("%s area: %.2f%n", shape.name, shape.area());
        }

        System.out.println("Sealed classes restrict inheritance to permitted subclasses\n");
    }

    /**
     * Switch expressions with yield demonstration
     *
     * KEY POINTS:
     * - Switch expressions must be exhaustive
     * - All branches must return same type
     * - yield enables complex logic in expressions
     * - More functional programming style
     *
     * ADVANTAGE: No break statements, no fall-through bugs
     */
    public static void demonstrateYield() {
        System.out.println("=== YIELD Example ===");

        // Switch expression returns values directly
        String result1 = processGrade('A'); // Multi-statement block with yield
        String result2 = processGrade('C'); // Type safety enforced
        String result3 = processGrade('X'); // Default case required

        System.out.println("Grade A result: " + result1);
        System.out.println("Grade C result: " + result2);
        System.out.println("Grade X result: " + result3);

        // Test calculation with yield
        int points1 = calculatePoints("add", 5, 3); // Addition operation
        int points2 = calculatePoints("multiply", 4, 7); // Multiplication operation
        int points3 = calculatePoints("unknown", 1, 2); // Unknown operation

        System.out.println("Add points: " + points1);
        System.out.println("Multiply points: " + points2);
        System.out.println("Unknown points: " + points3);
        System.out.println();
    }

    /**
     * Execute all keyword examples
     *
     * COVERAGE:
     * - transient: Serialization exclusion
     * - volatile: Thread visibility
     * - record: Immutable data classes
     * - sealed: Controlled inheritance
     * - yield: Switch expressions
     *
     * TIP: Run this to see all concepts in action
     */
    public static void main(String[] args) {
        try {
            demonstrateTransient(); // Security & serialization
            demonstrateVolatile(); // Concurrency & visibility
            demonstrateRecord(); // Modern data classes
            demonstrateSealed(); // Type safety & inheritance
            demonstrateYield(); // Functional switch expressions
        } catch (Exception e) {
            e.printStackTrace(); // Handle any runtime exceptions
        }
    }
}