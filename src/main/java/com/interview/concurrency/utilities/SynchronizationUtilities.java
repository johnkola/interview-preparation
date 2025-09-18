package com.interview.concurrency.utilities;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

/**
 * Synchronization Utilities - Comprehensive Guide to Java Concurrency Utilities
 *
 * This class demonstrates the advanced synchronization utilities provided by
 * java.util.concurrent package. These utilities solve common coordination problems
 * in multi-threaded applications and are essential for building robust concurrent systems.
 *
 * Java concurrency utilities provide higher-level abstractions than basic synchronized
 * blocks and wait/notify mechanisms. They offer better performance, more features,
 * and easier-to-use APIs for complex synchronization scenarios.
 *
 * Synchronization utilities covered:
 * - CountDownLatch: One-time synchronization barrier
 * - CyclicBarrier: Reusable synchronization barrier with action
 * - Semaphore: Resource access control with permits
 * - Phaser: Advanced multi-phase synchronization
 * - Exchanger: Data exchange between two threads
 * - ReadWriteLock: Separate read and write locking
 * - StampedLock: Optimistic reading with lock-free operations
 * - Atomic Variables: Lock-free thread-safe operations
 *
 * Key benefits:
 * - Better performance than traditional synchronization
 * - More flexible coordination mechanisms
 * - Reduced complexity in concurrent programming
 * - Built-in timeout and interruption support
 * - Lock-free algorithms for high performance
 *
 * Interview topics:
 * - When to use each synchronization utility
 * - Performance characteristics and trade-offs
 * - Deadlock prevention and avoidance
 * - Lock-free vs lock-based algorithms
 * - Producer-consumer patterns
 * - Barrier synchronization patterns
 *
 * Real-world applications:
 * - Parallel processing frameworks
 * - Web server request handling
 * - Database connection pooling
 * - Cache implementations
 * - Batch processing systems
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class SynchronizationUtilities {

    /**
     * Demonstrates CountDownLatch - one-time synchronization barrier
     *
     * CountDownLatch is a synchronization aid that allows one or more threads
     * to wait until a set of operations being performed in other threads completes.
     * It's initialized with a count and threads can wait until the count reaches zero.
     *
     * Key characteristics:
     * - One-time use (cannot be reset)
     * - Thread-safe countdown mechanism
     * - Blocking wait until count reaches zero
     * - Useful for coordinating start/completion of tasks
     *
     * Common use cases:
     * - Wait for all worker threads to complete before proceeding
     * - Signal multiple threads to start simultaneously
     * - Implement "join" behavior for multiple threads
     * - Coordinate phases in multi-threaded algorithms
     *
     * Pattern demonstrated:
     * 1. Create startSignal latch (count=1) to coordinate start
     * 2. Create doneSignal latch (count=workerCount) to wait for completion
     * 3. Workers wait for startSignal before beginning work
     * 4. Main thread counts down startSignal to release all workers
     * 5. Workers count down doneSignal when they complete
     * 6. Main thread waits on doneSignal for all workers to finish
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public static void demonstrateCountDownLatch() throws InterruptedException {
        System.out.println("\n=== CountDownLatch ===");
        int workerCount = 5;
        // Latch to coordinate worker start (count=1)
        CountDownLatch startSignal = new CountDownLatch(1);
        // Latch to wait for all workers to complete (count=workerCount)
        CountDownLatch doneSignal = new CountDownLatch(workerCount);

        ExecutorService executor = Executors.newFixedThreadPool(workerCount);

        // Submit worker tasks that wait for start signal
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Worker " + workerId + " waiting to start...");
                    // Wait for start signal from main thread
                    startSignal.await();
                    System.out.println("Worker " + workerId + " started working");
                    // Simulate work with random delay
                    Thread.sleep((long) (Math.random() * 1000));
                    System.out.println("Worker " + workerId + " completed");
                    // Signal completion to main thread
                    doneSignal.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Give workers time to get ready
        Thread.sleep(1000);
        System.out.println("Main thread: Starting all workers...");
        // Release all workers simultaneously
        startSignal.countDown();

        System.out.println("Main thread: Waiting for all workers to complete...");
        // Wait for all workers to signal completion
        doneSignal.await();
        System.out.println("Main thread: All workers completed!");

        executor.shutdown();
    }

    /**
     * Demonstrates CyclicBarrier - reusable synchronization barrier
     *
     * CyclicBarrier is a synchronization aid that allows a set of threads to
     * all wait for each other to reach a common barrier point. Unlike CountDownLatch,
     * it can be reused after the waiting threads are released.
     *
     * Key characteristics:
     * - Reusable (can be used multiple times)
     * - Fixed number of parties (threads) must arrive
     * - Optional barrier action executed when all parties arrive
     * - Useful for iterative algorithms with synchronization points
     *
     * Common use cases:
     * - Parallel algorithms with multiple phases
     * - Synchronizing worker threads at checkpoints
     * - Implementing parallel matrix operations
     * - Multi-phase simulations
     *
     * Pattern demonstrated:
     * 1. Create barrier with fixed number of parties and barrier action
     * 2. Each thread performs work for a phase
     * 3. Threads call await() when they reach the barrier
     * 4. When all parties arrive, barrier action executes
     * 5. All threads are released to continue to next phase
     * 6. Process repeats for subsequent phases
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public static void demonstrateCyclicBarrier() throws InterruptedException {
        System.out.println("\n=== CyclicBarrier ===");
        int parties = 4;

        // Create barrier with action that executes when all parties arrive
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> {
            System.out.println("Barrier reached! All parties arrived. Proceeding to next phase...\n");
        });

        ExecutorService executor = Executors.newFixedThreadPool(parties);

        // Submit participants that will synchronize at barrier points
        for (int i = 0; i < parties; i++) {
            final int participantId = i;
            executor.submit(() -> {
                try {
                    // Execute 3 phases with barrier synchronization
                    for (int phase = 1; phase <= 3; phase++) {
                        // Simulate work with random delay
                        Thread.sleep((long) (Math.random() * 1000));
                        System.out.println("Participant " + participantId + " completed phase " + phase);
                        // Wait for all participants to complete this phase
                        barrier.await();
                        // Barrier action executes here when all parties arrive
                        // Then all threads continue to next phase
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * Demonstrates Semaphore - resource access control with permits
     *
     * Semaphore is a counting semaphore that maintains a set of permits.
     * Threads can acquire permits to access a resource and release permits
     * when done. It's useful for controlling access to limited resources.
     *
     * Key characteristics:
     * - Maintains a count of available permits
     * - acquire() decrements permit count (blocks if none available)
     * - release() increments permit count
     * - Can be fair or unfair (FIFO vs arbitrary order)
     *
     * Common use cases:
     * - Database connection pooling
     * - Rate limiting (API calls, downloads)
     * - Resource pooling (file handles, sockets)
     * - Controlling concurrent access to services
     *
     * Pattern demonstrated:
     * 1. Create semaphore with limited permits (3 in this case)
     * 2. Multiple tasks (10) compete for permits
     * 3. Tasks acquire permit before accessing resource
     * 4. Tasks release permit after using resource
     * 5. Only 3 tasks can access resource simultaneously
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public static void demonstrateSemaphore() throws InterruptedException {
        System.out.println("\n=== Semaphore ===");
        int permits = 3;  // Allow only 3 concurrent resource users
        Semaphore semaphore = new Semaphore(permits);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Submit 10 tasks that will compete for 3 permits
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Task " + taskId + " waiting for permit...");
                    // Acquire permit (blocks if none available)
                    semaphore.acquire();
                    System.out.println("Task " + taskId + " acquired permit. Available permits: " +
                                     semaphore.availablePermits());
                    // Simulate resource usage
                    Thread.sleep(2000);
                    System.out.println("Task " + taskId + " releasing permit");
                    // Release permit for other tasks to use
                    semaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);
    }

    /**
     * Demonstrates Phaser - advanced multi-phase synchronization
     *
     * Phaser is a more flexible synchronization barrier that supports
     * multiple phases of execution. Unlike CyclicBarrier, it allows
     * dynamic registration/deregistration of parties and custom phase completion logic.
     *
     * Key characteristics:
     * - Supports multiple phases (0, 1, 2, ...)
     * - Dynamic party registration/deregistration
     * - Customizable phase advance logic via onAdvance()
     * - Can terminate when conditions are met
     * - Hierarchical phasers for tree-like synchronization
     *
     * Common use cases:
     * - Iterative parallel algorithms
     * - Fork-join style computations
     * - Parallel simulations with multiple rounds
     * - Pipeline processing with stages
     *
     * Pattern demonstrated:
     * 1. Create phaser with initial party count
     * 2. Override onAdvance() for custom phase completion logic
     * 3. Parties call arriveAndAwaitAdvance() at phase boundaries
     * 4. Phaser automatically advances to next phase when all parties arrive
     * 5. Custom logic determines when to terminate
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public static void demonstratePhaser() throws InterruptedException {
        System.out.println("\n=== Phaser ===");
        int parties = 3;
        // Create phaser with custom phase completion logic
        Phaser phaser = new Phaser(parties) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("Phase " + phase + " completed. Parties: " + registeredParties);
                // Terminate after phase 2 or when no parties left
                return phase >= 2 || registeredParties == 0;
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(parties);

        // Submit participants for multi-phase processing
        for (int i = 0; i < parties; i++) {
            final int participantId = i;
            executor.submit(() -> {
                try {
                    // Execute 3 phases (0, 1, 2)
                    for (int phase = 0; phase < 3; phase++) {
                        // Simulate work for current phase
                        Thread.sleep((long) (Math.random() * 1000));
                        System.out.println("Participant " + participantId + " arriving at phase " + phase);
                        // Arrive at phase barrier and wait for others
                        phaser.arriveAndAwaitAdvance();
                        // onAdvance() is called here when all parties arrive
                    }
                    // Participant automatically deregisters when task completes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * Demonstrates Exchanger - data exchange between two threads
     *
     * Exchanger provides a synchronization point where two threads can
     * exchange data. Each thread presents some data and receives data
     * from the other thread. It's useful for pipeline processing.
     *
     * Key characteristics:
     * - Synchronizes exactly two threads
     * - Bidirectional data exchange
     * - Blocking until both threads arrive
     * - Type-safe exchange of objects
     *
     * Common use cases:
     * - Pipeline stages exchanging work items
     * - Buffer swapping between producer/consumer
     * - Genetic algorithms (crossover operations)
     * - Parallel merge operations
     *
     * Pattern demonstrated:
     * 1. Create exchanger for specific data type
     * 2. Two threads each prepare their data
     * 3. Both threads call exchange() with their data
     * 4. Threads block until both arrive at exchange point
     * 5. Each thread receives the other's data
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public static void demonstrateExchanger() throws InterruptedException {
        System.out.println("\n=== Exchanger ===");
        Exchanger<String> exchanger = new Exchanger<>();

        // First thread prepares and exchanges data
        Thread thread1 = new Thread(() -> {
            try {
                String data1 = "Data from Thread-1";
                System.out.println("Thread-1 sending: " + data1);
                // Exchange data with other thread (blocks until both threads call exchange)
                String received = exchanger.exchange(data1);
                System.out.println("Thread-1 received: " + received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Second thread prepares and exchanges data
        Thread thread2 = new Thread(() -> {
            try {
                // Simulate preparation time
                Thread.sleep(1000);
                String data2 = "Data from Thread-2";
                System.out.println("Thread-2 sending: " + data2);
                // Exchange data with other thread
                String received = exchanger.exchange(data2);
                System.out.println("Thread-2 received: " + received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Start both threads
        thread1.start();
        thread2.start();
        // Wait for both threads to complete
        thread1.join();
        thread2.join();
    }

    /**
     * Demonstrates ReadWriteLock - separate read and write locking
     *
     * ReadWriteLock maintains a pair of locks: one for read operations and
     * one for write operations. Multiple readers can access data concurrently,
     * but writers have exclusive access. This improves performance when reads
     * are more frequent than writes.
     *
     * Key characteristics:
     * - Multiple concurrent readers allowed
     * - Exclusive access for writers
     * - No readers allowed while writing
     * - No writers allowed while reading
     * - Can be fair or unfair
     *
     * Common use cases:
     * - Cache implementations
     * - Configuration objects
     * - Read-heavy data structures
     * - Database-like scenarios
     *
     * Performance benefits:
     * - Higher throughput for read-heavy workloads
     * - Reduced contention compared to exclusive locks
     * - Better scalability with multiple readers
     *
     * Pattern demonstrated:
     * 1. Create ReadWriteLock and separate read/write locks
     * 2. Readers acquire read lock (multiple can proceed)
     * 3. Writers acquire write lock (exclusive access)
     * 4. Proper lock/unlock in try-finally blocks
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public static void demonstrateReadWriteLock() throws InterruptedException {
        System.out.println("\n=== ReadWriteLock ===");

        /**
         * Shared resource protected by ReadWriteLock
         * Demonstrates concurrent reads and exclusive writes
         */
        class SharedResource {
            private final ReadWriteLock lock = new ReentrantReadWriteLock();
            private final Lock readLock = lock.readLock();   // For concurrent reads
            private final Lock writeLock = lock.writeLock(); // For exclusive writes
            private int value = 0;

            /**
             * Read operation - allows concurrent access by multiple threads
             */
            public int read() {
                readLock.lock();  // Multiple threads can acquire read lock
                try {
                    System.out.println(Thread.currentThread().getName() + " reading: " + value);
                    Thread.sleep(100);  // Simulate read operation
                    return value;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                } finally {
                    readLock.unlock();
                }
            }

            /**
             * Write operation - requires exclusive access
             */
            public void write(int newValue) {
                writeLock.lock();  // Only one thread can acquire write lock
                try {
                    System.out.println(Thread.currentThread().getName() + " writing: " + newValue);
                    Thread.sleep(100);  // Simulate write operation
                    value = newValue;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    writeLock.unlock();
                }
            }
        }

        SharedResource resource = new SharedResource();
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Submit multiple read operations (can execute concurrently)
        for (int i = 0; i < 3; i++) {
            executor.submit(() -> resource.read());
        }

        // Submit write operation (will wait for all reads to complete)
        executor.submit(() -> resource.write(42));

        // Submit more read operations (will wait for write to complete)
        for (int i = 0; i < 2; i++) {
            executor.submit(() -> resource.read());
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Demonstrates StampedLock - optimistic reading with lock-free operations
     *
     * StampedLock is a capability-based lock with three modes: writing, reading,
     * and optimistic reading. The optimistic reading allows lock-free reads
     * that can improve performance significantly in read-heavy scenarios.
     *
     * Key characteristics:
     * - Three locking modes: write, read, optimistic read
     * - Optimistic reads are lock-free and very fast
     * - Uses stamps (long values) to validate lock state
     * - Can convert between lock modes
     * - Better performance than ReadWriteLock in many cases
     *
     * Locking modes:
     * - Write lock: Exclusive access for modifications
     * - Read lock: Shared access for reading
     * - Optimistic read: Lock-free read with validation
     *
     * Common use cases:
     * - High-performance read-mostly data structures
     * - Coordination in scientific computing
     * - Game state management
     * - Real-time systems requiring low latency
     *
     * Pattern demonstrated:
     * 1. Try optimistic read first (fastest path)
     * 2. Read data without holding any lock
     * 3. Validate that no writes occurred during read
     * 4. If validation fails, fall back to read lock
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public static void demonstrateStampedLock() throws InterruptedException {
        System.out.println("\n=== StampedLock ===");

        /**
         * Point class demonstrating StampedLock with optimistic reading
         */
        class Point {
            private double x, y;
            private final StampedLock lock = new StampedLock();

            /**
             * Write operation using exclusive write lock
             */
            public void move(double deltaX, double deltaY) {
                long stamp = lock.writeLock();  // Acquire exclusive write lock
                try {
                    x += deltaX;
                    y += deltaY;
                    System.out.println("Moved to (" + x + ", " + y + ")");
                } finally {
                    lock.unlockWrite(stamp);  // Release using stamp
                }
            }

            /**
             * Read operation using optimistic reading with fallback
             * Demonstrates the key benefit of StampedLock
             */
            public double distanceFromOrigin() {
                // Step 1: Try optimistic read (no locking)
                long stamp = lock.tryOptimisticRead();
                double currentX = x;  // Read without lock
                double currentY = y;  // Read without lock

                // Step 2: Validate that no write occurred during reads
                if (!lock.validate(stamp)) {
                    // Validation failed - fall back to read lock
                    stamp = lock.readLock();
                    try {
                        currentX = x;  // Re-read with lock
                        currentY = y;  // Re-read with lock
                    } finally {
                        lock.unlockRead(stamp);
                    }
                }
                // If validation succeeded, we have consistent data without locking!

                double distance = Math.sqrt(currentX * currentX + currentY * currentY);
                System.out.println("Distance from origin: " + distance);
                return distance;
            }
        }

        Point point = new Point();
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Submit alternating write and read operations
        for (int i = 0; i < 5; i++) {
            final int delta = i;
            // Write operation (uses write lock)
            executor.submit(() -> point.move(delta, delta));
            // Read operation (tries optimistic read first)
            executor.submit(() -> point.distanceFromOrigin());
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Demonstrates Atomic Variables - lock-free thread-safe operations
     *
     * Atomic variables provide lock-free, thread-safe operations on single variables.
     * They use low-level atomic hardware primitives (compare-and-swap) to ensure
     * atomicity without the overhead of synchronization.
     *
     * Key characteristics:
     * - Lock-free operations using compare-and-swap (CAS)
     * - Better performance than synchronized blocks for simple operations
     * - No risk of deadlock or priority inversion
     * - Built-in memory visibility guarantees
     * - Support for atomic read-modify-write operations
     *
     * Atomic classes available:
     * - AtomicInteger, AtomicLong, AtomicBoolean
     * - AtomicReference for object references
     * - AtomicIntegerArray, AtomicLongArray, AtomicReferenceArray
     * - LongAdder, LongAccumulator for high-contention scenarios
     *
     * Common use cases:
     * - Counters and statistics
     * - Flags and state variables
     * - Lock-free data structures
     * - Performance-critical scenarios
     *
     * Benefits over synchronized:
     * - Better performance under contention
     * - No blocking or waiting
     * - Immune to deadlock
     * - Guaranteed progress (wait-free for reads)
     */
    public static void demonstrateAtomicVariables() {
        System.out.println("\n=== Atomic Variables ===");

        // AtomicInteger - thread-safe integer operations
        AtomicInteger atomicInt = new AtomicInteger(0);
        System.out.println("AtomicInteger initial: " + atomicInt.get());
        System.out.println("Increment and get: " + atomicInt.incrementAndGet());  // ++i
        System.out.println("Get and increment: " + atomicInt.getAndIncrement());  // i++
        System.out.println("Current value: " + atomicInt.get());
        System.out.println("Add and get 5: " + atomicInt.addAndGet(5));
        // Compare-and-swap: if current value is 8, set to 10
        System.out.println("Compare and set (8, 10): " + atomicInt.compareAndSet(8, 10));
        System.out.println("Current value: " + atomicInt.get());

        // AtomicBoolean - thread-safe boolean operations
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        System.out.println("\nAtomicBoolean initial: " + atomicBoolean.get());
        System.out.println("Get and set true: " + atomicBoolean.getAndSet(true));
        System.out.println("Current value: " + atomicBoolean.get());

        // AtomicReference - thread-safe object reference operations
        AtomicReference<String> atomicRef = new AtomicReference<>("Initial");
        System.out.println("\nAtomicReference initial: " + atomicRef.get());
        atomicRef.set("Updated");
        System.out.println("After set: " + atomicRef.get());

        // AtomicLongArray - thread-safe array operations
        AtomicLongArray atomicArray = new AtomicLongArray(5);
        atomicArray.set(0, 100);
        atomicArray.set(1, 200);
        System.out.println("\nAtomicLongArray[0]: " + atomicArray.get(0));
        System.out.println("Increment array[1]: " + atomicArray.incrementAndGet(1));

        // LongAdder - optimized for high-contention counting
        LongAdder longAdder = new LongAdder();
        longAdder.add(10);
        longAdder.add(20);
        System.out.println("\nLongAdder sum: " + longAdder.sum());

        // LongAccumulator - generalized accumulation with custom function
        LongAccumulator accumulator = new LongAccumulator(Long::max, 0);
        accumulator.accumulate(5);   // max(0, 5) = 5
        accumulator.accumulate(10);  // max(5, 10) = 10
        accumulator.accumulate(3);   // max(10, 3) = 10
        System.out.println("LongAccumulator (max): " + accumulator.get());
    }

    /**
     * Main method demonstrating all synchronization utilities
     *
     * Executes each demonstration method in sequence, showing the full range
     * of Java's advanced synchronization utilities. Each example builds upon
     * fundamental concepts to create a comprehensive learning experience.
     *
     * Execution order progresses from simple to complex:
     * 1. CountDownLatch - basic one-time coordination
     * 2. CyclicBarrier - reusable barrier with actions
     * 3. Semaphore - resource access control
     * 4. Phaser - advanced multi-phase synchronization
     * 5. Exchanger - two-thread data exchange
     * 6. ReadWriteLock - reader-writer separation
     * 7. StampedLock - optimistic lock-free reading
     * 8. Atomic Variables - lock-free operations
     *
     * @param args command line arguments (not used)
     * @throws InterruptedException if any demonstration is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Synchronization Utilities Examples ===");

        demonstrateCountDownLatch();
        Thread.sleep(1000);  // Pause between demonstrations

        demonstrateCyclicBarrier();
        Thread.sleep(1000);

        demonstrateSemaphore();
        Thread.sleep(1000);

        demonstratePhaser();
        Thread.sleep(1000);

        demonstrateExchanger();
        Thread.sleep(1000);

        demonstrateReadWriteLock();
        Thread.sleep(1000);

        demonstrateStampedLock();
        Thread.sleep(1000);

        demonstrateAtomicVariables();
    }
}