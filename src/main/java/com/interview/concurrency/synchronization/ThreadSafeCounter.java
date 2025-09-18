package com.interview.concurrency.synchronization;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeCounter {

    public static class UnsafeCounter {
        private int count = 0;

        public void increment() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    public static class SynchronizedCounter {
        private int count = 0;

        public synchronized void increment() {
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    public static class LockCounter {
        private int count = 0;
        private final Lock lock = new ReentrantLock();

        public void increment() {
            lock.lock();
            try {
                count++;
            } finally {
                lock.unlock();
            }
        }

        public int getCount() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
    }

    public static class AtomicCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        public void increment() {
            count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }
    }

    public static class VolatileCounter {
        private volatile int count = 0;

        public synchronized void increment() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    public static void testCounter(String name, Runnable incrementTask, java.util.function.Supplier<Integer> getCount) {
        int numThreads = 10;
        int incrementsPerThread = 1000;

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    incrementTask.run();
                }
            });
        }

        long startTime = System.currentTimeMillis();

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.printf("%s: Final count = %d (Expected: %d), Time: %d ms%n",
                name, getCount.get(), numThreads * incrementsPerThread, (endTime - startTime));
    }

    public static void main(String[] args) {
        System.out.println("=== Thread-Safe Counter Implementations ===\n");

        UnsafeCounter unsafeCounter = new UnsafeCounter();
        testCounter("Unsafe Counter", unsafeCounter::increment, unsafeCounter::getCount);

        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        testCounter("Synchronized Counter", synchronizedCounter::increment, synchronizedCounter::getCount);

        LockCounter lockCounter = new LockCounter();
        testCounter("Lock Counter", lockCounter::increment, lockCounter::getCount);

        AtomicCounter atomicCounter = new AtomicCounter();
        testCounter("Atomic Counter", atomicCounter::increment, atomicCounter::getCount);

        VolatileCounter volatileCounter = new VolatileCounter();
        testCounter("Volatile Counter", volatileCounter::increment, volatileCounter::getCount);
    }
}