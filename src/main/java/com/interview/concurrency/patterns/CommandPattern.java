package com.interview.concurrency.patterns;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class CommandPattern {

    interface Command {
        void execute();
        void undo();
        String getDescription();
    }

    static class DatabaseCommand implements Command {
        private final String operation;
        private final String data;
        private boolean executed = false;

        public DatabaseCommand(String operation, String data) {
            this.operation = operation;
            this.data = data;
        }

        @Override
        public void execute() {
            System.out.println("Executing DB " + operation + ": " + data);
            try {
                Thread.sleep(500); // Simulate DB operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executed = true;
        }

        @Override
        public void undo() {
            if (executed) {
                System.out.println("Undoing DB " + operation + ": " + data);
                executed = false;
            }
        }

        @Override
        public String getDescription() {
            return "DatabaseCommand[" + operation + ": " + data + "]";
        }
    }

    static class EmailCommand implements Command {
        private final String recipient;
        private final String message;
        private boolean sent = false;

        public EmailCommand(String recipient, String message) {
            this.recipient = recipient;
            this.message = message;
        }

        @Override
        public void execute() {
            System.out.println("Sending email to " + recipient + ": " + message);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sent = true;
        }

        @Override
        public void undo() {
            if (sent) {
                System.out.println("Cannot undo email send to " + recipient);
                // In real world, you might log or send a correction email
            }
        }

        @Override
        public String getDescription() {
            return "EmailCommand[to: " + recipient + "]";
        }
    }

    static class LogCommand implements Command {
        private final String logLevel;
        private final String message;

        public LogCommand(String logLevel, String message) {
            this.logLevel = logLevel;
            this.message = message;
        }

        @Override
        public void execute() {
            System.out.println("[" + logLevel + "] " + message);
        }

        @Override
        public void undo() {
            System.out.println("Log entries cannot be undone");
        }

        @Override
        public String getDescription() {
            return "LogCommand[" + logLevel + "]";
        }
    }

    static class CommandExecutor {
        private final ExecutorService executor;
        private final BlockingQueue<Command> commandQueue;
        private final AtomicInteger executedCount = new AtomicInteger(0);
        private volatile boolean running = true;

        public CommandExecutor(int threadPoolSize) {
            this.executor = Executors.newFixedThreadPool(threadPoolSize);
            this.commandQueue = new LinkedBlockingQueue<>();
            startCommandProcessor();
        }

        private void startCommandProcessor() {
            executor.submit(() -> {
                while (running || !commandQueue.isEmpty()) {
                    try {
                        Command command = commandQueue.poll(1, TimeUnit.SECONDS);
                        if (command != null) {
                            System.out.println("Processing: " + command.getDescription());
                            command.execute();
                            executedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        public void submitCommand(Command command) {
            try {
                commandQueue.put(command);
                System.out.println("Queued: " + command.getDescription());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public Future<Void> executeCommand(Command command) {
            return executor.submit(() -> {
                command.execute();
                executedCount.incrementAndGet();
                return null;
            });
        }

        public <T> Future<T> executeCallableCommand(Callable<T> callable) {
            return executor.submit(callable);
        }

        public void shutdown() {
            running = false;
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            System.out.println("CommandExecutor shut down. Total commands executed: " + executedCount.get());
        }

        public int getExecutedCount() {
            return executedCount.get();
        }

        public int getQueuedCount() {
            return commandQueue.size();
        }
    }

    static class BatchCommandProcessor {
        private final ExecutorService executor = Executors.newCachedThreadPool();

        public CompletableFuture<Void> executeBatch(Command... commands) {
            CompletableFuture<?>[] futures = new CompletableFuture[commands.length];

            for (int i = 0; i < commands.length; i++) {
                final Command cmd = commands[i];
                futures[i] = CompletableFuture.runAsync(() -> {
                    System.out.println("Batch executing: " + cmd.getDescription());
                    cmd.execute();
                }, executor);
            }

            return CompletableFuture.allOf(futures);
        }

        public CompletableFuture<Void> executeSequentially(Command... commands) {
            CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

            for (Command cmd : commands) {
                chain = chain.thenRunAsync(() -> {
                    System.out.println("Sequential executing: " + cmd.getDescription());
                    cmd.execute();
                }, executor);
            }

            return chain;
        }

        public void shutdown() {
            executor.shutdown();
        }
    }

    static class RetryableCommand implements Command {
        private final Command wrappedCommand;
        private final int maxRetries;
        private int attempts = 0;

        public RetryableCommand(Command wrappedCommand, int maxRetries) {
            this.wrappedCommand = wrappedCommand;
            this.maxRetries = maxRetries;
        }

        @Override
        public void execute() {
            while (attempts < maxRetries) {
                try {
                    attempts++;
                    System.out.println("Attempt " + attempts + " for " + wrappedCommand.getDescription());

                    // Simulate random failure
                    if (Math.random() < 0.3 && attempts < maxRetries) {
                        throw new RuntimeException("Random failure simulation");
                    }

                    wrappedCommand.execute();
                    System.out.println("Success on attempt " + attempts);
                    return;
                } catch (Exception e) {
                    System.out.println("Failed attempt " + attempts + ": " + e.getMessage());
                    if (attempts >= maxRetries) {
                        System.out.println("Max retries exceeded for " + wrappedCommand.getDescription());
                        throw e;
                    }
                    try {
                        Thread.sleep(100 * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        @Override
        public void undo() {
            wrappedCommand.undo();
        }

        @Override
        public String getDescription() {
            return "RetryableCommand[" + wrappedCommand.getDescription() + ", maxRetries=" + maxRetries + "]";
        }
    }

    static class TimedCommand implements Command {
        private final Command wrappedCommand;
        private final long timeoutMs;

        public TimedCommand(Command wrappedCommand, long timeoutMs) {
            this.wrappedCommand = wrappedCommand;
            this.timeoutMs = timeoutMs;
        }

        @Override
        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> future = executor.submit(wrappedCommand::execute);

            try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
                System.out.println("Command completed within timeout: " + wrappedCommand.getDescription());
            } catch (TimeoutException e) {
                System.out.println("Command timed out: " + wrappedCommand.getDescription());
                future.cancel(true);
            } catch (Exception e) {
                System.out.println("Command failed: " + e.getMessage());
            } finally {
                executor.shutdown();
            }
        }

        @Override
        public void undo() {
            wrappedCommand.undo();
        }

        @Override
        public String getDescription() {
            return "TimedCommand[" + wrappedCommand.getDescription() + ", timeout=" + timeoutMs + "ms]";
        }
    }

    public static void demonstrateBasicCommandPattern() throws Exception {
        System.out.println("\n=== Basic Command Pattern ===");

        CommandExecutor executor = new CommandExecutor(3);

        executor.submitCommand(new DatabaseCommand("INSERT", "user data"));
        executor.submitCommand(new EmailCommand("user@example.com", "Welcome!"));
        executor.submitCommand(new LogCommand("INFO", "User registration completed"));
        executor.submitCommand(new DatabaseCommand("UPDATE", "user preferences"));

        Thread.sleep(3000);

        System.out.println("Queue status - Executed: " + executor.getExecutedCount() +
                          ", Queued: " + executor.getQueuedCount());

        executor.shutdown();
    }

    public static void demonstrateBatchExecution() throws Exception {
        System.out.println("\n=== Batch Command Execution ===");

        BatchCommandProcessor processor = new BatchCommandProcessor();

        Command[] parallelCommands = {
            new LogCommand("INFO", "Starting batch process"),
            new DatabaseCommand("SELECT", "user list"),
            new DatabaseCommand("SELECT", "product list"),
            new LogCommand("INFO", "Data loaded")
        };

        System.out.println("Executing commands in parallel:");
        CompletableFuture<Void> parallelExecution = processor.executeBatch(parallelCommands);
        parallelExecution.get();

        Thread.sleep(1000);

        Command[] sequentialCommands = {
            new DatabaseCommand("BEGIN", "transaction"),
            new DatabaseCommand("INSERT", "order data"),
            new DatabaseCommand("UPDATE", "inventory"),
            new DatabaseCommand("COMMIT", "transaction")
        };

        System.out.println("\nExecuting commands sequentially:");
        CompletableFuture<Void> sequentialExecution = processor.executeSequentially(sequentialCommands);
        sequentialExecution.get();

        processor.shutdown();
    }

    public static void demonstrateRetryableCommands() throws Exception {
        System.out.println("\n=== Retryable Commands ===");

        CommandExecutor executor = new CommandExecutor(2);

        Command unreliableDbCommand = new DatabaseCommand("UNRELIABLE_OP", "flaky data");
        Command retryableCommand = new RetryableCommand(unreliableDbCommand, 3);

        executor.executeCommand(retryableCommand).get();

        executor.shutdown();
    }

    public static void demonstrateTimedCommands() throws Exception {
        System.out.println("\n=== Timed Commands ===");

        CommandExecutor executor = new CommandExecutor(2);

        // Fast command - should complete
        Command fastCommand = new LogCommand("INFO", "Fast operation");
        Command timedFastCommand = new TimedCommand(fastCommand, 1000);
        executor.executeCommand(timedFastCommand).get();

        // Slow command - should timeout
        Command slowCommand = new DatabaseCommand("SLOW_QUERY", "large dataset");
        Command timedSlowCommand = new TimedCommand(slowCommand, 200);
        executor.executeCommand(timedSlowCommand).get();

        executor.shutdown();
    }

    public static void demonstrateCallableCommands() throws Exception {
        System.out.println("\n=== Callable Commands ===");

        CommandExecutor executor = new CommandExecutor(3);

        // Callable that returns a result
        Callable<String> dataFetcher = () -> {
            Thread.sleep(1000);
            return "Fetched data: " + System.currentTimeMillis();
        };

        Callable<Integer> calculator = () -> {
            Thread.sleep(500);
            return 42 * 24;
        };

        Future<String> dataResult = executor.executeCallableCommand(dataFetcher);
        Future<Integer> calcResult = executor.executeCallableCommand(calculator);

        System.out.println("Data result: " + dataResult.get());
        System.out.println("Calculation result: " + calcResult.get());

        executor.shutdown();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Command Pattern with Executors ===");

        demonstrateBasicCommandPattern();
        Thread.sleep(1000);

        demonstrateBatchExecution();
        Thread.sleep(1000);

        demonstrateRetryableCommands();
        Thread.sleep(1000);

        demonstrateTimedCommands();
        Thread.sleep(1000);

        demonstrateCallableCommands();
    }
}