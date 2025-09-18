package com.interview.concurrency.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadLocalExamples {

    // ThreadLocal with initialValue() override
    private static final ThreadLocal<Integer> threadLocalValue = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    // ThreadLocal with withInitial() - modern approach
    private static final ThreadLocal<StringBuilder> stringBuilder =
        ThreadLocal.withInitial(() -> new StringBuilder());

    // ThreadLocal SimpleDateFormat - avoids thread safety issues
    private static final ThreadLocal<SimpleDateFormat> dateFormatter =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    // ThreadLocal for user session context
    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    // User context data class
    static class UserContext {
        private final String userId;
        private final String sessionId;
        private final long timestamp;

        public UserContext(String userId, String sessionId) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.timestamp = System.currentTimeMillis();
        }

        public String getUserId() { return userId; }
        public String getSessionId() { return sessionId; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("UserContext{userId='%s', sessionId='%s', timestamp=%d}",
                userId, sessionId, timestamp);
        }
    }

    public static void demonstrateBasicThreadLocal() {
        System.out.println("=== Basic ThreadLocal Example ===");

        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();

            // Thread isolation - each thread has separate value
            threadLocalValue.set(new Random().nextInt(100));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.printf("Thread %s: ThreadLocal value = %d%n",
                threadName, threadLocalValue.get());

            // Modify ThreadLocal value
            threadLocalValue.set(threadLocalValue.get() + 10);
            System.out.printf("Thread %s: After increment = %d%n",
                threadName, threadLocalValue.get());
        };

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            executor.submit(task);
        }

        shutdownExecutor(executor);
    }

    public static void demonstrateStringBuilderThreadLocal() {
        System.out.println("\n=== StringBuilder ThreadLocal Example ===");

        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            StringBuilder sb = stringBuilder.get();

            // Reset and reuse StringBuilder safely
            sb.setLength(0);
            sb.append("Hello from ").append(threadName);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            sb.append(" - processed at ").append(System.currentTimeMillis());
            System.out.println(sb.toString());
        };

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 4; i++) {
            executor.submit(task);
        }

        shutdownExecutor(executor);
    }

    public static void demonstrateDateFormatterThreadLocal() {
        System.out.println("\n=== DateFormatter ThreadLocal Example ===");

        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            SimpleDateFormat formatter = dateFormatter.get();

            // Thread-safe date formatting
            String formattedDate = formatter.format(new Date());

            System.out.printf("Thread %s: Current time = %s%n", threadName, formattedDate);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Format again
            String formattedDate2 = formatter.format(new Date());
            System.out.printf("Thread %s: After delay = %s%n", threadName, formattedDate2);
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 3; i++) {
            executor.submit(task);
        }

        shutdownExecutor(executor);
    }

    public static void demonstrateUserContextThreadLocal() {
        System.out.println("\n=== User Context ThreadLocal Example ===");

        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();

            // Set context for current thread
            UserContext context = new UserContext(
                "user_" + threadName.substring(threadName.length() - 1),
                "session_" + System.currentTimeMillis()
            );
            userContext.set(context);

            // Process request using context
            processUserRequest();

            // Always clean up to prevent memory leaks
            userContext.remove();
        };

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 4; i++) {
            executor.submit(task);
        }

        shutdownExecutor(executor);
    }

    private static void processUserRequest() {
        String threadName = Thread.currentThread().getName();
        UserContext context = userContext.get();

        if (context != null) {
            System.out.printf("Thread %s: Processing request for %s%n",
                threadName, context);

            // Business logic with context access
            authenticateUser(context.getUserId());
            authorizeUser(context.getUserId());

        } else {
            System.out.printf("Thread %s: No user context found%n", threadName);
        }
    }

    private static void authenticateUser(String userId) {
        UserContext context = userContext.get();
        System.out.printf("  Authenticating user %s (session: %s)%n",
            userId, context != null ? context.getSessionId() : "unknown");
    }

    private static void authorizeUser(String userId) {
        UserContext context = userContext.get();
        System.out.printf("  Authorizing user %s (context: %s)%n",
            userId, context != null ? "present" : "missing");
    }

    public static void demonstrateInheritableThreadLocal() {
        System.out.println("\n=== InheritableThreadLocal Example ===");

        // Child threads inherit parent's value
        InheritableThreadLocal<String> inheritableValue = new InheritableThreadLocal<>();

        // Set value in parent
        inheritableValue.set("Value from parent thread");

        Thread childThread = new Thread(() -> {
            String inherited = inheritableValue.get();
            System.out.printf("Child thread inherited: %s%n", inherited);

            // Child can modify independently
            inheritableValue.set("Modified by child thread");
            System.out.printf("Child thread modified value to: %s%n", inheritableValue.get());
        });

        childThread.start();

        try {
            childThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Parent remains unchanged
        System.out.printf("Parent thread still has: %s%n", inheritableValue.get());
    }

    public static void demonstrateThreadLocalMemoryLeak() {
        System.out.println("\n=== ThreadLocal Memory Leak Prevention ===");

        ThreadLocal<byte[]> largeDataThreadLocal = new ThreadLocal<>();

        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();

            // Store large data
            largeDataThreadLocal.set(new byte[1024 * 1024]);
            System.out.printf("Thread %s: Stored large data in ThreadLocal%n", threadName);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Critical: clean up to prevent memory leaks
            largeDataThreadLocal.remove();
            System.out.printf("Thread %s: Cleaned up ThreadLocal%n", threadName);
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 3; i++) {
            executor.submit(task);
        }

        shutdownExecutor(executor);
    }

    private static void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        demonstrateBasicThreadLocal();
        demonstrateStringBuilderThreadLocal();
        demonstrateDateFormatterThreadLocal();
        demonstrateUserContextThreadLocal();
        demonstrateInheritableThreadLocal();
        demonstrateThreadLocalMemoryLeak();
    }
}