package com.interview;

/**
 * Helper class for running concurrency examples from command line
 *
 * Usage examples:
 *
 * # Build and run with Gradle:
 * ./gradlew build
 * ./gradlew run
 *
 * # Run specific examples:
 * ./gradlew runThreadCreation
 * ./gradlew runThreadSafeCounters
 * ./gradlew runProducerConsumer
 * ./gradlew runDeadlockExamples
 * ./gradlew runExecutorService
 * ./gradlew runAdvancedExecutors
 * ./gradlew runCommandPattern
 * ./gradlew runConcurrentCollections
 * ./gradlew runSynchronizationUtilities
 *
 * # Run interactive menu:
 * ./gradlew runInteractiveMenu
 *
 * # Run by example name:
 * ./gradlew runExample -Pexample=threads
 * ./gradlew runExample -Pexample=executors
 * ./gradlew runExample -Pexample=menu
 *
 * # List all available examples:
 * ./gradlew listExamples
 *
 */
public class ConcurrencyExamplesHelper {

    public static void main(String[] args) {
        System.out.println("=== Java Concurrency Examples Helper ===\n");

        System.out.println("Available Examples:");
        System.out.println("==================");

        System.out.println("\n1. THREAD BASICS:");
        System.out.println("   ./gradlew runThreadCreation");

        System.out.println("\n2. SYNCHRONIZATION:");
        System.out.println("   ./gradlew runThreadSafeCounters");

        System.out.println("\n3. PATTERNS:");
        System.out.println("   ./gradlew runProducerConsumer");
        System.out.println("   ./gradlew runDeadlockExamples");
        System.out.println("   ./gradlew runCommandPattern");

        System.out.println("\n4. EXECUTORS:");
        System.out.println("   ./gradlew runExecutorService");
        System.out.println("   ./gradlew runAdvancedExecutors");

        System.out.println("\n5. CONCURRENT COLLECTIONS:");
        System.out.println("   ./gradlew runConcurrentCollections");

        System.out.println("\n6. SYNCHRONIZATION UTILITIES:");
        System.out.println("   ./gradlew runSynchronizationUtilities");

        System.out.println("\n7. INTERACTIVE MENU:");
        System.out.println("   ./gradlew runInteractiveMenu");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("QUICK START COMMANDS:");
        System.out.println("=".repeat(70));

        System.out.println("\n# Build the project:");
        System.out.println("./gradlew build");

        System.out.println("\n# Run interactive menu:");
        System.out.println("./gradlew runInteractiveMenu");

        System.out.println("\n# Run default application:");
        System.out.println("./gradlew run");

        System.out.println("\n# Run specific example:");
        System.out.println("./gradlew runAdvancedExecutors");

        System.out.println("\n# Run by name:");
        System.out.println("./gradlew runExample -Pexample=threads");

        System.out.println("\n# List all examples:");
        System.out.println("./gradlew listExamples");

        if (args.length > 0) {
            String example = args[0].toLowerCase();
            System.out.println("\nRunning example: " + example);

            try {
                switch (example) {
                    case "threads":
                        com.interview.concurrency.basics.ThreadCreation.main(new String[]{});
                        break;
                    case "counters":
                        com.interview.concurrency.synchronization.ThreadSafeCounter.main(new String[]{});
                        break;
                    case "producer-consumer":
                        com.interview.concurrency.patterns.ProducerConsumer.main(new String[]{});
                        break;
                    case "deadlock":
                        com.interview.concurrency.patterns.DeadlockExample.main(new String[]{});
                        break;
                    case "executors":
                        com.interview.concurrency.executors.ExecutorServiceExamples.main(new String[]{});
                        break;
                    case "advanced-executors":
                        com.interview.concurrency.executors.AdvancedExecutorExamples.main(new String[]{});
                        break;
                    case "command-pattern":
                        com.interview.concurrency.patterns.CommandPattern.main(new String[]{});
                        break;
                    case "collections":
                        com.interview.concurrency.utilities.ConcurrentCollections.main(new String[]{});
                        break;
                    case "synchronization":
                        com.interview.concurrency.utilities.SynchronizationUtilities.main(new String[]{});
                        break;
                    case "menu":
                        com.interview.concurrency.ConcurrencyRunner.main(new String[]{});
                        break;
                    default:
                        System.out.println("Unknown example: " + example);
                        System.out.println("Available: threads, counters, producer-consumer, deadlock, executors, advanced-executors, command-pattern, collections, synchronization, menu");
                }
            } catch (Exception e) {
                System.err.println("Error running example: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}