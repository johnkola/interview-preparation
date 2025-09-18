package com.interview.concurrency.executors;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorsFactoryExamples {

    /**
     * Demonstrates Executors.newFixedThreadPool(int nThreads)
     *
     * What it does:
     * - Creates a thread pool with a fixed number of threads (3 in this case)
     * - Reuses threads from the pool for new tasks
     * - If all threads are busy, new tasks wait in an unbounded queue
     * - Threads are kept alive even when idle
     *
     * Use cases:
     * - Web servers with predictable load
     * - Applications where you want to limit resource usage
     * - When you know the optimal number of threads for your workload
     *
     * Advantages:
     * - Predictable resource usage
     * - No overhead of thread creation/destruction
     * - Good for CPU-bound tasks
     *
     * Disadvantages:
     * - Can cause memory issues if queue grows too large
     * - May not adapt well to varying loads
     */
    public static void demonstrateNewFixedThreadPool() throws InterruptedException {
        System.out.println("\n=== Executors.newFixedThreadPool() ===");

        // Creates a thread pool with fixed number of threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        System.out.println("Submitting 8 tasks to fixed thread pool of size 3:");

        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.printf("Task %d started on thread: %s%n",
                    taskId, Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.printf("Task %d completed%n", taskId);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("All tasks completed with fixed thread pool\n");
    }

    /**
     * Demonstrates Executors.newCachedThreadPool()
     *
     * What it does:
     * - Creates threads on demand as needed
     * - Reuses previously constructed threads when available
     * - Idle threads are kept for 60 seconds, then terminated
     * - No limit on number of threads (theoretically unlimited)
     *
     * Use cases:
     * - Applications with many short-lived asynchronous tasks
     * - I/O-bound operations where threads spend time waiting
     * - When task arrival rate varies significantly
     *
     * Advantages:
     * - Automatically scales with workload
     * - Good for I/O-bound tasks
     * - No queuing delay for tasks
     *
     * Disadvantages:
     * - Can create too many threads under high load
     * - Memory overhead from unlimited thread creation
     * - Risk of resource exhaustion
     */
    public static void demonstrateNewCachedThreadPool() throws InterruptedException {
        System.out.println("\n=== Executors.newCachedThreadPool() ===");

        // Creates a thread pool that creates threads as needed, reuses idle threads
        ExecutorService executor = Executors.newCachedThreadPool();

        System.out.println("Submitting 8 tasks to cached thread pool:");

        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.printf("Task %d started on thread: %s%n",
                    taskId, Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.printf("Task %d completed%n", taskId);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("All tasks completed with cached thread pool\n");
    }

    /**
     * Demonstrates Executors.newSingleThreadExecutor()
     *
     * What it does:
     * - Creates an executor with exactly one worker thread
     * - Tasks are executed sequentially in submission order
     * - If the thread dies due to failure, a new one is created
     * - Uses an unbounded queue to hold waiting tasks
     *
     * Use cases:
     * - Background processing that must be sequential
     * - Logging systems
     * - File I/O operations that must maintain order
     * - State machines that require sequential processing
     *
     * Advantages:
     * - Guarantees sequential execution
     * - Thread-safe by design (no concurrent execution)
     * - Simple and predictable behavior
     *
     * Disadvantages:
     * - Cannot take advantage of multiple cores
     * - Bottleneck if tasks take long time
     * - Queue can grow unbounded
     */
    public static void demonstrateNewSingleThreadExecutor() throws InterruptedException {
        System.out.println("\n=== Executors.newSingleThreadExecutor() ===");

        // Creates a thread pool with single thread, guarantees sequential execution
        ExecutorService executor = Executors.newSingleThreadExecutor();

        System.out.println("Submitting 5 tasks to single thread executor:");

        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.printf("Task %d executing on thread: %s%n",
                    taskId, Thread.currentThread().getName());
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.printf("Task %d completed%n", taskId);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("All tasks completed sequentially\n");
    }

    /**
     * Demonstrates Executors.newScheduledThreadPool(int corePoolSize)
     *
     * What it does:
     * - Creates a thread pool for scheduling tasks with delays or periodic execution
     * - Can schedule one-time delayed tasks
     * - Can schedule recurring tasks with fixed rate or fixed delay
     * - Core pool size determines minimum threads kept alive
     *
     * Key methods:
     * - schedule(): Execute once after delay
     * - scheduleAtFixedRate(): Execute periodically with fixed intervals
     * - scheduleWithFixedDelay(): Execute periodically with fixed delay between completions
     *
     * Use cases:
     * - Cron-like scheduling
     * - Periodic maintenance tasks
     * - Timeouts and retries
     * - Heartbeat mechanisms
     *
     * Advantages:
     * - Built-in timing capabilities
     * - Multiple scheduling options
     * - Can handle both one-time and recurring tasks
     *
     * Disadvantages:
     * - More complex than basic executors
     * - Overhead for timing mechanisms
     */
    public static void demonstrateNewScheduledThreadPool() throws InterruptedException {
        System.out.println("\n=== Executors.newScheduledThreadPool() ===");

        // Creates a thread pool that can schedule commands to run after delay or periodically
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        System.out.println("Scheduling various tasks:");

        // Schedule a one-time task with delay
        scheduler.schedule(() -> {
            System.out.println("One-time task executed after 1 second delay");
        }, 1, TimeUnit.SECONDS);

        // Schedule a periodic task with fixed rate
        ScheduledFuture<?> periodicTask = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Periodic task (fixed rate) at " + System.currentTimeMillis());
        }, 0, 2, TimeUnit.SECONDS);

        // Schedule a periodic task with fixed delay
        ScheduledFuture<?> delayTask = scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("Periodic task (fixed delay) at " + System.currentTimeMillis());
            try {
                Thread.sleep(500); // Task takes some time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 1, TimeUnit.SECONDS);

        Thread.sleep(6000); // Let it run for 6 seconds

        periodicTask.cancel(true);
        delayTask.cancel(true);

        scheduler.shutdown();
        scheduler.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("Scheduled tasks completed\n");
    }

    /**
     * Demonstrates Executors.newSingleThreadScheduledExecutor()
     *
     * What it does:
     * - Combines single-thread execution with scheduling capabilities
     * - All scheduled tasks execute on the same thread
     * - Maintains execution order for scheduled tasks
     * - Replaces thread if it dies due to failure
     *
     * Use cases:
     * - Sequential scheduled operations
     * - State-dependent periodic tasks
     * - Single-threaded timer services
     * - Ordered batch processing with delays
     *
     * Advantages:
     * - Thread-safe scheduling
     * - Predictable execution order
     * - No need for synchronization between tasks
     *
     * Disadvantages:
     * - Cannot execute scheduled tasks concurrently
     * - Single point of failure
     * - Limited throughput for high-frequency tasks
     */
    public static void demonstrateNewSingleThreadScheduledExecutor() throws InterruptedException {
        System.out.println("\n=== Executors.newSingleThreadScheduledExecutor() ===");

        // Creates a single-threaded executor that can schedule commands
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("Scheduling tasks on single thread:");

        // Multiple scheduled tasks will execute on the same thread
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            scheduler.schedule(() -> {
                System.out.printf("Scheduled task %d on thread: %s%n",
                    taskId, Thread.currentThread().getName());
            }, i, TimeUnit.SECONDS);
        }

        Thread.sleep(5000);

        scheduler.shutdown();
        scheduler.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("Single thread scheduled tasks completed\n");
    }

    /**
     * Demonstrates Executors.newWorkStealingPool()
     *
     * What it does:
     * - Creates a ForkJoinPool for work-stealing execution
     * - Uses available processors as parallelism level by default
     * - Idle threads "steal" work from busy threads' queues
     * - Optimized for divide-and-conquer algorithms
     *
     * Work-stealing algorithm:
     * - Each thread has its own work queue (deque)
     * - Thread takes work from its own queue's head
     * - When queue is empty, steals from tail of other threads' queues
     * - Reduces contention and improves load balancing
     *
     * Use cases:
     * - Recursive algorithms (divide-and-conquer)
     * - Parallel processing of large datasets
     * - Tasks that spawn other tasks
     * - CPU-intensive computations
     *
     * Advantages:
     * - Excellent load balancing
     * - Scales well with available processors
     * - Reduces idle time for threads
     *
     * Disadvantages:
     * - More complex than traditional thread pools
     * - Overhead of work-stealing mechanism
     * - Best for CPU-bound tasks, not I/O-bound
     */
    public static void demonstrateNewWorkStealingPool() throws InterruptedException {
        System.out.println("\n=== Executors.newWorkStealingPool() ===");

        // Creates a work-stealing thread pool (uses ForkJoinPool)
        ExecutorService executor = Executors.newWorkStealingPool();

        System.out.println("Submitting tasks to work-stealing pool:");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            Future<String> future = executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.printf("Task %d executing on: %s%n", taskId, threadName);

                // Simulate work with varying duration
                try {
                    Thread.sleep(100 + (taskId * 50));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                return "Result from task " + taskId + " (" + threadName + ")";
            });
            futures.add(future);
        }

        // Collect results
        for (Future<String> future : futures) {
            try {
                System.out.println("Received: " + future.get());
            } catch (ExecutionException e) {
                System.err.println("Task failed: " + e.getCause().getMessage());
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Work-stealing pool tasks completed\n");
    }

    /**
     * Demonstrates Executors.newWorkStealingPool(int parallelism)
     *
     * What it does:
     * - Creates a work-stealing pool with custom parallelism level
     * - Parallelism level determines target number of active threads
     * - Allows fine-tuning for specific hardware or workload requirements
     * - Still uses work-stealing algorithm for load balancing
     *
     * Parallelism vs Thread Count:
     * - Parallelism = target number of simultaneously executing tasks
     * - Actual thread count may vary based on workload
     * - Pool can create more threads than parallelism level if needed
     *
     * Use cases:
     * - When default parallelism (CPU cores) isn't optimal
     * - Testing different parallelism levels for performance tuning
     * - Applications with specific threading requirements
     * - Resource-constrained environments
     *
     * Tuning guidelines:
     * - CPU-bound tasks: parallelism = number of CPU cores
     * - I/O-bound tasks: parallelism > number of CPU cores
     * - Mixed workloads: experiment to find optimal value
     */
    public static void demonstrateNewWorkStealingPoolWithParallelism() throws InterruptedException {
        System.out.println("\n=== Executors.newWorkStealingPool(parallelism) ===");

        // Creates a work-stealing pool with specified parallelism level
        int parallelism = 4;
        ExecutorService executor = Executors.newWorkStealingPool(parallelism);

        System.out.println("Work-stealing pool with parallelism: " + parallelism);

        AtomicInteger taskCounter = new AtomicInteger(0);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Future<Void> future = executor.submit(() -> {
                int taskNum = taskCounter.incrementAndGet();
                String threadName = Thread.currentThread().getName();
                System.out.printf("Task %d on thread: %s%n", taskNum, threadName);

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
            futures.add(future);
        }

        // Wait for all tasks
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                System.err.println("Task failed: " + e.getCause().getMessage());
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Custom parallelism work-stealing tasks completed\n");
    }

    /**
     * Demonstrates Executors.unconfigurableExecutorService(ExecutorService executor)
     *
     * What it does:
     * - Wraps an ExecutorService to prevent configuration changes
     * - Hides methods like setCorePoolSize(), setMaximumPoolSize(), etc.
     * - Still allows task submission and shutdown operations
     * - Provides a read-only view of executor configuration
     *
     * Security purpose:
     * - Prevents unauthorized modification of executor settings
     * - Useful when passing executors to untrusted code
     * - Implements principle of least privilege
     *
     * Use cases:
     * - Library code that receives executors from clients
     * - API boundaries where you want to prevent misconfiguration
     * - Security-sensitive applications
     * - Multi-tenant environments
     *
     * What it blocks:
     * - Configuration methods (setCorePoolSize, setMaximumPoolSize, etc.)
     * - Direct access to underlying ThreadPoolExecutor
     *
     * What it allows:
     * - Task submission (submit, execute)
     * - Shutdown operations
     * - Status queries (isShutdown, isTerminated)
     */
    public static void demonstrateUnconfigurableExecutorService() throws InterruptedException {
        System.out.println("\n=== Executors.unconfigurableExecutorService() ===");

        // Creates an unmodifiable wrapper around an executor
        ExecutorService baseExecutor = Executors.newFixedThreadPool(2);
        ExecutorService unconfigurable = Executors.unconfigurableExecutorService(baseExecutor);

        System.out.println("Using unconfigurable executor wrapper:");

        // You can submit tasks but cannot reconfigure the executor
        unconfigurable.submit(() -> {
            System.out.println("Task running in unconfigurable executor on: " +
                Thread.currentThread().getName());
        });

        Thread.sleep(1000);

        // Shutdown through the wrapper
        unconfigurable.shutdown();
        unconfigurable.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("Unconfigurable executor completed\n");
    }

    /**
     * Demonstrates Executors.privilegedThreadFactory() and related security features
     *
     * What it does:
     * - Creates threads that inherit security context from creating thread
     * - Preserves AccessControlContext for security-sensitive operations
     * - Ensures proper permission checking in multi-threaded environments
     *
     * Security concepts:
     * - AccessControlContext: Contains security permissions for current execution
     * - Privileged actions: Operations that require special permissions
     * - Security policy: Rules that determine what code can do what
     *
     * Related methods:
     * - Executors.privilegedThreadFactory(): Creates privileged thread factory
     * - Executors.privilegedCallable(): Wraps Callable with current context
     * - Executors.privilegedCallableUsingCurrentClassLoader(): Adds classloader context
     *
     * Use cases:
     * - Applications running under SecurityManager
     * - Enterprise applications with complex security policies
     * - Code that needs to perform privileged operations in worker threads
     * - Applets and web applications with security constraints
     *
     * Without privileged factory:
     * - Worker threads may lack necessary permissions
     * - Security-sensitive operations may fail unexpectedly
     *
     * With privileged factory:
     * - Worker threads inherit creator's permissions
     * - Security context is properly maintained across thread boundaries
     */
    public static void demonstratePrivilegedExecutors() throws InterruptedException {
        System.out.println("\n=== Executors.privileged*** ===");

        // These methods create executors that preserve access control context
        ExecutorService privilegedExecutor = Executors.privilegedThreadFactory() != null ?
            Executors.newFixedThreadPool(2, Executors.privilegedThreadFactory()) :
            Executors.newFixedThreadPool(2);

        System.out.println("Using privileged thread factory:");

        privilegedExecutor.submit(() -> {
            System.out.println("Privileged task on: " + Thread.currentThread().getName());
            System.out.println("Thread is daemon: " + Thread.currentThread().isDaemon());
        });

        Thread.sleep(1000);

        privilegedExecutor.shutdown();
        privilegedExecutor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("Privileged executor completed\n");
    }

    /**
     * Performance comparison between different executor types
     *
     * What this demonstrates:
     * - How different executors perform under similar workloads
     * - Impact of thread management strategies on execution time
     * - Trade-offs between different executor implementations
     *
     * Test methodology:
     * - Same number of tasks for each executor type
     * - Same task duration (simulated work)
     * - Measure total execution time from submission to completion
     *
     * Expected results:
     * - FixedThreadPool: Consistent performance, limited parallelism
     * - CachedThreadPool: Fast for I/O-bound, may create many threads
     * - WorkStealingPool: Best for CPU-bound tasks with good load balancing
     * - SingleThreadExecutor: Slowest but guaranteed ordering
     *
     * Factors affecting performance:
     * - Task type (CPU-bound vs I/O-bound)
     * - System resources (CPU cores, memory)
     * - Task duration and arrival rate
     * - Thread creation/destruction overhead
     *
     * Performance considerations:
     * - Throughput: Tasks completed per unit time
     * - Latency: Time from submission to completion
     * - Resource utilization: CPU, memory usage
     * - Scalability: Performance under increasing load
     */
    public static void compareExecutorPerformance() throws InterruptedException {
        System.out.println("\n=== Executor Performance Comparison ===");

        int taskCount = 100;
        int taskDuration = 10; // milliseconds

        // Test different executors
        testExecutorPerformance("FixedThreadPool(4)",
            Executors.newFixedThreadPool(4), taskCount, taskDuration);

        testExecutorPerformance("CachedThreadPool",
            Executors.newCachedThreadPool(), taskCount, taskDuration);

        testExecutorPerformance("WorkStealingPool",
            Executors.newWorkStealingPool(), taskCount, taskDuration);

        testExecutorPerformance("SingleThreadExecutor",
            Executors.newSingleThreadExecutor(), taskCount / 10, taskDuration); // Fewer tasks for single thread
    }

    private static void testExecutorPerformance(String name, ExecutorService executor,
                                              int taskCount, int taskDurationMs) throws InterruptedException {
        System.out.printf("Testing %s with %d tasks:%n", name, taskCount);

        long startTime = System.currentTimeMillis();

        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            Future<Void> future = executor.submit(() -> {
                try {
                    Thread.sleep(taskDurationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
            futures.add(future);
        }

        // Wait for all tasks to complete
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                // Handle exceptions
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("%s completed in: %d ms%n%n", name, (endTime - startTime));

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * Real-world use cases for different executor types
     *
     * This method demonstrates practical scenarios where specific
     * executor types are most appropriate, showing how to match
     * executor characteristics with application requirements.
     *
     * Use cases covered:
     * 1. Web Server - FixedThreadPool for request handling
     * 2. Background Processing - SingleThreadExecutor for sequential tasks
     * 3. Scheduled Maintenance - ScheduledExecutorService for periodic tasks
     *
     * Selection criteria:
     * - Workload characteristics (CPU vs I/O bound)
     * - Concurrency requirements
     * - Resource constraints
     * - Ordering requirements
     * - Timing requirements
     *
     * Best practices:
     * - Match executor type to workload characteristics
     * - Consider resource limits and system capacity
     * - Plan for graceful shutdown
     * - Monitor performance and adjust as needed
     */
    public static void demonstratePracticalUseCases() throws InterruptedException {
        System.out.println("\n=== Practical Use Cases ===");

        // Use case 1: Web server request processing
        System.out.println("Use Case 1: Web Server (FixedThreadPool)");
        ExecutorService webServerPool = Executors.newFixedThreadPool(10);

        for (int i = 1; i <= 5; i++) {
            final int requestId = i;
            webServerPool.submit(() -> {
                System.out.printf("Processing HTTP request %d on %s%n",
                    requestId, Thread.currentThread().getName());
                // Simulate request processing
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.printf("HTTP request %d completed%n", requestId);
            });
        }

        webServerPool.shutdown();
        webServerPool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println();

        // Use case 2: Background task processing
        // SingleThreadExecutor ensures tasks execute in order and don't interfere
        // Perfect for sequential operations like email sending, logging, cleanup
        System.out.println("Use Case 2: Background Tasks (SingleThreadExecutor)");
        ExecutorService backgroundProcessor = Executors.newSingleThreadExecutor();

        backgroundProcessor.submit(() -> System.out.println("Email sending task"));
        backgroundProcessor.submit(() -> System.out.println("Log cleanup task"));
        backgroundProcessor.submit(() -> System.out.println("Cache warming task"));

        backgroundProcessor.shutdown();
        backgroundProcessor.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println();

        // Use case 3: Scheduled maintenance
        // ScheduledExecutorService for recurring system maintenance tasks
        // Handles periodic operations like cleanup, health checks, data backups
        System.out.println("Use Case 3: Scheduled Maintenance");
        ScheduledExecutorService maintenance = Executors.newSingleThreadScheduledExecutor();

        ScheduledFuture<?> cleanupTask = maintenance.scheduleAtFixedRate(() -> {
            System.out.println("Running cleanup task at " + new Date());
        }, 0, 2, TimeUnit.SECONDS);

        Thread.sleep(5000);
        cleanupTask.cancel(false);

        maintenance.shutdown();
        maintenance.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Comprehensive Executors.new*** Examples ===");

        demonstrateNewFixedThreadPool();
        Thread.sleep(500);

        demonstrateNewCachedThreadPool();
        Thread.sleep(500);

        demonstrateNewSingleThreadExecutor();
        Thread.sleep(500);

        demonstrateNewScheduledThreadPool();
        Thread.sleep(500);

        demonstrateNewSingleThreadScheduledExecutor();
        Thread.sleep(500);

        demonstrateNewWorkStealingPool();
        Thread.sleep(500);

        demonstrateNewWorkStealingPoolWithParallelism();
        Thread.sleep(500);

        demonstrateUnconfigurableExecutorService();
        Thread.sleep(500);

        demonstratePrivilegedExecutors();
        Thread.sleep(500);

        compareExecutorPerformance();
        Thread.sleep(500);

        demonstratePracticalUseCases();

        System.out.println("=== All Executors.new*** examples completed! ===");
    }
}