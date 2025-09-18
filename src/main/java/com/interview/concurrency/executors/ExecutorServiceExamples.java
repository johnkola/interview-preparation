package com.interview.concurrency.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ExecutorService Examples - Comprehensive Guide to Java's Concurrent Execution Framework
 *
 * This class demonstrates various types of ExecutorService implementations and their
 * practical applications in concurrent programming. It provides examples of different
 * thread pool configurations, task submission patterns, and lifecycle management.
 *
 * ExecutorService is a high-level replacement for working with threads directly.
 * It provides:
 * - Thread pool management
 * - Task queuing and scheduling
 * - Result retrieval mechanisms
 * - Graceful shutdown capabilities
 * - Better resource management
 *
 * Executor types covered:
 * - FixedThreadPool: Fixed number of threads for consistent workload
 * - CachedThreadPool: Dynamic thread creation for varying workload
 * - SingleThreadExecutor: Sequential task execution
 * - ScheduledExecutorService: Time-based task scheduling
 * - ForkJoinPool: Work-stealing for divide-and-conquer algorithms
 * - Custom ThreadPoolExecutor: Fine-grained control over thread pool behavior
 *
 * Key concepts demonstrated:
 * - Thread pool sizing strategies
 * - Task submission vs execution patterns
 * - Future and Callable for result retrieval
 * - CompletableFuture for asynchronous programming
 * - Proper executor shutdown procedures
 * - Exception handling in concurrent tasks
 *
 * Best practices illustrated:
 * - Always shut down executors to prevent resource leaks
 * - Use appropriate timeout values for termination
 * - Handle InterruptedException properly
 * - Choose the right executor type for your use case
 * - Consider queue capacity and rejection policies
 *
 * Interview topics covered:
 * - Differences between executor types
 * - When to use each executor type
 * - Thread pool configuration parameters
 * - Future vs CompletableFuture
 * - Fork-Join framework work-stealing algorithm
 * - Executor lifecycle management
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class ExecutorServiceExamples {

    /**
     * Demonstrates FixedThreadPool - a thread pool with a fixed number of threads
     *
     * FixedThreadPool maintains a constant number of threads throughout its lifetime.
     * It's ideal for applications with a known, stable workload where you want to
     * limit resource consumption and prevent thread creation overhead.
     *
     * Key characteristics:
     * - Fixed number of threads (3 in this example)
     * - Uses an unbounded LinkedBlockingQueue for task storage
     * - Threads are kept alive even when idle
     * - New tasks wait in queue if all threads are busy
     * - Provides predictable resource usage
     *
     * Use cases:
     * - Web servers with known concurrent user limits
     * - Batch processing systems
     * - Applications requiring resource usage control
     * - Long-running services with consistent load
     *
     * Advantages:
     * - Predictable memory and CPU usage
     * - No thread creation/destruction overhead during steady state
     * - Suitable for long-running applications
     * - Good for controlling resource consumption
     *
     * Disadvantages:
     * - May not scale well with sudden load spikes
     * - Unbounded queue can lead to OutOfMemoryError
     * - Idle threads consume memory even when not needed
     *
     * Performance considerations:
     * - Choose pool size based on CPU cores and I/O wait times
     * - For CPU-intensive tasks: pool size ≈ number of CPU cores
     * - For I/O-intensive tasks: pool size can be higher
     */
    public static void demonstrateFixedThreadPool() {
        System.out.println("\n=== Fixed Thread Pool ===");
        // Create fixed thread pool with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit 10 tasks to demonstrate thread reuse and queuing
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " executed by " + Thread.currentThread().getName());
                try {
                    // Simulate work with 1-second delay
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Restore interrupted status when handling InterruptedException
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Graceful shutdown - no new tasks accepted but existing tasks complete
        executor.shutdown();
        try {
            // Wait up to 15 seconds for tasks to complete
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                // Force shutdown if tasks don't complete within timeout
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // If interrupted while waiting, force immediate shutdown
            executor.shutdownNow();
        }
    }

    /**
     * Demonstrates CachedThreadPool - a dynamically sized thread pool
     *
     * CachedThreadPool creates new threads as needed and reuses previously
     * constructed threads when available. It's designed for applications with
     * many short-lived asynchronous tasks or highly variable workloads.
     *
     * Key characteristics:
     * - No core threads (core size = 0)
     * - Maximum threads = Integer.MAX_VALUE (effectively unlimited)
     * - Idle threads terminated after 60 seconds
     * - Uses SynchronousQueue (no storage capacity)
     * - Creates new thread immediately if no idle threads available
     *
     * Use cases:
     * - Applications with sporadic, short-lived tasks
     * - Highly variable workloads with unpredictable spikes
     * - Systems where task arrival rate varies significantly
     * - Scenarios requiring rapid response to load changes
     *
     * Advantages:
     * - Excellent performance for short-lived tasks
     * - Automatically scales with workload
     * - No queuing delays (new thread created immediately)
     * - Self-tuning - unused threads are automatically cleaned up
     *
     * Disadvantages:
     * - Unbounded thread creation can exhaust system resources
     * - Thread creation overhead for each new task initially
     * - Memory usage can be unpredictable
     * - Not suitable for long-running tasks or high sustained load
     *
     * Performance characteristics:
     * - SynchronousQueue has no storage - direct handoff between producer/consumer
     * - Thread creation overhead amortized across task lifetime
     * - 60-second keep-alive prevents excessive resource usage during idle periods
     *
     * Best practices:
     * - Monitor thread count in production to prevent resource exhaustion
     * - Consider rate limiting to prevent overwhelming the system
     * - Use for short-lived tasks (< 100ms typically)
     */
    public static void demonstrateCachedThreadPool() {
        System.out.println("\n=== Cached Thread Pool ===");
        // Create cached thread pool with dynamic thread creation
        ExecutorService executor = Executors.newCachedThreadPool();

        // Submit 10 tasks quickly to demonstrate rapid thread creation
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " executed by " + Thread.currentThread().getName());
                try {
                    // Short task duration (100ms) - typical for cached thread pool usage
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Initiate graceful shutdown
        executor.shutdown();
        try {
            // Shorter timeout since tasks are brief
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    /**
     * Demonstrates SingleThreadExecutor - guaranteed sequential task execution
     *
     * SingleThreadExecutor uses exactly one worker thread to execute tasks.
     * It guarantees that tasks are executed sequentially in the order they
     * were submitted, making it ideal for scenarios requiring strict ordering.
     *
     * Key characteristics:
     * - Exactly one worker thread throughout executor lifetime
     * - Uses unbounded LinkedBlockingQueue for task storage
     * - Tasks executed in submission order (FIFO)
     * - Thread replaced if it dies due to failure during execution
     * - Equivalent to FixedThreadPool(1) but with additional guarantees
     *
     * Use cases:
     * - Event processing where order matters (e.g., UI updates)
     * - Sequential file processing
     * - State machine implementations
     * - Logging systems requiring chronological order
     * - Database operations requiring strict ordering
     *
     * Advantages:
     * - Guaranteed sequential execution
     * - No synchronization needed between tasks
     * - Simple mental model - no race conditions
     * - Automatic thread replacement on failure
     * - Resource predictability (single thread)
     *
     * Disadvantages:
     * - No parallelism - throughput limited to single thread
     * - Can become bottleneck for high-throughput applications
     * - Long-running tasks block all subsequent tasks
     * - Not suitable for CPU-intensive workloads on multi-core systems
     *
     * Comparison with manual thread management:
     * - Simpler than managing a single worker thread manually
     * - Built-in lifecycle management and exception handling
     * - Integration with executor framework (shutdown, monitoring)
     *
     * Interview points:
     * - Difference between SingleThreadExecutor and synchronized methods
     * - When sequential processing is required vs when it's acceptable
     * - Impact on application scalability and performance
     */
    public static void demonstrateSingleThreadExecutor() {
        System.out.println("\n=== Single Thread Executor ===");
        // Create single-threaded executor for sequential task execution
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Submit 5 tasks to demonstrate sequential execution order
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                // Note: All tasks will execute on the same thread sequentially
                System.out.println("Task " + taskId + " executed by " + Thread.currentThread().getName());
            });
        }

        // Initiate graceful shutdown
        executor.shutdown();
        try {
            // Wait for all tasks to complete
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    /**
     * Demonstrates ScheduledExecutorService - time-based task scheduling
     *
     * ScheduledExecutorService extends ExecutorService to support delayed and
     * periodic task execution. It's essential for applications requiring
     * time-based operations like cron jobs, timeouts, and periodic maintenance.
     *
     * Key scheduling methods:
     * - schedule(): Execute task once after specified delay
     * - scheduleAtFixedRate(): Execute task periodically with fixed rate
     * - scheduleWithFixedDelay(): Execute task with fixed delay between executions
     *
     * Timing behavior:
     * - scheduleAtFixedRate: Next execution = start time + (n * period)
     * - scheduleWithFixedDelay: Next execution = previous completion + delay
     *
     * Use cases:
     * - Periodic data synchronization
     * - Cache cleanup and maintenance
     * - Health checks and monitoring
     * - Session timeout management
     * - Retry mechanisms with delays
     * - Heartbeat implementations
     *
     * Advantages:
     * - Built-in timing mechanisms
     * - Thread pool management for scheduled tasks
     * - Cancellable scheduled tasks
     * - Exception handling in scheduled context
     *
     * Best practices:
     * - Always cancel periodic tasks to prevent resource leaks
     * - Handle exceptions in scheduled tasks (they're suppressed by default)
     * - Consider task execution time vs scheduling period
     * - Use appropriate pool size for concurrent scheduled tasks
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static void demonstrateScheduledExecutor() throws InterruptedException {
        System.out.println("\n=== Scheduled Executor ===");
        // Create scheduled executor with 2 threads for concurrent scheduled tasks
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // Schedule one-time task with 2-second delay
        scheduler.schedule(() -> {
            System.out.println("Task executed after 2 seconds delay");
        }, 2, TimeUnit.SECONDS);

        // Schedule periodic task - executes every 2 seconds starting after 1 second
        ScheduledFuture<?> periodicTask = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Periodic task executed at " + System.currentTimeMillis());
        }, 1, 2, TimeUnit.SECONDS);

        // Let periodic task run for 7 seconds
        Thread.sleep(7000);
        // Cancel periodic task to prevent it from running indefinitely
        periodicTask.cancel(true);

        // Shutdown scheduler
        scheduler.shutdown();
    }

    /**
     * Demonstrates Callable and Future - task execution with return values
     *
     * Unlike Runnable which cannot return values or throw checked exceptions,
     * Callable<V> can return a result and throw exceptions. Future<V> represents
     * the result of an asynchronous computation.
     *
     * Key differences from Runnable:
     * - Callable.call() returns a value (generic type V)
     * - Can throw checked exceptions
     * - Used with submit() method to get Future<V>
     *
     * Future methods:
     * - get(): Blocking call to retrieve result
     * - get(timeout, unit): Blocking call with timeout
     * - isDone(): Check if computation completed
     * - cancel(): Attempt to cancel execution
     *
     * invokeAll() method:
     * - Submits all tasks and waits for completion
     * - Returns List<Future<T>> in same order as input
     * - Blocks until all tasks complete or timeout
     *
     * Use cases:
     * - Parallel computation with result aggregation
     * - Batch processing with individual results
     * - Mathematical calculations requiring return values
     * - Data transformation pipelines
     *
     * Exception handling:
     * - Callable exceptions wrapped in ExecutionException
     * - InterruptedException for thread interruption
     * - TimeoutException for get() with timeout
     *
     * @throws InterruptedException if interrupted while waiting
     * @throws ExecutionException if computation threw an exception
     */
    public static void demonstrateCallableAndFuture() throws InterruptedException, ExecutionException {
        System.out.println("\n=== Callable and Future ===");
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Create list of Callable tasks that return computed values
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int num = i;
            tasks.add(() -> {
                Thread.sleep(1000);  // Simulate computation time
                return num * num;    // Return computed result
            });
        }

        // Submit all tasks and wait for completion - returns futures in order
        List<Future<Integer>> futures = executor.invokeAll(tasks);

        // Retrieve results from futures (all tasks already completed due to invokeAll)
        for (int i = 0; i < futures.size(); i++) {
            System.out.println("Result of task " + (i + 1) + ": " + futures.get(i).get());
        }

        executor.shutdown();
    }

    /**
     * Demonstrates CompletableFuture - advanced asynchronous programming
     *
     * CompletableFuture represents a Future that can be explicitly completed
     * and supports functional-style operations. It's the foundation of modern
     * asynchronous programming in Java, providing a rich API for composition.
     *
     * Key CompletableFuture features:
     * - supplyAsync(): Start async computation with return value
     * - runAsync(): Start async computation without return value
     * - thenApply(): Transform result synchronously
     * - thenApplyAsync(): Transform result asynchronously
     * - thenCombine(): Combine two independent futures
     * - allOf(): Wait for multiple futures to complete
     * - anyOf(): Complete when any future completes
     *
     * Composition patterns demonstrated:
     * 1. Parallel execution with combination
     * 2. Sequential transformation chains
     * 3. Coordination of multiple async operations
     *
     * Advantages over traditional Future:
     * - Fluent API for chaining operations
     * - Built-in support for composition and combination
     * - Exception handling with exceptionally() and handle()
     * - Non-blocking completion callbacks
     * - Integration with lambda expressions
     *
     * Use cases:
     * - Microservice orchestration
     * - Parallel data processing pipelines
     * - Non-blocking I/O operations
     * - Complex async workflows
     * - Reactive programming patterns
     *
     * Performance considerations:
     * - Uses ForkJoinPool.commonPool() by default
     * - Can specify custom executor for better control
     * - Minimal overhead compared to manual Future handling
     *
     * @throws InterruptedException if interrupted while waiting
     * @throws ExecutionException if computation threw an exception
     */
    public static void demonstrateCompletableFuture() throws InterruptedException, ExecutionException {
        System.out.println("\n=== CompletableFuture ===");

        // Create two independent async computations
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);  // Simulate 1-second operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Result from Future 1";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);  // Simulate 2-second operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Result from Future 2";
        });

        // Combine results from both futures (waits for both to complete)
        CompletableFuture<String> combinedFuture = future1.thenCombine(future2,
                (result1, result2) -> result1 + " + " + result2);

        System.out.println("Combined result: " + combinedFuture.get());

        // Demonstrate chaining transformations
        CompletableFuture<Integer> chainedFuture = CompletableFuture
                .supplyAsync(() -> 5)          // Start with value 5
                .thenApply(n -> n * 2)         // Transform: 5 -> 10
                .thenApply(n -> n + 3);        // Transform: 10 -> 13

        System.out.println("Chained result: " + chainedFuture.get());

        // Coordinate multiple async operations with allOf
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> System.out.println("Task 1")),
                CompletableFuture.runAsync(() -> System.out.println("Task 2")),
                CompletableFuture.runAsync(() -> System.out.println("Task 3"))
        );
        allOf.get();  // Wait for all tasks to complete
    }

    /**
     * Demonstrates ForkJoinPool - work-stealing thread pool for divide-and-conquer algorithms
     *
     * ForkJoinPool is designed for work that can be broken down into smaller pieces
     * recursively. It uses a work-stealing algorithm where idle threads steal work
     * from busy threads' work queues, providing excellent performance for recursive tasks.
     *
     * Key concepts:
     * - Fork: Split task into subtasks and execute them asynchronously
     * - Join: Wait for subtask completion and combine results
     * - Work-stealing: Idle threads steal work from other threads' queues
     * - Recursive decomposition: Break large problems into smaller subproblems
     *
     * ForkJoinTask subclasses:
     * - RecursiveTask<V>: Tasks that return a result
     * - RecursiveAction: Tasks that don't return a result
     *
     * Work-stealing advantages:
     * - Automatic load balancing across threads
     * - Efficient utilization of all CPU cores
     * - Minimal contention on shared work queues
     * - Scales well with number of processors
     *
     * Algorithm pattern demonstrated:
     * 1. Check if task is small enough to solve directly (threshold)
     * 2. If small: solve directly and return result
     * 3. If large: split into subtasks, fork left, compute right, join left
     *
     * Use cases:
     * - Parallel algorithms (merge sort, quick sort)
     * - Mathematical computations (matrix operations)
     * - Tree/graph traversals
     * - Image/signal processing
     * - MapReduce-style operations
     *
     * Performance considerations:
     * - Threshold tuning is critical for performance
     * - Too small threshold: excessive overhead
     * - Too large threshold: insufficient parallelism
     * - Optimal threshold typically 100-10000 operations
     *
     * Interview topics:
     * - Difference between fork-join and traditional thread pools
     * - Work-stealing algorithm explanation
     * - When to use ForkJoinPool vs other executors
     * - Threshold selection strategies
     */
    public static void demonstrateForkJoinPool() {
        System.out.println("\n=== ForkJoinPool ===");

        /**
         * RecursiveTask for computing sum of array elements using divide-and-conquer
         *
         * This demonstrates the classic fork-join pattern:
         * - Divide array into smaller segments
         * - Process segments in parallel
         * - Combine results from all segments
         */
        class RecursiveSum extends RecursiveTask<Long> {
            private final long[] numbers;
            private final int start;
            private final int end;
            private static final int THRESHOLD = 10;  // Process directly if <= 10 elements

            public RecursiveSum(long[] numbers, int start, int end) {
                this.numbers = numbers;
                this.start = start;
                this.end = end;
            }

            @Override
            protected Long compute() {
                int length = end - start;
                if (length <= THRESHOLD) {
                    // Base case: compute sum directly for small arrays
                    long sum = 0;
                    for (int i = start; i < end; i++) {
                        sum += numbers[i];
                    }
                    return sum;
                } else {
                    // Recursive case: divide array and fork subtasks
                    int middle = start + length / 2;
                    RecursiveSum leftTask = new RecursiveSum(numbers, start, middle);
                    RecursiveSum rightTask = new RecursiveSum(numbers, middle, end);

                    // Fork left task to run asynchronously
                    leftTask.fork();
                    // Compute right task in current thread
                    long rightResult = rightTask.compute();
                    // Wait for left task completion and get result
                    long leftResult = leftTask.join();

                    // Combine results from both subtasks
                    return leftResult + rightResult;
                }
            }
        }

        // Create array of numbers 1 to 100
        long[] numbers = IntStream.rangeClosed(1, 100).asLongStream().toArray();

        // Create ForkJoinPool (uses all available processors by default)
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        // Create root task and execute it
        RecursiveSum task = new RecursiveSum(numbers, 0, numbers.length);
        Long result = forkJoinPool.invoke(task);
        System.out.println("Sum of 1 to 100: " + result);

        forkJoinPool.shutdown();
    }

    /**
     * Demonstrates custom ThreadPoolExecutor - fine-grained thread pool control
     *
     * ThreadPoolExecutor is the implementation class behind most Executors factory methods.
     * Creating it directly gives you complete control over thread pool behavior,
     * including custom thread factories and rejection policies.
     *
     * Constructor parameters explained:
     * - corePoolSize: Minimum number of threads to keep alive
     * - maximumPoolSize: Maximum number of threads allowed
     * - keepAliveTime: How long excess threads stay alive when idle
     * - workQueue: Queue for holding tasks before execution
     * - threadFactory: Factory for creating new threads
     * - rejectedExecutionHandler: Policy for handling rejected tasks
     *
     * Thread pool sizing behavior:
     * 1. If threads < corePoolSize: create new thread for each task
     * 2. If threads = corePoolSize: queue the task
     * 3. If queue is full and threads < maxPoolSize: create new thread
     * 4. If threads = maxPoolSize and queue is full: reject task
     *
     * Queue types and their impact:
     * - LinkedBlockingQueue: Bounded or unbounded FIFO queue
     * - ArrayBlockingQueue: Bounded FIFO queue with fixed capacity
     * - SynchronousQueue: Direct handoff, no storage capacity
     * - PriorityBlockingQueue: Priority-based task ordering
     *
     * Rejection policies:
     * - AbortPolicy: Throw RejectedExecutionException (default)
     * - CallerRunsPolicy: Execute task in caller thread
     * - DiscardPolicy: Silently discard the task
     * - DiscardOldestPolicy: Discard oldest queued task
     *
     * Custom ThreadFactory benefits:
     * - Control thread naming for debugging
     * - Set thread priority and daemon status
     * - Add custom exception handlers
     * - Implement thread pooling metrics
     *
     * Monitoring capabilities:
     * - getPoolSize(): Current number of threads
     * - getActiveCount(): Threads actively executing tasks
     * - getQueue().size(): Tasks waiting in queue
     * - getCompletedTaskCount(): Historical task completion count
     *
     * Use cases for custom ThreadPoolExecutor:
     * - Applications requiring specific thread pool behavior
     * - Systems needing custom rejection handling
     * - Scenarios requiring thread pool monitoring
     * - Performance tuning for specific workload patterns
     */
    public static void demonstrateThreadPoolExecutor() {
        System.out.println("\n=== Custom ThreadPoolExecutor ===");

        // Create custom ThreadPoolExecutor with fine-grained control
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                      // Core pool size - minimum threads
                4,                      // Maximum pool size - max threads
                60,                     // Keep alive time for excess threads
                TimeUnit.SECONDS,       // Time unit for keep alive
                new LinkedBlockingQueue<>(10),  // Bounded queue with capacity 10
                // Custom thread factory for controlled thread creation
                new ThreadFactory() {
                    private int counter = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("CustomThread-" + counter++);  // Custom naming
                        return thread;
                    }
                },
                // Custom rejection handler for tasks that can't be queued
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        System.out.println("Task rejected: " + r.toString());
                    }
                }
        );

        // Submit 20 tasks to demonstrate queue filling and potential rejection
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " executed by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(500);  // Simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Monitor thread pool state during execution
        System.out.println("Core pool size: " + executor.getCorePoolSize());
        System.out.println("Pool size: " + executor.getPoolSize());
        System.out.println("Active count: " + executor.getActiveCount());
        System.out.println("Queue size: " + executor.getQueue().size());

        // Graceful shutdown with monitoring
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    /**
     * Main method demonstrating all ExecutorService examples
     *
     * Executes each demonstration method in sequence with pauses between them
     * to allow for clear observation of different executor behaviors. Each
     * example showcases different aspects of Java's concurrent execution framework.
     *
     * Execution order:
     * 1. FixedThreadPool - predictable resource usage
     * 2. CachedThreadPool - dynamic scaling
     * 3. SingleThreadExecutor - sequential execution
     * 4. ScheduledExecutor - time-based scheduling
     * 5. Callable/Future - result retrieval
     * 6. CompletableFuture - advanced async composition
     * 7. ForkJoinPool - work-stealing parallelism
     * 8. Custom ThreadPoolExecutor - fine-grained control
     *
     * Learning progression:
     * - Start with basic thread pool concepts (Fixed, Cached, Single)
     * - Progress to timing and scheduling (Scheduled)
     * - Learn result handling (Callable/Future)
     * - Master advanced composition (CompletableFuture)
     * - Understand specialized algorithms (ForkJoin)
     * - Achieve expert-level control (Custom ThreadPoolExecutor)
     *
     * @param args command line arguments (not used)
     * @throws Exception if any demonstration method fails
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== Executor Service Examples ===");

        demonstrateFixedThreadPool();
        Thread.sleep(2000);  // Pause to observe output

        demonstrateCachedThreadPool();
        Thread.sleep(2000);

        demonstrateSingleThreadExecutor();
        Thread.sleep(2000);

        demonstrateScheduledExecutor();
        Thread.sleep(2000);

        demonstrateCallableAndFuture();
        Thread.sleep(2000);

        demonstrateCompletableFuture();
        Thread.sleep(2000);

        demonstrateForkJoinPool();
        Thread.sleep(2000);

        demonstrateThreadPoolExecutor();
    }
}