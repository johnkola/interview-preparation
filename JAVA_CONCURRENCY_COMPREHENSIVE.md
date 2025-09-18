# Java Concurrency Comprehensive Guide & Interview Preparation

## 📖 Table of Contents

1. [Introduction to Java Concurrency](#introduction)
2. [Thread Fundamentals](#thread-fundamentals)
3. [Synchronization Mechanisms](#synchronization-mechanisms)
4. [Executor Framework](#executor-framework)
5. [Concurrent Collections](#concurrent-collections)
6. [Synchronization Utilities](#synchronization-utilities)
7. [Banking-Specific Concurrency Patterns](#banking-patterns)
8. [Common Concurrency Problems](#concurrency-problems)
9. [Interview Questions & Answers](#interview-questions)
10. [Best Practices](#best-practices)

---

## 🎯 Introduction to Java Concurrency {#introduction}

Java concurrency is the ability to execute multiple threads simultaneously to improve application performance, responsiveness, and resource utilization. In banking systems, concurrency is crucial for handling multiple transactions, user requests, and system operations simultaneously while maintaining data consistency and integrity.

### Why Concurrency Matters in Banking:
- **High Transaction Volume**: Process thousands of simultaneous transactions
- **Real-time Processing**: Immediate response to customer requests
- **Resource Optimization**: Efficient use of server resources
- **Scalability**: Handle increasing load without proportional resource increase
- **Regulatory Compliance**: Ensure data consistency for audit trails

### Key Concepts:
- **Thread**: Lightweight process that can run concurrently
- **Process**: Heavy-weight execution context with separate memory space
- **Concurrency**: Logical simultaneity (interleaving)
- **Parallelism**: Physical simultaneity (multiple cores)

---

## 🧵 Thread Fundamentals {#thread-fundamentals}

### Thread Creation Methods

Java provides multiple ways to create and manage threads:

```java
// Method 1: Extending Thread Class
public class BankTransactionThread extends Thread {
    private final String transactionId;
    private final AccountService accountService;

    public BankTransactionThread(String transactionId, AccountService accountService) {
        this.transactionId = transactionId;
        this.accountService = accountService;
        setName("Transaction-" + transactionId);
    }

    @Override
    public void run() {
        try {
            System.out.println("Processing transaction: " + transactionId +
                             " on thread: " + Thread.currentThread().getName());

            // Simulate transaction processing
            accountService.processTransaction(transactionId);

            System.out.println("Transaction " + transactionId + " completed successfully");

        } catch (Exception e) {
            System.err.println("Transaction " + transactionId + " failed: " + e.getMessage());
        }
    }
}

// Method 2: Implementing Runnable Interface (Preferred)
public class AccountBalanceChecker implements Runnable {
    private final String accountNumber;
    private final AccountService accountService;

    public AccountBalanceChecker(String accountNumber, AccountService accountService) {
        this.accountNumber = accountNumber;
        this.accountService = accountService;
    }

    @Override
    public void run() {
        try {
            BigDecimal balance = accountService.getBalance(accountNumber);
            System.out.println("Account " + accountNumber + " balance: $" + balance);

            // Check for low balance alert
            if (balance.compareTo(new BigDecimal("100")) < 0) {
                System.out.println("LOW BALANCE ALERT for account: " + accountNumber);
            }

        } catch (Exception e) {
            System.err.println("Error checking balance for " + accountNumber + ": " + e.getMessage());
        }
    }
}

// Method 3: Lambda Expressions (Modern Approach)
public class ConcurrentBankingExample {
    public static void demonstrateThreadCreation() {
        // Using lambda for simple tasks
        Thread auditThread = new Thread(() -> {
            System.out.println("Starting audit process...");
            try {
                Thread.sleep(2000); // Simulate audit work
                System.out.println("Audit completed on thread: " +
                                 Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Audit interrupted");
            }
        }, "Audit-Thread");

        // Start threads
        BankTransactionThread transaction = new BankTransactionThread("TXN001", new AccountService());
        Thread balanceChecker = new Thread(new AccountBalanceChecker("ACC123", new AccountService()));

        transaction.start();
        balanceChecker.start();
        auditThread.start();

        try {
            // Wait for all threads to complete
            transaction.join();
            balanceChecker.join();
            auditThread.join();

            System.out.println("All banking operations completed");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted");
        }
    }
}
```

### Thread Lifecycle and States

```java
public class ThreadLifecycleDemo {
    public static void demonstrateThreadStates() {
        Thread bankingThread = new Thread(() -> {
            try {
                System.out.println("Thread starting work...");

                // RUNNABLE state
                Thread.sleep(1000);  // TIMED_WAITING state

                System.out.println("Thread work completed");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted");
            }
        }, "Banking-Worker");

        // NEW state
        System.out.println("Initial State: " + bankingThread.getState());

        // Start the thread -> RUNNABLE
        bankingThread.start();
        System.out.println("After start(): " + bankingThread.getState());

        try {
            Thread.sleep(100);
            // During sleep -> TIMED_WAITING
            System.out.println("During execution: " + bankingThread.getState());

            // Wait for completion
            bankingThread.join();
            // After completion -> TERMINATED
            System.out.println("After completion: " + bankingThread.getState());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### Thread Safety Fundamentals

```java
public class ThreadSafetyExample {

    // Unsafe counter - Race condition prone
    static class UnsafeCounter {
        private int count = 0;

        public void increment() {
            count++; // Not atomic: read -> modify -> write
        }

        public int getCount() {
            return count;
        }
    }

    // Thread-safe counter using synchronization
    static class SafeCounter {
        private int count = 0;

        public synchronized void increment() {
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    // Atomic counter - Lock-free thread safety
    static class AtomicCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        public void increment() {
            count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }
    }

    public static void demonstrateThreadSafety() {
        final int THREAD_COUNT = 10;
        final int INCREMENTS_PER_THREAD = 1000;

        // Test unsafe counter
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        testCounter("Unsafe Counter", unsafeCounter::increment,
                   unsafeCounter::getCount, THREAD_COUNT, INCREMENTS_PER_THREAD);

        // Test safe counter
        SafeCounter safeCounter = new SafeCounter();
        testCounter("Safe Counter", safeCounter::increment,
                   safeCounter::getCount, THREAD_COUNT, INCREMENTS_PER_THREAD);

        // Test atomic counter
        AtomicCounter atomicCounter = new AtomicCounter();
        testCounter("Atomic Counter", atomicCounter::increment,
                   atomicCounter::getCount, THREAD_COUNT, INCREMENTS_PER_THREAD);
    }

    private static void testCounter(String name, Runnable incrementOp,
                                  Supplier<Integer> getCountOp,
                                  int threadCount, int incrementsPerThread) {

        System.out.println("\nTesting " + name + ":");

        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    incrementOp.run();
                }
            });
        }

        long startTime = System.currentTimeMillis();

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();
        int finalCount = getCountOp.get();
        int expectedCount = threadCount * incrementsPerThread;

        System.out.println("Expected: " + expectedCount +
                          ", Actual: " + finalCount +
                          ", Time: " + (endTime - startTime) + "ms");
        System.out.println("Correct: " + (finalCount == expectedCount));
    }
}
```

---

## 🔒 Synchronization Mechanisms {#synchronization-mechanisms}

### Synchronized Methods and Blocks

```java
public class BankAccount {
    private BigDecimal balance;
    private final String accountNumber;
    private final Object lock = new Object(); // Custom lock object

    public BankAccount(String accountNumber, BigDecimal initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    // Synchronized method - locks entire method
    public synchronized void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        balance = balance.add(amount);
        System.out.println("Deposited $" + amount + " to " + accountNumber +
                          ". New balance: $" + balance);

        // Notify waiting threads that balance has changed
        notifyAll();
    }

    // Synchronized method with exception handling
    public synchronized void withdraw(BigDecimal amount) throws InsufficientFundsException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Current balance: $" + balance);
        }

        balance = balance.subtract(amount);
        System.out.println("Withdrew $" + amount + " from " + accountNumber +
                          ". New balance: $" + balance);
    }

    // Synchronized block - more granular control
    public void transfer(BankAccount toAccount, BigDecimal amount) throws InsufficientFundsException {
        // Lock ordering to prevent deadlock
        BankAccount firstLock = this.accountNumber.compareTo(toAccount.accountNumber) < 0 ? this : toAccount;
        BankAccount secondLock = this.accountNumber.compareTo(toAccount.accountNumber) < 0 ? toAccount : this;

        synchronized (firstLock) {
            synchronized (secondLock) {
                this.withdraw(amount);
                toAccount.deposit(amount);

                System.out.println("Transferred $" + amount + " from " +
                                 this.accountNumber + " to " + toAccount.accountNumber);
            }
        }
    }

    // Read operation - synchronized for consistency
    public synchronized BigDecimal getBalance() {
        return balance;
    }

    // Wait/notify pattern for conditional synchronization
    public synchronized void waitForMinimumBalance(BigDecimal minimumBalance) {
        while (balance.compareTo(minimumBalance) < 0) {
            try {
                System.out.println("Waiting for minimum balance of $" + minimumBalance +
                                 " in account " + accountNumber);
                wait(); // Release lock and wait
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        System.out.println("Minimum balance achieved in account " + accountNumber);
    }
}

// Custom exception for banking operations
class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
```

### Producer-Consumer Pattern

```java
public class TransactionProcessor {

    // Thread-safe queue for transactions
    static class TransactionQueue {
        private final Queue<Transaction> queue = new LinkedList<>();
        private final int maxSize;

        public TransactionQueue(int maxSize) {
            this.maxSize = maxSize;
        }

        // Producer method
        public synchronized void addTransaction(Transaction transaction) throws InterruptedException {
            while (queue.size() >= maxSize) {
                System.out.println("Queue full, producer waiting...");
                wait(); // Wait until space is available
            }

            queue.offer(transaction);
            System.out.println("Added transaction: " + transaction.getId() +
                             " (Queue size: " + queue.size() + ")");

            notifyAll(); // Notify consumers that new transaction is available
        }

        // Consumer method
        public synchronized Transaction processTransaction() throws InterruptedException {
            while (queue.isEmpty()) {
                System.out.println("Queue empty, consumer waiting...");
                wait(); // Wait until transaction is available
            }

            Transaction transaction = queue.poll();
            System.out.println("Processing transaction: " + transaction.getId() +
                             " (Queue size: " + queue.size() + ")");

            notifyAll(); // Notify producers that space is available
            return transaction;
        }

        public synchronized int size() {
            return queue.size();
        }
    }

    // Transaction data class
    static class Transaction {
        private final String id;
        private final String fromAccount;
        private final String toAccount;
        private final BigDecimal amount;
        private final LocalDateTime timestamp;

        public Transaction(String id, String fromAccount, String toAccount, BigDecimal amount) {
            this.id = id;
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public String getId() { return id; }
        public String getFromAccount() { return fromAccount; }
        public String getToAccount() { return toAccount; }
        public BigDecimal getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // Producer thread
    static class TransactionProducer implements Runnable {
        private final TransactionQueue queue;
        private final String producerId;

        public TransactionProducer(TransactionQueue queue, String producerId) {
            this.queue = queue;
            this.producerId = producerId;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 5; i++) {
                    Transaction transaction = new Transaction(
                        producerId + "-TXN-" + i,
                        "ACC" + (1000 + i),
                        "ACC" + (2000 + i),
                        new BigDecimal("100.00")
                    );

                    queue.addTransaction(transaction);
                    Thread.sleep(100 + (int)(Math.random() * 200)); // Simulate varying load
                }
                System.out.println("Producer " + producerId + " finished");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Producer " + producerId + " interrupted");
            }
        }
    }

    // Consumer thread
    static class TransactionConsumer implements Runnable {
        private final TransactionQueue queue;
        private final String consumerId;

        public TransactionConsumer(TransactionQueue queue, String consumerId) {
            this.queue = queue;
            this.consumerId = consumerId;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Transaction transaction = queue.processTransaction();

                    // Simulate transaction processing time
                    Thread.sleep(150 + (int)(Math.random() * 100));

                    System.out.println("Consumer " + consumerId +
                                     " completed transaction: " + transaction.getId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Consumer " + consumerId + " interrupted");
            }
        }
    }

    public static void demonstrateProducerConsumer() {
        TransactionQueue queue = new TransactionQueue(5);

        // Create producer threads
        Thread producer1 = new Thread(new TransactionProducer(queue, "P1"), "Producer-1");
        Thread producer2 = new Thread(new TransactionProducer(queue, "P2"), "Producer-2");

        // Create consumer threads
        Thread consumer1 = new Thread(new TransactionConsumer(queue, "C1"), "Consumer-1");
        Thread consumer2 = new Thread(new TransactionConsumer(queue, "C2"), "Consumer-2");

        // Start all threads
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();

        try {
            // Wait for producers to finish
            producer1.join();
            producer2.join();

            // Give consumers time to process remaining transactions
            Thread.sleep(2000);

            // Stop consumers
            consumer1.interrupt();
            consumer2.interrupt();

            System.out.println("Producer-Consumer demo completed");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### Deadlock Prevention

```java
public class DeadlockPrevention {

    // Deadlock scenario: Two accounts trying to transfer to each other simultaneously
    static class UnsafeTransfer {
        public static void transfer(BankAccount from, BankAccount to, BigDecimal amount) {
            synchronized (from) {
                try {
                    Thread.sleep(100); // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                synchronized (to) {
                    try {
                        from.withdraw(amount);
                        to.deposit(amount);
                    } catch (InsufficientFundsException e) {
                        System.err.println("Transfer failed: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Safe transfer using lock ordering
    static class SafeTransfer {
        public static void transfer(BankAccount from, BankAccount to, BigDecimal amount) {
            // Always acquire locks in the same order based on account number
            BankAccount firstLock = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
            BankAccount secondLock = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? to : from;

            synchronized (firstLock) {
                synchronized (secondLock) {
                    try {
                        from.withdraw(amount);
                        to.deposit(amount);
                        System.out.println("Safe transfer completed: $" + amount +
                                         " from " + from.getAccountNumber() +
                                         " to " + to.getAccountNumber());
                    } catch (InsufficientFundsException e) {
                        System.err.println("Transfer failed: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Timeout-based approach
    static class TimeoutTransfer {
        private static final int TIMEOUT_MS = 1000;

        public static boolean transferWithTimeout(BankAccount from, BankAccount to, BigDecimal amount) {
            long deadline = System.currentTimeMillis() + TIMEOUT_MS;

            while (System.currentTimeMillis() < deadline) {
                if (tryTransfer(from, to, amount)) {
                    return true;
                }

                try {
                    Thread.sleep(10); // Brief pause before retry
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            System.err.println("Transfer timed out after " + TIMEOUT_MS + "ms");
            return false;
        }

        private static boolean tryTransfer(BankAccount from, BankAccount to, BigDecimal amount) {
            if (tryLock(from)) {
                try {
                    if (tryLock(to)) {
                        try {
                            from.withdraw(amount);
                            to.deposit(amount);
                            System.out.println("Timeout transfer completed: $" + amount);
                            return true;
                        } catch (InsufficientFundsException e) {
                            System.err.println("Transfer failed: " + e.getMessage());
                            return false;
                        } finally {
                            releaseLock(to);
                        }
                    }
                } finally {
                    releaseLock(from);
                }
            }
            return false;
        }

        private static boolean tryLock(BankAccount account) {
            // Simplified lock attempt - in real implementation, use ReentrantLock.tryLock()
            return true; // Placeholder
        }

        private static void releaseLock(BankAccount account) {
            // Simplified lock release - in real implementation, use ReentrantLock.unlock()
        }
    }

    public static void demonstrateDeadlockPrevention() {
        BankAccount account1 = new BankAccount("ACC001", new BigDecimal("1000"));
        BankAccount account2 = new BankAccount("ACC002", new BigDecimal("1000"));

        // Demonstrate safe transfer
        Thread t1 = new Thread(() -> {
            SafeTransfer.transfer(account1, account2, new BigDecimal("100"));
        }, "Transfer-1");

        Thread t2 = new Thread(() -> {
            SafeTransfer.transfer(account2, account1, new BigDecimal("200"));
        }, "Transfer-2");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();

            System.out.println("Final balance ACC001: $" + account1.getBalance());
            System.out.println("Final balance ACC002: $" + account2.getBalance());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## ⚡ Executor Framework {#executor-framework}

### Basic Executor Services

```java
public class BankingExecutorExamples {

    // Fixed thread pool for batch processing
    public static void demonstrateFixedThreadPool() {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        System.out.println("Processing daily interest calculations...");

        List<String> accounts = Arrays.asList(
            "ACC001", "ACC002", "ACC003", "ACC004", "ACC005",
            "ACC006", "ACC007", "ACC008", "ACC009", "ACC010"
        );

        for (String accountNumber : accounts) {
            executor.submit(() -> {
                try {
                    // Simulate interest calculation
                    BigDecimal interest = calculateInterest(accountNumber);
                    Thread.sleep(100); // Simulate processing time

                    System.out.println("Interest calculated for " + accountNumber +
                                     ": $" + interest +
                                     " [Thread: " + Thread.currentThread().getName() + "]");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Interest calculation interrupted for " + accountNumber);
                }
            });
        }

        shutdownExecutor(executor, "Interest Calculation");
    }

    // Scheduled executor for periodic tasks
    public static void demonstrateScheduledExecutor() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

        // Schedule daily backup at fixed rate
        ScheduledFuture<?> dailyBackup = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Performing daily backup... " + LocalDateTime.now());
            try {
                Thread.sleep(500); // Simulate backup time
                System.out.println("Daily backup completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 2, TimeUnit.SECONDS); // Start immediately, repeat every 2 seconds

        // Schedule monthly report with delay
        scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("Generating monthly report... " + LocalDateTime.now());
            try {
                Thread.sleep(800); // Simulate report generation
                System.out.println("Monthly report completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 1, 3, TimeUnit.SECONDS); // Start after 1 second, then every 3 seconds after completion

        // Schedule one-time compliance check
        scheduler.schedule(() -> {
            System.out.println("Running compliance check... " + LocalDateTime.now());
            System.out.println("Compliance check passed");
        }, 2, TimeUnit.SECONDS);

        // Run for 10 seconds then shutdown
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        scheduler.shutdown();
        System.out.println("Scheduled tasks completed");
    }

    // Callable and Future for result retrieval
    public static void demonstrateCallableAndFuture() {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Create tasks that return results
        List<Callable<AccountReport>> tasks = Arrays.asList(
            () -> generateAccountReport("ACC001"),
            () -> generateAccountReport("ACC002"),
            () -> generateAccountReport("ACC003")
        );

        try {
            // Submit all tasks and get futures
            List<Future<AccountReport>> futures = executor.invokeAll(tasks);

            // Collect results
            System.out.println("Account Reports Generated:");
            for (Future<AccountReport> future : futures) {
                try {
                    AccountReport report = future.get(5, TimeUnit.SECONDS);
                    System.out.println(report);
                } catch (TimeoutException e) {
                    System.err.println("Report generation timed out");
                    future.cancel(true);
                } catch (ExecutionException e) {
                    System.err.println("Report generation failed: " + e.getCause().getMessage());
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        shutdownExecutor(executor, "Report Generation");
    }

    // CompletableFuture for asynchronous processing
    public static void demonstrateCompletableFuture() {
        System.out.println("Demonstrating CompletableFuture for transaction processing...");

        // Async transaction validation
        CompletableFuture<Boolean> validationFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Validating transaction... [" + Thread.currentThread().getName() + "]");
            try {
                Thread.sleep(500); // Simulate validation time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            System.out.println("Transaction validation completed");
            return true;
        });

        // Async fraud check
        CompletableFuture<Boolean> fraudCheckFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Performing fraud check... [" + Thread.currentThread().getName() + "]");
            try {
                Thread.sleep(300); // Simulate fraud check time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            System.out.println("Fraud check completed");
            return true;
        });

        // Async balance verification
        CompletableFuture<Boolean> balanceCheckFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Verifying account balance... [" + Thread.currentThread().getName() + "]");
            try {
                Thread.sleep(200); // Simulate balance check time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            System.out.println("Balance verification completed");
            return true;
        });

        // Combine all checks
        CompletableFuture<String> transactionResult = validationFuture
            .thenCombine(fraudCheckFuture, (validation, fraudCheck) -> validation && fraudCheck)
            .thenCombine(balanceCheckFuture, (previousChecks, balanceCheck) -> previousChecks && balanceCheck)
            .thenApply(allChecksPassed -> {
                if (allChecksPassed) {
                    System.out.println("All checks passed, processing transaction...");
                    return "Transaction processed successfully";
                } else {
                    System.out.println("Transaction failed validation checks");
                    return "Transaction failed";
                }
            })
            .exceptionally(throwable -> {
                System.err.println("Transaction processing error: " + throwable.getMessage());
                return "Transaction failed due to error";
            });

        // Wait for result
        try {
            String result = transactionResult.get(10, TimeUnit.SECONDS);
            System.out.println("Final Result: " + result);
        } catch (Exception e) {
            System.err.println("Failed to get transaction result: " + e.getMessage());
        }
    }

    // Helper methods
    private static BigDecimal calculateInterest(String accountNumber) {
        // Simulate interest calculation
        return new BigDecimal("10.50").add(new BigDecimal(Math.random() * 100));
    }

    private static AccountReport generateAccountReport(String accountNumber) throws InterruptedException {
        Thread.sleep(200 + (int)(Math.random() * 300)); // Simulate processing time
        return new AccountReport(accountNumber, new BigDecimal("1500.75"), 25);
    }

    private static void shutdownExecutor(ExecutorService executor, String taskName) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println(taskName + " tasks did not complete within timeout");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    // Data class for account reports
    static class AccountReport {
        private final String accountNumber;
        private final BigDecimal balance;
        private final int transactionCount;

        public AccountReport(String accountNumber, BigDecimal balance, int transactionCount) {
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.transactionCount = transactionCount;
        }

        @Override
        public String toString() {
            return String.format("AccountReport{account='%s', balance=$%.2f, transactions=%d}",
                               accountNumber, balance, transactionCount);
        }
    }
}
```

---

## 🗂️ Concurrent Collections {#concurrent-collections}

### Thread-Safe Collections for Banking

```java
public class BankingConcurrentCollections {

    // ConcurrentHashMap for account cache
    private static final ConcurrentHashMap<String, BankAccount> accountCache = new ConcurrentHashMap<>();

    // CopyOnWriteArrayList for audit trail (read-heavy, write-rare)
    private static final CopyOnWriteArrayList<AuditEntry> auditTrail = new CopyOnWriteArrayList<>();

    // BlockingQueue for transaction processing
    private static final BlockingQueue<Transaction> transactionQueue = new LinkedBlockingQueue<>(1000);

    // ConcurrentLinkedQueue for notifications (lock-free)
    private static final ConcurrentLinkedQueue<Notification> notificationQueue = new ConcurrentLinkedQueue<>();

    public static void demonstrateConcurrentHashMap() {
        System.out.println("=== ConcurrentHashMap Demo ===");

        // Populate account cache
        accountCache.put("ACC001", new BankAccount("ACC001", new BigDecimal("1000")));
        accountCache.put("ACC002", new BankAccount("ACC002", new BigDecimal("2000")));
        accountCache.put("ACC003", new BankAccount("ACC003", new BigDecimal("1500")));

        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Concurrent read operations
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            executor.submit(() -> {
                String accountId = "ACC00" + ((threadId % 3) + 1);
                BankAccount account = accountCache.get(accountId);
                if (account != null) {
                    System.out.println("Thread " + threadId + " read balance for " +
                                     accountId + ": $" + account.getBalance());
                }
            });
        }

        // Concurrent update operations using atomic methods
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            executor.submit(() -> {
                String accountId = "ACC00" + ((threadId % 3) + 1);

                // Atomic compute operation
                accountCache.computeIfPresent(accountId, (key, account) -> {
                    // Simulate interest addition
                    BigDecimal interest = new BigDecimal("5.00");
                    account.deposit(interest);
                    System.out.println("Thread " + threadId + " added interest to " + accountId);
                    return account;
                });
            });
        }

        // Demonstrate putIfAbsent for thread-safe insertion
        executor.submit(() -> {
            BankAccount newAccount = new BankAccount("ACC004", new BigDecimal("500"));
            BankAccount existing = accountCache.putIfAbsent("ACC004", newAccount);

            if (existing == null) {
                System.out.println("New account ACC004 added to cache");
            } else {
                System.out.println("Account ACC004 already exists in cache");
            }
        });

        shutdownExecutor(executor, "ConcurrentHashMap Demo");

        // Print final state
        System.out.println("Final cache contents:");
        accountCache.forEach((key, value) ->
            System.out.println(key + ": $" + value.getBalance()));
    }

    public static void demonstrateBlockingQueue() {
        System.out.println("\n=== BlockingQueue Demo ===");

        ExecutorService executor = Executors.newFixedThreadPool(6);

        // Producer threads
        for (int i = 1; i <= 3; i++) {
            final int producerId = i;
            executor.submit(() -> {
                try {
                    for (int j = 1; j <= 5; j++) {
                        Transaction transaction = new Transaction(
                            "P" + producerId + "-TXN" + j,
                            "ACC00" + j,
                            "ACC00" + (j + 1),
                            new BigDecimal("100.00")
                        );

                        // Blocking put - waits if queue is full
                        transactionQueue.put(transaction);
                        System.out.println("Producer " + producerId + " added: " + transaction.getId());

                        Thread.sleep(100); // Simulate varying production rate
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Consumer threads
        for (int i = 1; i <= 3; i++) {
            final int consumerId = i;
            executor.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        // Blocking take - waits if queue is empty
                        Transaction transaction = transactionQueue.poll(2, TimeUnit.SECONDS);

                        if (transaction != null) {
                            System.out.println("Consumer " + consumerId + " processing: " + transaction.getId());
                            Thread.sleep(150); // Simulate processing time
                            System.out.println("Consumer " + consumerId + " completed: " + transaction.getId());
                        } else {
                            System.out.println("Consumer " + consumerId + " timed out waiting for transaction");
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Let it run for 5 seconds
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        shutdownExecutor(executor, "BlockingQueue Demo");
        System.out.println("Remaining transactions in queue: " + transactionQueue.size());
    }

    public static void demonstrateCopyOnWriteArrayList() {
        System.out.println("\n=== CopyOnWriteArrayList Demo ===");

        ExecutorService executor = Executors.newFixedThreadPool(8);

        // Multiple readers (common case for audit trail)
        for (int i = 1; i <= 5; i++) {
            final int readerId = i;
            executor.submit(() -> {
                for (int j = 0; j < 3; j++) {
                    try {
                        System.out.println("Reader " + readerId + " - Audit entries count: " + auditTrail.size());

                        // Iterate safely even during concurrent modifications
                        for (AuditEntry entry : auditTrail) {
                            if (entry.getAction().contains("TRANSFER")) {
                                System.out.println("Reader " + readerId + " found transfer: " + entry.getAction());
                            }
                        }

                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // Few writers (less common for audit trail)
        for (int i = 1; i <= 3; i++) {
            final int writerId = i;
            executor.submit(() -> {
                try {
                    for (int j = 1; j <= 3; j++) {
                        AuditEntry entry = new AuditEntry(
                            "Writer " + writerId + " - Action " + j,
                            "USER" + writerId,
                            LocalDateTime.now()
                        );

                        auditTrail.add(entry);
                        System.out.println("Writer " + writerId + " added audit entry: " + entry.getAction());

                        Thread.sleep(300);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        shutdownExecutor(executor, "CopyOnWriteArrayList Demo");
        System.out.println("Final audit trail size: " + auditTrail.size());
    }

    public static void demonstrateConcurrentLinkedQueue() {
        System.out.println("\n=== ConcurrentLinkedQueue Demo ===");

        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Notification producers
        for (int i = 1; i <= 2; i++) {
            final int producerId = i;
            executor.submit(() -> {
                for (int j = 1; j <= 10; j++) {
                    Notification notification = new Notification(
                        "LOW_BALANCE",
                        "Account ACC00" + j + " has low balance",
                        LocalDateTime.now()
                    );

                    notificationQueue.offer(notification); // Non-blocking
                    System.out.println("Producer " + producerId + " queued notification: " + notification.getMessage());

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // Notification consumers
        for (int i = 1; i <= 2; i++) {
            final int consumerId = i;
            executor.submit(() -> {
                int processed = 0;
                while (processed < 10) {
                    Notification notification = notificationQueue.poll(); // Non-blocking

                    if (notification != null) {
                        System.out.println("Consumer " + consumerId + " processed: " + notification.getMessage());
                        processed++;

                        try {
                            Thread.sleep(100); // Simulate processing
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        // Queue is empty, wait a bit
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            });
        }

        shutdownExecutor(executor, "ConcurrentLinkedQueue Demo");
        System.out.println("Remaining notifications: " + notificationQueue.size());
    }

    // Helper classes
    static class AuditEntry {
        private final String action;
        private final String userId;
        private final LocalDateTime timestamp;

        public AuditEntry(String action, String userId, LocalDateTime timestamp) {
            this.action = action;
            this.userId = userId;
            this.timestamp = timestamp;
        }

        public String getAction() { return action; }
        public String getUserId() { return userId; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    static class Notification {
        private final String type;
        private final String message;
        private final LocalDateTime timestamp;

        public Notification(String type, String message, LocalDateTime timestamp) {
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getType() { return type; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    private static void shutdownExecutor(ExecutorService executor, String taskName) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println(taskName + " tasks did not complete within timeout");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    public static void main(String[] args) {
        demonstrateConcurrentHashMap();
        demonstrateBlockingQueue();
        demonstrateCopyOnWriteArrayList();
        demonstrateConcurrentLinkedQueue();
    }
}
```

---

## 🔧 Synchronization Utilities {#synchronization-utilities}

### Advanced Synchronization Patterns

```java
public class BankingSynchronizationUtilities {

    // CountDownLatch for coordinating batch processing
    public static void demonstrateCountDownLatch() {
        System.out.println("=== CountDownLatch Demo: End-of-Day Processing ===");

        final int DEPARTMENT_COUNT = 5;
        CountDownLatch completionLatch = new CountDownLatch(DEPARTMENT_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(DEPARTMENT_COUNT + 1);

        // Department processing tasks
        String[] departments = {"Loans", "Deposits", "Credit Cards", "Investment", "Compliance"};

        for (String department : departments) {
            executor.submit(() -> {
                try {
                    // Wait for start signal
                    startLatch.await();

                    System.out.println(department + " department starting end-of-day processing...");

                    // Simulate processing time
                    Thread.sleep(1000 + (int)(Math.random() * 2000));

                    System.out.println(department + " department completed processing");

                    // Signal completion
                    completionLatch.countDown();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println(department + " processing interrupted");
                }
            });
        }

        // Coordinator task
        executor.submit(() -> {
            try {
                System.out.println("Coordinator: Starting end-of-day processing for all departments");
                startLatch.countDown(); // Signal all departments to start

                // Wait for all departments to complete
                completionLatch.await();

                System.out.println("Coordinator: All departments completed. Generating final reports...");
                Thread.sleep(500);
                System.out.println("Coordinator: End-of-day processing completed successfully");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Coordinator interrupted");
            }
        });

        shutdownExecutor(executor, "CountDownLatch Demo");
    }

    // CyclicBarrier for multi-phase processing
    public static void demonstrateCyclicBarrier() {
        System.out.println("\n=== CyclicBarrier Demo: Multi-Phase Reconciliation ===");

        final int PROCESSOR_COUNT = 4;
        final int PHASE_COUNT = 3;

        CyclicBarrier barrier = new CyclicBarrier(PROCESSOR_COUNT, () -> {
            System.out.println("🔄 All processors completed current phase. Moving to next phase...");
        });

        ExecutorService executor = Executors.newFixedThreadPool(PROCESSOR_COUNT);

        for (int i = 1; i <= PROCESSOR_COUNT; i++) {
            final int processorId = i;
            executor.submit(() -> {
                try {
                    for (int phase = 1; phase <= PHASE_COUNT; phase++) {
                        System.out.println("Processor " + processorId + " starting Phase " + phase);

                        // Simulate different processing times for each phase
                        Thread.sleep(500 + (int)(Math.random() * 1000));

                        System.out.println("Processor " + processorId + " completed Phase " + phase);

                        // Wait for all processors to complete this phase
                        barrier.await();

                        System.out.println("Processor " + processorId + " ready for next phase");
                    }

                    System.out.println("✅ Processor " + processorId + " completed all phases");

                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Processor " + processorId + " interrupted: " + e.getMessage());
                }
            });
        }

        shutdownExecutor(executor, "CyclicBarrier Demo");
    }

    // Semaphore for resource access control
    public static void demonstrateSemaphore() {
        System.out.println("\n=== Semaphore Demo: Database Connection Pool ===");

        final int CONNECTION_POOL_SIZE = 3;
        final int CLIENT_COUNT = 8;

        Semaphore connectionPool = new Semaphore(CONNECTION_POOL_SIZE);

        ExecutorService executor = Executors.newFixedThreadPool(CLIENT_COUNT);

        for (int i = 1; i <= CLIENT_COUNT; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Client " + clientId + " requesting database connection...");

                    // Acquire connection from pool
                    connectionPool.acquire();

                    System.out.println("✅ Client " + clientId + " acquired connection. " +
                                     "Available connections: " + connectionPool.availablePermits());

                    // Simulate database work
                    Thread.sleep(1000 + (int)(Math.random() * 2000));

                    System.out.println("Client " + clientId + " finished database work");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Client " + clientId + " interrupted");
                } finally {
                    // Always release the connection
                    connectionPool.release();
                    System.out.println("🔄 Client " + clientId + " released connection. " +
                                     "Available connections: " + connectionPool.availablePermits());
                }
            });
        }

        shutdownExecutor(executor, "Semaphore Demo");
    }

    // Phaser for complex multi-stage coordination
    public static void demonstratePhaser() {
        System.out.println("\n=== Phaser Demo: Transaction Batch Processing ===");

        final int PROCESSOR_COUNT = 3;
        Phaser phaser = new Phaser(1); // Register main thread

        ExecutorService executor = Executors.newFixedThreadPool(PROCESSOR_COUNT);

        // Register all processors
        for (int i = 1; i <= PROCESSOR_COUNT; i++) {
            phaser.register();
            final int processorId = i;

            executor.submit(() -> {
                try {
                    // Phase 0: Data collection
                    System.out.println("Processor " + processorId + " collecting transaction data...");
                    Thread.sleep(500 + (int)(Math.random() * 500));
                    System.out.println("Processor " + processorId + " data collection complete");
                    phaser.arriveAndAwaitAdvance(); // Wait for phase 0 completion

                    // Phase 1: Validation
                    System.out.println("Processor " + processorId + " validating transactions...");
                    Thread.sleep(400 + (int)(Math.random() * 600));
                    System.out.println("Processor " + processorId + " validation complete");
                    phaser.arriveAndAwaitAdvance(); // Wait for phase 1 completion

                    // Phase 2: Processing
                    System.out.println("Processor " + processorId + " processing transactions...");
                    Thread.sleep(600 + (int)(Math.random() * 400));
                    System.out.println("Processor " + processorId + " processing complete");
                    phaser.arriveAndAwaitAdvance(); // Wait for phase 2 completion

                    // Phase 3: Reporting
                    System.out.println("Processor " + processorId + " generating reports...");
                    Thread.sleep(300 + (int)(Math.random() * 300));
                    System.out.println("Processor " + processorId + " reporting complete");
                    phaser.arriveAndDeregister(); // Complete and leave phaser

                } catch (Exception e) {
                    System.err.println("Processor " + processorId + " error: " + e.getMessage());
                    phaser.arriveAndDeregister(); // Still need to deregister on error
                }
            });
        }

        // Main thread coordinates phases
        executor.submit(() -> {
            try {
                System.out.println("🚀 Starting Phase 0: Data Collection");
                phaser.arriveAndAwaitAdvance(); // Phase 0

                System.out.println("🔍 Starting Phase 1: Validation");
                phaser.arriveAndAwaitAdvance(); // Phase 1

                System.out.println("⚙️ Starting Phase 2: Processing");
                phaser.arriveAndAwaitAdvance(); // Phase 2

                System.out.println("📊 Starting Phase 3: Reporting");
                phaser.arriveAndAwaitAdvance(); // Phase 3

                System.out.println("✅ All phases completed successfully");
                phaser.arriveAndDeregister(); // Main thread leaves

            } catch (Exception e) {
                System.err.println("Main coordinator error: " + e.getMessage());
            }
        });

        shutdownExecutor(executor, "Phaser Demo");
    }

    // Exchanger for data exchange between threads
    public static void demonstrateExchanger() {
        System.out.println("\n=== Exchanger Demo: Transaction Data Exchange ===");

        Exchanger<List<Transaction>> exchanger = new Exchanger<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Producer thread
        executor.submit(() -> {
            try {
                for (int round = 1; round <= 3; round++) {
                    List<Transaction> producedTransactions = new ArrayList<>();

                    // Generate transactions
                    for (int i = 1; i <= 5; i++) {
                        producedTransactions.add(new Transaction(
                            "PROD-R" + round + "-T" + i,
                            "ACC" + (100 + i),
                            "ACC" + (200 + i),
                            new BigDecimal("100.00")
                        ));
                    }

                    System.out.println("Producer generated " + producedTransactions.size() +
                                     " transactions for round " + round);

                    // Exchange with consumer
                    List<Transaction> processedTransactions = exchanger.exchange(producedTransactions);

                    System.out.println("Producer received " + processedTransactions.size() +
                                     " processed transactions from round " + (round - 1));

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Producer interrupted");
            }
        });

        // Consumer thread
        executor.submit(() -> {
            try {
                List<Transaction> processedTransactions = new ArrayList<>(); // Initially empty

                for (int round = 1; round <= 3; round++) {
                    // Exchange with producer
                    List<Transaction> newTransactions = exchanger.exchange(processedTransactions);

                    System.out.println("Consumer received " + newTransactions.size() +
                                     " new transactions for round " + round);

                    // Process transactions
                    processedTransactions = new ArrayList<>();
                    for (Transaction txn : newTransactions) {
                        // Simulate processing
                        Thread.sleep(100);
                        processedTransactions.add(new Transaction(
                            "PROCESSED-" + txn.getId(),
                            txn.getFromAccount(),
                            txn.getToAccount(),
                            txn.getAmount()
                        ));
                    }

                    System.out.println("Consumer processed " + processedTransactions.size() +
                                     " transactions for round " + round);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Consumer interrupted");
            }
        });

        shutdownExecutor(executor, "Exchanger Demo");
    }

    private static void shutdownExecutor(ExecutorService executor, String taskName) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println(taskName + " tasks did not complete within timeout");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    public static void main(String[] args) {
        demonstrateCountDownLatch();
        demonstrateCyclicBarrier();
        demonstrateSemaphore();
        demonstratePhaser();
        demonstrateExchanger();
    }
}
```

---

## 🏦 Banking-Specific Concurrency Patterns {#banking-patterns}

### High-Performance Transaction Processing

```java
public class BankingConcurrencyPatterns {

    // Thread-safe transaction processor with rate limiting
    public static class TransactionProcessor {
        private final Semaphore rateLimiter;
        private final BlockingQueue<Transaction> transactionQueue;
        private final ExecutorService processingPool;
        private final AtomicLong processedCount = new AtomicLong(0);
        private final AtomicLong failedCount = new AtomicLong(0);

        public TransactionProcessor(int maxTransactionsPerSecond, int queueSize, int poolSize) {
            this.rateLimiter = new Semaphore(maxTransactionsPerSecond);
            this.transactionQueue = new LinkedBlockingQueue<>(queueSize);
            this.processingPool = Executors.newFixedThreadPool(poolSize);

            // Start consumer threads
            for (int i = 0; i < poolSize; i++) {
                processingPool.submit(this::processTransactions);
            }

            // Rate limiter refill thread
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                rateLimiter.release(maxTransactionsPerSecond - rateLimiter.availablePermits());
            }, 1, 1, TimeUnit.SECONDS);
        }

        public boolean submitTransaction(Transaction transaction) {
            try {
                return transactionQueue.offer(transaction, 1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        private void processTransactions() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Transaction transaction = transactionQueue.take();

                    // Rate limiting
                    rateLimiter.acquire();

                    // Process transaction
                    boolean success = processTransaction(transaction);

                    if (success) {
                        processedCount.incrementAndGet();
                    } else {
                        failedCount.incrementAndGet();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        private boolean processTransaction(Transaction transaction) {
            try {
                // Simulate transaction processing
                Thread.sleep(10 + (int)(Math.random() * 20));

                // 95% success rate
                boolean success = Math.random() < 0.95;

                if (success) {
                    System.out.println("✅ Processed: " + transaction.getId());
                } else {
                    System.out.println("❌ Failed: " + transaction.getId());
                }

                return success;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        public void getStats() {
            System.out.println("Processed: " + processedCount.get() +
                             ", Failed: " + failedCount.get() +
                             ", Queue size: " + transactionQueue.size());
        }

        public void shutdown() {
            processingPool.shutdown();
        }
    }

    // Lock-free account balance updates using compare-and-swap
    public static class LockFreeAccount {
        private final String accountNumber;
        private final AtomicReference<BigDecimal> balance;
        private final AtomicLong version = new AtomicLong(0);

        public LockFreeAccount(String accountNumber, BigDecimal initialBalance) {
            this.accountNumber = accountNumber;
            this.balance = new AtomicReference<>(initialBalance);
        }

        public boolean deposit(BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            BigDecimal currentBalance;
            BigDecimal newBalance;

            do {
                currentBalance = balance.get();
                newBalance = currentBalance.add(amount);
            } while (!balance.compareAndSet(currentBalance, newBalance));

            version.incrementAndGet();
            System.out.println("Deposited $" + amount + " to " + accountNumber +
                             ". New balance: $" + newBalance);
            return true;
        }

        public boolean withdraw(BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            BigDecimal currentBalance;
            BigDecimal newBalance;

            do {
                currentBalance = balance.get();
                if (currentBalance.compareTo(amount) < 0) {
                    return false; // Insufficient funds
                }
                newBalance = currentBalance.subtract(amount);
            } while (!balance.compareAndSet(currentBalance, newBalance));

            version.incrementAndGet();
            System.out.println("Withdrew $" + amount + " from " + accountNumber +
                             ". New balance: $" + newBalance);
            return true;
        }

        public BigDecimal getBalance() {
            return balance.get();
        }

        public long getVersion() {
            return version.get();
        }

        public String getAccountNumber() {
            return accountNumber;
        }
    }

    // Event-driven architecture for transaction notifications
    public static class TransactionEventProcessor {
        private final Map<String, List<TransactionListener>> listeners = new ConcurrentHashMap<>();
        private final ExecutorService notificationPool = Executors.newFixedThreadPool(5);

        public void addListener(String eventType, TransactionListener listener) {
            listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        }

        public void publishEvent(String eventType, TransactionEvent event) {
            List<TransactionListener> eventListeners = listeners.get(eventType);
            if (eventListeners != null) {
                for (TransactionListener listener : eventListeners) {
                    notificationPool.submit(() -> {
                        try {
                            listener.onEvent(event);
                        } catch (Exception e) {
                            System.err.println("Error in event listener: " + e.getMessage());
                        }
                    });
                }
            }
        }

        public void shutdown() {
            notificationPool.shutdown();
        }
    }

    @FunctionalInterface
    public interface TransactionListener {
        void onEvent(TransactionEvent event);
    }

    public static class TransactionEvent {
        private final String transactionId;
        private final String eventType;
        private final String accountNumber;
        private final BigDecimal amount;
        private final LocalDateTime timestamp;

        public TransactionEvent(String transactionId, String eventType, String accountNumber,
                               BigDecimal amount, LocalDateTime timestamp) {
            this.transactionId = transactionId;
            this.eventType = eventType;
            this.accountNumber = accountNumber;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        // Getters
        public String getTransactionId() { return transactionId; }
        public String getEventType() { return eventType; }
        public String getAccountNumber() { return accountNumber; }
        public BigDecimal getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // Demonstration of banking concurrency patterns
    public static void demonstrateBankingPatterns() {
        System.out.println("=== Banking Concurrency Patterns Demo ===");

        // 1. Transaction Processor Demo
        System.out.println("\n--- Transaction Processor ---");
        TransactionProcessor processor = new TransactionProcessor(10, 100, 3);

        // Submit transactions
        for (int i = 1; i <= 20; i++) {
            Transaction txn = new Transaction(
                "TXN" + String.format("%03d", i),
                "ACC" + (100 + i % 5),
                "ACC" + (200 + i % 5),
                new BigDecimal("100.00")
            );
            processor.submitTransaction(txn);
        }

        // Wait and check stats
        try {
            Thread.sleep(3000);
            processor.getStats();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        processor.shutdown();

        // 2. Lock-Free Account Demo
        System.out.println("\n--- Lock-Free Account ---");
        LockFreeAccount account = new LockFreeAccount("ACC001", new BigDecimal("1000.00"));

        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Concurrent deposits and withdrawals
        for (int i = 0; i < 20; i++) {
            final int operationId = i;
            executor.submit(() -> {
                if (operationId % 2 == 0) {
                    account.deposit(new BigDecimal("50.00"));
                } else {
                    account.withdraw(new BigDecimal("30.00"));
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Final balance: $" + account.getBalance() +
                             " (Version: " + account.getVersion() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. Event Processor Demo
        System.out.println("\n--- Event Processor ---");
        TransactionEventProcessor eventProcessor = new TransactionEventProcessor();

        // Add listeners
        eventProcessor.addListener("LARGE_TRANSACTION", event -> {
            System.out.println("🚨 Large transaction alert: $" + event.getAmount() +
                             " in account " + event.getAccountNumber());
        });

        eventProcessor.addListener("FRAUD_DETECTION", event -> {
            System.out.println("⚠️ Fraud detection: Suspicious activity on " + event.getAccountNumber());
        });

        eventProcessor.addListener("BALANCE_UPDATE", event -> {
            System.out.println("📊 Balance updated for " + event.getAccountNumber() +
                             ": $" + event.getAmount());
        });

        // Publish events
        eventProcessor.publishEvent("LARGE_TRANSACTION",
            new TransactionEvent("TXN001", "LARGE_TRANSACTION", "ACC001",
                               new BigDecimal("10000.00"), LocalDateTime.now()));

        eventProcessor.publishEvent("FRAUD_DETECTION",
            new TransactionEvent("TXN002", "FRAUD_DETECTION", "ACC002",
                               new BigDecimal("500.00"), LocalDateTime.now()));

        eventProcessor.publishEvent("BALANCE_UPDATE",
            new TransactionEvent("TXN003", "BALANCE_UPDATE", "ACC003",
                               new BigDecimal("250.00"), LocalDateTime.now()));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        eventProcessor.shutdown();
    }

    public static void main(String[] args) {
        demonstrateBankingPatterns();
    }
}
```

---

## ⚠️ Common Concurrency Problems {#concurrency-problems}

### Identifying and Solving Common Issues

```java
public class ConcurrencyProblems {

    // 1. Race Condition Example and Solution
    public static class RaceConditionExample {

        // Problematic code - Race condition
        static class UnsafeBankAccount {
            private int balance = 1000;

            public void withdraw(int amount) {
                if (balance >= amount) {
                    // Race condition here - another thread might modify balance
                    try {
                        Thread.sleep(1); // Simulate processing time
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    balance -= amount;
                    System.out.println("Withdrew " + amount + ", remaining: " + balance);
                }
            }
        }

        // Safe solution
        static class SafeBankAccount {
            private int balance = 1000;

            public synchronized void withdraw(int amount) {
                if (balance >= amount) {
                    try {
                        Thread.sleep(1); // Simulate processing time
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    balance -= amount;
                    System.out.println("Safely withdrew " + amount + ", remaining: " + balance);
                }
            }
        }
    }

    // 2. Deadlock Example and Prevention
    public static class DeadlockExample {
        private static final Object lock1 = new Object();
        private static final Object lock2 = new Object();

        // Deadlock scenario
        public static void createDeadlock() {
            Thread t1 = new Thread(() -> {
                synchronized (lock1) {
                    System.out.println("Thread 1: Holding lock 1...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    System.out.println("Thread 1: Waiting for lock 2...");
                    synchronized (lock2) {
                        System.out.println("Thread 1: Holding both locks");
                    }
                }
            }, "DeadlockThread1");

            Thread t2 = new Thread(() -> {
                synchronized (lock2) {
                    System.out.println("Thread 2: Holding lock 2...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    System.out.println("Thread 2: Waiting for lock 1...");
                    synchronized (lock1) {
                        System.out.println("Thread 2: Holding both locks");
                    }
                }
            }, "DeadlockThread2");

            t1.start();
            t2.start();

            // This will deadlock - threads will wait forever
        }

        // Deadlock prevention using lock ordering
        public static void preventDeadlock() {
            // Always acquire locks in the same order
            Thread t1 = new Thread(() -> {
                synchronized (lock1) {
                    System.out.println("Thread 1: Holding lock 1...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    synchronized (lock2) {
                        System.out.println("Thread 1: Acquired both locks safely");
                    }
                }
            }, "SafeThread1");

            Thread t2 = new Thread(() -> {
                synchronized (lock1) { // Same order as t1
                    System.out.println("Thread 2: Holding lock 1...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    synchronized (lock2) {
                        System.out.println("Thread 2: Acquired both locks safely");
                    }
                }
            }, "SafeThread2");

            t1.start();
            t2.start();

            try {
                t1.join();
                t2.join();
                System.out.println("Both threads completed successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // 3. Memory Consistency Errors
    public static class MemoryConsistencyExample {

        // Problematic code - visibility issue
        static class VisibilityProblem {
            private boolean stopRequested = false;

            public void startWork() {
                Thread worker = new Thread(() -> {
                    int count = 0;
                    while (!stopRequested) {
                        count++;
                        // This might run forever due to visibility issues
                    }
                    System.out.println("Worker stopped after " + count + " iterations");
                });
                worker.start();
            }

            public void requestStop() {
                stopRequested = true; // Other thread might not see this change
            }
        }

        // Solution using volatile
        static class VisibilitySolution {
            private volatile boolean stopRequested = false;

            public void startWork() {
                Thread worker = new Thread(() -> {
                    int count = 0;
                    while (!stopRequested) {
                        count++;
                        if (count % 1000000 == 0) {
                            System.out.println("Worker still running... " + count);
                        }
                    }
                    System.out.println("Worker stopped after " + count + " iterations");
                });
                worker.start();
            }

            public void requestStop() {
                stopRequested = true; // Volatile ensures visibility
            }
        }
    }

    // 4. Livelock Example
    public static class LivelockExample {

        static class PoliteAccount {
            private final String name;
            private boolean isTransferring = false;

            public PoliteAccount(String name) {
                this.name = name;
            }

            public synchronized boolean transfer(PoliteAccount to, int amount) {
                if (isTransferring) {
                    return false; // Already transferring
                }

                isTransferring = true;

                try {
                    // Check if the other account is also transferring
                    if (to.isTransferring()) {
                        // Be polite and back off
                        System.out.println(name + " backing off from transfer to " + to.name);
                        Thread.sleep(10 + (int)(Math.random() * 10));
                        return false; // Will cause livelock
                    }

                    // Perform transfer
                    System.out.println(name + " successfully transferred to " + to.name);
                    Thread.sleep(100); // Simulate transfer time
                    return true;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                } finally {
                    isTransferring = false;
                }
            }

            public synchronized boolean isTransferring() {
                return isTransferring;
            }
        }

        public static void demonstrateLivelock() {
            PoliteAccount account1 = new PoliteAccount("Account1");
            PoliteAccount account2 = new PoliteAccount("Account2");

            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    boolean success = account1.transfer(account2, 100);
                    if (!success) {
                        System.out.println("Account1 transfer failed, retrying...");
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            });

            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    boolean success = account2.transfer(account1, 200);
                    if (!success) {
                        System.out.println("Account2 transfer failed, retrying...");
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            });

            t1.start();
            t2.start();

            try {
                t1.join(5000); // Timeout to prevent hanging
                t2.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // 5. Performance Issues
    public static class PerformanceIssues {

        // Excessive synchronization
        static class OverSynchronized {
            private final List<String> data = new ArrayList<>();

            public synchronized void add(String item) {
                data.add(item);
            }

            public synchronized String get(int index) {
                return data.get(index);
            }

            public synchronized int size() {
                return data.size();
            }

            // Every method is synchronized, reducing concurrency
        }

        // Better approach with appropriate synchronization
        static class OptimizedSynchronization {
            private final List<String> data = Collections.synchronizedList(new ArrayList<>());
            private final ReadWriteLock lock = new ReentrantReadWriteLock();

            public void add(String item) {
                lock.writeLock().lock();
                try {
                    data.add(item);
                } finally {
                    lock.writeLock().unlock();
                }
            }

            public String get(int index) {
                lock.readLock().lock();
                try {
                    return data.get(index);
                } finally {
                    lock.readLock().unlock();
                }
            }

            public int size() {
                lock.readLock().lock();
                try {
                    return data.size();
                } finally {
                    lock.readLock().unlock();
                }
            }

            // Multiple readers can access simultaneously
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Concurrency Problems Demonstration ===");

        // Demonstrate deadlock prevention
        System.out.println("\n--- Deadlock Prevention ---");
        DeadlockExample.preventDeadlock();

        // Demonstrate visibility solution
        System.out.println("\n--- Visibility Solution ---");
        MemoryConsistencyExample.VisibilitySolution solution =
            new MemoryConsistencyExample.VisibilitySolution();
        solution.startWork();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        solution.requestStop();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Demonstrate livelock
        System.out.println("\n--- Livelock Example ---");
        LivelockExample.demonstrateLivelock();
    }
}
```

---

## ❓ Interview Questions & Answers {#interview-questions}

### Comprehensive Concurrency Interview Q&A

#### Q1: What is the difference between concurrency and parallelism?

**Answer:**
- **Concurrency**: Multiple tasks making progress by interleaving their execution on potentially a single core. It's about dealing with lots of things at once.
- **Parallelism**: Multiple tasks executing simultaneously on multiple cores. It's about doing lots of things at once.

```java
// Concurrency example - single core, time-sliced execution
ExecutorService executor = Executors.newSingleThreadExecutor();
executor.submit(() -> processTransactions());
executor.submit(() -> generateReports());

// Parallelism example - multiple cores, simultaneous execution
ExecutorService parallelExecutor = Executors.newFixedThreadPool(4);
parallelExecutor.submit(() -> processTransactions());
parallelExecutor.submit(() -> generateReports());
```

#### Q2: Explain the difference between synchronized methods and synchronized blocks.

**Answer:**

```java
public class SynchronizationComparison {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    private int balance = 1000;

    // Synchronized method - locks entire method on 'this' object
    public synchronized void deposit(int amount) {
        balance += amount; // Only one thread can execute this method at a time
    }

    // Synchronized block - more granular control
    public void withdraw(int amount) {
        // Non-critical code can run concurrently
        validateAmount(amount);

        synchronized(this) {
            // Only this section is synchronized
            if (balance >= amount) {
                balance -= amount;
            }
        }

        // More non-critical code
        logTransaction(amount);
    }

    // Multiple locks for different operations
    public void transferFunds(int amount) {
        synchronized(lock1) {
            // Balance operations
            balance -= amount;
        }

        synchronized(lock2) {
            // Audit operations
            recordTransfer(amount);
        }
    }
}
```

**Key Differences:**
- **Synchronized methods** lock the entire method using the object's intrinsic lock
- **Synchronized blocks** provide more granular control and can use custom locks
- **Performance**: Blocks can be more efficient by reducing lock contention

#### Q3: What are the different types of locks in Java and when would you use each?

**Answer:**

```java
public class LockTypesExample {

    // 1. Intrinsic locks (synchronized)
    private final Object intrinsicLock = new Object();

    // 2. ReentrantLock - explicit locking with advanced features
    private final ReentrantLock reentrantLock = new ReentrantLock();

    // 3. ReadWriteLock - optimized for read-heavy scenarios
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // 4. StampedLock - optimistic reading (Java 8+)
    private final StampedLock stampedLock = new StampedLock();

    private String data = "Initial Data";

    // Intrinsic lock - simple, automatic release
    public void updateWithSynchronized(String newData) {
        synchronized(intrinsicLock) {
            data = newData;
        }
    }

    // ReentrantLock - manual control, try-lock, timeouts
    public boolean updateWithReentrantLock(String newData, long timeoutMs) {
        try {
            if (reentrantLock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                try {
                    data = newData;
                    return true;
                } finally {
                    reentrantLock.unlock(); // Must manually unlock
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    // ReadWriteLock - multiple readers, exclusive writer
    public String readData() {
        readWriteLock.readLock().lock();
        try {
            return data; // Multiple threads can read simultaneously
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void writeData(String newData) {
        readWriteLock.writeLock().lock();
        try {
            data = newData; // Exclusive access for writing
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    // StampedLock - optimistic reads for high-performance scenarios
    public String readDataOptimistic() {
        long stamp = stampedLock.tryOptimisticRead();
        String result = data; // Optimistic read

        if (!stampedLock.validate(stamp)) {
            // Validation failed, fall back to pessimistic read
            stamp = stampedLock.readLock();
            try {
                result = data;
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return result;
    }
}
```

**When to use each:**
- **Synchronized**: Simple cases, automatic cleanup
- **ReentrantLock**: Need timeouts, try-lock, or lock interruption
- **ReadWriteLock**: Read-heavy scenarios (10:1 read:write ratio or higher)
- **StampedLock**: Extremely read-heavy scenarios requiring maximum performance

#### Q4: Explain the Producer-Consumer pattern and its implementation.

**Answer:**

```java
public class ProducerConsumerInterview {

    // Method 1: Using wait/notify (traditional approach)
    static class WaitNotifyBuffer {
        private final Queue<String> buffer = new LinkedList<>();
        private final int maxSize;

        public WaitNotifyBuffer(int maxSize) {
            this.maxSize = maxSize;
        }

        public synchronized void produce(String item) throws InterruptedException {
            while (buffer.size() >= maxSize) {
                wait(); // Wait until space is available
            }

            buffer.offer(item);
            System.out.println("Produced: " + item + " (Buffer size: " + buffer.size() + ")");
            notifyAll(); // Notify consumers
        }

        public synchronized String consume() throws InterruptedException {
            while (buffer.isEmpty()) {
                wait(); // Wait until item is available
            }

            String item = buffer.poll();
            System.out.println("Consumed: " + item + " (Buffer size: " + buffer.size() + ")");
            notifyAll(); // Notify producers
            return item;
        }
    }

    // Method 2: Using BlockingQueue (recommended approach)
    static class BlockingQueueBuffer {
        private final BlockingQueue<String> buffer;

        public BlockingQueueBuffer(int maxSize) {
            this.buffer = new LinkedBlockingQueue<>(maxSize);
        }

        public void produce(String item) throws InterruptedException {
            buffer.put(item); // Blocks if queue is full
            System.out.println("Produced: " + item + " (Buffer size: " + buffer.size() + ")");
        }

        public String consume() throws InterruptedException {
            String item = buffer.take(); // Blocks if queue is empty
            System.out.println("Consumed: " + item + " (Buffer size: " + buffer.size() + ")");
            return item;
        }
    }

    public static void demonstrateProducerConsumer() {
        BlockingQueueBuffer buffer = new BlockingQueueBuffer(5);

        // Producer
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.produce("Item-" + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.consume();
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

#### Q5: How do you prevent deadlocks?

**Answer:**

**Deadlock Prevention Strategies:**

1. **Lock Ordering** - Always acquire locks in the same order
2. **Timeout** - Use tryLock with timeout
3. **Deadlock Detection** - Monitor and break deadlocks
4. **Avoid Nested Locks** - Minimize lock dependencies

```java
public class DeadlockPrevention {

    // Strategy 1: Lock Ordering
    public static class LockOrdering {
        private static final Object lock1 = new Object();
        private static final Object lock2 = new Object();

        public void method1() {
            synchronized(lock1) {
                synchronized(lock2) {
                    // Critical section
                }
            }
        }

        public void method2() {
            synchronized(lock1) { // Same order as method1
                synchronized(lock2) {
                    // Critical section
                }
            }
        }
    }

    // Strategy 2: Timeout-based locks
    public static class TimeoutLocks {
        private final ReentrantLock lock1 = new ReentrantLock();
        private final ReentrantLock lock2 = new ReentrantLock();

        public boolean transfer(int amount) {
            boolean lock1Acquired = false;
            boolean lock2Acquired = false;

            try {
                lock1Acquired = lock1.tryLock(1000, TimeUnit.MILLISECONDS);
                if (!lock1Acquired) return false;

                lock2Acquired = lock2.tryLock(1000, TimeUnit.MILLISECONDS);
                if (!lock2Acquired) return false;

                // Perform transfer
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } finally {
                if (lock2Acquired) lock2.unlock();
                if (lock1Acquired) lock1.unlock();
            }
        }
    }

    // Strategy 3: Single-threaded access to shared resources
    public static class SingleThreadedAccess {
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        public Future<Boolean> transfer(String from, String to, int amount) {
            return executor.submit(() -> {
                // All operations serialized - no deadlock possible
                withdrawFrom(from, amount);
                depositTo(to, amount);
                return true;
            });
        }

        private void withdrawFrom(String account, int amount) { /* implementation */ }
        private void depositTo(String account, int amount) { /* implementation */ }
    }
}
```

#### Q6: What is the difference between CountDownLatch, CyclicBarrier, and Semaphore?

**Answer:**

| Feature | CountDownLatch | CyclicBarrier | Semaphore |
|---------|----------------|---------------|-----------|
| **Purpose** | One-time coordination | Recurring synchronization | Resource access control |
| **Reusable** | No | Yes | Yes |
| **Thread participation** | Threads wait for counter to reach zero | All threads wait for each other | Threads acquire/release permits |
| **Use case** | Startup coordination | Multi-phase processing | Connection pools, rate limiting |

```java
public class SynchronizationUtilitiesComparison {

    // CountDownLatch - One-time event coordination
    public static void demonstrateCountDownLatch() {
        final int WORKERS = 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(WORKERS);

        for (int i = 0; i < WORKERS; i++) {
            final int workerId = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for start signal
                    doWork(workerId);
                    doneLatch.countDown(); // Signal completion
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        // Start all workers
        startLatch.countDown();

        try {
            doneLatch.await(); // Wait for all workers to complete
            System.out.println("All workers completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // CyclicBarrier - Recurring synchronization points
    public static void demonstrateCyclicBarrier() {
        final int WORKERS = 3;
        final int PHASES = 3;

        CyclicBarrier barrier = new CyclicBarrier(WORKERS, () -> {
            System.out.println("All workers completed current phase");
        });

        for (int i = 0; i < WORKERS; i++) {
            final int workerId = i;
            new Thread(() -> {
                try {
                    for (int phase = 1; phase <= PHASES; phase++) {
                        doWork(workerId, phase);
                        barrier.await(); // Wait for all workers to complete phase
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    // Semaphore - Resource access control
    public static void demonstrateSemaphore() {
        final int PERMITS = 2; // Only 2 concurrent database connections
        Semaphore dbConnections = new Semaphore(PERMITS);

        for (int i = 0; i < 5; i++) {
            final int clientId = i;
            new Thread(() -> {
                try {
                    dbConnections.acquire(); // Get connection
                    System.out.println("Client " + clientId + " acquired DB connection");

                    Thread.sleep(2000); // Use connection

                    System.out.println("Client " + clientId + " released DB connection");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    dbConnections.release(); // Always release
                }
            }).start();
        }
    }

    private static void doWork(int workerId) {
        System.out.println("Worker " + workerId + " working...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void doWork(int workerId, int phase) {
        System.out.println("Worker " + workerId + " working on phase " + phase);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## 🎯 Best Practices {#best-practices}

### Concurrency Best Practices for Banking Systems

#### 1. Thread Safety Guidelines

```java
public class ThreadSafetyBestPractices {

    // ✅ Good: Immutable objects are inherently thread-safe
    public static final class ImmutableTransaction {
        private final String id;
        private final String fromAccount;
        private final String toAccount;
        private final BigDecimal amount;
        private final LocalDateTime timestamp;

        public ImmutableTransaction(String id, String fromAccount, String toAccount,
                                   BigDecimal amount, LocalDateTime timestamp) {
            this.id = Objects.requireNonNull(id);
            this.fromAccount = Objects.requireNonNull(fromAccount);
            this.toAccount = Objects.requireNonNull(toAccount);
            this.amount = Objects.requireNonNull(amount);
            this.timestamp = Objects.requireNonNull(timestamp);
        }

        // Only getters, no setters
        public String getId() { return id; }
        public String getFromAccount() { return fromAccount; }
        public String getToAccount() { return toAccount; }
        public BigDecimal getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // ✅ Good: Proper encapsulation with thread-safe operations
    public static class ThreadSafeAccountManager {
        private final ConcurrentHashMap<String, AtomicReference<BigDecimal>> accounts = new ConcurrentHashMap<>();
        private final ReadWriteLock accountsLock = new ReentrantReadWriteLock();

        public void createAccount(String accountNumber, BigDecimal initialBalance) {
            accountsLock.writeLock().lock();
            try {
                accounts.putIfAbsent(accountNumber, new AtomicReference<>(initialBalance));
            } finally {
                accountsLock.writeLock().unlock();
            }
        }

        public BigDecimal getBalance(String accountNumber) {
            accountsLock.readLock().lock();
            try {
                AtomicReference<BigDecimal> balance = accounts.get(accountNumber);
                return balance != null ? balance.get() : BigDecimal.ZERO;
            } finally {
                accountsLock.readLock().unlock();
            }
        }

        public boolean updateBalance(String accountNumber, Function<BigDecimal, BigDecimal> updater) {
            accountsLock.readLock().lock();
            try {
                AtomicReference<BigDecimal> balanceRef = accounts.get(accountNumber);
                if (balanceRef == null) return false;

                BigDecimal current, updated;
                do {
                    current = balanceRef.get();
                    updated = updater.apply(current);
                    if (updated.compareTo(BigDecimal.ZERO) < 0) {
                        return false; // Negative balance not allowed
                    }
                } while (!balanceRef.compareAndSet(current, updated));

                return true;
            } finally {
                accountsLock.readLock().unlock();
            }
        }
    }

    // ❌ Bad: Shared mutable state without proper synchronization
    public static class UnsafeAccountManager {
        private final Map<String, BigDecimal> accounts = new HashMap<>(); // Not thread-safe

        public void updateBalance(String accountNumber, BigDecimal amount) {
            BigDecimal current = accounts.get(accountNumber); // Race condition
            if (current != null) {
                accounts.put(accountNumber, current.add(amount)); // Race condition
            }
        }
    }
}
```

#### 2. Performance Optimization

```java
public class PerformanceOptimization {

    // ✅ Good: Minimize lock contention with proper granularity
    public static class OptimizedBankingSystem {
        private final ConcurrentHashMap<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicReference<BigDecimal>> balances = new ConcurrentHashMap<>();

        public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
            // Lock only specific accounts, not the entire system
            ReentrantLock fromLock = getAccountLock(fromAccount);
            ReentrantLock toLock = getAccountLock(toAccount);

            // Always acquire locks in consistent order to prevent deadlock
            ReentrantLock firstLock = fromAccount.compareTo(toAccount) < 0 ? fromLock : toLock;
            ReentrantLock secondLock = fromAccount.compareTo(toAccount) < 0 ? toLock : fromLock;

            firstLock.lock();
            try {
                secondLock.lock();
                try {
                    // Actual transfer logic
                    if (withdraw(fromAccount, amount)) {
                        deposit(toAccount, amount);
                    }
                } finally {
                    secondLock.unlock();
                }
            } finally {
                firstLock.unlock();
            }
        }

        private ReentrantLock getAccountLock(String accountNumber) {
            return accountLocks.computeIfAbsent(accountNumber, k -> new ReentrantLock());
        }

        private boolean withdraw(String account, BigDecimal amount) {
            AtomicReference<BigDecimal> balance = balances.get(account);
            if (balance == null) return false;

            BigDecimal current, updated;
            do {
                current = balance.get();
                if (current.compareTo(amount) < 0) return false;
                updated = current.subtract(amount);
            } while (!balance.compareAndSet(current, updated));

            return true;
        }

        private void deposit(String account, BigDecimal amount) {
            balances.computeIfAbsent(account, k -> new AtomicReference<>(BigDecimal.ZERO))
                   .updateAndGet(current -> current.add(amount));
        }
    }

    // ✅ Good: Use appropriate data structures
    public static class EfficientDataStructures {

        // For read-heavy operations
        private final CopyOnWriteArrayList<AuditEntry> auditLog = new CopyOnWriteArrayList<>();

        // For high-throughput scenarios
        private final ConcurrentLinkedQueue<Notification> notifications = new ConcurrentLinkedQueue<>();

        // For producer-consumer with backpressure
        private final LinkedBlockingQueue<Transaction> transactionQueue = new LinkedBlockingQueue<>(10000);

        // For cached data with concurrent access
        private final ConcurrentHashMap<String, Customer> customerCache = new ConcurrentHashMap<>();

        public void addAuditEntry(AuditEntry entry) {
            auditLog.add(entry); // Efficient for read-heavy scenarios
        }

        public void processNotification(Notification notification) {
            notifications.offer(notification); // Lock-free, high performance
        }

        public void submitTransaction(Transaction transaction) throws InterruptedException {
            transactionQueue.put(transaction); // Blocking with capacity limit
        }

        public Customer getCustomer(String id) {
            return customerCache.computeIfAbsent(id, this::loadCustomerFromDatabase);
        }

        private Customer loadCustomerFromDatabase(String id) {
            // Database lookup
            return new Customer(id, "Customer " + id);
        }
    }

    static class AuditEntry {
        final String action;
        final LocalDateTime timestamp;

        AuditEntry(String action, LocalDateTime timestamp) {
            this.action = action;
            this.timestamp = timestamp;
        }
    }

    static class Notification {
        final String message;
        final String type;

        Notification(String message, String type) {
            this.message = message;
            this.type = type;
        }
    }

    static class Customer {
        final String id;
        final String name;

        Customer(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
```

#### 3. Error Handling and Recovery

```java
public class ConcurrencyErrorHandling {

    public static class RobustTransactionProcessor {
        private final ExecutorService executor = Executors.newFixedThreadPool(10);
        private final BlockingQueue<Transaction> retryQueue = new LinkedBlockingQueue<>();
        private final AtomicInteger activeTransactions = new AtomicInteger(0);

        public CompletableFuture<TransactionResult> processTransaction(Transaction transaction) {
            activeTransactions.incrementAndGet();

            return CompletableFuture.supplyAsync(() -> {
                try {
                    return doProcessTransaction(transaction);
                } catch (Exception e) {
                    // Handle specific exceptions
                    if (e instanceof InsufficientFundsException) {
                        return TransactionResult.failed("Insufficient funds");
                    } else if (e instanceof AccountNotFoundException) {
                        return TransactionResult.failed("Account not found");
                    } else {
                        // Unknown error - schedule for retry
                        scheduleRetry(transaction);
                        return TransactionResult.retrying("Scheduled for retry");
                    }
                }
            }, executor)
            .whenComplete((result, throwable) -> {
                activeTransactions.decrementAndGet();

                if (throwable != null) {
                    // Log unexpected errors
                    System.err.println("Unexpected error processing transaction: " + throwable.getMessage());
                }
            });
        }

        private TransactionResult doProcessTransaction(Transaction transaction) {
            // Simulate processing
            if (Math.random() < 0.1) { // 10% failure rate
                throw new RuntimeException("Random processing error");
            }

            return TransactionResult.success("Transaction processed");
        }

        private void scheduleRetry(Transaction transaction) {
            try {
                retryQueue.offer(transaction, 1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Failed to schedule retry for transaction: " + transaction.getId());
            }
        }

        // Graceful shutdown
        public void shutdown() {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    System.out.println("Forcing shutdown after timeout");
                    executor.shutdownNow();

                    // Wait a bit more for tasks to respond to being cancelled
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        System.err.println("Executor did not terminate gracefully");
                    }
                }

                System.out.println("Shutdown completed. Remaining active transactions: " + activeTransactions.get());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }

    static class TransactionResult {
        private final boolean success;
        private final String message;
        private final String status;

        private TransactionResult(boolean success, String message, String status) {
            this.success = success;
            this.message = message;
            this.status = status;
        }

        public static TransactionResult success(String message) {
            return new TransactionResult(true, message, "SUCCESS");
        }

        public static TransactionResult failed(String message) {
            return new TransactionResult(false, message, "FAILED");
        }

        public static TransactionResult retrying(String message) {
            return new TransactionResult(false, message, "RETRYING");
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
}
```

#### 4. Testing Concurrent Code

```java
public class ConcurrencyTesting {

    public static class ConcurrentAccountTest {

        @Test
        public void testConcurrentDeposits() throws InterruptedException {
            ThreadSafeAccount account = new ThreadSafeAccount("TEST001", BigDecimal.ZERO);
            int threadCount = 10;
            int depositsPerThread = 100;
            BigDecimal depositAmount = new BigDecimal("10.00");

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // Submit concurrent deposit tasks
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < depositsPerThread; j++) {
                            account.deposit(depositAmount);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all threads to complete
            latch.await(10, TimeUnit.SECONDS);

            // Verify final balance
            BigDecimal expectedBalance = depositAmount
                .multiply(new BigDecimal(threadCount * depositsPerThread));

            assertEquals(expectedBalance, account.getBalance());

            executor.shutdown();
        }

        @Test
        public void testDeadlockDetection() {
            ThreadSafeAccount account1 = new ThreadSafeAccount("ACC001", new BigDecimal("1000"));
            ThreadSafeAccount account2 = new ThreadSafeAccount("ACC002", new BigDecimal("1000"));

            AtomicBoolean deadlockDetected = new AtomicBoolean(false);

            Thread t1 = new Thread(() -> {
                try {
                    account1.transferTo(account2, new BigDecimal("100"));
                } catch (Exception e) {
                    if (e.getMessage().contains("timeout")) {
                        deadlockDetected.set(true);
                    }
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    account2.transferTo(account1, new BigDecimal("100"));
                } catch (Exception e) {
                    if (e.getMessage().contains("timeout")) {
                        deadlockDetected.set(true);
                    }
                }
            });

            t1.start();
            t2.start();

            try {
                t1.join(5000);
                t2.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // In a well-designed system, deadlock should be prevented or detected
            assertFalse("Deadlock should be prevented", deadlockDetected.get());
        }
    }

    // Helper class for testing
    static class ThreadSafeAccount {
        private final String accountNumber;
        private final AtomicReference<BigDecimal> balance;
        private final ReentrantLock lock = new ReentrantLock();

        public ThreadSafeAccount(String accountNumber, BigDecimal initialBalance) {
            this.accountNumber = accountNumber;
            this.balance = new AtomicReference<>(initialBalance);
        }

        public void deposit(BigDecimal amount) {
            balance.updateAndGet(current -> current.add(amount));
        }

        public void transferTo(ThreadSafeAccount toAccount, BigDecimal amount) {
            // Implementation with timeout to prevent deadlock
            boolean acquired = false;
            try {
                acquired = lock.tryLock(1, TimeUnit.SECONDS);
                if (!acquired) {
                    throw new RuntimeException("Transfer timeout - possible deadlock");
                }

                if (balance.get().compareTo(amount) >= 0) {
                    balance.updateAndGet(current -> current.subtract(amount));
                    toAccount.deposit(amount);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Transfer interrupted");
            } finally {
                if (acquired) {
                    lock.unlock();
                }
            }
        }

        public BigDecimal getBalance() {
            return balance.get();
        }

        public String getAccountNumber() {
            return accountNumber;
        }
    }
}
```

### Summary of Best Practices

1. **Prefer immutable objects** when possible
2. **Use concurrent collections** instead of synchronized wrappers
3. **Minimize lock scope** and avoid nested locks
4. **Use atomic operations** for simple updates
5. **Implement proper error handling** and graceful shutdown
6. **Test thoroughly** with multiple threads and stress scenarios
7. **Monitor performance** and lock contention
8. **Document thread-safety guarantees** in your APIs
9. **Use higher-level abstractions** (ExecutorService, CompletableFuture) over raw threads
10. **Plan for failure scenarios** and implement retry mechanisms

This comprehensive guide provides the foundation for understanding and implementing robust concurrent systems in Java, specifically tailored for banking and financial applications where correctness, performance, and reliability are paramount.