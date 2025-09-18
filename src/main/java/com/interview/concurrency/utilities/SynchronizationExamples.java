package com.interview.concurrency.utilities;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Random;

/**
 * Advanced Synchronization Utilities Examples
 *
 * COVERED UTILITIES:
 * 1. COUNTDOWNLATCH - One-time coordination, waiting for multiple tasks
 * 2. CYCLICBARRIER - Reusable synchronization point for threads
 * 3. SEMAPHORE - Resource access control, permits management
 * 4. PHASER - Advanced barrier with dynamic registration
 * 5. EXCHANGER - Thread pair data exchange
 * 6. REENTRANTLOCK - Explicit locking with fairness
 * 7. READWRITELOCK - Separate read/write access control
 */
public class SynchronizationExamples {

    // =========================
    // COUNTDOWNLATCH
    // =========================

    /**
     * COUNTDOWNLATCH: One-time coordination mechanism
     *
     * PURPOSE: Wait for multiple threads to complete before proceeding
     * USE CASES: Service startup, batch processing, parallel initialization
     *
     * KEY CONCEPTS:
     * - CountDownLatch vs CyclicBarrier: CountDownLatch = one-time use, CyclicBarrier = reusable
     * - Master-worker pattern: One thread waits for multiple workers to finish
     * - count() decrements from initial value to zero
     */
    public static void demonstrateCountDownLatch() throws InterruptedException {
        System.out.println("=== COUNTDOWNLATCH Example ===");

        int numTasks = 3;
        CountDownLatch latch = new CountDownLatch(numTasks); // Count starts at 3

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit multiple tasks
        for (int i = 0; i < numTasks; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.printf("Task %d starting...%n", taskId);
                    Thread.sleep(1000 + new Random().nextInt(2000)); // Simulate work
                    System.out.printf("Task %d completed%n", taskId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown(); // Decrement count (3 -> 2 -> 1 -> 0)
                }
            });
        }

        System.out.println("Main thread waiting for all tasks to complete...");
        latch.await(); // Blocks until count reaches 0
        System.out.println("All tasks completed! Main thread proceeding.\n");

        executor.shutdown();
    }

    // =========================
    // CYCLICBARRIER
    // =========================

    /**
     * CYCLICBARRIER: Reusable synchronization point
     *
     * PURPOSE: Synchronization point where threads wait for each other
     * USE CASES: Parallel algorithms, phase-based processing, gaming
     *
     * KEY CONCEPTS:
     * - 'Cyclic' because it can be reused after all threads reach barrier
     * - Barrier vs Latch: Barrier = threads wait for each other, Latch = one waits for many
     * - Barrier action executes when all threads reach the barrier
     */
    public static void demonstrateCyclicBarrier() throws InterruptedException {
        System.out.println("=== CYCLICBARRIER Example ===");

        int numThreads = 3;
        CyclicBarrier barrier = new CyclicBarrier(numThreads, () -> {
            // Barrier action runs when all threads reach barrier
            System.out.println("*** All threads reached barrier! Proceeding to next phase ***");
        });

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // Phase 1
                    System.out.printf("Thread %d: Phase 1 work...%n", threadId);
                    Thread.sleep(1000 + new Random().nextInt(2000));
                    System.out.printf("Thread %d: Phase 1 complete, waiting at barrier%n", threadId);

                    barrier.await(); // Wait for all threads to reach here

                    // Phase 2
                    System.out.printf("Thread %d: Phase 2 work...%n", threadId);
                    Thread.sleep(1000);
                    System.out.printf("Thread %d: Phase 2 complete%n", threadId);

                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Thread.sleep(6000); // Wait for completion
        executor.shutdown();
        System.out.println();
    }

    // =========================
    // SEMAPHORE
    // =========================

    /**
     * SEMAPHORE: Resource access control
     *
     * PURPOSE: Control access to limited resources
     * USE CASES: Connection pools, rate limiting, resource management
     *
     * KEY CONCEPTS:
     * - Binary Semaphore vs Mutex: Similar purpose, but semaphore doesn't have ownership
     * - Permits system: acquire() takes permit, release() returns permit
     * - Common use: Limiting concurrent access to resources (DB connections, etc.)
     */
    public static void demonstrateSemaphore() throws InterruptedException {
        System.out.println("=== SEMAPHORE Example ===");

        Semaphore semaphore = new Semaphore(2); // Only 2 permits available
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.printf("Task %d: Requesting resource...%n", taskId);
                    semaphore.acquire(); // Acquire permit (blocks if none available)
                    System.out.printf("Task %d: Got resource! Working...%n", taskId);

                    Thread.sleep(2000); // Simulate resource usage

                    System.out.printf("Task %d: Releasing resource%n", taskId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release(); // Always release in finally block
                }
            });
        }

        Thread.sleep(8000);
        executor.shutdown();
        System.out.println();
    }

    // =========================
    // PHASER
    // =========================

    /**
     * PHASER: Advanced barrier with dynamic registration (Java 7+)
     *
     * PURPOSE: Advanced barrier with dynamic thread registration
     * ADVANTAGES: Dynamic parties, multiple phases, more flexible than CyclicBarrier
     *
     * FEATURES:
     * - Dynamic registration/deregistration of parties
     * - Multiple phases with different participants
     * - Modern alternative to CountDownLatch/CyclicBarrier
     */
    public static void demonstratePhaser() throws InterruptedException {
        System.out.println("=== PHASER Example ===");

        Phaser phaser = new Phaser(1); // Start with 1 party (main thread)

        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            phaser.register(); // Dynamically register new party

            new Thread(() -> {
                System.out.printf("Task %d: Phase 0 work%n", taskId);
                phaser.arriveAndAwaitAdvance(); // Wait for phase 0 completion

                System.out.printf("Task %d: Phase 1 work%n", taskId);
                phaser.arriveAndAwaitAdvance(); // Wait for phase 1 completion

                System.out.printf("Task %d: Finished%n", taskId);
                phaser.arriveAndDeregister(); // Unregister from phaser
            }).start();
        }

        phaser.arriveAndAwaitAdvance(); // Main thread waits for phase 0
        System.out.println("Main: All tasks completed phase 0");

        phaser.arriveAndAwaitAdvance(); // Main thread waits for phase 1
        System.out.println("Main: All tasks completed phase 1");

        phaser.arriveAndDeregister(); // Main thread unregisters
        Thread.sleep(1000);
        System.out.println();
    }

    // =========================
    // EXCHANGER
    // =========================

    /**
     * EXCHANGER: Thread pair data exchange
     *
     * PURPOSE: Two threads exchange data at synchronization point
     * USE CASES: Producer-consumer, data pipelines, thread coordination
     *
     * LIMITATION: Works with exactly 2 threads only
     * BEHAVIOR: Both threads must call exchange() to proceed
     */
    public static void demonstrateExchanger() throws InterruptedException {
        System.out.println("=== EXCHANGER Example ===");

        Exchanger<String> exchanger = new Exchanger<>();

        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                String data = "Data from Producer";
                System.out.println("Producer: Sending - " + data);
                String received = exchanger.exchange(data); // Exchange data
                System.out.println("Producer: Received - " + received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                String data = "Data from Consumer";
                System.out.println("Consumer: Sending - " + data);
                String received = exchanger.exchange(data); // Exchange data
                System.out.println("Consumer: Received - " + received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        System.out.println();
    }

    // =========================
    // REENTRANTLOCK
    // =========================

    /**
     * REENTRANTLOCK: Explicit locking with advanced features
     *
     * PURPOSE: Explicit locking with advanced features
     * ADVANTAGES: Fairness, try-lock, interruptible, condition variables
     *
     * COMPARISON:
     * - ReentrantLock: More features, explicit control, fairness options
     * - synchronized: Simpler syntax, automatic unlock, JVM optimized
     */
    public static void demonstrateReentrantLock() throws InterruptedException {
        System.out.println("=== REENTRANTLOCK Example ===");

        ReentrantLock lock = new ReentrantLock(true); // Fair lock (FIFO order)
        AtomicInteger counter = new AtomicInteger(0);

        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();

            try {
                // Try to acquire lock with timeout
                if (lock.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        System.out.printf("%s: Acquired lock%n", threadName);
                        counter.incrementAndGet();
                        Thread.sleep(500);
                        System.out.printf("%s: Counter = %d%n", threadName, counter.get());
                    } finally {
                        lock.unlock(); // Always unlock in finally
                        System.out.printf("%s: Released lock%n", threadName);
                    }
                } else {
                    System.out.printf("%s: Could not acquire lock%n", threadName);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executor.submit(task);
        }

        Thread.sleep(3000);
        executor.shutdown();
        System.out.println();
    }

    // =========================
    // READWRITELOCK
    // =========================

    /**
     * READWRITELOCK: Separate read and write access control
     *
     * PURPOSE: Separate read and write locks for better concurrency
     * BENEFIT: Multiple readers can access simultaneously, writers are exclusive
     *
     * OPTIMAL FOR: Read-heavy scenarios where writes are infrequent
     * BEHAVIOR: Readers share access, writers get exclusive access
     */
    public static void demonstrateReadWriteLock() throws InterruptedException {
        System.out.println("=== READWRITELOCK Example ===");

        ReadWriteLock rwLock = new ReentrantReadWriteLock();
        AtomicInteger sharedData = new AtomicInteger(0);

        // Reader task
        Runnable reader = () -> {
            String threadName = Thread.currentThread().getName();
            rwLock.readLock().lock(); // Multiple readers can acquire this
            try {
                System.out.printf("%s: Reading data = %d%n", threadName, sharedData.get());
                Thread.sleep(1000); // Simulate read operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.readLock().unlock();
                System.out.printf("%s: Read lock released%n", threadName);
            }
        };

        // Writer task
        Runnable writer = () -> {
            String threadName = Thread.currentThread().getName();
            rwLock.writeLock().lock(); // Only one writer can acquire this
            try {
                int newValue = sharedData.incrementAndGet();
                System.out.printf("%s: Writing data = %d%n", threadName, newValue);
                Thread.sleep(1000); // Simulate write operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.writeLock().unlock();
                System.out.printf("%s: Write lock released%n", threadName);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Submit readers and writers
        executor.submit(reader);
        executor.submit(reader);
        executor.submit(writer);
        executor.submit(reader);
        executor.submit(writer);

        Thread.sleep(6000);
        executor.shutdown();
        System.out.println();
    }

    /**
     * Main method to run all synchronization examples
     *
     * DEMONSTRATES:
     * - CountDownLatch: One-time coordination
     * - CyclicBarrier: Reusable synchronization points
     * - Semaphore: Resource access control
     * - Phaser: Dynamic barrier management
     * - Exchanger: Thread pair data exchange
     * - ReentrantLock: Advanced explicit locking
     * - ReadWriteLock: Concurrent read access
     */
    public static void main(String[] args) {
        try {
            demonstrateCountDownLatch(); // Master-worker coordination
            demonstrateCyclicBarrier();  // Phase-based synchronization
            demonstrateSemaphore();      // Resource access limiting
            demonstratePhaser();         // Dynamic barrier coordination
            demonstrateExchanger();      // Thread pair data exchange
            demonstrateReentrantLock();  // Advanced locking features
            demonstrateReadWriteLock();  // Read-write access optimization
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}