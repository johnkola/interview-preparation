# Java Thread Safety Best Practices - Complete Guide

> **Comprehensive guide to writing thread-safe code with practical examples and performance comparisons**
> Focuses on safe programming practices, common pitfalls, and performance trade-offs

---

## 📋 Table of Contents

### 🔒 **Thread Safety Fundamentals**
- **[Q1-Q10: Thread Safety Concepts](#thread-safety-concepts)** - What makes code thread-safe
- **[Q11-Q20: Common Thread Safety Issues](#thread-safety-issues)** - Race conditions, visibility problems
- **[Q21-Q30: Immutability Patterns](#immutability-patterns)** - Creating immutable objects

### 🛠️ **Thread-Safe vs Non-Thread-Safe Classes**
- **[Q31-Q40: String Operations](#string-operations)** - StringBuilder vs StringBuffer vs String
- **[Q41-Q50: Utility Classes](#utility-classes)** - Date vs LocalDateTime, Random vs ThreadLocalRandom

*📋 **Note:** For comprehensive collection thread-safety comparisons (ArrayList vs Vector, HashMap vs ConcurrentHashMap, etc.), see: `JAVA_COLLECTIONS_STREAMS.md` and `JAVA_CONCURRENCY_COMPREHENSIVE.md`*

### 🔧 **Synchronization Mechanisms**
- **[Q61-Q70: Synchronization Techniques](#synchronization-techniques)** - synchronized, volatile, atomic classes
- **[Q71-Q80: Lock-Free Programming](#lock-free-programming)** - Atomic operations, compare-and-swap
- **[Q81-Q90: Thread-Local Storage](#thread-local-storage)** - ThreadLocal usage patterns

### ⚡ **Performance & Best Practices**
- **[Q91-Q100: Performance Comparisons](#performance-comparisons)** - Benchmarking thread-safe operations
- **[Q101-Q110: Best Practices](#best-practices)** - Design patterns for thread safety
- **[Q111-Q120: Common Pitfalls](#common-pitfalls)** - What to avoid in concurrent code

---

## Thread Safety Concepts

### Q1: What is thread safety and why is it important?

**Answer:** Thread safety means that a class or method can be used by multiple threads simultaneously without causing data corruption or inconsistent state.

```java
public class ThreadSafetyDemo {

    // NON-THREAD-SAFE example
    static class UnsafeCounter {
        private int count = 0;

        public void increment() {
            count++; // NOT atomic! Read-modify-write operation
        }

        public int getCount() {
            return count;
        }
    }

    // THREAD-SAFE example using synchronization
    static class SafeCounter {
        private int count = 0;

        public synchronized void increment() {
            count++; // Now atomic due to synchronization
        }

        public synchronized int getCount() {
            return count;
        }
    }

    // THREAD-SAFE example using atomic classes
    static class AtomicCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        public void increment() {
            count.incrementAndGet(); // Atomic operation
        }

        public int getCount() {
            return count.get();
        }
    }

    public static void demonstrateThreadSafety() {
        System.out.println("=== Thread Safety Demonstration ===");

        final int THREAD_COUNT = 10;
        final int INCREMENTS_PER_THREAD = 1000;
        final int EXPECTED_RESULT = THREAD_COUNT * INCREMENTS_PER_THREAD;

        // Test unsafe counter
        testCounter("UNSAFE", new UnsafeCounter(), THREAD_COUNT, INCREMENTS_PER_THREAD, EXPECTED_RESULT);

        // Test safe counter
        testCounter("SAFE (synchronized)", new SafeCounter(), THREAD_COUNT, INCREMENTS_PER_THREAD, EXPECTED_RESULT);

        // Test atomic counter
        testCounter("ATOMIC", new AtomicCounter(), THREAD_COUNT, INCREMENTS_PER_THREAD, EXPECTED_RESULT);
    }

    private static void testCounter(String type, Object counter, int threadCount,
                                   int incrementsPerThread, int expectedResult) {
        System.out.println("\n--- Testing " + type + " Counter ---");

        var threads = new Thread[threadCount];
        long startTime = System.currentTimeMillis();

        // Create and start threads
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    if (counter instanceof UnsafeCounter) {
                        ((UnsafeCounter) counter).increment();
                    } else if (counter instanceof SafeCounter) {
                        ((SafeCounter) counter).increment();
                    } else if (counter instanceof AtomicCounter) {
                        ((AtomicCounter) counter).increment();
                    }
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();

        // Get final count
        int finalCount = 0;
        if (counter instanceof UnsafeCounter) {
            finalCount = ((UnsafeCounter) counter).getCount();
        } else if (counter instanceof SafeCounter) {
            finalCount = ((SafeCounter) counter).getCount();
        } else if (counter instanceof AtomicCounter) {
            finalCount = ((AtomicCounter) counter).getCount();
        }

        System.out.println("Expected: " + expectedResult);
        System.out.println("Actual: " + finalCount);
        System.out.println("Difference: " + (expectedResult - finalCount));
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Thread safe: " + (finalCount == expectedResult ? "YES" : "NO"));
    }

    public static void main(String[] args) {
        demonstrateThreadSafety();
    }
}
```

### Q2: Demonstrate the difference between StringBuilder and StringBuffer

**Answer:** StringBuffer is thread-safe (synchronized), while StringBuilder is not thread-safe but faster in single-threaded scenarios.

```java
public class StringBuilderVsStringBufferDemo {

    public static void demonstrateBasicDifferences() {
        System.out.println("=== StringBuilder vs StringBuffer ===");

        // Performance comparison in single-threaded environment
        compareSingleThreadedPerformance();

        // Thread safety comparison
        compareThreadSafety();

        // Memory usage comparison
        compareMemoryUsage();
    }

    private static void compareSingleThreadedPerformance() {
        System.out.println("\n--- Single-Threaded Performance ---");

        final int ITERATIONS = 100_000;
        String baseString = "Performance test ";

        // StringBuilder performance
        long startTime = System.nanoTime();
        var stringBuilder = new StringBuilder();
        for (int i = 0; i < ITERATIONS; i++) {
            stringBuilder.append(baseString).append(i);
        }
        String sbResult = stringBuilder.toString();
        long sbTime = System.nanoTime() - startTime;

        // StringBuffer performance
        startTime = System.nanoTime();
        var stringBuffer = new StringBuffer();
        for (int i = 0; i < ITERATIONS; i++) {
            stringBuffer.append(baseString).append(i);
        }
        String sbufResult = stringBuffer.toString();
        long sbufTime = System.nanoTime() - startTime;

        // String concatenation (for comparison)
        startTime = System.nanoTime();
        String stringResult = "";
        for (int i = 0; i < Math.min(ITERATIONS, 1000); i++) { // Reduced iterations
            stringResult += baseString + i;
        }
        long stringTime = System.nanoTime() - startTime;

        System.out.println("StringBuilder time: " + sbTime / 1_000_000 + " ms");
        System.out.println("StringBuffer time: " + sbufTime / 1_000_000 + " ms");
        System.out.println("String concat time (1000 iterations): " + stringTime / 1_000_000 + " ms");
        System.out.println("StringBuffer is " + String.format("%.2f", (double) sbufTime / sbTime) + "x slower than StringBuilder");

        // Verify results are the same
        System.out.println("Results identical: " + sbResult.equals(sbufResult));
    }

    private static void compareThreadSafety() {
        System.out.println("\n--- Thread Safety Comparison ---");

        final int THREAD_COUNT = 10;
        final int OPERATIONS_PER_THREAD = 1000;

        // Test StringBuilder (NOT thread-safe)
        testStringBuilderThreadSafety(THREAD_COUNT, OPERATIONS_PER_THREAD);

        // Test StringBuffer (thread-safe)
        testStringBufferThreadSafety(THREAD_COUNT, OPERATIONS_PER_THREAD);
    }

    private static void testStringBuilderThreadSafety(int threadCount, int operationsPerThread) {
        System.out.println("\nTesting StringBuilder (NOT thread-safe):");

        var sharedStringBuilder = new StringBuilder();
        var threads = new Thread[threadCount];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    sharedStringBuilder.append("T").append(threadId).append("-").append(j).append(" ");
                }
            });
            threads[i].start();
        }

        // Wait for completion
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();
        int expectedLength = threadCount * operationsPerThread * 6; // Approximate
        int actualLength = sharedStringBuilder.length();

        System.out.println("Expected approximate length: " + expectedLength);
        System.out.println("Actual length: " + actualLength);
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Data corruption likely: " + (Math.abs(actualLength - expectedLength) > expectedLength * 0.1));
    }

    private static void testStringBufferThreadSafety(int threadCount, int operationsPerThread) {
        System.out.println("\nTesting StringBuffer (thread-safe):");

        var sharedStringBuffer = new StringBuffer();
        var threads = new Thread[threadCount];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    sharedStringBuffer.append("T").append(threadId).append("-").append(j).append(" ");
                }
            });
            threads[i].start();
        }

        // Wait for completion
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();
        int expectedLength = threadCount * operationsPerThread * 6; // Approximate
        int actualLength = sharedStringBuffer.length();

        System.out.println("Expected approximate length: " + expectedLength);
        System.out.println("Actual length: " + actualLength);
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Thread safe: " + (Math.abs(actualLength - expectedLength) <= expectedLength * 0.01));
    }

    private static void compareMemoryUsage() {
        System.out.println("\n--- Memory Usage Comparison ---");

        // Create large strings to see memory impact
        final int SIZE = 100_000;

        // StringBuilder memory usage
        long beforeSB = getUsedMemory();
        var stringBuilder = new StringBuilder(SIZE);
        for (int i = 0; i < SIZE; i++) {
            stringBuilder.append("A");
        }
        long afterSB = getUsedMemory();

        // StringBuffer memory usage
        long beforeSBuf = getUsedMemory();
        var stringBuffer = new StringBuffer(SIZE);
        for (int i = 0; i < SIZE; i++) {
            stringBuffer.append("A");
        }
        long afterSBuf = getUsedMemory();

        System.out.println("StringBuilder memory: " + formatBytes(afterSB - beforeSB));
        System.out.println("StringBuffer memory: " + formatBytes(afterSBuf - beforeSBuf));
    }

    // Thread-safe alternative: using ThreadLocal
    public static void demonstrateThreadLocalAlternative() {
        System.out.println("\n=== ThreadLocal StringBuilder Alternative ===");

        final ThreadLocal<StringBuilder> threadLocalBuilder = ThreadLocal.withInitial(StringBuilder::new);

        final int THREAD_COUNT = 5;
        final int OPERATIONS_PER_THREAD = 1000;
        var results = new String[THREAD_COUNT];
        var threads = new Thread[THREAD_COUNT];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                StringBuilder sb = threadLocalBuilder.get();
                sb.setLength(0); // Clear any previous content

                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    sb.append("Thread-").append(threadId).append("-Item-").append(j).append(" ");
                }

                results[threadId] = sb.toString();
                threadLocalBuilder.remove(); // Clean up ThreadLocal
            });
            threads[i].start();
        }

        // Wait for completion
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("ThreadLocal StringBuilder approach:");
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("All threads completed successfully: " +
                          Arrays.stream(results).allMatch(Objects::nonNull));

        // Show sample results
        for (int i = 0; i < Math.min(results.length, 2); i++) {
            String sample = results[i].length() > 100 ?
                           results[i].substring(0, 100) + "..." : results[i];
            System.out.println("Thread " + i + " result sample: " + sample);
        }
    }

    // Best practices demonstration
    public static void demonstrateBestPractices() {
        System.out.println("\n=== Best Practices ===");

        // 1. Use StringBuilder for single-threaded scenarios
        demonstrateSingleThreadedBestPractice();

        // 2. Use StringBuffer for shared mutable strings
        demonstrateSharedMutableStringBestPractice();

        // 3. Use immutable strings when possible
        demonstrateImmutableStringBestPractice();

        // 4. Use specific capacity to avoid resizing
        demonstrateCapacityBestPractice();
    }

    private static void demonstrateSingleThreadedBestPractice() {
        System.out.println("\n--- Single-threaded: Use StringBuilder ---");

        // Building SQL query example
        var query = new StringBuilder(200); // Pre-size for better performance
        query.append("SELECT u.name, u.email, p.title ")
             .append("FROM users u ")
             .append("JOIN posts p ON u.id = p.user_id ")
             .append("WHERE u.active = true ")
             .append("AND p.published_date > ? ")
             .append("ORDER BY p.published_date DESC");

        System.out.println("Built query: " + query.toString());
    }

    private static void demonstrateSharedMutableStringBestPractice() {
        System.out.println("\n--- Shared mutable: Use StringBuffer ---");

        var sharedLog = new StringBuffer();

        // Simulate multiple threads writing to shared log
        var logWriter1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                sharedLog.append("[Thread-1] Log entry ").append(i).append("\n");
                try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });

        var logWriter2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                sharedLog.append("[Thread-2] Log entry ").append(i).append("\n");
                try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });

        logWriter1.start();
        logWriter2.start();

        try {
            logWriter1.join();
            logWriter2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Shared log content:\n" + sharedLog.toString());
    }

    private static void demonstrateImmutableStringBestPractice() {
        System.out.println("\n--- Immutable when possible: Use String ---");

        // When strings don't change, use String for thread safety
        final String CONFIG_MESSAGE = "Application started successfully";
        final String ERROR_PREFIX = "[ERROR] ";
        final String INFO_PREFIX = "[INFO] ";

        // Thread-safe access to immutable strings
        Runnable logTask = () -> {
            String threadName = Thread.currentThread().getName();
            System.out.println(INFO_PREFIX + threadName + ": " + CONFIG_MESSAGE);
        };

        var t1 = new Thread(logTask, "Logger-1");
        var t2 = new Thread(logTask, "Logger-2");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void demonstrateCapacityBestPractice() {
        System.out.println("\n--- Performance: Pre-size builders ---");

        final int EXPECTED_SIZE = 10000;

        // Without initial capacity (will resize multiple times)
        long startTime = System.nanoTime();
        var builderNoCapacity = new StringBuilder();
        for (int i = 0; i < EXPECTED_SIZE; i++) {
            builderNoCapacity.append("X");
        }
        long timeNoCapacity = System.nanoTime() - startTime;

        // With initial capacity (no resizing needed)
        startTime = System.nanoTime();
        var builderWithCapacity = new StringBuilder(EXPECTED_SIZE);
        for (int i = 0; i < EXPECTED_SIZE; i++) {
            builderWithCapacity.append("X");
        }
        long timeWithCapacity = System.nanoTime() - startTime;

        System.out.println("Without capacity: " + timeNoCapacity / 1_000_000.0 + " ms");
        System.out.println("With capacity: " + timeWithCapacity / 1_000_000.0 + " ms");
        System.out.println("Improvement: " + String.format("%.2f", (double) timeNoCapacity / timeWithCapacity) + "x faster");
    }

    // Utility methods
    private static long getUsedMemory() {
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }

    public static void main(String[] args) {
        demonstrateBasicDifferences();
        demonstrateThreadLocalAlternative();
        demonstrateBestPractices();
    }
}
```

### Q3: When should you use different utility classes for thread safety?

*📋 **Note:** For detailed comparisons of thread-safe vs non-thread-safe collections (ArrayList vs Vector, HashMap vs ConcurrentHashMap, etc.), see: `JAVA_COLLECTIONS_STREAMS.md` and `JAVA_CONCURRENCY_COMPREHENSIVE.md`*

**Answer:** Choose utility classes based on thread safety requirements:

```java
// Quick reference for thread-safe alternatives
public class ThreadSafeAlternatives {

    // Use concurrent collections when needed
    private final Map<String, String> threadSafeMap = new ConcurrentHashMap<>();
    private final List<String> threadSafeList = new CopyOnWriteArrayList<>();

    // Use atomic classes for simple values
    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicReference<String> state = new AtomicReference<>();

    // Use ThreadLocal for per-thread instances
    private final ThreadLocal<StringBuilder> localBuilder =
        ThreadLocal.withInitial(StringBuilder::new);
}
```

**Best Practice Guidelines:**
- Use `ConcurrentHashMap` instead of `HashMap` for concurrent access
- Use `CopyOnWriteArrayList` instead of `ArrayList` for read-heavy scenarios
- Use `AtomicXxx` classes instead of primitive wrappers with synchronization
- Use `ThreadLocal` for per-thread state instead of shared mutable state


### Q4: Compare Date vs LocalDateTime and other thread-safety considerations

**Answer:** Date is mutable and not thread-safe, while LocalDateTime is immutable and thread-safe:

```java
public class DateThreadSafetyDemo {

    public static void demonstrateDateVsLocalDateTime() {
        System.out.println("=== Date vs LocalDateTime Thread Safety ===");

        // Date thread safety issues
        demonstrateDateThreadSafetyIssues();

        // LocalDateTime thread safety
        demonstrateLocalDateTimeThreadSafety();

        // Other thread-safe alternatives
        demonstrateThreadSafeAlternatives();
    }

    @SuppressWarnings("deprecation")
    private static void demonstrateDateThreadSafetyIssues() {
        System.out.println("\n--- Date Thread Safety Issues ---");

        // Shared mutable Date object
        var sharedDate = new Date();
        final int THREAD_COUNT = 10;
        final int OPERATIONS_PER_THREAD = 100;

        System.out.println("Initial date: " + sharedDate);

        var threads = new Thread[THREAD_COUNT];
        long startTime = System.currentTimeMillis();

        // Multiple threads modifying the same Date object
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    // Date methods are not synchronized
                    sharedDate.setTime(System.currentTimeMillis() + threadId * 1000 + j);
                    sharedDate.setYear(120 + threadId); // Year 2020 + threadId
                    sharedDate.setMonth(threadId % 12);
                    sharedDate.setDate(j % 28 + 1);
                }
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Final date (unpredictable): " + sharedDate);
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Result is unpredictable due to race conditions");
    }

    private static void demonstrateLocalDateTimeThreadSafety() {
        System.out.println("\n--- LocalDateTime Thread Safety ---");

        // LocalDateTime is immutable
        final LocalDateTime baseDateTime = LocalDateTime.now();
        final int THREAD_COUNT = 10;
        final int OPERATIONS_PER_THREAD = 100;

        System.out.println("Base LocalDateTime: " + baseDateTime);

        var results = new LocalDateTime[THREAD_COUNT];
        var threads = new Thread[THREAD_COUNT];
        long startTime = System.currentTimeMillis();

        // Multiple threads creating new LocalDateTime objects
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                LocalDateTime current = baseDateTime;
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    // Each operation returns a new immutable object
                    current = current.plusDays(1)
                                   .plusHours(threadId)
                                   .plusMinutes(j);
                }
                results[threadId] = current;
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Results (predictable and thread-safe):");
        for (int i = 0; i < Math.min(results.length, 3); i++) {
            System.out.println("Thread " + i + ": " + results[i]);
        }
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("All results are predictable and consistent");
    }

    private static void demonstrateThreadSafeAlternatives() {
        System.out.println("\n--- Thread-Safe Alternatives ---");

        // 1. AtomicReference for mutable shared state
        demonstrateAtomicReference();

        // 2. ThreadLocal for per-thread state
        demonstrateThreadLocalDate();

        // 3. Synchronized access to mutable objects
        demonstrateSynchronizedDateAccess();
    }

    private static void demonstrateAtomicReference() {
        System.out.println("\nAtomicReference for thread-safe updates:");

        var atomicDateTime = new AtomicReference<>(LocalDateTime.now());
        final int THREAD_COUNT = 5;

        var threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                // Atomic update operation
                atomicDateTime.updateAndGet(current ->
                    current.plusHours(threadId).plusMinutes(threadId * 10));

                System.out.println("Thread " + threadId + " updated to: " +
                                 atomicDateTime.get());
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Final atomic value: " + atomicDateTime.get());
    }

    private static void demonstrateThreadLocalDate() {
        System.out.println("\nThreadLocal for per-thread date state:");

        var threadLocalDate = ThreadLocal.withInitial(LocalDateTime::now);
        final int THREAD_COUNT = 5;

        var threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                // Each thread has its own copy
                LocalDateTime threadDate = threadLocalDate.get();
                threadDate = threadDate.plusDays(threadId);
                threadLocalDate.set(threadDate);

                System.out.println("Thread " + threadId + " date: " +
                                 threadLocalDate.get());

                // Clean up
                threadLocalDate.remove();
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void demonstrateSynchronizedDateAccess() {
        System.out.println("\nSynchronized access to mutable Date:");

        var sharedDate = new Date();
        final Object lock = new Object();
        final int THREAD_COUNT = 5;

        var threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                synchronized (lock) {
                    // Thread-safe access through synchronization
                    long currentTime = sharedDate.getTime();
                    sharedDate.setTime(currentTime + threadId * 1000);
                    System.out.println("Thread " + threadId + " set date to: " + sharedDate);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Final synchronized date: " + sharedDate);
    }

    // Random vs ThreadLocalRandom demonstration
    public static void demonstrateRandomThreadSafety() {
        System.out.println("\n=== Random vs ThreadLocalRandom ===");

        final int THREAD_COUNT = 10;
        final int OPERATIONS_PER_THREAD = 100_000;

        // Shared Random (has internal synchronization)
        testRandomPerformance("Shared Random", new Random(), THREAD_COUNT, OPERATIONS_PER_THREAD);

        // ThreadLocalRandom (no synchronization needed)
        testThreadLocalRandomPerformance("ThreadLocalRandom", THREAD_COUNT, OPERATIONS_PER_THREAD);
    }

    private static void testRandomPerformance(String type, Random random,
                                            int threadCount, int operationsPerThread) {
        System.out.println("\nTesting: " + type);

        var threads = new Thread[threadCount];
        var results = new long[threadCount];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                long sum = 0;
                for (int j = 0; j < operationsPerThread; j++) {
                    sum += random.nextInt(100);
                }
                results[threadId] = sum;
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();
        long totalSum = Arrays.stream(results).sum();

        System.out.println("Total operations: " + (threadCount * operationsPerThread));
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Sum verification: " + totalSum);
    }

    private static void testThreadLocalRandomPerformance(String type,
                                                       int threadCount, int operationsPerThread) {
        System.out.println("\nTesting: " + type);

        var threads = new Thread[threadCount];
        var results = new long[threadCount];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                long sum = 0;
                for (int j = 0; j < operationsPerThread; j++) {
                    sum += ThreadLocalRandom.current().nextInt(100);
                }
                results[threadId] = sum;
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();
        long totalSum = Arrays.stream(results).sum();

        System.out.println("Total operations: " + (threadCount * operationsPerThread));
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Sum verification: " + totalSum);
    }

    public static void main(String[] args) {
        demonstrateDateVsLocalDateTime();
        demonstrateRandomThreadSafety();
    }
}
```

## **Thread Safety Quick Reference**

### **Thread-Safe vs Non-Thread-Safe Classes**

| Category | Non-Thread-Safe | Thread-Safe Alternative | Performance Impact |
|----------|-----------------|------------------------|-------------------|
| **String Building** | StringBuilder | StringBuffer | ~20-30% slower |
| **Lists** | ArrayList | Vector, Collections.synchronizedList(), CopyOnWriteArrayList | Varies |
| **Maps** | HashMap | Hashtable, Collections.synchronizedMap(), ConcurrentHashMap | ConcurrentHashMap is fastest |
| **Sets** | HashSet | Collections.synchronizedSet(), ConcurrentSkipListSet | Depends on usage pattern |
| **Dates** | Date, Calendar | LocalDateTime, ZonedDateTime (immutable) | No performance penalty |
| **Random** | Random (shared) | ThreadLocalRandom | Significantly faster |

### **Best Practices Summary**

1. **Use StringBuilder for single-threaded scenarios**
2. **Use StringBuffer only when sharing across threads**
3. **Prefer ConcurrentHashMap over synchronized collections**
4. **Use immutable objects (LocalDateTime) when possible**
5. **Use ThreadLocalRandom instead of shared Random**
6. **Consider ThreadLocal for per-thread state**
7. **Use atomic classes for simple shared counters**

This guide provides practical examples for writing thread-safe code and understanding the performance trade-offs involved.

*[Continue with remaining sections covering synchronization mechanisms, performance optimizations, and common pitfalls]*