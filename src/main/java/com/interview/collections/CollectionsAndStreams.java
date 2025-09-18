package com.interview.collections;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.*;
import java.util.stream.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

/**
 * Collections and Streams - Comprehensive Guide to Java Collections Framework and Stream API
 *
 * This class demonstrates the Java Collections Framework and Stream API, covering
 * collection types, stream operations, collectors, and advanced stream processing
 * techniques. These are fundamental tools for data processing in modern Java.
 *
 * The Collections Framework provides:
 * - Data structures for organizing and storing objects
 * - Algorithms for searching, sorting, and manipulating collections
 * - Interfaces that define common collection operations
 * - Implementations optimized for different use cases
 *
 * The Stream API provides:
 * - Functional-style operations on collections
 * - Lazy evaluation for efficient processing
 * - Parallel processing capabilities
 * - Rich set of intermediate and terminal operations
 *
 * Collections covered:
 * - List implementations (ArrayList, LinkedList, Vector)
 * - Set implementations (HashSet, LinkedHashSet, TreeSet)
 * - Map implementations (HashMap, LinkedHashMap, TreeMap)
 * - Queue implementations (ArrayDeque, PriorityQueue)
 * - Concurrent collections (ConcurrentHashMap, CopyOnWriteArrayList)
 *
 * Stream operations covered:
 * - Intermediate operations (filter, map, sorted, distinct)
 * - Terminal operations (forEach, collect, reduce, find)
 * - Collectors (grouping, partitioning, joining, statistics)
 * - Parallel streams and performance considerations
 *
 * Interview topics:
 * - Collection interface hierarchy
 * - Performance characteristics of different implementations
 * - When to use each collection type
 * - Stream vs traditional iteration
 * - Parallel stream considerations
 * - Custom collectors and stream operations
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class CollectionsAndStreams {

    /**
     * Demonstrates List implementations and their characteristics
     *
     * Lists are ordered collections that allow duplicate elements and provide
     * positional access to elements. Different implementations have different
     * performance characteristics for various operations.
     *
     * ArrayList:
     * - Resizable array implementation
     * - Fast random access (O(1))
     * - Slow insertion/deletion in middle (O(n))
     * - Good for read-heavy operations
     *
     * LinkedList:
     * - Doubly-linked list implementation
     * - Fast insertion/deletion at ends (O(1))
     * - Slow random access (O(n))
     * - Good for frequent insertions/deletions
     *
     * Vector:
     * - Synchronized version of ArrayList
     * - Thread-safe but slower than ArrayList
     * - Legacy class, prefer ArrayList with synchronization
     */
    public static void demonstrateListImplementations() {
        System.out.println("\n=== List Implementations ===");

        // ArrayList demonstration
        List<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList("Apple", "Banana", "Cherry", "Date", "Elderberry"));

        System.out.println("ArrayList operations:");
        System.out.println("Original: " + arrayList);
        arrayList.add(2, "Coconut");  // Insert at specific position
        System.out.println("After insertion at index 2: " + arrayList);
        arrayList.set(0, "Apricot");  // Replace element
        System.out.println("After replacing index 0: " + arrayList);

        // LinkedList demonstration
        LinkedList<String> linkedList = new LinkedList<>(arrayList);
        System.out.println("\nLinkedList operations:");
        linkedList.addFirst("Avocado");  // Add to beginning
        linkedList.addLast("Fig");       // Add to end
        System.out.println("After adding first and last: " + linkedList);
        System.out.println("First element: " + linkedList.peekFirst());
        System.out.println("Last element: " + linkedList.peekLast());

        // Performance comparison example
        System.out.println("\nPerformance considerations:");
        List<Integer> arrayListInt = new ArrayList<>();
        List<Integer> linkedListInt = new LinkedList<>();

        // Adding elements at the end (ArrayList wins)
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            arrayListInt.add(i);
        }
        long arrayListTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            linkedListInt.add(i);
        }
        long linkedListTime = System.nanoTime() - startTime;

        System.out.println("ArrayList add time: " + arrayListTime + " ns");
        System.out.println("LinkedList add time: " + linkedListTime + " ns");

        // List utilities
        List<Integer> numbers = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5);
        System.out.println("\nList utilities:");
        System.out.println("Original: " + numbers);

        List<Integer> sortedNumbers = new ArrayList<>(numbers);
        Collections.sort(sortedNumbers);
        System.out.println("Sorted: " + sortedNumbers);

        Collections.reverse(sortedNumbers);
        System.out.println("Reversed: " + sortedNumbers);

        Collections.shuffle(sortedNumbers);
        System.out.println("Shuffled: " + sortedNumbers);

        System.out.println("Max element: " + Collections.max(numbers));
        System.out.println("Min element: " + Collections.min(numbers));
        System.out.println("Frequency of 1: " + Collections.frequency(numbers, 1));
    }

    /**
     * Demonstrates Set implementations and their characteristics
     *
     * Sets are collections that contain no duplicate elements. Different
     * implementations provide different ordering guarantees and performance
     * characteristics.
     *
     * HashSet:
     * - Hash table implementation
     * - No ordering guarantee
     * - Fast operations (O(1) average)
     * - Best general-purpose set implementation
     *
     * LinkedHashSet:
     * - Hash table with linked list
     * - Maintains insertion order
     * - Slightly slower than HashSet
     * - Good when ordering matters
     *
     * TreeSet:
     * - Red-black tree implementation
     * - Natural ordering or custom comparator
     * - Slower operations (O(log n))
     * - Good when sorted order needed
     */
    public static void demonstrateSetImplementations() {
        System.out.println("\n=== Set Implementations ===");

        // HashSet demonstration
        Set<String> hashSet = new HashSet<>();
        hashSet.addAll(Arrays.asList("Apple", "Banana", "Apple", "Cherry", "Banana"));
        System.out.println("HashSet (no order, no duplicates): " + hashSet);

        // LinkedHashSet demonstration
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(Arrays.asList("Apple", "Banana", "Apple", "Cherry", "Banana"));
        System.out.println("LinkedHashSet (insertion order): " + linkedHashSet);

        // TreeSet demonstration
        Set<String> treeSet = new TreeSet<>();
        treeSet.addAll(Arrays.asList("Apple", "Banana", "Apple", "Cherry", "Banana"));
        System.out.println("TreeSet (sorted order): " + treeSet);

        // TreeSet with custom comparator
        Set<String> treeLengthSet = new TreeSet<>(Comparator.comparing(String::length).thenComparing(String::compareTo));
        treeLengthSet.addAll(Arrays.asList("Apple", "Banana", "Cherry", "Date", "A", "BB"));
        System.out.println("TreeSet by length then alphabetical: " + treeLengthSet);

        // Set operations
        Set<Integer> set1 = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Set<Integer> set2 = new HashSet<>(Arrays.asList(4, 5, 6, 7, 8));

        System.out.println("\nSet operations:");
        System.out.println("Set 1: " + set1);
        System.out.println("Set 2: " + set2);

        // Union
        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);
        System.out.println("Union: " + union);

        // Intersection
        Set<Integer> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        System.out.println("Intersection: " + intersection);

        // Difference
        Set<Integer> difference = new HashSet<>(set1);
        difference.removeAll(set2);
        System.out.println("Set1 - Set2: " + difference);

        // Symmetric difference
        Set<Integer> symmetricDiff = new HashSet<>(union);
        symmetricDiff.removeAll(intersection);
        System.out.println("Symmetric difference: " + symmetricDiff);
    }

    /**
     * Demonstrates Map implementations and their characteristics
     *
     * Maps store key-value pairs and provide fast lookup by key. Different
     * implementations have different ordering and performance characteristics.
     *
     * HashMap:
     * - Hash table implementation
     * - No ordering guarantee
     * - Fast operations (O(1) average)
     * - Allows one null key and multiple null values
     *
     * LinkedHashMap:
     * - Hash table with linked list
     * - Maintains insertion or access order
     * - Useful for implementing LRU caches
     *
     * TreeMap:
     * - Red-black tree implementation
     * - Natural ordering of keys or custom comparator
     * - Slower operations (O(log n))
     * - NavigableMap interface with range operations
     */
    public static void demonstrateMapImplementations() {
        System.out.println("\n=== Map Implementations ===");

        // HashMap demonstration
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("Apple", 5);
        hashMap.put("Banana", 3);
        hashMap.put("Cherry", 8);
        hashMap.put("Date", 2);
        System.out.println("HashMap: " + hashMap);

        // LinkedHashMap demonstration (insertion order)
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("Apple", 5);
        linkedHashMap.put("Banana", 3);
        linkedHashMap.put("Cherry", 8);
        linkedHashMap.put("Date", 2);
        System.out.println("LinkedHashMap (insertion order): " + linkedHashMap);

        // LinkedHashMap with access order (LRU cache behavior)
        Map<String, Integer> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true);
        accessOrderMap.put("A", 1);
        accessOrderMap.put("B", 2);
        accessOrderMap.put("C", 3);
        System.out.println("Before access: " + accessOrderMap);
        accessOrderMap.get("A");  // Access A
        System.out.println("After accessing A: " + accessOrderMap);

        // TreeMap demonstration
        Map<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("Banana", 3);
        treeMap.put("Apple", 5);
        treeMap.put("Date", 2);
        treeMap.put("Cherry", 8);
        System.out.println("TreeMap (sorted by key): " + treeMap);

        // TreeMap with custom comparator
        Map<String, Integer> lengthTreeMap = new TreeMap<>(Comparator.comparing(String::length).thenComparing(String::compareTo));
        lengthTreeMap.putAll(treeMap);
        System.out.println("TreeMap sorted by key length: " + lengthTreeMap);

        // Map operations
        System.out.println("\nMap operations:");
        Map<String, Integer> fruits = new HashMap<>();
        fruits.put("Apple", 5);
        fruits.put("Banana", 3);

        // getOrDefault
        System.out.println("Cherry count: " + fruits.getOrDefault("Cherry", 0));

        // putIfAbsent
        fruits.putIfAbsent("Cherry", 7);
        System.out.println("After putIfAbsent: " + fruits);

        // compute methods
        fruits.compute("Apple", (key, value) -> value == null ? 1 : value + 1);
        fruits.computeIfAbsent("Date", key -> 4);
        fruits.computeIfPresent("Banana", (key, value) -> value * 2);
        System.out.println("After compute operations: " + fruits);

        // merge
        fruits.merge("Apple", 3, Integer::sum);
        System.out.println("After merge: " + fruits);

        // Iterating over maps
        System.out.println("\nMap iteration:");
        fruits.forEach((key, value) -> System.out.println(key + " -> " + value));

        System.out.println("Entry set iteration:");
        for (Map.Entry<String, Integer> entry : fruits.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    /**
     * Demonstrates basic Stream operations
     *
     * Streams provide a functional approach to processing collections of data.
     * They support intermediate operations (lazy) and terminal operations
     * (eager) to create processing pipelines.
     *
     * Intermediate operations:
     * - filter(): Filter elements based on predicate
     * - map(): Transform elements to different type/value
     * - sorted(): Sort elements
     * - distinct(): Remove duplicates
     * - limit(): Limit number of elements
     * - skip(): Skip first n elements
     *
     * Terminal operations:
     * - forEach(): Execute action for each element
     * - collect(): Collect elements to collection
     * - reduce(): Reduce to single value
     * - count(): Count elements
     * - anyMatch(), allMatch(), noneMatch(): Boolean checks
     */
    public static void demonstrateBasicStreamOperations() {
        System.out.println("\n=== Basic Stream Operations ===");

        List<String> fruits = Arrays.asList("apple", "banana", "cherry", "date", "elderberry", "fig", "grape");
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4);

        // filter() - select elements based on condition
        System.out.println("Fruits with more than 4 characters:");
        fruits.stream()
                .filter(fruit -> fruit.length() > 4)
                .forEach(System.out::println);

        // map() - transform elements
        System.out.println("\nFruit lengths:");
        List<Integer> lengths = fruits.stream()
                .map(String::length)
                .collect(Collectors.toList());
        System.out.println(lengths);

        // sorted() - sort elements
        System.out.println("\nFruits sorted alphabetically:");
        fruits.stream()
                .sorted()
                .forEach(System.out::println);

        System.out.println("\nFruits sorted by length (descending):");
        fruits.stream()
                .sorted(Comparator.comparing(String::length).reversed())
                .forEach(System.out::println);

        // distinct() - remove duplicates
        System.out.println("\nUnique numbers:");
        List<Integer> uniqueNumbers = numbers.stream()
                .distinct()
                .collect(Collectors.toList());
        System.out.println(uniqueNumbers);

        // limit() and skip()
        System.out.println("\nFirst 3 fruits:");
        fruits.stream()
                .limit(3)
                .forEach(System.out::println);

        System.out.println("\nFruits after skipping first 2:");
        fruits.stream()
                .skip(2)
                .limit(3)
                .forEach(System.out::println);

        // Chaining operations
        System.out.println("\nChained operations (long fruits, uppercase, sorted):");
        List<String> processed = fruits.stream()
                .filter(fruit -> fruit.length() > 4)
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
        System.out.println(processed);

        // reduce() - combine elements
        System.out.println("\nReduction operations:");
        Optional<Integer> sum = numbers.stream().reduce(Integer::sum);
        Optional<Integer> max = numbers.stream().reduce(Integer::max);
        Optional<String> concatenated = fruits.stream().reduce((s1, s2) -> s1 + ", " + s2);

        System.out.println("Sum: " + sum.orElse(0));
        System.out.println("Max: " + max.orElse(0));
        System.out.println("Concatenated: " + concatenated.orElse(""));

        // Match operations
        System.out.println("\nMatch operations:");
        boolean anyLongFruit = fruits.stream().anyMatch(fruit -> fruit.length() > 8);
        boolean allShortFruits = fruits.stream().allMatch(fruit -> fruit.length() < 20);
        boolean noNumberFruit = fruits.stream().noneMatch(fruit -> Character.isDigit(fruit.charAt(0)));

        System.out.println("Any fruit > 8 chars: " + anyLongFruit);
        System.out.println("All fruits < 20 chars: " + allShortFruits);
        System.out.println("No fruit starts with number: " + noNumberFruit);
    }

    /**
     * Demonstrates advanced Stream operations and collectors
     *
     * Advanced stream operations include complex transformations, grouping,
     * partitioning, and custom collectors. These operations enable sophisticated
     * data processing with concise, readable code.
     *
     * Advanced operations:
     * - flatMap(): Flatten nested structures
     * - Collectors.groupingBy(): Group elements by classifier
     * - Collectors.partitioningBy(): Split into two groups
     * - Collectors.joining(): Join strings
     * - Custom collectors for specialized processing
     */
    public static void demonstrateAdvancedStreamOperations() {
        System.out.println("\n=== Advanced Stream Operations ===");

        // Sample data
        List<Employee> employees = Arrays.asList(
                new Employee("Alice", 30, "Engineering", 75000),
                new Employee("Bob", 25, "Engineering", 65000),
                new Employee("Charlie", 35, "Marketing", 60000),
                new Employee("Diana", 28, "Engineering", 70000),
                new Employee("Eve", 32, "Marketing", 55000),
                new Employee("Frank", 40, "Sales", 80000),
                new Employee("Grace", 27, "Sales", 50000)
        );

        // flatMap() - flatten nested structures
        List<List<String>> nestedList = Arrays.asList(
                Arrays.asList("apple", "banana"),
                Arrays.asList("cherry", "date"),
                Arrays.asList("elderberry", "fig")
        );

        System.out.println("Flattened list:");
        List<String> flattened = nestedList.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        System.out.println(flattened);

        // groupingBy() - group elements
        System.out.println("\nEmployees grouped by department:");
        Map<String, List<Employee>> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));
        byDepartment.forEach((dept, empList) -> {
            System.out.println(dept + ": " + empList.stream().map(Employee::getName).collect(Collectors.toList()));
        });

        // groupingBy with downstream collector
        System.out.println("\nAverage salary by department:");
        Map<String, Double> avgSalaryByDept = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.averagingDouble(Employee::getSalary)
                ));
        avgSalaryByDept.forEach((dept, avgSalary) ->
                System.out.printf("%s: $%.2f%n", dept, avgSalary));

        // Multiple grouping levels
        System.out.println("\nEmployees grouped by department then age group:");
        Map<String, Map<String, List<Employee>>> nestedGrouping = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.groupingBy(emp -> emp.getAge() < 30 ? "Young" : "Senior")
                ));
        nestedGrouping.forEach((dept, ageGroups) -> {
            System.out.println(dept + ":");
            ageGroups.forEach((ageGroup, empList) ->
                    System.out.println("  " + ageGroup + ": " + empList.size() + " employees"));
        });

        // partitioningBy() - split into two groups
        System.out.println("\nEmployees partitioned by high salary (>= 65000):");
        Map<Boolean, List<Employee>> partitioned = employees.stream()
                .collect(Collectors.partitioningBy(emp -> emp.getSalary() >= 65000));
        System.out.println("High earners: " + partitioned.get(true).size());
        System.out.println("Others: " + partitioned.get(false).size());

        // joining() - join strings
        System.out.println("\nEmployee names joined:");
        String allNames = employees.stream()
                .map(Employee::getName)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println(allNames);

        // Custom collector
        System.out.println("\nSalary statistics:");
        SalaryStatistics stats = employees.stream()
                .collect(SalaryStatistics.collector());
        System.out.println(stats);

        // Complex stream processing
        System.out.println("\nTop 2 highest paid employees per department:");
        Map<String, List<Employee>> topEarnersByDept = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                empList -> empList.stream()
                                        .sorted(Comparator.comparing(Employee::getSalary).reversed())
                                        .limit(2)
                                        .collect(Collectors.toList())
                        )
                ));

        topEarnersByDept.forEach((dept, topEarners) -> {
            System.out.println(dept + ":");
            topEarners.forEach(emp -> System.out.printf("  %s: $%.0f%n", emp.getName(), emp.getSalary()));
        });
    }

    /**
     * Demonstrates parallel streams and performance considerations
     *
     * Parallel streams can improve performance for large datasets by utilizing
     * multiple CPU cores. However, they come with overhead and are not always
     * faster than sequential streams.
     *
     * When to use parallel streams:
     * - Large datasets (typically > 10,000 elements)
     * - CPU-intensive operations
     * - Independent operations (no shared state)
     * - Operations that benefit from parallelization
     *
     * When to avoid parallel streams:
     * - Small datasets
     * - I/O bound operations
     * - Operations with side effects
     * - Order-dependent operations
     */
    public static void demonstrateParallelStreams() {
        System.out.println("\n=== Parallel Streams ===");

        List<Integer> largeList = IntStream.rangeClosed(1, 1_000_000)
                .boxed()
                .collect(Collectors.toList());

        // Sequential vs parallel performance comparison
        System.out.println("Performance comparison (sum of squares):");

        // Sequential stream
        long startTime = System.currentTimeMillis();
        long sequentialSum = largeList.stream()
                .mapToLong(x -> x * x)
                .sum();
        long sequentialTime = System.currentTimeMillis() - startTime;

        // Parallel stream
        startTime = System.currentTimeMillis();
        long parallelSum = largeList.parallelStream()
                .mapToLong(x -> x * x)
                .sum();
        long parallelTime = System.currentTimeMillis() - startTime;

        System.out.println("Sequential result: " + sequentialSum + " (time: " + sequentialTime + "ms)");
        System.out.println("Parallel result: " + parallelSum + " (time: " + parallelTime + "ms)");
        System.out.println("Speedup: " + ((double) sequentialTime / parallelTime) + "x");

        // Demonstrating fork-join pool usage
        System.out.println("\nForkJoinPool common pool parallelism: " +
                          ForkJoinPool.commonPool().getParallelism());

        // Custom ForkJoinPool
        ForkJoinPool customThreadPool = new ForkJoinPool(2);
        try {
            startTime = System.currentTimeMillis();
            long customPoolSum = customThreadPool.submit(() ->
                    largeList.parallelStream()
                            .mapToLong(x -> x * x)
                            .sum()
            ).get();
            long customPoolTime = System.currentTimeMillis() - startTime;

            System.out.println("Custom pool (2 threads) result: " + customPoolSum +
                             " (time: " + customPoolTime + "ms)");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
        }

        // Demonstrating parallel stream characteristics
        System.out.println("\nParallel stream processing order:");
        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .parallelStream()
                .map(x -> {
                    System.out.println("Processing " + x + " on thread: " +
                                     Thread.currentThread().getName());
                    return x * x;
                })
                .collect(Collectors.toList());

        // Parallel collectors
        System.out.println("\nParallel grouping:");
        Map<Boolean, Long> evenOddCount = largeList.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                        x -> x % 2 == 0,
                        Collectors.counting()
                ));
        System.out.println("Even numbers: " + evenOddCount.get(true));
        System.out.println("Odd numbers: " + evenOddCount.get(false));
    }

    /**
     * Demonstrates Stream creation methods and specialized streams
     *
     * Streams can be created from various sources and Java provides specialized
     * primitive streams for better performance with numeric data.
     *
     * Stream creation methods:
     * - Collection.stream()
     * - Arrays.stream()
     * - Stream.of()
     * - Stream.generate()
     * - Stream.iterate()
     * - Files.lines()
     * - IntStream.range()
     *
     * Specialized streams:
     * - IntStream, LongStream, DoubleStream
     * - Better performance for primitive operations
     * - Additional methods like sum(), average(), max(), min()
     */
    public static void demonstrateStreamCreationAndSpecialized() {
        System.out.println("\n=== Stream Creation and Specialized Streams ===");

        // Various ways to create streams
        System.out.println("Different stream creation methods:");

        // From arrays
        String[] array = {"apple", "banana", "cherry"};
        Arrays.stream(array)
                .map(String::toUpperCase)
                .forEach(System.out::println);

        // Stream.of()
        Stream.of("one", "two", "three")
                .forEach(System.out::println);

        // Stream.generate() - infinite stream
        System.out.println("\nRandom numbers (first 5):");
        Stream.generate(Math::random)
                .limit(5)
                .forEach(x -> System.out.printf("%.3f ", x));
        System.out.println();

        // Stream.iterate() - infinite stream with pattern
        System.out.println("Powers of 2 (first 8):");
        Stream.iterate(1, x -> x * 2)
                .limit(8)
                .forEach(x -> System.out.print(x + " "));
        System.out.println();

        // IntStream demonstration
        System.out.println("\nIntStream operations:");
        IntStream.rangeClosed(1, 10)
                .filter(x -> x % 2 == 0)
                .map(x -> x * x)
                .forEach(x -> System.out.print(x + " "));
        System.out.println();

        // IntStream statistics
        IntSummaryStatistics stats = IntStream.rangeClosed(1, 100)
                .summaryStatistics();
        System.out.println("IntStream statistics (1-100):");
        System.out.println("Count: " + stats.getCount());
        System.out.println("Sum: " + stats.getSum());
        System.out.println("Average: " + stats.getAverage());
        System.out.println("Min: " + stats.getMin());
        System.out.println("Max: " + stats.getMax());

        // DoubleStream demonstration
        System.out.println("\nDoubleStream operations:");
        double average = DoubleStream.of(1.5, 2.5, 3.5, 4.5, 5.5)
                .average()
                .orElse(0.0);
        System.out.println("Average: " + average);

        // Converting between stream types
        System.out.println("\nStream type conversions:");
        List<Integer> squares = IntStream.rangeClosed(1, 5)
                .map(x -> x * x)
                .boxed()  // Convert to Stream<Integer>
                .collect(Collectors.toList());
        System.out.println("Squares: " + squares);

        // Converting back to primitive stream
        int sum = squares.stream()
                .mapToInt(Integer::intValue)  // Convert to IntStream
                .sum();
        System.out.println("Sum of squares: " + sum);

        // LongStream for large numbers
        System.out.println("\nLongStream for large calculations:");
        long factorial = LongStream.rangeClosed(1, 10)
                .reduce(1, (a, b) -> a * b);
        System.out.println("10! = " + factorial);
    }

    /**
     * Demonstrates more Stream function examples for interview preparation
     *
     * This method showcases additional Stream operations commonly asked in interviews,
     * including finding elements, working with Optional, complex transformations,
     * and practical real-world scenarios.
     */
    public static void demonstrateMoreStreamExamples() {
        System.out.println("\n=== More Stream Examples ===");

        // Sample data for examples
        List<String> words = Arrays.asList("apple", "banana", "cherry", "date", "elderberry", "fig", "grape", "apricot");
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 20, 25, 30);

        List<Person> people = Arrays.asList(
            new Person("Alice", 25, "Engineering"),
            new Person("Bob", 30, "Marketing"),
            new Person("Charlie", 35, "Engineering"),
            new Person("Diana", 28, "Sales"),
            new Person("Eve", 32, "Marketing"),
            new Person("Frank", 45, "Engineering")
        );

        // findFirst() and findAny() examples
        System.out.println("Finding elements:");
        Optional<String> firstLongWord = words.stream()
            .filter(word -> word.length() > 6)
            .findFirst();
        System.out.println("First word longer than 6 chars: " + firstLongWord.orElse("None"));

        Optional<Integer> anyEvenNumber = numbers.stream()
            .filter(n -> n % 2 == 0)
            .findAny();
        System.out.println("Any even number: " + anyEvenNumber.orElse(-1));

        // min() and max() with custom comparators
        System.out.println("\nMin/Max operations:");
        Optional<String> shortestWord = words.stream()
            .min(Comparator.comparing(String::length));
        System.out.println("Shortest word: " + shortestWord.orElse("None"));

        Optional<Person> oldestPerson = people.stream()
            .max(Comparator.comparing(Person::getAge));
        System.out.println("Oldest person: " + oldestPerson.map(Person::getName).orElse("None"));

        // takeWhile() and dropWhile() (Java 9+)
        System.out.println("\nTakeWhile/DropWhile operations:");
        List<Integer> sortedNumbers = Arrays.asList(1, 2, 3, 4, 5, 8, 9, 10);
        List<Integer> takeWhileResult = sortedNumbers.stream()
            .takeWhile(n -> n < 6)
            .collect(Collectors.toList());
        System.out.println("Take while < 6: " + takeWhileResult);

        List<Integer> dropWhileResult = sortedNumbers.stream()
            .dropWhile(n -> n < 6)
            .collect(Collectors.toList());
        System.out.println("Drop while < 6: " + dropWhileResult);

        // Complex filtering and mapping
        System.out.println("\nComplex transformations:");
        Map<String, List<String>> wordsByFirstLetter = words.stream()
            .filter(word -> word.length() > 4)
            .collect(Collectors.groupingBy(word -> word.substring(0, 1).toUpperCase()));
        System.out.println("Words > 4 chars grouped by first letter: " + wordsByFirstLetter);

        // Working with nested objects
        System.out.println("\nNested object operations:");
        Map<String, Double> avgAgeByDepartment = people.stream()
            .collect(Collectors.groupingBy(
                Person::getDepartment,
                Collectors.averagingInt(Person::getAge)
            ));
        System.out.println("Average age by department: " + avgAgeByDepartment);

        // Conditional mapping
        List<String> processedPeople = people.stream()
            .map(person -> person.getAge() >= 30 ?
                person.getName().toUpperCase() + " (SENIOR)" :
                person.getName().toLowerCase() + " (junior)")
            .collect(Collectors.toList());
        System.out.println("Processed people names: " + processedPeople);

        // Multiple collectors in one operation
        System.out.println("\nMultiple statistics:");
        DoubleSummaryStatistics ageStats = people.stream()
            .mapToDouble(Person::getAge)
            .summaryStatistics();
        System.out.printf("Age stats - Count: %d, Average: %.1f, Min: %.0f, Max: %.0f%n",
            ageStats.getCount(), ageStats.getAverage(), ageStats.getMin(), ageStats.getMax());

        // Peek for debugging
        System.out.println("\nUsing peek() for debugging:");
        List<Integer> debugResult = numbers.stream()
            .filter(n -> n > 10)
            .peek(n -> System.out.println("After filter: " + n))
            .map(n -> n * 2)
            .peek(n -> System.out.println("After map: " + n))
            .collect(Collectors.toList());
        System.out.println("Final result: " + debugResult);
    }

    /**
     * Demonstrates practical Stream use cases for real-world scenarios
     */
    public static void demonstratePracticalStreamUseCases() {
        System.out.println("\n=== Practical Stream Use Cases ===");

        // Sample order data
        List<Order> orders = Arrays.asList(
            new Order("O001", "Alice", Arrays.asList(
                new OrderItem("Laptop", 999.99, 1),
                new OrderItem("Mouse", 25.99, 2)
            )),
            new Order("O002", "Bob", Arrays.asList(
                new OrderItem("Phone", 699.99, 1),
                new OrderItem("Case", 19.99, 1)
            )),
            new Order("O003", "Alice", Arrays.asList(
                new OrderItem("Tablet", 399.99, 1),
                new OrderItem("Stylus", 99.99, 1)
            ))
        );

        // Calculate total order values
        System.out.println("Order totals:");
        orders.stream()
            .collect(Collectors.toMap(
                Order::getOrderId,
                order -> order.getItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum()
            ))
            .forEach((orderId, total) ->
                System.out.printf("%s: $%.2f%n", orderId, total));

        // Find customers with orders over $500
        System.out.println("\nHigh-value customers:");
        Set<String> highValueCustomers = orders.stream()
            .filter(order -> order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum() > 500)
            .map(Order::getCustomer)
            .collect(Collectors.toSet());
        System.out.println(highValueCustomers);

        // Most popular products
        System.out.println("\nProduct popularity (total quantity sold):");
        Map<String, Integer> productPopularity = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .collect(Collectors.groupingBy(
                OrderItem::getProduct,
                Collectors.summingInt(OrderItem::getQuantity)
            ));
        productPopularity.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry ->
                System.out.printf("%s: %d units%n", entry.getKey(), entry.getValue()));

        // Customer spending analysis
        System.out.println("\nCustomer total spending:");
        Map<String, Double> customerSpending = orders.stream()
            .collect(Collectors.groupingBy(
                Order::getCustomer,
                Collectors.summingDouble(order ->
                    order.getItems().stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum())
            ));
        customerSpending.forEach((customer, total) ->
            System.out.printf("%s: $%.2f%n", customer, total));

        // Complex analysis: Average order value per customer
        System.out.println("\nAverage order value per customer:");
        Map<String, Double> avgOrderValuePerCustomer = orders.stream()
            .collect(Collectors.groupingBy(
                Order::getCustomer,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    orderList -> orderList.stream()
                        .mapToDouble(order -> order.getItems().stream()
                            .mapToDouble(item -> item.getPrice() * item.getQuantity())
                            .sum())
                        .average()
                        .orElse(0.0)
                )
            ));
        avgOrderValuePerCustomer.forEach((customer, avgValue) ->
            System.out.printf("%s: $%.2f%n", customer, avgValue));
    }

    /**
     * Main method demonstrating all collections and streams concepts
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Collections and Streams Demonstration ===");

        demonstrateListImplementations();
        demonstrateSetImplementations();
        demonstrateMapImplementations();
        demonstrateBasicStreamOperations();
        demonstrateAdvancedStreamOperations();
        demonstrateParallelStreams();
        demonstrateStreamCreationAndSpecialized();
        demonstrateMoreStreamExamples();
        demonstratePracticalStreamUseCases();

        System.out.println("\n=== Collections and Streams Complete ===");
    }
}

// Helper classes for demonstrations
class Employee {
    private final String name;
    private final int age;
    private final String department;
    private final double salary;

    public Employee(String name, int age, String department, double salary) {
        this.name = name;
        this.age = age;
        this.department = department;
        this.salary = salary;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDepartment() { return department; }
    public double getSalary() { return salary; }

    @Override
    public String toString() {
        return String.format("Employee{name='%s', age=%d, dept='%s', salary=%.0f}",
                           name, age, department, salary);
    }
}

// Custom collector for salary statistics
class SalaryStatistics {
    private final double total;
    private final double average;
    private final double min;
    private final double max;
    private final long count;

    public SalaryStatistics(double total, double average, double min, double max, long count) {
        this.total = total;
        this.average = average;
        this.min = min;
        this.max = max;
        this.count = count;
    }

    public static Collector<Employee, ?, SalaryStatistics> collector() {
        return Collector.of(
                () -> new double[4], // [sum, count, min, max]
                (acc, emp) -> {
                    acc[0] += emp.getSalary(); // sum
                    acc[1]++; // count
                    acc[2] = acc[1] == 1 ? emp.getSalary() : Math.min(acc[2], emp.getSalary()); // min
                    acc[3] = acc[1] == 1 ? emp.getSalary() : Math.max(acc[3], emp.getSalary()); // max
                },
                (acc1, acc2) -> {
                    acc1[0] += acc2[0]; // sum
                    acc1[1] += acc2[1]; // count
                    acc1[2] = Math.min(acc1[2], acc2[2]); // min
                    acc1[3] = Math.max(acc1[3], acc2[3]); // max
                    return acc1;
                },
                acc -> new SalaryStatistics(
                        acc[0], // total
                        acc[1] > 0 ? acc[0] / acc[1] : 0, // average
                        acc[2], // min
                        acc[3], // max
                        (long) acc[1] // count
                )
        );
    }

    @Override
    public String toString() {
        return String.format("SalaryStatistics{total=%.2f, average=%.2f, min=%.2f, max=%.2f, count=%d}",
                           total, average, min, max, count);
    }
}

// Additional helper classes for stream examples
class Person {
    private final String name;
    private final int age;
    private final String department;

    public Person(String name, int age, String department) {
        this.name = name;
        this.age = age;
        this.department = department;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDepartment() { return department; }

    @Override
    public String toString() {
        return String.format("Person{name='%s', age=%d, dept='%s'}", name, age, department);
    }
}

class Order {
    private final String orderId;
    private final String customer;
    private final List<OrderItem> items;

    public Order(String orderId, String customer, List<OrderItem> items) {
        this.orderId = orderId;
        this.customer = customer;
        this.items = items;
    }

    public String getOrderId() { return orderId; }
    public String getCustomer() { return customer; }
    public List<OrderItem> getItems() { return items; }

    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', items=%d}", orderId, customer, items.size());
    }
}

class OrderItem {
    private final String product;
    private final double price;
    private final int quantity;

    public OrderItem(String product, double price, int quantity) {
        this.product = product;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProduct() { return product; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    @Override
    public String toString() {
        return String.format("OrderItem{product='%s', price=%.2f, qty=%d}", product, price, quantity);
    }
}