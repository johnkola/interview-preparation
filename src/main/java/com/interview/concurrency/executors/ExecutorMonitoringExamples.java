package com.interview.concurrency.executors;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorMonitoringExamples {

    /**
     * Demonstrates comprehensive monitoring of ThreadPoolExecutor
     *
     * What this shows:
     * - How to track executor state during execution
     * - Key metrics for understanding thread pool behavior
     * - Real-time monitoring of pool size, active threads, and queue status
     *
     * Key metrics explained:
     * - Core pool size: Minimum number of threads kept alive
     * - Current pool size: Current number of threads in the pool
     * - Active threads: Number of threads currently executing tasks
     * - Maximum pool size: Maximum allowed number of threads
     * - Queue size: Number of tasks waiting to be executed
     * - Completed tasks: Total number of completed tasks
     * - Total tasks: Total number of tasks submitted
     *
     * Monitoring benefits:
     * - Identify performance bottlenecks
     * - Detect resource starvation
     * - Optimize pool configuration
     * - Track application health
     *
     * When to monitor:
     * - During performance testing
     * - In production for health checks
     * - When tuning executor parameters
     * - For capacity planning
     */
    public static void demonstrateThreadPoolExecutorMonitoring() throws InterruptedException {
        System.out.println("\n=== ThreadPoolExecutor Monitoring ===");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                      // Core pool size
            5,                      // Maximum pool size
            60L, TimeUnit.SECONDS,  // Keep alive time
            new LinkedBlockingQueue<>(10), // Work queue
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "MonitoredThread-" + threadNumber.getAndIncrement());
                    thread.setDaemon(false);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );

        // Submit tasks and monitor
        System.out.println("Submitting tasks and monitoring executor state:");

        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.printf("Task %d executing on %s%n", taskId, Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Monitor executor state after every few submissions
            if (i % 5 == 0) {
                printExecutorStats("After submitting " + i + " tasks", executor);
                Thread.sleep(500);
            }
        }

        // Final monitoring
        Thread.sleep(2000);
        printExecutorStats("Near completion", executor);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        printExecutorStats("After shutdown", executor);
        System.out.println();
    }

    private static void printExecutorStats(String phase, ThreadPoolExecutor executor) {
        System.out.printf("%n--- %s ---%n", phase);
        System.out.printf("Core pool size: %d%n", executor.getCorePoolSize());
        System.out.printf("Current pool size: %d%n", executor.getPoolSize());
        System.out.printf("Active threads: %d%n", executor.getActiveCount());
        System.out.printf("Maximum pool size: %d%n", executor.getMaximumPoolSize());
        System.out.printf("Queue size: %d%n", executor.getQueue().size());
        System.out.printf("Completed tasks: %d%n", executor.getCompletedTaskCount());
        System.out.printf("Total tasks: %d%n", executor.getTaskCount());
        System.out.printf("Is shutdown: %s%n", executor.isShutdown());
        System.out.printf("Is terminated: %s%n", executor.isTerminated());
        System.out.println();
    }

    /**
     * Custom metrics collection for executor performance analysis
     *
     * What this demonstrates:
     * - How to extend ThreadPoolExecutor for custom monitoring
     * - Tracking task execution times and success/failure rates
     * - Building performance dashboards and alerting systems
     *
     * Custom metrics collected:
     * - Total tasks submitted
     * - Completed vs failed task counts
     * - Individual task execution times
     * - Success rate percentage
     *
     * Implementation technique:
     * - Override beforeExecute() to record start time
     * - Override afterExecute() to calculate duration and track completion
     * - Use thread-safe counters (AtomicInteger) for metrics
     * - Use ConcurrentHashMap for per-task timing data
     *
     * Use cases:
     * - Performance profiling
     * - SLA monitoring
     * - Capacity planning
     * - Error rate tracking
     * - Service health dashboards
     *
     * Production considerations:
     * - Minimal performance overhead
     * - Memory efficient storage of metrics
     * - Periodic metrics export/reset
     * - Integration with monitoring systems (Prometheus, etc.)
     */
    public static void demonstrateExecutorServiceMetrics() throws InterruptedException {
        System.out.println("\n=== Executor Service with Custom Metrics ===");

        class MetricsCollectingExecutor extends ThreadPoolExecutor {
            private final AtomicInteger totalTasks = new AtomicInteger(0);
            private final AtomicInteger completedTasks = new AtomicInteger(0);
            private final AtomicInteger failedTasks = new AtomicInteger(0);
            private final ConcurrentHashMap<String, Long> taskExecutionTimes = new ConcurrentHashMap<>();

            public MetricsCollectingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                           TimeUnit unit, BlockingQueue<Runnable> workQueue) {
                super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
            }

            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                totalTasks.incrementAndGet();
                taskExecutionTimes.put(r.toString(), System.currentTimeMillis());
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Long startTime = taskExecutionTimes.remove(r.toString());
                if (startTime != null) {
                    long duration = System.currentTimeMillis() - startTime;
                    if (t == null) {
                        completedTasks.incrementAndGet();
                        System.out.printf("Task completed in %d ms%n", duration);
                    } else {
                        failedTasks.incrementAndGet();
                        System.out.printf("Task failed after %d ms: %s%n", duration, t.getMessage());
                    }
                }
            }

            public void printMetrics() {
                System.out.printf("%nExecutor Metrics:%n");
                System.out.printf("Total tasks submitted: %d%n", totalTasks.get());
                System.out.printf("Completed tasks: %d%n", completedTasks.get());
                System.out.printf("Failed tasks: %d%n", failedTasks.get());
                System.out.printf("Success rate: %.2f%%%n",
                    totalTasks.get() > 0 ? (completedTasks.get() * 100.0 / totalTasks.get()) : 0.0);
            }
        }

        MetricsCollectingExecutor executor = new MetricsCollectingExecutor(
            3, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
        );

        // Submit various tasks
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                if (taskId == 5) {
                    throw new RuntimeException("Simulated task failure");
                }
                Thread.sleep(200 + (taskId * 50));
                return "Result " + taskId;
            });
        }

        Thread.sleep(3000);

        executor.printMetrics();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println();
    }

    /**
     * System-level resource monitoring during executor operations
     *
     * What this monitors:
     * - JVM thread statistics using ThreadMXBean
     * - Memory usage patterns during task execution
     * - System resource utilization
     *
     * ThreadMXBean metrics:
     * - Current thread count: Active threads in JVM
     * - Peak thread count: Maximum threads ever created
     * - Total started threads: Cumulative thread creation count
     *
     * Memory metrics:
     * - Total memory: Maximum memory allocated to JVM
     * - Free memory: Available memory in heap
     * - Used memory: Currently allocated memory
     *
     * Why monitor system resources:
     * - Detect memory leaks from thread pools
     * - Identify thread creation patterns
     * - Monitor system impact of concurrent operations
     * - Optimize JVM settings and pool configurations
     *
     * Monitoring techniques:
     * - Before/during/after snapshots
     * - Correlation with task execution
     * - Trend analysis over time
     *
     * Warning signs:
     * - Continuously growing thread count
     * - Memory usage not returning to baseline
     * - Thread count approaching system limits
     */
    public static void demonstrateSystemResourceMonitoring() throws InterruptedException {
        System.out.println("\n=== System Resource Monitoring ===");

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Runtime runtime = Runtime.getRuntime();

        ExecutorService executor = Executors.newFixedThreadPool(4);

        System.out.println("Initial system state:");
        printSystemStats(threadBean, runtime);

        // Submit CPU-intensive tasks
        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.printf("CPU-intensive task %d started%n", taskId);

                // Simulate CPU work
                long start = System.currentTimeMillis();
                double result = 0;
                while (System.currentTimeMillis() - start < 1000) {
                    result += Math.sqrt(Math.random());
                }

                System.out.printf("Task %d completed with result: %.2f%n", taskId, result);
            });
        }

        // Monitor during execution
        Thread.sleep(2000);
        System.out.println("\nDuring task execution:");
        printSystemStats(threadBean, runtime);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("\nAfter task completion:");
        printSystemStats(threadBean, runtime);

        System.out.println();
    }

    private static void printSystemStats(ThreadMXBean threadBean, Runtime runtime) {
        System.out.printf("Thread count: %d%n", threadBean.getThreadCount());
        System.out.printf("Peak thread count: %d%n", threadBean.getPeakThreadCount());
        System.out.printf("Total started threads: %d%n", threadBean.getTotalStartedThreadCount());

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        System.out.printf("Memory - Used: %d MB, Free: %d MB, Total: %d MB%n",
            usedMemory / (1024 * 1024), freeMemory / (1024 * 1024), totalMemory / (1024 * 1024));

        System.out.printf("Available processors: %d%n", runtime.availableProcessors());
        System.out.println();
    }

    /**
     * Implementing health checks for executor services
     *
     * What this demonstrates:
     * - Automated health monitoring of executor state
     * - Defining health criteria for thread pools
     * - Alerting mechanisms for unhealthy executors
     *
     * Health criteria implemented:
     * - Queue not at capacity (can accept new tasks)
     * - Threads are active or queue is empty (making progress)
     * - Executor not in shutdown state
     *
     * Health check benefits:
     * - Early detection of performance issues
     * - Automated alerting for operational problems
     * - Integration with load balancers and service discovery
     * - Proactive maintenance and scaling decisions
     *
     * Implementation patterns:
     * - Periodic health checks using ScheduledExecutorService
     * - Health state transitions (healthy ↔ unhealthy)
     * - Configurable health criteria
     * - Integration with monitoring systems
     *
     * Production considerations:
     * - Health check frequency vs overhead
     * - Graceful degradation when unhealthy
     * - Circuit breaker patterns
     * - Automatic recovery mechanisms
     *
     * Common health indicators:
     * - Queue utilization percentage
     * - Task completion rate
     * - Thread utilization
     * - Error rate thresholds
     */
    public static void demonstrateExecutorHealthCheck() throws InterruptedException {
        System.out.println("\n=== Executor Health Check ===");

        class HealthMonitoredExecutor {
            private final ThreadPoolExecutor executor;
            private final ScheduledExecutorService healthChecker;
            private volatile boolean healthy = true;

            public HealthMonitoredExecutor(int poolSize) {
                this.executor = new ThreadPoolExecutor(
                    poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(50)
                );
                this.healthChecker = Executors.newSingleThreadScheduledExecutor();
                startHealthChecking();
            }

            private void startHealthChecking() {
                healthChecker.scheduleAtFixedRate(() -> {
                    checkHealth();
                }, 1, 1, TimeUnit.SECONDS);
            }

            private void checkHealth() {
                boolean wasHealthy = healthy;

                // Health criteria
                boolean queueNotFull = executor.getQueue().size() < executor.getQueue().remainingCapacity();
                boolean threadsActive = executor.getActiveCount() > 0 || executor.getQueue().isEmpty();
                boolean notShutdown = !executor.isShutdown();

                healthy = queueNotFull && threadsActive && notShutdown;

                if (healthy && !wasHealthy) {
                    System.out.println("✅ Executor is now HEALTHY");
                } else if (!healthy && wasHealthy) {
                    System.out.println("❌ Executor is now UNHEALTHY");
                }

                System.out.printf("Health Check - Queue: %d/%d, Active: %d, Healthy: %s%n",
                    executor.getQueue().size(),
                    executor.getQueue().size() + executor.getQueue().remainingCapacity(),
                    executor.getActiveCount(),
                    healthy ? "✅" : "❌"
                );
            }

            public void submit(Runnable task) {
                if (!healthy) {
                    System.out.println("⚠️  Warning: Submitting to unhealthy executor");
                }
                executor.submit(task);
            }

            public boolean isHealthy() {
                return healthy;
            }

            public void shutdown() {
                executor.shutdown();
                healthChecker.shutdown();
            }

            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                boolean executorTerminated = executor.awaitTermination(timeout, unit);
                healthChecker.awaitTermination(1, TimeUnit.SECONDS);
                return executorTerminated;
            }
        }

        HealthMonitoredExecutor monitoredExecutor = new HealthMonitoredExecutor(2);

        // Submit normal load
        System.out.println("Submitting normal load...");
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            monitoredExecutor.submit(() -> {
                try {
                    Thread.sleep(500);
                    System.out.printf("Normal task %d completed%n", taskId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Thread.sleep(3000);

        // Submit heavy load to potentially make it unhealthy
        System.out.println("\nSubmitting heavy load...");
        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            monitoredExecutor.submit(() -> {
                try {
                    Thread.sleep(2000);
                    System.out.printf("Heavy task %d completed%n", taskId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Thread.sleep(5000);

        System.out.printf("Final health status: %s%n", monitoredExecutor.isHealthy() ? "✅ Healthy" : "❌ Unhealthy");

        monitoredExecutor.shutdown();
        monitoredExecutor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println();
    }

    /**
     * Debugging techniques for troubleshooting executor issues
     *
     * What this covers:
     * - Custom thread naming for easier debugging
     * - Uncaught exception handling in worker threads
     * - Rejection handling when queues are full
     * - Logging executor state during problem scenarios
     *
     * Common executor problems:
     * - Tasks getting stuck or hanging
     * - RejectedExecutionException when overloaded
     * - Memory leaks from uncompleted tasks
     * - Uncaught exceptions causing thread death
     *
     * Debugging techniques shown:
     * 1. Custom ThreadFactory for meaningful thread names
     * 2. UncaughtExceptionHandler for error tracking
     * 3. Custom RejectedExecutionHandler for overload handling
     * 4. Detailed logging of executor state
     *
     * ThreadFactory benefits:
     * - Easier thread identification in logs
     * - Custom thread properties (daemon, priority)
     * - Consistent naming conventions
     *
     * Exception handling:
     * - Prevents silent task failures
     * - Enables proper error logging and alerting
     * - Maintains pool stability
     *
     * Rejection policies:
     * - AbortPolicy: Throw RejectedExecutionException (default)
     * - CallerRunsPolicy: Execute in caller thread (shown here)
     * - DiscardPolicy: Silently discard task
     * - DiscardOldestPolicy: Remove oldest queued task
     *
     * Production debugging tips:
     * - Always use meaningful thread names
     * - Log executor metrics during issues
     * - Implement proper exception handling
     * - Monitor rejection rates
     */
    public static void demonstrateExecutorDebugging() throws InterruptedException {
        System.out.println("\n=== Executor Debugging Techniques ===");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 2, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(5),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("DebugThread-" + thread.getId());
                thread.setUncaughtExceptionHandler((t, e) -> {
                    System.err.printf("Uncaught exception in thread %s: %s%n", t.getName(), e.getMessage());
                    e.printStackTrace();
                });
                return thread;
            },
            (r, executor1) -> {
                System.err.println("Task rejected! Queue is full. Task: " + r.toString());
                System.err.printf("Executor state - Pool size: %d, Active: %d, Queue size: %d%n",
                    executor1.getPoolSize(), executor1.getActiveCount(), executor1.getQueue().size());
            }
        );

        System.out.println("Submitting tasks with debugging enabled:");

        // Submit tasks that will fill the queue and trigger rejection
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            try {
                executor.submit(() -> {
                    System.out.printf("Debug task %d running on %s%n", taskId, Thread.currentThread().getName());

                    if (taskId == 3) {
                        throw new RuntimeException("Simulated exception in task " + taskId);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.printf("Task %d was interrupted%n", taskId);
                    }
                });
            } catch (RejectedExecutionException e) {
                System.err.println("Task " + taskId + " was rejected: " + e.getMessage());
            }

            Thread.sleep(100); // Small delay between submissions
        }

        Thread.sleep(5000);

        System.out.println("Shutting down executor...");
        executor.shutdown();

        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("Executor did not terminate gracefully, forcing shutdown...");
            executor.shutdownNow();
        }

        System.out.println("Debugging demonstration completed\n");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Executor Monitoring and Debugging Examples ===");

        demonstrateThreadPoolExecutorMonitoring();
        Thread.sleep(1000);

        demonstrateExecutorServiceMetrics();
        Thread.sleep(1000);

        demonstrateSystemResourceMonitoring();
        Thread.sleep(1000);

        demonstrateExecutorHealthCheck();
        Thread.sleep(1000);

        demonstrateExecutorDebugging();

        System.out.println("=== All monitoring examples completed! ===");
    }
}