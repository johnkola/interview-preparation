package com.interview.concurrency.executors;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Advanced Executor Examples with Futures, Callbacks, and Asynchronous Programming
 *
 * This class demonstrates sophisticated concurrent programming patterns using executors,
 * futures, and modern asynchronous programming techniques. These examples showcase
 * real-world patterns commonly used in enterprise applications and are frequently
 * discussed in senior-level technical interviews.
 *
 * Topics covered:
 * - Future-based callback mechanisms
 * - CompletableFuture chaining and composition
 * - Asynchronous pipeline processing
 * - Error handling in async operations
 * - Custom executor implementations with monitoring
 * - Reactive programming patterns
 *
 * Key concepts:
 * - Non-blocking programming
 * - Callback-driven architecture
 * - Future composition and transformation
 * - Exception propagation in async code
 * - Performance monitoring and metrics
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class AdvancedExecutorExamples {

    /**
     * Demonstrates callback-style programming using Future and ScheduledExecutorService
     *
     * This method shows how to implement callback mechanisms when CompletableFuture
     * is not available or when you need more control over the callback timing.
     * It uses polling to check Future completion status.
     *
     * Key concepts demonstrated:
     * - Future.isDone() for non-blocking status checking
     * - ScheduledExecutorService for periodic polling
     * - Callback execution upon task completion
     * - Graceful shutdown of multiple executors
     *
     * Pattern explanation:
     * 1. Submit long-running task to executor
     * 2. Set up periodic checker using scheduler
     * 3. Poll future status until completion
     * 4. Execute callback when task is done
     * 5. Clean up resources
     *
     * Pros:
     * - Works with older Java versions (pre-CompletableFuture)
     * - Fine-grained control over polling frequency
     * - Can handle multiple different callback types
     *
     * Cons:
     * - Polling overhead (CPU cycles wasted on checking)
     * - More complex setup compared to CompletableFuture
     * - Potential resource leaks if not cleaned up properly
     *
     * Real-world usage:
     * - Legacy systems integration
     * - Custom callback frameworks
     * - Fine-tuned performance monitoring
     *
     * @throws Exception if task execution fails
     */
    public static void demonstrateFutureCallbacks() throws Exception {
        System.out.println("\n=== Future with Callbacks ===");

        // Create executor for main task
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit a long-running task that returns a result
        Future<String> future = executor.submit(() -> {
            Thread.sleep(2000);  // Simulate long-running operation
            return "Task completed successfully!";
        });

        System.out.println("Task submitted, doing other work...");

        // Create scheduler for periodic callback checking
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Poll the future every 500ms and execute callback when done
        scheduler.scheduleAtFixedRate(() -> {
            if (future.isDone()) {
                try {
                    // Execute callback - retrieve and process result
                    System.out.println("Callback: " + future.get());
                } catch (Exception e) {
                    // Handle callback errors gracefully
                    System.out.println("Callback error: " + e.getMessage());
                }
                // Stop polling once task is complete
                scheduler.shutdown();
            } else {
                // Continue polling - show progress
                System.out.println("Still waiting for result...");
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

        // Main thread can also wait for result
        String result = future.get();
        System.out.println("Main thread received: " + result);

        // Clean up resources
        executor.shutdown();
        scheduler.shutdown();
    }

    /**
     * Demonstrates advanced CompletableFuture chaining and transformation
     *
     * This method showcases the power of CompletableFuture for building
     * asynchronous processing pipelines. It demonstrates method chaining,
     * transformation, error handling, and completion callbacks.
     *
     * Pipeline stages demonstrated:
     * 1. supplyAsync() - Start async computation
     * 2. thenApplyAsync() - Transform result asynchronously
     * 3. Multiple chained transformations
     * 4. whenComplete() - Handle success/failure
     * 5. exceptionally() - Error recovery
     *
     * Key CompletableFuture methods:
     * - supplyAsync(): Start async computation with result
     * - thenApplyAsync(): Transform result asynchronously
     * - whenComplete(): Handle both success and failure cases
     * - exceptionally(): Provide fallback for errors
     *
     * Benefits of this pattern:
     * - Fluent, readable API for complex async workflows
     * - Automatic thread management
     * - Built-in error handling and propagation
     * - Composable and reusable pipeline stages
     *
     * Real-world applications:
     * - User data processing pipelines
     * - API request/response transformation
     * - Multi-stage data validation and enrichment
     * - Microservice orchestration
     *
     * @throws Exception if any stage of the pipeline fails
     */
    public static void demonstrateCompletableFutureChaining() throws Exception {
        System.out.println("\n=== CompletableFuture Advanced Chaining ===");

        // Build an asynchronous processing pipeline
        CompletableFuture<String> future = CompletableFuture
            // Stage 1: Initial data fetching
            .supplyAsync(() -> {
                System.out.println("Step 1: Fetching user data...");
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "user123";
            })
            // Stage 2: Data processing and enrichment
            .thenApplyAsync(userId -> {
                System.out.println("Step 2: Processing user: " + userId);
                try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "Profile for " + userId;
            })
            // Stage 3: Additional data enrichment
            .thenApplyAsync(profile -> {
                System.out.println("Step 3: Enriching profile: " + profile);
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return profile + " with additional data";
            })
            // Completion handler - executes on success OR failure
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    System.out.println("Error occurred: " + throwable.getMessage());
                } else {
                    System.out.println("Success callback: " + result);
                }
            })
            // Exception handler - provides fallback value
            .exceptionally(throwable -> {
                System.out.println("Exception handler: " + throwable.getMessage());
                return "Default fallback value";
            });

        // Wait for pipeline completion and get final result
        System.out.println("Final result: " + future.get());
    }

    /**
     * Demonstrates CompletableFuture composition and combination patterns
     *
     * This method shows how to combine multiple independent async operations
     * using various CompletableFuture composition methods. It covers parallel
     * execution, result combination, and race conditions.
     *
     * Patterns demonstrated:
     * - Parallel execution of independent tasks
     * - thenCombine() for combining two futures
     * - anyOf() for racing multiple futures
     * - allOf() for waiting on all futures
     * - Result collection and transformation
     *
     * Key methods explained:
     * - thenCombine(): Combines two futures when both complete
     * - anyOf(): Returns result of first future to complete
     * - allOf(): Waits for all futures to complete
     * - join(): Blocking call to get result (alternative to get())
     *
     * Use cases:
     * - Parallel data fetching from multiple sources
     * - Fan-out/fan-in patterns
     * - Race conditions for fastest response
     * - Batch processing with synchronization points
     *
     * Performance benefits:
     * - True parallelism when tasks are independent
     * - Reduced overall latency through concurrent execution
     * - Efficient resource utilization
     *
     * @throws Exception if any combination operation fails
     */
    public static void demonstrateCompletableFutureCombinations() throws Exception {
        System.out.println("\n=== CompletableFuture Combinations ===");

        // Create three independent async tasks
        CompletableFuture<String> fetchUser = CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching user...");
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "John Doe";
        });

        CompletableFuture<Integer> fetchAge = CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching age...");
            try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return 30;
        });

        CompletableFuture<String> fetchEmail = CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching email...");
            try { Thread.sleep(600); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "john@example.com";
        });

        // Combine results from multiple futures sequentially
        CompletableFuture<String> combined = fetchUser
            .thenCombine(fetchAge, (name, age) -> name + " (age: " + age + ")")
            .thenCombine(fetchEmail, (userInfo, email) -> userInfo + ", email: " + email);

        System.out.println("Combined result: " + combined.get());

        // Demonstrate anyOf - race condition (first to complete wins)
        CompletableFuture<Object> anyOfResult = CompletableFuture.anyOf(
            CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "Fast result";
            }),
            CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "Slow result";
            })
        );

        // Convert Object result back to String for type safety
        CompletableFuture<String> anyOf = anyOfResult.thenApply(result -> (String) result);
        System.out.println("First completed: " + anyOf.get());

        // Demonstrate allOf - wait for all to complete
        List<CompletableFuture<String>> futures = Arrays.asList(
            CompletableFuture.supplyAsync(() -> "Result 1"),
            CompletableFuture.supplyAsync(() -> "Result 2"),
            CompletableFuture.supplyAsync(() -> "Result 3")
        );

        // Wait for all futures and collect results
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<String>> allResults = allOf.thenApply(v ->
            futures.stream()
                .map(CompletableFuture::join)  // join() doesn't throw checked exceptions
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll)
        );

        System.out.println("All results: " + allResults.get());
    }

    /**
     * Demonstrates callback-driven asynchronous programming patterns
     *
     * This method shows how to build callback-based APIs that are common
     * in reactive programming and event-driven architectures. It demonstrates
     * success/error callback patterns and non-blocking operation handling.
     *
     * Pattern elements:
     * - Callback interfaces (Consumer<T> for success, Consumer<Throwable> for error)
     * - Non-blocking method signatures
     * - Asynchronous execution with callback invocation
     * - Error propagation through callbacks
     *
     * Key design principles:
     * - Separation of success and error handling
     * - Non-blocking API design
     * - Caller provides both success and error callbacks
     * - Immediate return from async method calls
     *
     * Benefits:
     * - Highly responsive user interfaces
     * - Efficient resource utilization
     * - Scalable server-side processing
     * - Event-driven architecture support
     *
     * Common use cases:
     * - HTTP client libraries
     * - Database access layers
     * - Message queue consumers
     * - Real-time data processing
     *
     * @throws Exception if callback setup fails
     */
    public static void demonstrateAsyncCallbacks() throws Exception {
        System.out.println("\n=== Async Callbacks Pattern ===");

        /**
         * Custom async processor demonstrating callback-based API design
         *
         * This inner class shows how to build libraries that accept callbacks
         * for handling asynchronous operation results. The pattern is widely
         * used in reactive frameworks and event-driven systems.
         */
        class AsyncProcessor {
            private final ExecutorService executor = Executors.newCachedThreadPool();

            /**
             * Processes data asynchronously and invokes appropriate callback
             *
             * @param data input data to process
             * @param onSuccess callback invoked on successful completion
             * @param onError callback invoked on error
             */
            public void processAsync(String data,
                                   java.util.function.Consumer<String> onSuccess,
                                   java.util.function.Consumer<Throwable> onError) {
                // Submit async task - method returns immediately
                executor.submit(() -> {
                    try {
                        // Simulate processing time
                        Thread.sleep(1000);

                        // Simulate conditional failure for demonstration
                        if (data.contains("error")) {
                            throw new RuntimeException("Processing failed for: " + data);
                        }

                        // Process data and invoke success callback
                        String result = "Processed: " + data.toUpperCase();
                        onSuccess.accept(result);

                    } catch (Exception e) {
                        // Invoke error callback on any exception
                        onError.accept(e);
                    }
                });
            }

            public void shutdown() {
                executor.shutdown();
            }
        }

        AsyncProcessor processor = new AsyncProcessor();

        System.out.println("Starting async processing...");

        // Submit successful processing request
        processor.processAsync("hello world",
            result -> System.out.println("Success callback: " + result),
            error -> System.out.println("Error callback: " + error.getMessage())
        );

        // Submit request that will trigger error callback
        processor.processAsync("error data",
            result -> System.out.println("Success callback: " + result),
            error -> System.out.println("Error callback: " + error.getMessage())
        );

        // Wait for async operations to complete
        Thread.sleep(2000);
        processor.shutdown();
    }

    /**
     * Demonstrates custom executor implementation with performance monitoring
     *
     * This method shows how to extend ThreadPoolExecutor to add custom monitoring,
     * metrics collection, and debugging capabilities. This is essential for
     * production systems where performance monitoring is critical.
     *
     * Custom features implemented:
     * - Task execution time tracking
     * - Success/failure rate monitoring
     * - Thread lifecycle callbacks
     * - Performance metrics collection
     * - Graceful shutdown with statistics
     *
     * ThreadPoolExecutor hooks used:
     * - beforeExecute(): Called before task execution
     * - afterExecute(): Called after task completion
     * - terminated(): Called when executor shuts down
     *
     * Monitoring data collected:
     * - Individual task execution times
     * - Success vs failure counts
     * - Thread utilization patterns
     * - Overall performance metrics
     *
     * Production applications:
     * - Application performance monitoring (APM)
     * - Service Level Agreement (SLA) tracking
     * - Capacity planning and optimization
     * - Debugging performance bottlenecks
     *
     * @throws Exception if executor operations fail
     */
    public static void demonstrateCustomExecutor() throws Exception {
        System.out.println("\n=== Custom Executor with Monitoring ===");

        /**
         * Enhanced ThreadPoolExecutor with comprehensive monitoring capabilities
         *
         * This class demonstrates how to extend the standard ThreadPoolExecutor
         * to add production-grade monitoring and metrics collection.
         */
        class MonitoredThreadPoolExecutor extends ThreadPoolExecutor {
            // Thread-safe collections for metrics
            private final Map<Runnable, Long> taskStartTimes = new ConcurrentHashMap<>();

            public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                             TimeUnit unit, BlockingQueue<Runnable> workQueue) {
                super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
            }

            /**
             * Called before each task execution - record start time and thread info
             */
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                taskStartTimes.put(r, System.currentTimeMillis());
                System.out.println("Starting task on thread: " + t.getName());
            }

            /**
             * Called after each task completion - calculate metrics and handle errors
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);

                // Calculate and report execution time
                Long startTime = taskStartTimes.remove(r);
                if (startTime != null) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    System.out.println("Task completed in " + executionTime + "ms");
                }

                // Handle any exceptions that occurred
                if (t != null) {
                    System.out.println("Task failed with exception: " + t.getMessage());
                }
            }

            /**
             * Called when executor shuts down - report final statistics
             */
            @Override
            protected void terminated() {
                super.terminated();
                System.out.println("Thread pool has been terminated");
            }
        }

        // Create monitored executor with custom settings
        MonitoredThreadPoolExecutor executor = new MonitoredThreadPoolExecutor(
            2, 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );

        // Submit various tasks to demonstrate monitoring
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    // Variable execution time for realistic simulation
                    Thread.sleep(1000 + (taskId * 200));

                    // Simulate occasional failures
                    if (taskId == 3) {
                        throw new RuntimeException("Simulated error in task " + taskId);
                    }

                    System.out.println("Task " + taskId + " completed successfully");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    throw e; // Re-throw to trigger afterExecute error handling
                }
            });
        }

        // Graceful shutdown with monitoring
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * Demonstrates ExecutorCompletionService for processing results as they become available
     *
     * ExecutorCompletionService provides a powerful pattern for handling multiple
     * concurrent tasks where you want to process results in completion order
     * rather than submission order. This is particularly useful for improving
     * user experience and system responsiveness.
     *
     * Key benefits:
     * - Process results as soon as they're available
     * - Don't wait for slowest task to complete
     * - Better resource utilization
     * - Improved user experience (progressive results)
     *
     * How it works:
     * 1. Wrap an executor with CompletionService
     * 2. Submit multiple tasks
     * 3. Use take() to get results in completion order
     * 4. Process each result immediately
     *
     * Real-world applications:
     * - Search result aggregation
     * - Parallel data processing with progressive updates
     * - Multiple API calls with varying response times
     * - Batch job processing with partial results
     *
     * @throws Exception if task execution or result retrieval fails
     */
    public static void demonstrateExecutorCompletionService() throws Exception {
        System.out.println("\n=== ExecutorCompletionService ===");

        ExecutorService executor = Executors.newFixedThreadPool(3);
        ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(executor);

        // Define tasks with different execution times
        String[] tasks = {"Task A", "Task B", "Task C", "Task D", "Task E"};
        int[] delays = {3000, 1000, 2000, 500, 1500};  // Varying delays

        // Submit all tasks to completion service
        for (int i = 0; i < tasks.length; i++) {
            final String task = tasks[i];
            final int delay = delays[i];
            completionService.submit(() -> {
                Thread.sleep(delay);
                return task + " completed in " + delay + "ms";
            });
        }

        System.out.println("All tasks submitted, retrieving results as they complete:");

        // Retrieve and process results in completion order (not submission order)
        for (int i = 0; i < tasks.length; i++) {
            // take() blocks until a result is available
            Future<String> future = completionService.take();
            String result = future.get();
            System.out.println("Retrieved: " + result);
        }

        executor.shutdown();
    }

    /**
     * Demonstrates Future timeout handling and cancellation
     *
     * This method shows essential patterns for handling timeouts in concurrent
     * programming, including task cancellation and resource cleanup. Timeout
     * handling is critical for building robust, responsive applications.
     *
     * Timeout scenarios covered:
     * - Task completes within timeout (normal case)
     * - Task exceeds timeout (cancellation case)
     * - Proper cleanup after cancellation
     *
     * Key methods demonstrated:
     * - Future.get(timeout, unit): Blocking get with timeout
     * - Future.cancel(mayInterruptIfRunning): Task cancellation
     * - TimeoutException handling
     *
     * Best practices shown:
     * - Always specify reasonable timeouts
     * - Handle TimeoutException appropriately
     * - Cancel tasks that exceed timeout
     * - Clean up resources after cancellation
     *
     * Production considerations:
     * - Circuit breaker patterns
     * - Retry mechanisms with backoff
     * - Monitoring timeout rates
     * - Graceful degradation strategies
     *
     * @throws Exception if executor setup fails
     */
    public static void demonstrateFutureTimeout() throws Exception {
        System.out.println("\n=== Future with Timeout ===");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Submit task that completes within timeout
        Future<String> quickTask = executor.submit(() -> {
            Thread.sleep(500);  // Short execution time
            return "Quick task completed";
        });

        // Submit task that will exceed timeout
        Future<String> slowTask = executor.submit(() -> {
            Thread.sleep(3000);  // Long execution time
            return "Slow task completed";
        });

        // Handle quick task - should complete successfully
        try {
            String result1 = quickTask.get(1, TimeUnit.SECONDS);  // 1 second timeout
            System.out.println("Quick task result: " + result1);
        } catch (TimeoutException e) {
            System.out.println("Quick task timed out");
            quickTask.cancel(true);  // Cancel if timed out
        }

        // Handle slow task - should timeout and be cancelled
        try {
            String result2 = slowTask.get(2, TimeUnit.SECONDS);  // 2 second timeout
            System.out.println("Slow task result: " + result2);
        } catch (TimeoutException e) {
            System.out.println("Slow task timed out");
            slowTask.cancel(true);  // Cancel the long-running task
        }

        executor.shutdown();
    }

    /**
     * Demonstrates reactive programming patterns with CompletableFuture
     *
     * This method showcases how to build reactive, event-driven processing
     * pipelines using CompletableFuture composition. It demonstrates error
     * handling, pipeline stages, and asynchronous orchestration patterns
     * commonly used in microservice architectures.
     *
     * Reactive programming principles shown:
     * - Asynchronous data flows
     * - Non-blocking operations
     * - Error propagation and handling
     * - Composable processing stages
     * - Functional programming style
     *
     * Pipeline stages:
     * 1. Input validation
     * 2. Asynchronous processing
     * 3. Data transformation
     * 4. Error handling and recovery
     *
     * Error handling strategies:
     * - Validation at pipeline entry
     * - Exception propagation through pipeline
     * - Graceful error recovery
     * - Comprehensive error reporting
     *
     * Real-world applications:
     * - Microservice request processing
     * - Event-driven architectures
     * - Streaming data processing
     * - API gateway patterns
     *
     * @throws Exception if pipeline setup fails
     */
    public static void demonstrateReactivePattern() throws Exception {
        System.out.println("\n=== Reactive Pattern with CompletableFuture ===");

        /**
         * Reactive processor demonstrating event-driven, asynchronous processing
         *
         * This class shows how to build reactive systems using CompletableFuture
         * composition and functional programming techniques.
         */
        class ReactiveProcessor {
            private final ExecutorService executor = Executors.newCachedThreadPool();

            /**
             * Processes input through a reactive pipeline with validation,
             * transformation, and error handling stages.
             *
             * @param input data to process through the pipeline
             * @return CompletableFuture representing the processing result
             */
            public CompletableFuture<String> processReactively(String input) {
                return CompletableFuture
                    // Stage 1: Input validation
                    .supplyAsync(() -> {
                        System.out.println("Stage 1: Validating input: " + input);
                        if (input == null || input.trim().isEmpty()) {
                            throw new IllegalArgumentException("Input cannot be empty");
                        }
                        return input.trim();
                    }, executor)

                    // Stage 2: Asynchronous processing
                    .thenCompose(validated -> CompletableFuture.supplyAsync(() -> {
                        System.out.println("Stage 2: Processing: " + validated);
                        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        return validated.toUpperCase();
                    }, executor))

                    // Stage 3: Final transformation
                    .thenCompose(processed -> CompletableFuture.supplyAsync(() -> {
                        System.out.println("Stage 3: Formatting: " + processed);
                        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        return "RESULT: " + processed;
                    }, executor))

                    // Completion handler - logs success or failure
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            System.out.println("Processing failed: " + throwable.getMessage());
                        } else {
                            System.out.println("Processing completed: " + result);
                        }
                    });
            }

            public void shutdown() {
                executor.shutdown();
            }
        }

        ReactiveProcessor processor = new ReactiveProcessor();

        // Process multiple inputs through reactive pipeline
        List<CompletableFuture<String>> futures = Arrays.asList(
            processor.processReactively("hello"),     // Should succeed
            processor.processReactively("world"),     // Should succeed
            processor.processReactively("")           // Should fail validation
        );

        // Collect all results, handling both success and failure cases
        CompletableFuture<List<String>> allResults = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        // Convert exceptions to error strings for demonstration
                        return "ERROR: " + e.getCause().getMessage();
                    }
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll)
            );

        System.out.println("All results: " + allResults.get());
        processor.shutdown();
    }

    /**
     * Main method demonstrating all advanced executor patterns
     *
     * Executes all demonstration methods in sequence, showing the full range
     * of advanced concurrent programming patterns and techniques. Each example
     * builds upon previous concepts to create a comprehensive learning experience.
     *
     * @param args command line arguments (not used)
     * @throws Exception if any demonstration fails
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== Advanced Executor Examples ===");

        demonstrateFutureCallbacks();
        Thread.sleep(1000);

        demonstrateCompletableFutureChaining();
        Thread.sleep(1000);

        demonstrateCompletableFutureCombinations();
        Thread.sleep(1000);

        demonstrateAsyncCallbacks();
        Thread.sleep(1000);

        demonstrateCustomExecutor();
        Thread.sleep(1000);

        demonstrateExecutorCompletionService();
        Thread.sleep(1000);

        demonstrateFutureTimeout();
        Thread.sleep(1000);

        demonstrateReactivePattern();
    }
}