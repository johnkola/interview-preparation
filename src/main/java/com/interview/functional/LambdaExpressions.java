package com.interview.functional;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Lambda Expressions - Comprehensive Guide to Functional Programming in Java
 *
 * This class demonstrates lambda expressions, functional interfaces, and functional
 * programming concepts introduced in Java 8 and enhanced in subsequent versions.
 * Lambda expressions enable functional programming paradigms in Java.
 *
 * Lambda expressions provide:
 * - Concise syntax for anonymous functions
 * - Functional programming capabilities
 * - Better integration with collections and streams
 * - Improved code readability for simple operations
 * - Support for parallel processing
 *
 * Key concepts covered:
 * - Lambda syntax and structure
 * - Built-in functional interfaces (Function, Predicate, Consumer, Supplier)
 * - Method references and constructor references
 * - Closure and variable capture
 * - Higher-order functions
 * - Function composition and chaining
 *
 * Functional interfaces demonstrated:
 * - Predicate<T>: boolean-valued function
 * - Function<T,R>: function that accepts T and returns R
 * - Consumer<T>: function that accepts T and returns void
 * - Supplier<T>: function that returns T with no arguments
 * - BinaryOperator<T>: function that accepts two T and returns T
 * - UnaryOperator<T>: function that accepts T and returns T
 *
 * Interview topics:
 * - Lambda vs anonymous inner classes
 * - Functional interface requirements
 * - Variable capture and effectively final
 * - Method reference types
 * - Performance implications of lambdas
 * - Functional programming benefits
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class LambdaExpressions {

    /**
     * Demonstrates basic lambda syntax and structure
     *
     * Lambda expressions provide a concise way to represent anonymous functions.
     * They consist of parameters, arrow token (->), and body. The body can be
     * an expression or a statement block.
     *
     * Lambda syntax forms:
     * - (parameters) -> expression
     * - (parameters) -> { statements; }
     * - parameter -> expression (single parameter, no parentheses)
     * - () -> expression (no parameters)
     *
     * Benefits over anonymous inner classes:
     * - More concise syntax
     * - Better performance (no extra class file)
     * - Type inference reduces boilerplate
     * - Better support for functional programming
     */
    public static void demonstrateBasicLambdaSyntax() {
        System.out.println("\n=== Basic Lambda Syntax ===");

        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Lambda with no parameters
        Runnable greet = () -> System.out.println("Hello from lambda!");
        greet.run();

        // Lambda with single parameter (parentheses optional)
        names.forEach(name -> System.out.println("Name: " + name));

        // Lambda with multiple parameters
        BinaryOperator<Integer> add = (a, b) -> a + b;
        System.out.println("5 + 3 = " + add.apply(5, 3));

        // Lambda with block body
        numbers.forEach(num -> {
            int square = num * num;
            if (square > 25) {
                System.out.println(num + " squared is " + square + " (> 25)");
            }
        });

        // Lambda with return statement
        Function<String, Integer> stringLength = s -> {
            if (s == null) return 0;
            return s.length();
        };
        names.forEach(name -> System.out.println(name + " has " + stringLength.apply(name) + " characters"));
    }

    /**
     * Demonstrates built-in functional interfaces
     *
     * Java 8 introduced several built-in functional interfaces in java.util.function
     * package. These interfaces provide common functional programming patterns
     * and are used extensively with lambda expressions and streams.
     *
     * Key functional interfaces:
     * - Predicate<T>: T -> boolean (testing/filtering)
     * - Function<T,R>: T -> R (transformation/mapping)
     * - Consumer<T>: T -> void (side effects/processing)
     * - Supplier<T>: () -> T (generation/factory)
     * - BinaryOperator<T>: (T,T) -> T (reduction/combination)
     * - UnaryOperator<T>: T -> T (transformation of same type)
     */
    public static void demonstrateFunctionalInterfaces() {
        System.out.println("\n=== Functional Interfaces ===");

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<String> words = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");

        // Predicate<T> - for testing/filtering
        Predicate<Integer> isEven = num -> num % 2 == 0;
        Predicate<Integer> greaterThanFive = num -> num > 5;
        Predicate<String> startsWithVowel = word -> "aeiou".indexOf(word.toLowerCase().charAt(0)) >= 0;

        System.out.println("Even numbers: " + numbers.stream()
                .filter(isEven)
                .collect(Collectors.toList()));

        System.out.println("Numbers > 5 and even: " + numbers.stream()
                .filter(greaterThanFive.and(isEven))
                .collect(Collectors.toList()));

        System.out.println("Words starting with vowel: " + words.stream()
                .filter(startsWithVowel)
                .collect(Collectors.toList()));

        // Function<T,R> - for transformation/mapping
        Function<String, Integer> wordLength = String::length;
        Function<Integer, String> numberToHex = Integer::toHexString;
        Function<String, String> toUpperCase = String::toUpperCase;

        System.out.println("Word lengths: " + words.stream()
                .map(wordLength)
                .collect(Collectors.toList()));

        System.out.println("Numbers as hex: " + numbers.stream()
                .map(numberToHex)
                .collect(Collectors.toList()));

        // Function composition
        Function<String, String> processWord = toUpperCase.compose(word -> word.trim());
        System.out.println("Processed word: " + processWord.apply("  hello  "));

        // Consumer<T> - for side effects/processing
        Consumer<String> printer = System.out::println;
        Consumer<String> logger = msg -> System.out.println("[LOG] " + msg);
        Consumer<Integer> squarePrinter = num -> System.out.println(num + "² = " + (num * num));

        System.out.println("Printing words:");
        words.forEach(printer);

        System.out.println("Logging with consumer chaining:");
        words.forEach(printer.andThen(logger));

        System.out.println("Square calculations:");
        numbers.stream().limit(5).forEach(squarePrinter);

        // Supplier<T> - for generation/factory
        Supplier<Double> randomValue = Math::random;
        Supplier<String> timestampSupplier = () -> "Generated at: " + System.currentTimeMillis();
        Supplier<List<String>> listFactory = ArrayList::new;

        System.out.println("Random values: " + IntStream.range(0, 3)
                .mapToObj(i -> randomValue.get())
                .collect(Collectors.toList()));

        System.out.println(timestampSupplier.get());

        List<String> newList = listFactory.get();
        newList.add("Created by supplier");
        System.out.println("Factory-created list: " + newList);

        // BinaryOperator<T> and UnaryOperator<T>
        BinaryOperator<Integer> multiply = (a, b) -> a * b;
        BinaryOperator<String> concatenate = (s1, s2) -> s1 + " " + s2;
        UnaryOperator<String> reverse = s -> new StringBuilder(s).reverse().toString();

        System.out.println("Product of 6 and 7: " + multiply.apply(6, 7));
        System.out.println("Concatenated: " + concatenate.apply("Hello", "World"));
        System.out.println("Reversed 'lambda': " + reverse.apply("lambda"));

        // Reduction using BinaryOperator
        Optional<Integer> sum = numbers.stream().reduce(Integer::sum);
        Optional<String> longestWord = words.stream().reduce((w1, w2) -> w1.length() > w2.length() ? w1 : w2);

        System.out.println("Sum of numbers: " + sum.orElse(0));
        System.out.println("Longest word: " + longestWord.orElse("none"));
    }

    /**
     * Demonstrates method references and constructor references
     *
     * Method references provide a shorthand notation for lambda expressions that
     * call a specific method. They make code more readable when the lambda
     * expression only calls an existing method.
     *
     * Types of method references:
     * - Static method reference: ClassName::staticMethod
     * - Instance method reference: instance::instanceMethod
     * - Instance method reference on parameter: ClassName::instanceMethod
     * - Constructor reference: ClassName::new
     *
     * Benefits:
     * - More concise than equivalent lambda expressions
     * - Improved readability when calling existing methods
     * - Better performance in some cases
     * - Clear intent when transforming or processing data
     */
    public static void demonstrateMethodReferences() {
        System.out.println("\n=== Method References ===");

        List<String> words = Arrays.asList("apple", "Banana", "cherry", "DATE", "elderberry");
        List<Integer> numbers = Arrays.asList(1, 4, 9, 16, 25);

        // Static method references
        System.out.println("Using static method reference Math::sqrt:");
        numbers.stream()
                .map(Math::sqrt)
                .forEach(System.out::println);

        System.out.println("Using static method reference Integer::parseInt:");
        List<String> numberStrings = Arrays.asList("1", "2", "3", "4", "5");
        List<Integer> parsedNumbers = numberStrings.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        System.out.println("Parsed numbers: " + parsedNumbers);

        // Instance method reference on a particular object
        String prefix = "PREFIX_";
        Function<String, String> addPrefix = prefix::concat;
        System.out.println("Adding prefix:");
        words.stream()
                .map(addPrefix)
                .forEach(System.out::println);

        // Instance method reference on parameter
        System.out.println("Using instance method reference String::toUpperCase:");
        words.stream()
                .map(String::toUpperCase)
                .forEach(System.out::println);

        System.out.println("Using instance method reference String::length:");
        words.stream()
                .map(String::length)
                .forEach(length -> System.out.println("Length: " + length));

        // Constructor references
        System.out.println("Using constructor references:");

        // Constructor reference for ArrayList
        Supplier<List<String>> listSupplier = ArrayList::new;
        List<String> newList = listSupplier.get();
        newList.addAll(words);
        System.out.println("New list created: " + newList);

        // Constructor reference for creating objects
        List<String> personNames = Arrays.asList("John", "Jane", "Bob");
        List<Person> people = personNames.stream()
                .map(Person::new)  // Person constructor reference
                .collect(Collectors.toList());

        System.out.println("Created people:");
        people.forEach(System.out::println);

        // Constructor reference with multiple parameters
        BiFunction<String, Integer, PersonWithAge> personFactory = PersonWithAge::new;
        PersonWithAge person = personFactory.apply("Alice", 25);
        System.out.println("Created person with age: " + person);

        // Array constructor reference
        IntFunction<String[]> arrayCreator = String[]::new;
        String[] stringArray = words.stream().toArray(arrayCreator);
        System.out.println("Array created: " + Arrays.toString(stringArray));

        // Equivalent using method reference shorthand
        String[] stringArray2 = words.stream().toArray(String[]::new);
        System.out.println("Array created (shorthand): " + Arrays.toString(stringArray2));
    }

    /**
     * Demonstrates variable capture and closure
     *
     * Lambda expressions can capture variables from their enclosing scope.
     * These variables must be effectively final (not modified after initialization).
     * This creates closures that can access external state.
     *
     * Variable capture rules:
     * - Local variables must be effectively final
     * - Instance variables can be accessed and modified
     * - Static variables can be accessed and modified
     * - Parameters are treated like local variables
     *
     * Use cases:
     * - Configuration-based filtering
     * - Contextual processing
     * - State-dependent operations
     * - Parameterized behavior
     */
    public static void demonstrateVariableCapture() {
        System.out.println("\n=== Variable Capture and Closure ===");

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Capturing effectively final local variables
        int threshold = 5;  // Effectively final
        String prefix = "Number: ";  // Effectively final

        List<String> filtered = numbers.stream()
                .filter(num -> num > threshold)  // Captures threshold
                .map(num -> prefix + num)        // Captures prefix
                .collect(Collectors.toList());

        System.out.println("Filtered numbers > " + threshold + ": " + filtered);

        // Demonstrating closure with different contexts
        List<Predicate<Integer>> predicates = createPredicates(Arrays.asList(2, 3, 5));

        numbers.forEach(num -> {
            System.out.print("Number " + num + " is divisible by: ");
            predicates.stream()
                    .filter(predicate -> predicate.test(num))
                    .forEach(p -> System.out.print("? "));  // Can't easily get the divisor back
            System.out.println();
        });

        // Better approach with context preservation
        Map<Integer, Predicate<Integer>> divisibilityTests = Map.of(
                2, num -> num % 2 == 0,
                3, num -> num % 3 == 0,
                5, num -> num % 5 == 0
        );

        numbers.forEach(num -> {
            System.out.print("Number " + num + " is divisible by: ");
            divisibilityTests.entrySet().stream()
                    .filter(entry -> entry.getValue().test(num))
                    .map(Map.Entry::getKey)
                    .forEach(divisor -> System.out.print(divisor + " "));
            System.out.println();
        });

        // Demonstrating configuration-based filtering
        FilterConfig config = new FilterConfig(3, 8, true);
        List<Integer> configuredFilter = numbers.stream()
                .filter(createConfiguredFilter(config))
                .collect(Collectors.toList());

        System.out.println("Configured filter result: " + configuredFilter);
    }

    /**
     * Creates predicates that capture different divisor values
     */
    private static List<Predicate<Integer>> createPredicates(List<Integer> divisors) {
        return divisors.stream()
                .map(divisor -> (Predicate<Integer>) num -> num % divisor == 0)
                .collect(Collectors.toList());
    }

    /**
     * Creates a filter based on configuration, demonstrating closure
     */
    private static Predicate<Integer> createConfiguredFilter(FilterConfig config) {
        return num -> {
            boolean inRange = num >= config.min && num <= config.max;
            boolean evenCheck = !config.onlyEven || num % 2 == 0;
            return inRange && evenCheck;
        };
    }

    /**
     * Demonstrates higher-order functions and function composition
     *
     * Higher-order functions are functions that take other functions as parameters
     * or return functions as results. Function composition allows combining
     * simple functions to create more complex operations.
     *
     * Higher-order function benefits:
     * - Code reusability through function composition
     * - Separation of concerns
     * - Configurable behavior
     * - Functional programming patterns
     *
     * Composition techniques:
     * - Function.compose(): Apply first, then second
     * - Function.andThen(): Apply first, then second (reverse order)
     * - Predicate.and(), or(), negate(): Logical composition
     * - Custom composition utilities
     */
    public static void demonstrateHigherOrderFunctions() {
        System.out.println("\n=== Higher-Order Functions ===");

        List<String> words = Arrays.asList("hello", "WORLD", "Java", "lambda", "FUNCTION");

        // Function composition with andThen
        Function<String, String> addPrefix = s -> "PREFIX_" + s;
        Function<String, String> addSuffix = s -> s + "_SUFFIX";
        Function<String, String> toUpper = String::toUpperCase;

        Function<String, String> composedFunction = addPrefix
                .andThen(toUpper)
                .andThen(addSuffix);

        System.out.println("Function composition with andThen:");
        words.stream()
                .map(composedFunction)
                .forEach(System.out::println);

        // Function composition with compose
        Function<String, Integer> getLength = String::length;
        Function<Integer, String> formatLength = len -> "Length: " + len;
        Function<String, String> stringLengthFormatter = formatLength.compose(getLength);

        System.out.println("Function composition with compose:");
        words.stream()
                .map(stringLengthFormatter)
                .forEach(System.out::println);

        // Predicate composition
        Predicate<String> isLongWord = s -> s.length() > 4;
        Predicate<String> startsWithUpperCase = s -> Character.isUpperCase(s.charAt(0));
        Predicate<String> complexPredicate = isLongWord.and(startsWithUpperCase);

        System.out.println("Complex predicate (long words starting with uppercase):");
        words.stream()
                .filter(complexPredicate)
                .forEach(System.out::println);

        // Higher-order function that returns a function
        Function<String, Function<String, Function<String, String>>> createReplacer = target -> replacement ->
                text -> text.replace(target, replacement);

        Function<String, String> replaceVowels = createReplacer.apply("a").apply("*");
        System.out.println("Replace 'a' with '*':");
        words.stream()
                .map(replaceVowels)
                .forEach(System.out::println);

        // Higher-order function for custom processing
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        // Function that takes a processor and applies it to all numbers
        Function<Function<Integer, String>, List<String>> processNumbers = processor ->
                numbers.stream().map(processor).collect(Collectors.toList());

        List<String> squares = processNumbers.apply(n -> n + "² = " + (n * n));
        List<String> cubes = processNumbers.apply(n -> n + "³ = " + (n * n * n));

        System.out.println("Squares: " + squares);
        System.out.println("Cubes: " + cubes);

        // Custom utility for function chaining
        List<String> processed = words.stream()
                .map(chain((String s) -> s.toLowerCase())
                        .then(s -> s.replace("a", "@"))
                        .then(s -> "[" + s + "]")
                        .build())
                .collect(Collectors.toList());

        System.out.println("Chained processing: " + processed);
    }

    /**
     * Utility method for function chaining
     */
    private static <T> FunctionChain<T, T> chain(Function<T, T> function) {
        return new FunctionChain<>(function);
    }

    /**
     * Demonstrates performance considerations and best practices
     *
     * Lambda expressions have performance implications that should be considered
     * in performance-critical applications. Understanding when and how to use
     * lambdas effectively is important for optimal performance.
     *
     * Performance considerations:
     * - Lambda creation overhead vs reuse
     * - Method reference performance
     * - Primitive specializations
     * - Parallel processing benefits
     * - Memory usage patterns
     *
     * Best practices:
     * - Prefer method references when possible
     * - Reuse lambda expressions when appropriate
     * - Use primitive streams for numeric operations
     * - Consider parallel processing for large datasets
     * - Avoid capturing large objects unnecessarily
     */
    public static void demonstratePerformanceConsiderations() {
        System.out.println("\n=== Performance Considerations ===");

        List<Integer> largeList = IntStream.rangeClosed(1, 1_000_000)
                .boxed()
                .collect(Collectors.toList());

        // Demonstrate primitive stream performance
        long startTime = System.currentTimeMillis();
        long sum1 = largeList.stream()
                .mapToInt(Integer::intValue)  // Convert to primitive stream
                .sum();
        long time1 = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        long sum2 = largeList.stream()
                .reduce(0, Integer::sum);  // Using wrapper types
        long time2 = System.currentTimeMillis() - startTime;

        System.out.println("Primitive stream sum: " + sum1 + " (time: " + time1 + "ms)");
        System.out.println("Wrapper stream sum: " + sum2 + " (time: " + time2 + "ms)");

        // Demonstrate parallel processing
        startTime = System.currentTimeMillis();
        long parallelSum = largeList.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();
        long parallelTime = System.currentTimeMillis() - startTime;

        System.out.println("Parallel sum: " + parallelSum + " (time: " + parallelTime + "ms)");

        // Demonstrate lambda reuse vs recreation
        Function<Integer, Integer> square = x -> x * x;

        startTime = System.currentTimeMillis();
        List<Integer> squares1 = largeList.stream()
                .limit(100_000)
                .map(square)  // Reused lambda
                .collect(Collectors.toList());
        time1 = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        List<Integer> squares2 = largeList.stream()
                .limit(100_000)
                .map(x -> x * x)  // New lambda each time (JVM optimizes this)
                .collect(Collectors.toList());
        time2 = System.currentTimeMillis() - startTime;

        System.out.println("Reused lambda time: " + time1 + "ms");
        System.out.println("Inline lambda time: " + time2 + "ms");

        // Demonstrate method reference vs lambda performance
        List<String> strings = IntStream.rangeClosed(1, 100_000)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());

        startTime = System.currentTimeMillis();
        List<Integer> lengths1 = strings.stream()
                .map(String::length)  // Method reference
                .collect(Collectors.toList());
        time1 = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        List<Integer> lengths2 = strings.stream()
                .map(s -> s.length())  // Lambda expression
                .collect(Collectors.toList());
        time2 = System.currentTimeMillis() - startTime;

        System.out.println("Method reference time: " + time1 + "ms");
        System.out.println("Lambda expression time: " + time2 + "ms");
    }

    /**
     * Main method demonstrating all lambda expression concepts
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Lambda Expressions Demonstration ===");

        demonstrateBasicLambdaSyntax();
        demonstrateFunctionalInterfaces();
        demonstrateMethodReferences();
        demonstrateVariableCapture();
        demonstrateHigherOrderFunctions();
        demonstratePerformanceConsiderations();

        System.out.println("\n=== Lambda Expressions Complete ===");
    }
}

// Helper classes for demonstrations
class Person {
    private final String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "'}";
    }
}

class PersonWithAge {
    private final String name;
    private final int age;

    public PersonWithAge(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "PersonWithAge{name='" + name + "', age=" + age + "}";
    }
}

class FilterConfig {
    final int min;
    final int max;
    final boolean onlyEven;

    FilterConfig(int min, int max, boolean onlyEven) {
        this.min = min;
        this.max = max;
        this.onlyEven = onlyEven;
    }
}

class FunctionChain<T, R> {
    private final Function<T, R> function;

    FunctionChain(Function<T, R> function) {
        this.function = function;
    }

    public <V> FunctionChain<T, V> then(Function<R, V> next) {
        return new FunctionChain<>(function.andThen(next));
    }

    public Function<T, R> build() {
        return function;
    }

    // Implicit conversion to Function
    public static <T, R> Function<T, R> toFunction(FunctionChain<T, R> chain) {
        return chain.function;
    }

    // Allow direct usage as Function through implicit conversion
    public R apply(T input) {
        return function.apply(input);
    }
}