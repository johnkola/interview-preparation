package com.interview.concurrency;

import com.interview.concurrency.basics.ThreadCreation;
import com.interview.concurrency.executors.AdvancedExecutorExamples;
import com.interview.concurrency.executors.ExecutorServiceExamples;
import com.interview.concurrency.patterns.CommandPattern;
import com.interview.concurrency.patterns.DeadlockExample;
import com.interview.concurrency.patterns.ProducerConsumer;
import com.interview.concurrency.synchronization.ThreadSafeCounter;
import com.interview.concurrency.utilities.ConcurrentCollections;
import com.interview.concurrency.utilities.SynchronizationUtilities;

import java.util.Scanner;

/**
 * ConcurrencyRunner - Interactive CLI for Java Concurrency Examples
 *
 * This class provides an interactive command-line interface for running and exploring
 * various Java concurrency examples. It serves as a comprehensive learning tool for
 * understanding multi-threading, synchronization, executors, and concurrent collections.
 *
 * The runner organizes concurrency examples into categories:
 * - Basic Threading: Thread creation, lifecycle, and states
 * - Synchronization: Thread-safe counters and synchronization techniques
 * - Design Patterns: Producer-consumer, deadlock scenarios, command pattern
 * - Executors: Basic and advanced executor service usage
 * - Concurrent Collections: Thread-safe collection implementations
 * - Synchronization Utilities: Locks, barriers, semaphores, and more
 *
 * Features:
 * - Interactive menu-driven interface
 * - Individual example execution
 * - Batch execution of all examples
 * - Error handling and recovery
 * - Programmatic API for automated testing
 *
 * Usage patterns:
 * 1. Interactive mode: Run main() and follow menu prompts
 * 2. Programmatic mode: Use runExample() method from other classes
 * 3. Batch mode: Select "Run ALL Examples" from menu
 *
 * Educational benefits:
 * - Progressive learning path from basics to advanced topics
 * - Real-time output observation
 * - Safe environment for experimenting with concurrency
 * - Comprehensive coverage of Java concurrency features
 *
 * Interview preparation:
 * - Covers common concurrency interview topics
 * - Demonstrates best practices and patterns
 * - Shows potential pitfalls and solutions
 * - Provides hands-on experience with concurrent programming
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class ConcurrencyRunner {

    /**
     * Main entry point for the interactive concurrency examples runner
     *
     * Provides a menu-driven interface for exploring Java concurrency examples.
     * Users can select individual examples, run specific categories, or execute
     * all examples in sequence. The interface handles user input, error recovery,
     * and provides a controlled environment for learning concurrency concepts.
     *
     * Program flow:
     * 1. Display welcome message and menu
     * 2. Wait for user input
     * 3. Execute selected example(s)
     * 4. Return to menu for next selection
     * 5. Continue until user exits
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== Java Concurrency Examples Runner ===");
        System.out.println("Choose an example to run:");

        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-12, or 0 to exit): ");

            try {
                int choice = scanner.nextInt();

                // Process user selection
                switch (choice) {
                    case 0:
                        // Exit the program
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    case 1:
                        runThreadCreationExamples();
                        break;
                    case 2:
                        runThreadSafeCounterExamples();
                        break;
                    case 3:
                        runProducerConsumerExamples();
                        break;
                    case 4:
                        runDeadlockExamples();
                        break;
                    case 5:
                        runExecutorServiceExamples();
                        break;
                    case 6:
                        runAdvancedExecutorExamples();
                        break;
                    case 7:
                        runCommandPatternExamples();
                        break;
                    case 8:
                        runConcurrentCollectionsExamples();
                        break;
                    case 9:
                        runSynchronizationUtilitiesExamples();
                        break;
                    case 10:
                        runSpecificThreadExample(scanner);
                        break;
                    case 11:
                        runSpecificExecutorExample(scanner);
                        break;
                    case 12:
                        runAllExamples();
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }

                // Pause between examples for better readability
                if (running) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine(); // consume newline from previous input
                    scanner.nextLine(); // wait for user to press Enter
                }

            } catch (Exception e) {
                // Handle invalid input and other errors gracefully
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine(); // consume invalid input to prevent infinite loop
            }
        }

        scanner.close();
    }

    /**
     * Displays the interactive menu with all available example categories
     *
     * The menu is organized from basic to advanced topics, providing a
     * structured learning path for Java concurrency. Each option corresponds
     * to a specific concurrency concept or pattern.
     *
     * Menu organization:
     * - Basic Threading (1-2): Foundation concepts
     * - Patterns (3-4): Common concurrency patterns and problems
     * - Executors (5-7): Task execution frameworks
     * - Collections & Utilities (8-9): Concurrent data structures and tools
     * - Specialized Options (10-11): Fine-grained example selection
     * - Batch Execution (12): Run all examples in sequence
     */
    private static void printMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("CONCURRENCY EXAMPLES MENU");
        System.out.println("=".repeat(50));
        System.out.println("1.  Thread Creation & Lifecycle");
        System.out.println("2.  Thread-Safe Counters");
        System.out.println("3.  Producer-Consumer Pattern");
        System.out.println("4.  Deadlock Examples & Prevention");
        System.out.println("5.  Basic Executor Service");
        System.out.println("6.  Advanced Executor & Futures");
        System.out.println("7.  Command Pattern with Executors");
        System.out.println("8.  Concurrent Collections");
        System.out.println("9.  Synchronization Utilities");
        System.out.println("10. Specific Thread Examples");
        System.out.println("11. Specific Executor Examples");
        System.out.println("12. Run ALL Examples");
        System.out.println("0.  Exit");
        System.out.println("=".repeat(50));
    }

    /**
     * Executes thread creation and lifecycle examples
     *
     * Demonstrates:
     * - Different ways to create threads (Thread class vs Runnable)
     * - Thread lifecycle and state transitions
     * - Basic thread control methods (start, join, sleep)
     * - Thread naming and identification
     */
    private static void runThreadCreationExamples() {
        try {
            System.out.println("\n>>> Running Thread Creation Examples <<<");
            ThreadCreation.demonstrateThreadCreation();
            System.out.println();
            ThreadCreation.demonstrateThreadStates();
        } catch (Exception e) {
            System.err.println("Error running thread creation examples: " + e.getMessage());
        }
    }

    /**
     * Executes thread-safe counter implementation examples
     *
     * Demonstrates:
     * - Race conditions with unsynchronized access
     * - Synchronized methods and blocks
     * - Volatile variables
     * - Atomic variables (AtomicInteger)
     * - Lock-based synchronization
     */
    private static void runThreadSafeCounterExamples() {
        try {
            System.out.println("\n>>> Running Thread-Safe Counter Examples <<<");
            ThreadSafeCounter.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Error running thread-safe counter examples: " + e.getMessage());
        }
    }

    /**
     * Executes producer-consumer pattern examples
     *
     * Demonstrates:
     * - Classic producer-consumer problem
     * - Wait/notify mechanism
     * - BlockingQueue implementation
     * - Lock and Condition variables
     * - Buffer management and coordination
     */
    private static void runProducerConsumerExamples() {
        try {
            System.out.println("\n>>> Running Producer-Consumer Examples <<<");
            ProducerConsumer.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Error running producer-consumer examples: " + e.getMessage());
        }
    }

    /**
     * Executes deadlock scenario and prevention examples
     *
     * Demonstrates:
     * - Classic deadlock scenarios
     * - Circular wait conditions
     * - Lock ordering strategies
     * - Deadlock detection techniques
     * - Prevention strategies
     *
     * WARNING: May create actual deadlocks for demonstration purposes
     */
    private static void runDeadlockExamples() {
        try {
            System.out.println("\n>>> Running Deadlock Examples <<<");
            DeadlockExample.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Error running deadlock examples: " + e.getMessage());
        }
    }

    /**
     * Executes basic executor service examples
     *
     * Demonstrates:
     * - FixedThreadPool, CachedThreadPool, SingleThreadExecutor
     * - ScheduledExecutorService for delayed/periodic tasks
     * - Callable and Future for result retrieval
     * - CompletableFuture basics
     * - ForkJoinPool for recursive tasks
     * - Custom ThreadPoolExecutor configuration
     */
    private static void runExecutorServiceExamples() {
        try {
            System.out.println("\n>>> Running Basic Executor Service Examples <<<");
            ExecutorServiceExamples.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Error running executor service examples: " + e.getMessage());
        }
    }

    /**
     * Executes advanced executor and asynchronous programming examples
     *
     * Demonstrates:
     * - Future-based callbacks
     * - CompletableFuture chaining and composition
     * - Async callbacks and reactive patterns
     * - Custom executor with monitoring
     * - ExecutorCompletionService
     * - Timeout handling and cancellation
     */
    private static void runAdvancedExecutorExamples() {
        try {
            System.out.println("\n>>> Running Advanced Executor Examples <<<");
            AdvancedExecutorExamples.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Error running advanced executor examples: " + e.getMessage());
        }
    }

    /**
     * Executes command pattern with concurrent execution examples
     *
     * Demonstrates:
     * - Command pattern implementation
     * - Asynchronous command execution
     * - Command queuing and scheduling
     * - Retry and error handling
     * - Batch command processing
     */
    private static void runCommandPatternExamples() {
        try {
            System.out.println("\n>>> Running Command Pattern Examples <<<");
            CommandPattern.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Error running command pattern examples: " + e.getMessage());
        }
    }

    /**
     * Executes concurrent collection implementation examples
     *
     * Demonstrates:
     * - ConcurrentHashMap and its atomic operations
     * - CopyOnWriteArrayList for read-heavy scenarios
     * - ConcurrentLinkedQueue lock-free operations
     * - Various BlockingQueue implementations
     * - ConcurrentSkipListMap for sorted concurrent access
     * - Performance comparisons
     */
    private static void runConcurrentCollectionsExamples() {
        try {
            System.out.println("\n>>> Running Concurrent Collections Examples <<<");
            ConcurrentCollections.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Error running concurrent collections examples: " + e.getMessage());
        }
    }

    /**
     * Executes synchronization utility examples
     *
     * Demonstrates:
     * - CountDownLatch for one-time synchronization
     * - CyclicBarrier for reusable barriers
     * - Semaphore for resource access control
     * - Phaser for multi-phase synchronization
     * - Exchanger for data exchange
     * - ReadWriteLock and StampedLock
     * - Atomic variables and lock-free operations
     */
    private static void runSynchronizationUtilitiesExamples() {
        try {
            System.out.println("\n>>> Running Synchronization Utilities Examples <<<");
            SynchronizationUtilities.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Error running synchronization utilities examples: " + e.getMessage());
        }
    }

    /**
     * Provides submenu for specific thread-related examples
     *
     * Allows users to select individual thread demonstrations without
     * running the entire thread creation suite. Useful for focused
     * learning or debugging specific thread concepts.
     *
     * Options:
     * 1. Thread Creation Demo - Different ways to create threads
     * 2. Thread States Demo - Thread lifecycle and state transitions
     *
     * @param scanner Scanner instance for user input
     */
    private static void runSpecificThreadExample(Scanner scanner) {
        System.out.println("\nSpecific Thread Examples:");
        System.out.println("1. Thread Creation Demo");
        System.out.println("2. Thread States Demo");
        System.out.print("Choose (1-2): ");

        try {
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    ThreadCreation.demonstrateThreadCreation();
                    break;
                case 2:
                    ThreadCreation.demonstrateThreadStates();
                    break;
                default:
                    System.out.println("Invalid choice");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Provides submenu for specific executor-related examples
     *
     * Allows users to select individual executor demonstrations for
     * targeted learning of specific asynchronous programming concepts.
     * Each option demonstrates a different aspect of executor usage.
     *
     * Options:
     * 1. Future with Callbacks - Callback-style programming
     * 2. CompletableFuture Chaining - Pipeline processing
     * 3. CompletableFuture Combinations - Combining multiple futures
     * 4. Async Callbacks - Non-blocking callback patterns
     * 5. Custom Executor - Extending ThreadPoolExecutor
     * 6. ExecutorCompletionService - Processing results as available
     * 7. Future with Timeout - Handling timeouts and cancellation
     * 8. Reactive Pattern - Reactive programming with CompletableFuture
     *
     * @param scanner Scanner instance for user input
     */
    private static void runSpecificExecutorExample(Scanner scanner) {
        System.out.println("\nSpecific Executor Examples:");
        System.out.println("1. Future with Callbacks");
        System.out.println("2. CompletableFuture Chaining");
        System.out.println("3. CompletableFuture Combinations");
        System.out.println("4. Async Callbacks");
        System.out.println("5. Custom Executor");
        System.out.println("6. ExecutorCompletionService");
        System.out.println("7. Future with Timeout");
        System.out.println("8. Reactive Pattern");
        System.out.print("Choose (1-8): ");

        try {
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    AdvancedExecutorExamples.demonstrateFutureCallbacks();
                    break;
                case 2:
                    AdvancedExecutorExamples.demonstrateCompletableFutureChaining();
                    break;
                case 3:
                    AdvancedExecutorExamples.demonstrateCompletableFutureCombinations();
                    break;
                case 4:
                    AdvancedExecutorExamples.demonstrateAsyncCallbacks();
                    break;
                case 5:
                    AdvancedExecutorExamples.demonstrateCustomExecutor();
                    break;
                case 6:
                    AdvancedExecutorExamples.demonstrateExecutorCompletionService();
                    break;
                case 7:
                    AdvancedExecutorExamples.demonstrateFutureTimeout();
                    break;
                case 8:
                    AdvancedExecutorExamples.demonstrateReactivePattern();
                    break;
                default:
                    System.out.println("Invalid choice");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Executes all concurrency examples in sequence
     *
     * Runs through the complete suite of concurrency demonstrations,
     * providing a comprehensive overview of Java concurrency features.
     * Includes pauses between examples for better readability.
     *
     * Execution order:
     * 1. Thread creation and lifecycle
     * 2. Thread-safe counters
     * 3. Producer-consumer pattern
     * 4. Executor services (basic and advanced)
     * 5. Command pattern
     * 6. Concurrent collections
     * 7. Synchronization utilities
     *
     * NOTE: Deadlock examples are skipped in batch mode to prevent hanging
     *
     * Total execution time: Approximately 3-5 minutes
     */
    private static void runAllExamples() {
        System.out.println("\n>>> Running ALL Concurrency Examples <<<");
        System.out.println("This will take several minutes...\n");

        try {
            runThreadCreationExamples();
            Thread.sleep(2000);  // Pause between example categories

            runThreadSafeCounterExamples();
            Thread.sleep(2000);

            runProducerConsumerExamples();
            Thread.sleep(2000);

            runExecutorServiceExamples();
            Thread.sleep(2000);

            runAdvancedExecutorExamples();
            Thread.sleep(2000);

            runCommandPatternExamples();
            Thread.sleep(2000);

            runConcurrentCollectionsExamples();
            Thread.sleep(2000);

            runSynchronizationUtilitiesExamples();
            Thread.sleep(2000);

            // Skip deadlock examples in batch mode to avoid hanging
            // Deadlocks would block the entire program execution
            System.out.println(">>> Skipping Deadlock Examples in 'Run All' mode <<<");
            System.out.println(">>> (Run them separately to see deadlock behavior) <<<");

            System.out.println("\n>>> ALL EXAMPLES COMPLETED <<<");

        } catch (Exception e) {
            System.err.println("Error running all examples: " + e.getMessage());
        }
    }

    /**
     * Programmatic API for running specific examples by name
     *
     * Provides a way to execute concurrency examples programmatically
     * from other classes or test suites. Useful for automated testing,
     * integration with build tools, or embedding in educational frameworks.
     *
     * Supported example names:
     * - "threads" or "thread-creation": Thread creation examples
     * - "counters" or "thread-safe-counters": Thread-safe counter examples
     * - "producer-consumer": Producer-consumer pattern
     * - "deadlock": Deadlock scenarios
     * - "executors" or "executor-service": Basic executor examples
     * - "advanced-executors": Advanced executor patterns
     * - "command-pattern": Command pattern implementation
     * - "collections" or "concurrent-collections": Concurrent collections
     * - "synchronization" or "sync-utilities": Synchronization utilities
     *
     * @param exampleName the name of the example to run (case-insensitive)
     */
    public static void runExample(String exampleName) {
        try {
            switch (exampleName.toLowerCase()) {
                case "threads":
                case "thread-creation":
                    runThreadCreationExamples();
                    break;
                case "counters":
                case "thread-safe-counters":
                    runThreadSafeCounterExamples();
                    break;
                case "producer-consumer":
                    runProducerConsumerExamples();
                    break;
                case "deadlock":
                    runDeadlockExamples();
                    break;
                case "executors":
                case "executor-service":
                    runExecutorServiceExamples();
                    break;
                case "advanced-executors":
                    runAdvancedExecutorExamples();
                    break;
                case "command-pattern":
                    runCommandPatternExamples();
                    break;
                case "collections":
                case "concurrent-collections":
                    runConcurrentCollectionsExamples();
                    break;
                case "synchronization":
                case "sync-utilities":
                    runSynchronizationUtilitiesExamples();
                    break;
                default:
                    // Provide helpful error message with available options
                    System.out.println("Unknown example: " + exampleName);
                    System.out.println("Available examples: threads, counters, producer-consumer, deadlock, executors, advanced-executors, command-pattern, collections, synchronization");
            }
        } catch (Exception e) {
            System.err.println("Error running example '" + exampleName + "': " + e.getMessage());
        }
    }
}