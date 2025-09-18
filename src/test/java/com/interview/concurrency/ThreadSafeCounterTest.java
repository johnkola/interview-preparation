package com.interview.concurrency;

import com.interview.concurrency.synchronization.ThreadSafeCounter;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;

class ThreadSafeCounterTest {

    @Test
    void testSynchronizedCounter() throws InterruptedException {
        ThreadSafeCounter.SynchronizedCounter counter = new ThreadSafeCounter.SynchronizedCounter();
        int numThreads = 10;
        int incrementsPerThread = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(counter.getCount()).isEqualTo(numThreads * incrementsPerThread);
    }

    @Test
    void testAtomicCounter() throws InterruptedException {
        ThreadSafeCounter.AtomicCounter counter = new ThreadSafeCounter.AtomicCounter();
        int numThreads = 10;
        int incrementsPerThread = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(counter.getCount()).isEqualTo(numThreads * incrementsPerThread);
    }
}