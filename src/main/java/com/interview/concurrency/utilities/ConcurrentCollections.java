package com.interview.concurrency.utilities;

import java.util.*;
import java.util.concurrent.*;

/**
 * Concurrent Collections - Comprehensive Guide to Thread-Safe Collection Implementations
 *
 * This class demonstrates Java's concurrent collection implementations from the
 * java.util.concurrent package. These collections are designed for multi-threaded
 * environments and provide better performance than synchronized wrappers.
 *
 * Traditional collections (HashMap, ArrayList, etc.) are not thread-safe and
 * require external synchronization. Java provides several alternatives:
 * 1. Synchronized wrappers (Collections.synchronizedMap, etc.)
 * 2. Concurrent collections (ConcurrentHashMap, etc.)
 * 3. Copy-on-write collections (CopyOnWriteArrayList, etc.)
 *
 * Concurrent collections covered:
 * - ConcurrentHashMap: Thread-safe hash map with segment-based locking
 * - CopyOnWriteArrayList: Thread-safe list using copy-on-write strategy
 * - ConcurrentLinkedQueue: Lock-free thread-safe queue
 * - BlockingQueue implementations: Thread-safe queues with blocking operations
 * - ConcurrentSkipListMap: Thread-safe sorted map
 *
 * Key advantages of concurrent collections:
 * - Better performance than synchronized wrappers
 * - Designed for concurrent access from ground up
 * - Lock-free or fine-grained locking strategies
 * - Atomic operations and memory consistency guarantees
 * - Scalability under high contention
 *
 * Performance characteristics:
 * - ConcurrentHashMap: Excellent for read-heavy and mixed workloads
 * - CopyOnWriteArrayList: Great for read-heavy, small collections
 * - ConcurrentLinkedQueue: High-performance lock-free operations
 * - BlockingQueues: Efficient producer-consumer coordination
 *
 * Interview topics:
 * - When to use each concurrent collection
 * - Performance trade-offs vs synchronized collections
 * - Internal implementation details (segmentation, copy-on-write)
 * - Memory consistency and happens-before relationships
 * - Blocking vs non-blocking operations
 *
 * Real-world applications:
 * - Web application caches
 * - Producer-consumer systems
 * - Configuration management
 * - Message passing systems
 * - High-throughput data processing
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class ConcurrentCollections {

    /**
     * Demonstrates ConcurrentHashMap - high-performance thread-safe hash map
     *
     * ConcurrentHashMap is a thread-safe implementation of Map that provides
     * better performance than synchronized HashMap or Hashtable. It uses
     * segment-based locking (Java 7) or node-based locking (Java 8+) to
     * allow concurrent access.
     *
     * Key characteristics:
     * - Thread-safe without external synchronization
     * - Allows concurrent reads and writes
     * - No blocking on read operations
     * - Fine-grained locking for better performance
     * - Atomic operations for common patterns
     *
     * Performance benefits:
     * - Multiple readers can access concurrently
     * - Writers only block conflicting operations
     * - Lock-free reads in most cases
     * - Better scalability under high contention
     *
     * Atomic operations demonstrated:
     * - compute(): Atomic computation of new value
     * - merge(): Atomic merge of existing and new values
     * - reduceValues(): Parallel reduction operation
     *
     * Use cases:
     * - Application caches
     * - Session storage
     * - Configuration maps
     * - Statistics collection
     */
    public static void demonstrateConcurrentHashMap() {
        System.out.println("\n=== ConcurrentHashMap ===");
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Writer task - demonstrates concurrent write operations
        Runnable writerTask = () -> {
            for (int i = 0; i < 100; i++) {
                String key = "key" + i;
                // put() is thread-safe and atomic
                map.put(key, i);
                System.out.println(Thread.currentThread().getName() + " put: " + key + " = " + i);
                try {
                    Thread.sleep(10);  // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Reader task - demonstrates concurrent read operations
        Runnable readerTask = () -> {
            for (int i = 0; i < 100; i++) {
                String key = "key" + i;
                // get() is lock-free and doesn't block writes
                Integer value = map.get(key);
                if (value != null) {
                    System.out.println(Thread.currentThread().getName() + " read: " + key + " = " + value);
                }
                try {
                    Thread.sleep(10);  // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Submit 1 writer and 2 readers to demonstrate concurrent access
        executor.submit(writerTask);
        executor.submit(readerTask);
        executor.submit(readerTask);

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        // Demonstrate atomic operations
        // compute() atomically computes new value based on key and current value
        map.compute("key50", (k, v) -> v == null ? 1 : v + 1);
        System.out.println("After compute on key50: " + map.get("key50"));

        // merge() atomically merges new value with existing value
        map.merge("key60", 100, Integer::sum);
        System.out.println("After merge on key60: " + map.get("key60"));

        // reduceValues() performs parallel reduction across all values
        long count = map.reduceValues(1, v -> v > 50 ? 1L : 0L, Long::sum);
        System.out.println("Count of values > 50: " + count);
    }

    /**
     * Demonstrates CopyOnWriteArrayList - thread-safe list with copy-on-write strategy
     *
     * CopyOnWriteArrayList is a thread-safe variant of ArrayList where all write
     * operations (add, set, remove) create a new copy of the underlying array.
     * This provides thread safety without locking for read operations.
     *
     * Key characteristics:
     * - Thread-safe without external synchronization
     * - Lock-free reads (very fast)
     * - Expensive writes (creates new array copy)
     * - Snapshot consistency for iterations
     * - Memory overhead due to copying
     *
     * Copy-on-write strategy:
     * 1. Read operations access current array directly (no locking)
     * 2. Write operations create a new array with changes
     * 3. Reference to array is atomically updated
     * 4. Old array remains unchanged for ongoing iterations
     *
     * Performance characteristics:
     * - Excellent for read-heavy workloads
     * - Poor for write-heavy workloads
     * - Memory usage can be high during writes
     * - No blocking between readers and writers
     *
     * Use cases:
     * - Configuration lists that rarely change
     * - Observer/listener lists
     * - Cache invalidation lists
     * - Event handler registrations
     *
     * Trade-offs:
     * - Pros: Fast reads, no read-write contention, snapshot consistency
     * - Cons: Expensive writes, memory overhead, eventual consistency
     */
    public static void demonstrateCopyOnWriteArrayList() {
        System.out.println("\n=== CopyOnWriteArrayList ===");
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Writer task - demonstrates copy-on-write behavior
        Runnable writerTask = () -> {
            for (int i = 0; i < 10; i++) {
                String item = Thread.currentThread().getName() + "-item-" + i;
                // add() creates a new array copy (expensive operation)
                list.add(item);
                System.out.println("Added: " + item);
                try {
                    Thread.sleep(100);  // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Reader task - demonstrates lock-free reading and snapshot consistency
        Runnable readerTask = () -> {
            for (int i = 0; i < 5; i++) {
                // size() and iteration are lock-free and very fast
                System.out.println(Thread.currentThread().getName() + " reading list size: " + list.size());
                // Iterator provides snapshot view - won't see changes during iteration
                for (String item : list) {
                    System.out.println("  " + item);
                }
                try {
                    Thread.sleep(200);  // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Submit writer and reader to demonstrate concurrent access
        executor.submit(writerTask);
        executor.submit(readerTask);

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("Final list size: " + list.size());
    }

    /**
     * Demonstrates ConcurrentLinkedQueue - high-performance lock-free queue
     *
     * ConcurrentLinkedQueue is an unbounded, thread-safe FIFO queue based on
     * linked nodes. It uses lock-free algorithms (compare-and-swap operations)
     * to provide excellent performance under high contention.
     *
     * Key characteristics:
     * - Unbounded capacity (limited only by memory)
     * - Lock-free implementation using CAS operations
     * - FIFO (First-In-First-Out) ordering
     * - Non-blocking operations
     * - Wait-free reads and writes
     *
     * Performance benefits:
     * - No locking overhead
     * - Excellent scalability under contention
     * - No thread blocking or waiting
     * - Cache-friendly memory access patterns
     *
     * Operations demonstrated:
     * - offer(): Add element to tail (always succeeds)
     * - poll(): Remove and return head element (null if empty)
     * - size(): Count elements (expensive operation - O(n))
     *
     * Use cases:
     * - High-throughput producer-consumer scenarios
     * - Message passing between threads
     * - Work stealing queues
     * - Event processing systems
     *
     * Important notes:
     * - size() operation is expensive (linear time)
     * - No blocking - poll() returns null if empty
     * - Memory usage grows with queue size
     */
    public static void demonstrateConcurrentLinkedQueue() {
        System.out.println("\n=== ConcurrentLinkedQueue ===");
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Producer task - demonstrates lock-free insertion
        Runnable producerTask = () -> {
            for (int i = 0; i < 20; i++) {
                // offer() is lock-free and always succeeds (unbounded queue)
                queue.offer(i);
                System.out.println(Thread.currentThread().getName() + " produced: " + i);
                try {
                    Thread.sleep(50);  // Simulate production time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Consumer task - demonstrates lock-free removal
        Runnable consumerTask = () -> {
            for (int i = 0; i < 10; i++) {
                // poll() is lock-free and returns null if queue is empty
                Integer value = queue.poll();
                if (value != null) {
                    System.out.println(Thread.currentThread().getName() + " consumed: " + value);
                }
                try {
                    Thread.sleep(100);  // Simulate consumption time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Submit 2 producers and 2 consumers for concurrent access
        executor.submit(producerTask);
        executor.submit(producerTask);
        executor.submit(consumerTask);
        executor.submit(consumerTask);

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        // Note: size() is O(n) operation - expensive for large queues
        System.out.println("Remaining items in queue: " + queue.size());
    }

    /**
     * Demonstrates various BlockingQueue implementations
     *
     * BlockingQueue extends Queue to provide blocking operations that wait
     * when the queue is empty (for retrieval) or full (for insertion).
     * These queues are essential for producer-consumer patterns.
     *
     * BlockingQueue implementations:
     * - ArrayBlockingQueue: Bounded FIFO queue backed by array
     * - LinkedBlockingQueue: Optionally bounded FIFO queue backed by linked nodes
     * - PriorityBlockingQueue: Unbounded priority queue
     * - DelayQueue: Unbounded queue of delayed elements
     * - SynchronousQueue: Zero-capacity direct handoff queue
     *
     * Blocking operations:
     * - put(): Insert element, wait if queue is full
     * - take(): Remove element, wait if queue is empty
     * - offer(timeout): Insert with timeout
     * - poll(timeout): Remove with timeout
     *
     * Key benefits:
     * - Built-in producer-consumer coordination
     * - No need for explicit wait/notify logic
     * - Automatic blocking and unblocking
     * - Thread-safe operations
     *
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public static void demonstrateBlockingQueues() throws InterruptedException {
        System.out.println("\n=== Blocking Queues Comparison ===");

        // ArrayBlockingQueue: Fixed-size array-based queue
        System.out.println("ArrayBlockingQueue (bounded):");
        ArrayBlockingQueue<Integer> arrayQueue = new ArrayBlockingQueue<>(5);
        testBlockingQueue(arrayQueue);

        // LinkedBlockingQueue: Linked nodes with optional capacity limit
        System.out.println("\nLinkedBlockingQueue (optionally bounded):");
        LinkedBlockingQueue<Integer> linkedQueue = new LinkedBlockingQueue<>(5);
        testBlockingQueue(linkedQueue);

        // PriorityBlockingQueue: Unbounded queue with natural ordering
        System.out.println("\nPriorityBlockingQueue (unbounded, ordered):");
        PriorityBlockingQueue<Integer> priorityQueue = new PriorityBlockingQueue<>();
        priorityQueue.offer(5);  // Elements are ordered by natural ordering
        priorityQueue.offer(1);  // or custom Comparator
        priorityQueue.offer(3);
        System.out.println("Elements taken in order: " + priorityQueue.take() + ", " +
                         priorityQueue.take() + ", " + priorityQueue.take());

        // DelayQueue: Elements available only after their delay expires
        System.out.println("\nDelayQueue:");
        DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();
        delayQueue.offer(new DelayedTask("Task1", 1000));  // 1 second delay
        delayQueue.offer(new DelayedTask("Task2", 500));   // 0.5 second delay
        delayQueue.offer(new DelayedTask("Task3", 1500));  // 1.5 second delay

        System.out.println("Taking delayed tasks:");
        for (int i = 0; i < 3; i++) {
            // take() blocks until element's delay expires
            DelayedTask task = delayQueue.take();
            System.out.println("Executed: " + task.getName());
        }

        // SynchronousQueue: Direct handoff between producer and consumer
        System.out.println("\nSynchronousQueue (no capacity):");
        SynchronousQueue<Integer> syncQueue = new SynchronousQueue<>();

        // Producer thread - blocks until consumer takes the value
        Thread producer = new Thread(() -> {
            try {
                System.out.println("Producer offering value...");
                // put() blocks until another thread calls take()
                syncQueue.put(42);
                System.out.println("Producer: Value accepted");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer thread - takes value directly from producer
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(1000);  // Delay to show producer waiting
                System.out.println("Consumer taking value...");
                // take() receives value directly from producer thread
                Integer value = syncQueue.take();
                System.out.println("Consumer received: " + value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Start threads to demonstrate direct handoff
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }

    /**
     * Helper method to test common BlockingQueue operations
     *
     * @param queue the BlockingQueue implementation to test
     * @throws InterruptedException if thread is interrupted while waiting
     */
    private static void testBlockingQueue(BlockingQueue<Integer> queue) throws InterruptedException {
        // Add elements using offer() (non-blocking)
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        System.out.println("Initial queue: " + queue);
        // take() blocks if queue is empty
        System.out.println("Take: " + queue.take());
        // poll() with timeout waits for specified time
        System.out.println("Poll with timeout: " + queue.poll(1, TimeUnit.SECONDS));
        System.out.println("Remaining: " + queue);
    }

    /**
     * DelayedTask implements Delayed interface for use with DelayQueue
     *
     * Delayed interface requires:
     * - getDelay(): Returns remaining delay time
     * - compareTo(): Compares delays for ordering
     */
    static class DelayedTask implements Delayed {
        private final String name;
        private final long delayTime;
        private final long startTime;  // Absolute time when task becomes available

        public DelayedTask(String name, long delayMillis) {
            this.name = name;
            this.delayTime = delayMillis;
            this.startTime = System.currentTimeMillis() + delayMillis;
        }

        public String getName() {
            return name;
        }

        /**
         * Returns remaining delay time in specified units
         * Negative values indicate delay has expired
         */
        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        /**
         * Compares delays for ordering in DelayQueue
         * Earlier expiration times have higher priority
         */
        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.startTime, ((DelayedTask) o).startTime);
        }
    }

    /**
     * Demonstrates ConcurrentSkipListMap - thread-safe sorted map
     *
     * ConcurrentSkipListMap is a thread-safe, sorted map implementation
     * based on skip lists. It provides expected O(log n) performance
     * for most operations and maintains keys in sorted order.
     *
     * Key characteristics:
     * - Thread-safe NavigableMap implementation
     * - Keys maintained in sorted order
     * - O(log n) expected time for most operations
     * - Lock-free reads in most cases
     * - Supports range operations
     *
     * Skip list advantages:
     * - Simpler than balanced trees
     * - Better for concurrent access
     * - Probabilistic balancing
     * - Lock-free search operations
     *
     * NavigableMap operations:
     * - firstEntry(), lastEntry(): Access endpoints
     * - subMap(), headMap(), tailMap(): Range views
     * - ceilingKey(), floorKey(): Approximate searches
     *
     * Use cases:
     * - Thread-safe sorted caches
     * - Priority-based data structures
     * - Range queries on sorted data
     * - Concurrent sorted collections
     */
    public static void demonstrateConcurrentSkipListMap() {
        System.out.println("\n=== ConcurrentSkipListMap ===");
        ConcurrentSkipListMap<Integer, String> skipListMap = new ConcurrentSkipListMap<>();

        // Insert elements in random order - they'll be stored sorted
        skipListMap.put(3, "Three");
        skipListMap.put(1, "One");
        skipListMap.put(4, "Four");
        skipListMap.put(2, "Two");

        // Elements are automatically sorted by key
        System.out.println("Sorted map: " + skipListMap);
        System.out.println("First entry: " + skipListMap.firstEntry());
        System.out.println("Last entry: " + skipListMap.lastEntry());

        // Range operations
        System.out.println("Submap [2,4): " + skipListMap.subMap(2, 4));
        System.out.println("Head map (< 3): " + skipListMap.headMap(3));
        System.out.println("Tail map (>= 3): " + skipListMap.tailMap(3));
    }

    /**
     * Compares performance of different thread-safe map implementations
     *
     * This method demonstrates the performance differences between:
     * 1. HashMap with external synchronization
     * 2. Collections.synchronizedMap wrapper
     * 3. ConcurrentHashMap
     *
     * Performance characteristics:
     * - HashMap + sync: Coarse-grained locking, poor scalability
     * - synchronizedMap: Wrapper with method-level synchronization
     * - ConcurrentHashMap: Fine-grained locking, better scalability
     *
     * Key differences:
     * - Synchronization granularity
     * - Read vs write performance
     * - Scalability under contention
     * - Memory overhead
     *
     * Expected results (single-threaded):
     * - Similar performance for sequential access
     * - ConcurrentHashMap may have slight overhead
     * - Real benefits appear under concurrent access
     *
     * @throws InterruptedException if thread is interrupted
     */
    public static void compareCollections() throws InterruptedException {
        System.out.println("\n=== Collections Performance Comparison ===");
        int iterations = 100000;

        // Different map implementations for comparison
        Map<String, Integer> hashMap = new HashMap<>();
        Map<String, Integer> synchronizedMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, Integer> concurrentMap = new ConcurrentHashMap<>();

        System.out.println("Testing with " + iterations + " iterations:");

        // HashMap with external synchronization (coarse-grained locking)
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            synchronized (hashMap) {  // Synchronize on entire map
                hashMap.put("key" + i, i);
            }
        }
        System.out.println("HashMap with synchronization: " + (System.currentTimeMillis() - startTime) + " ms");

        // Collections.synchronizedMap (method-level synchronization)
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            synchronizedMap.put("key" + i, i);  // Each method call is synchronized
        }
        System.out.println("Collections.synchronizedMap: " + (System.currentTimeMillis() - startTime) + " ms");

        // ConcurrentHashMap (fine-grained, lock-free design)
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            concurrentMap.put("key" + i, i);  // Optimized for concurrent access
        }
        System.out.println("ConcurrentHashMap: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    /**
     * Main method demonstrating all concurrent collection implementations
     *
     * Executes each demonstration method in sequence, showcasing the complete
     * range of Java's concurrent collections. Each example builds understanding
     * of thread-safe collection design patterns and performance characteristics.
     *
     * Execution flow progresses through different collection types:
     * 1. ConcurrentHashMap - high-performance thread-safe map
     * 2. CopyOnWriteArrayList - read-optimized thread-safe list
     * 3. ConcurrentLinkedQueue - lock-free high-performance queue
     * 4. BlockingQueue variants - producer-consumer coordination
     * 5. ConcurrentSkipListMap - thread-safe sorted map
     * 6. Performance comparison - empirical performance analysis
     *
     * Learning progression:
     * - Understand different synchronization strategies
     * - Learn when to use each collection type
     * - Appreciate performance trade-offs
     * - Master concurrent programming patterns
     *
     * @param args command line arguments (not used)
     * @throws InterruptedException if any demonstration is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Collections Examples ===");

        demonstrateConcurrentHashMap();
        Thread.sleep(1000);  // Pause between demonstrations

        demonstrateCopyOnWriteArrayList();
        Thread.sleep(1000);

        demonstrateConcurrentLinkedQueue();
        Thread.sleep(1000);

        demonstrateBlockingQueues();
        Thread.sleep(1000);

        demonstrateConcurrentSkipListMap();
        Thread.sleep(1000);

        compareCollections();
    }
}