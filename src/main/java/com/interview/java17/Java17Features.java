package com.interview.java17;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

/**
 * Java 17 Features - Comprehensive Guide to Latest Language Enhancements
 *
 * This class demonstrates the key features introduced in Java 17 LTS, including
 * language enhancements, API improvements, and new functionality that enhances
 * developer productivity and code readability.
 *
 * Java 17 is a Long Term Support (LTS) release, making it an important milestone
 * for enterprise adoption. It includes features from Java 9-17 that are commonly
 * discussed in technical interviews.
 *
 * Key Java 17 features covered:
 * - Sealed Classes (Preview in 15, Standard in 17)
 * - Pattern Matching for instanceof (Standard in 16, Enhanced in 17)
 * - Records (Standard in 14, Enhanced through 17)
 * - Text Blocks (Standard in 15)
 * - Switch Expressions (Standard in 14)
 * - Enhanced Random Number Generators
 * - New Stream Methods
 * - Helpful NullPointerExceptions
 *
 * Interview topics:
 * - Modern Java language features vs legacy approaches
 * - When to use sealed classes vs inheritance
 * - Benefits of records over traditional POJOs
 * - Pattern matching performance and readability
 * - Text blocks vs string concatenation
 * - Switch expressions vs traditional switch statements
 *
 * Performance improvements:
 * - Reduced boilerplate code
 * - Better memory efficiency with records
 * - Enhanced JVM optimizations
 * - Improved garbage collection
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class Java17Features {

    /**
     * Demonstrates Sealed Classes - restricted inheritance hierarchies
     *
     * Sealed classes allow you to control which classes can extend or implement them.
     * This provides better control over inheritance hierarchies and enables more
     * precise pattern matching and exhaustive switch expressions.
     *
     * Key benefits:
     * - Controlled inheritance - only specified classes can extend
     * - Exhaustive pattern matching without default cases
     * - Better domain modeling with finite type hierarchies
     * - Compiler optimizations for sealed type hierarchies
     *
     * Use cases:
     * - Domain modeling with finite sets of types
     * - State machines with known states
     * - API design where extension should be controlled
     * - Algebraic data types similar to other languages
     *
     * Sealed class rules:
     * - Must specify permitted subclasses with 'permits' clause
     * - Permitted classes must be final, sealed, or non-sealed
     * - All permitted classes must be in same module or package
     */
    public static void demonstrateSealedClasses() {
        System.out.println("\n=== Sealed Classes ===");

        // Create instances of different shape types
        Shape circle = new Circle(5.0);
        Shape rectangle = new Rectangle(4.0, 6.0);
        Shape triangle = new Triangle(3.0, 4.0, 5.0);

        // Process shapes using pattern matching
        List<Shape> shapes = List.of(circle, rectangle, triangle);
        shapes.forEach(shape -> {
            double area = calculateArea(shape);
            System.out.println(shape.getClass().getSimpleName() + " area: " + area);
        });

        // Demonstrate pattern matching with sealed types using instanceof
        shapes.forEach(shape -> {
            String description;
            if (shape instanceof Circle c) {
                description = "Circle with radius " + c.radius();
            } else if (shape instanceof Rectangle r) {
                description = "Rectangle " + r.width() + "x" + r.height();
            } else if (shape instanceof Triangle t) {
                description = "Triangle with sides " + t.a() + ", " + t.b() + ", " + t.c();
            } else {
                description = "Unknown shape";
            }
            System.out.println(description);
        });
    }

    /**
     * Calculates area using pattern matching for instanceof
     *
     * Pattern matching for instanceof eliminates the need for explicit casting
     * after type checking, making code more concise and readable.
     */
    private static double calculateArea(Shape shape) {
        // Pattern matching for instanceof - no explicit casting needed
        if (shape instanceof Circle c) {
            return Math.PI * c.radius() * c.radius();
        } else if (shape instanceof Rectangle r) {
            return r.width() * r.height();
        } else if (shape instanceof Triangle t) {
            // Heron's formula for triangle area
            double s = (t.a() + t.b() + t.c()) / 2;
            return Math.sqrt(s * (s - t.a()) * (s - t.b()) * (s - t.c()));
        }
        throw new IllegalArgumentException("Unknown shape type");
    }

    /**
     * Demonstrates Records - concise data carriers
     *
     * Records provide a compact syntax for declaring classes that are primarily
     * data carriers. They automatically generate constructor, accessors, equals(),
     * hashCode(), and toString() methods.
     *
     * Record benefits:
     * - Immutable by default
     * - Automatic generation of boilerplate methods
     * - Clear intent - this is a data carrier
     * - Better performance than traditional POJOs
     * - Compact syntax reduces code verbosity
     *
     * Use cases:
     * - DTOs (Data Transfer Objects)
     * - Value objects in domain modeling
     * - API response/request objects
     * - Configuration objects
     * - Tuples and data groupings
     */
    public static void demonstrateRecords() {
        System.out.println("\n=== Records ===");

        // Create person records
        Person person1 = new Person("John", "Doe", 30, "john.doe@email.com");
        Person person2 = new Person("Jane", "Smith", 25, "jane.smith@email.com");

        // Records automatically provide toString()
        System.out.println("Person 1: " + person1);
        System.out.println("Person 2: " + person2);

        // Records provide automatic accessor methods
        System.out.println("Person 1 full name: " + person1.firstName() + " " + person1.lastName());
        System.out.println("Person 1 age: " + person1.age());

        // Records provide automatic equals() and hashCode()
        Person person1Copy = new Person("John", "Doe", 30, "john.doe@email.com");
        System.out.println("person1.equals(person1Copy): " + person1.equals(person1Copy));
        System.out.println("person1.hashCode() == person1Copy.hashCode(): " +
                         (person1.hashCode() == person1Copy.hashCode()));

        // Demonstrate record with validation in compact constructor
        try {
            PersonWithValidation validPerson = new PersonWithValidation("Alice", 25);
            System.out.println("Valid person: " + validPerson);

            PersonWithValidation invalidPerson = new PersonWithValidation("", -5);
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }

        // Demonstrate record methods
        Employee employee = new Employee("Bob", 50000, "Engineering");
        System.out.println("Employee: " + employee);
        System.out.println("Employee with raise: " + employee.withSalaryIncrease(5000));
        System.out.println("Formatted info: " + employee.getFormattedInfo());
    }

    /**
     * Demonstrates Text Blocks - multi-line string literals
     *
     * Text blocks provide a way to write multi-line strings without excessive
     * string concatenation or escape sequences. They preserve formatting and
     * improve readability for SQL queries, JSON, HTML, etc.
     *
     * Text block features:
     * - Multi-line strings with preserved formatting
     * - Automatic indentation management
     * - No need for escape sequences in most cases
     * - Better readability for embedded languages
     *
     * Use cases:
     * - SQL queries
     * - JSON templates
     * - HTML snippets
     * - XML configurations
     * - Multi-line documentation
     */
    public static void demonstrateTextBlocks() {
        System.out.println("\n=== Text Blocks ===");

        // SQL query using text block
        String sqlQuery = """
                SELECT u.id, u.name, u.email, p.title
                FROM users u
                JOIN profiles p ON u.id = p.user_id
                WHERE u.active = true
                  AND u.created_date > '2023-01-01'
                ORDER BY u.name ASC
                """;
        System.out.println("SQL Query:");
        System.out.println(sqlQuery);

        // JSON template using text block
        String jsonTemplate = """
                {
                    "id": %d,
                    "name": "%s",
                    "email": "%s",
                    "profile": {
                        "age": %d,
                        "department": "%s"
                    },
                    "active": true
                }
                """;

        String formattedJson = jsonTemplate.formatted(1, "John Doe", "john@example.com", 30, "Engineering");
        System.out.println("JSON Template Result:");
        System.out.println(formattedJson);

        // HTML snippet using text block
        String htmlTemplate = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>%s</title>
                </head>
                <body>
                    <h1>Welcome, %s!</h1>
                    <p>Your email: %s</p>
                    <div class="profile">
                        <span>Age: %d</span>
                    </div>
                </body>
                </html>
                """;

        String formattedHtml = htmlTemplate.formatted("User Profile", "Jane Smith", "jane@example.com", 25);
        System.out.println("HTML Template Result:");
        System.out.println(formattedHtml);
    }

    /**
     * Demonstrates Switch Expressions - enhanced switch with values
     *
     * Switch expressions extend traditional switch statements to return values
     * and support pattern matching. They provide more concise and safer syntax
     * with exhaustiveness checking.
     *
     * Switch expression benefits:
     * - Can return values directly
     * - Exhaustiveness checking for enums and sealed types
     * - No fall-through by default
     * - More concise syntax with arrow notation
     * - Support for multiple case labels
     *
     * Use cases:
     * - Value computation based on input
     * - State machine implementations
     * - Mapping operations
     * - Configuration selection
     */
    public static void demonstrateSwitchExpressions() {
        System.out.println("\n=== Switch Expressions ===");

        // Traditional enum processing with switch expression
        for (Day day : Day.values()) {
            String dayType = switch (day) {
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
                case SATURDAY, SUNDAY -> "Weekend";
            };
            System.out.println(day + " is a " + dayType);
        }

        // Switch expression with yield for complex logic
        for (Month month : Month.values()) {
            var season = switch (month) {
                case DECEMBER, JANUARY, FEBRUARY -> {
                    System.out.println("Processing winter month: " + month);
                    yield "Winter";
                }
                case MARCH, APRIL, MAY -> {
                    System.out.println("Processing spring month: " + month);
                    yield "Spring";
                }
                case JUNE, JULY, AUGUST -> {
                    System.out.println("Processing summer month: " + month);
                    yield "Summer";
                }
                case SEPTEMBER, OCTOBER, NOVEMBER -> {
                    System.out.println("Processing fall month: " + month);
                    yield  "Fall";
                }
            };
            System.out.println(month + " -> " + season);
        }

        // Switch expression for calculating values
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        numbers.forEach(num -> {
            String description = switch (num % 3) {
                case 0 -> "Divisible by 3";
                case 1 -> "Remainder 1 when divided by 3";
                case 2 -> "Remainder 2 when divided by 3";
                default -> "Unexpected remainder"; // Should never happen
            };
            System.out.println(num + ": " + description);
        });
    }

    /**
     * Demonstrates Enhanced Random Number Generators
     *
     * Java 17 introduces new random number generator interfaces and implementations
     * that provide better performance, more algorithms, and cleaner APIs.
     *
     * New random features:
     * - RandomGenerator interface for consistent API
     * - Multiple algorithm implementations
     * - Better performance for concurrent usage
     * - Stream-based random generation
     * - Jumping and leaping for parallel streams
     *
     * Use cases:
     * - High-performance simulations
     * - Concurrent random number generation
     * - Statistical sampling
     * - Game development
     * - Scientific computing
     */
    public static void demonstrateEnhancedRandomGenerators() {
        System.out.println("\n=== Enhanced Random Generators ===");

        // List available random generator algorithms
        System.out.println("Available Random Generator Algorithms:");
        RandomGeneratorFactory.all()
                .map(RandomGeneratorFactory::name)
                .sorted()
                .forEach(name -> System.out.println("  - " + name));

        // Use different random generator implementations
        RandomGenerator defaultRandom = RandomGenerator.getDefault();
        RandomGenerator xoshiro = RandomGeneratorFactory.of("Xoshiro256PlusPlus").create();
        RandomGenerator l64x128 = RandomGeneratorFactory.of("L64X128MixRandom").create();

        System.out.println("\nRandom number comparison:");
        System.out.println("Default: " + defaultRandom.nextInt(1, 101));
        System.out.println("Xoshiro256PlusPlus: " + xoshiro.nextInt(1, 101));
        System.out.println("L64X128MixRandom: " + l64x128.nextInt(1, 101));

        // Generate random streams
        System.out.println("\nRandom integer stream (1-10):");
        defaultRandom.ints(10, 1, 11)
                .forEach(i -> System.out.print(i + " "));
        System.out.println();

        // Generate random doubles
        System.out.println("\nRandom double stream (0.0-1.0):");
        defaultRandom.doubles(5)
                .forEach(d -> System.out.printf("%.3f ", d));
        System.out.println();

        // Demonstrate random boolean generation
        System.out.println("\nRandom booleans:");
        for (int i = 0; i < 10; i++) {
            System.out.print(defaultRandom.nextBoolean() + " ");
        }
        System.out.println();
    }

    /**
     * Demonstrates New Stream Methods
     *
     * Java 16-17 introduced new Stream methods that provide additional functionality
     * for stream processing, making common operations more convenient.
     *
     * New stream methods:
     * - toList(): Direct conversion to List
     * - mapMulti(): One-to-many mapping
     * - Additional collectors and utilities
     *
     * Benefits:
     * - More concise code
     * - Better performance for specific operations
     * - Improved readability
     */
    public static void demonstrateNewStreamMethods() {
        System.out.println("\n=== New Stream Methods ===");

        List<String> names = List.of("John", "Jane", "Bob", "Alice", "Charlie");

        // toList() method - direct conversion to List
        List<String> upperCaseNames = names.stream()
                .map(String::toUpperCase)
                .toList(); // More concise than .collect(Collectors.toList())

        System.out.println("Original names: " + names);
        System.out.println("Uppercase names: " + upperCaseNames);

        // mapMulti() method - one-to-many mapping
        List<String> sentences = List.of("Hello World", "Java Programming", "Stream API");

        List<String> words = sentences.stream()
                .<String>mapMulti((sentence, consumer) -> {
                    for (String word : sentence.split(" ")) {
                        consumer.accept(word.toLowerCase());
                    }
                })
                .toList();

        System.out.println("Sentences: " + sentences);
        System.out.println("Words: " + words);

        // Another mapMulti example - filtering and transforming
        List<String> items = List.of("apple", "banana", "cherry", "date", "elderberry");

        List<String> processedItems = items.stream()
                .<String>mapMulti((item, consumer) -> {
                    if (item.length() > 4) { // Filter long names
                        consumer.accept(item.toUpperCase()); // Transform to uppercase
                        consumer.accept(item.length() + " chars"); // Add length info
                    }
                })
                .toList();

        System.out.println("Original items: " + items);
        System.out.println("Processed items: " + processedItems);
    }

    /**
     * Main method demonstrating all Java 17 features
     *
     * Executes all demonstration methods to showcase the complete range of
     * Java 17 features and enhancements.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Java 17 Features Demonstration ===");

        demonstrateSealedClasses();
        demonstrateRecords();
        demonstrateTextBlocks();
        demonstrateSwitchExpressions();
        demonstrateEnhancedRandomGenerators();
        demonstrateNewStreamMethods();

        System.out.println("\n=== Java 17 Features Complete ===");
    }
}

// Sealed class hierarchy for shapes
sealed interface Shape permits Circle, Rectangle, Triangle {
}

record Circle(double radius) implements Shape {
    public Circle {
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
    }
}

record Rectangle(double width, double height) implements Shape {
    public Rectangle {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
    }
}

record Triangle(double a, double b, double c) implements Shape {
    public Triangle {
        if (a <= 0 || b <= 0 || c <= 0) {
            throw new IllegalArgumentException("All sides must be positive");
        }
        if (a + b <= c || a + c <= b || b + c <= a) {
            throw new IllegalArgumentException("Invalid triangle sides");
        }
    }
}

// Record examples
record Person(String firstName, String lastName, int age, String email) {
    // Records can have additional methods
    public String fullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdult() {
        return age >= 18;
    }
}

record PersonWithValidation(String name, int age) {
    // Compact constructor for validation
    public PersonWithValidation {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        name = name.trim(); // Normalize the name
    }
}

record Employee(String name, double salary, String department) {
    // Records can have static methods
    public static Employee createIntern(String name, String department) {
        return new Employee(name, 30000, department);
    }

    // Records can have instance methods
    public Employee withSalaryIncrease(double increase) {
        return new Employee(name, salary + increase, department);
    }

    public String getFormattedInfo() {
        return String.format("%s works in %s with salary $%.2f", name, department, salary);
    }
}

// Enums for switch expression examples
enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

enum Month {
    JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE,
    JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
}