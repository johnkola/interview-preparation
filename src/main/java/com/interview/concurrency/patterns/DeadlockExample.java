package com.interview.concurrency.patterns;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockExample {

    static class Account {
        private final String name;
        private double balance;
        private final Lock lock = new ReentrantLock();

        public Account(String name, double balance) {
            this.name = name;
            this.balance = balance;
        }

        public String getName() {
            return name;
        }

        public double getBalance() {
            return balance;
        }

        public void debit(double amount) {
            balance -= amount;
        }

        public void credit(double amount) {
            balance += amount;
        }

        public Lock getLock() {
            return lock;
        }
    }

    public static void demonstrateDeadlock() {
        System.out.println("\n=== Deadlock Example ===");
        Account account1 = new Account("Account1", 1000);
        Account account2 = new Account("Account2", 2000);

        Thread thread1 = new Thread(() -> {
            transferWithDeadlock(account1, account2, 100);
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            transferWithDeadlock(account2, account1, 200);
        }, "Thread-2");

        thread1.start();
        thread2.start();

        try {
            thread1.join(3000);
            thread2.join(3000);

            if (thread1.isAlive() || thread2.isAlive()) {
                System.out.println("Deadlock detected! Threads are still running.");
                thread1.interrupt();
                thread2.interrupt();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void transferWithDeadlock(Account from, Account to, double amount) {
        System.out.println(Thread.currentThread().getName() + " attempting to transfer " +
                amount + " from " + from.getName() + " to " + to.getName());

        synchronized (from) {
            System.out.println(Thread.currentThread().getName() + " locked " + from.getName());

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            synchronized (to) {
                System.out.println(Thread.currentThread().getName() + " locked " + to.getName());
                from.debit(amount);
                to.credit(amount);
                System.out.println(Thread.currentThread().getName() + " completed transfer");
            }
        }
    }

    public static void demonstrateDeadlockPrevention() {
        System.out.println("\n=== Deadlock Prevention - Ordered Locking ===");
        Account account1 = new Account("Account1", 1000);
        Account account2 = new Account("Account2", 2000);

        Thread thread1 = new Thread(() -> {
            transferWithOrderedLocks(account1, account2, 100);
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            transferWithOrderedLocks(account2, account1, 200);
        }, "Thread-2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
            System.out.println("All transfers completed successfully!");
            System.out.println(account1.getName() + " balance: " + account1.getBalance());
            System.out.println(account2.getName() + " balance: " + account2.getBalance());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void transferWithOrderedLocks(Account from, Account to, double amount) {
        Account firstLock = from.hashCode() < to.hashCode() ? from : to;
        Account secondLock = from.hashCode() < to.hashCode() ? to : from;

        synchronized (firstLock) {
            synchronized (secondLock) {
                System.out.println(Thread.currentThread().getName() + " transferring " +
                        amount + " from " + from.getName() + " to " + to.getName());
                from.debit(amount);
                to.credit(amount);
            }
        }
    }

    public static void demonstrateTryLock() {
        System.out.println("\n=== Deadlock Prevention - TryLock with Timeout ===");
        Account account1 = new Account("Account1", 1000);
        Account account2 = new Account("Account2", 2000);

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                if (transferWithTryLock(account1, account2, 100)) {
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                if (transferWithTryLock(account2, account1, 200)) {
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
            System.out.println("All transfers completed!");
            System.out.println(account1.getName() + " balance: " + account1.getBalance());
            System.out.println(account2.getName() + " balance: " + account2.getBalance());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean transferWithTryLock(Account from, Account to, double amount) {
        boolean fromLocked = false;
        boolean toLocked = false;

        try {
            fromLocked = from.getLock().tryLock(1000, TimeUnit.MILLISECONDS);
            if (fromLocked) {
                toLocked = to.getLock().tryLock(1000, TimeUnit.MILLISECONDS);
                if (toLocked) {
                    System.out.println(Thread.currentThread().getName() + " transferring " +
                            amount + " from " + from.getName() + " to " + to.getName());
                    from.debit(amount);
                    to.credit(amount);
                    return true;
                }
            }

            System.out.println(Thread.currentThread().getName() + " failed to acquire locks, will retry");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (toLocked) {
                to.getLock().unlock();
            }
            if (fromLocked) {
                from.getLock().unlock();
            }
        }
    }

    static class PhilosopherDeadlock {
        static class Fork {
            private final int id;

            public Fork(int id) {
                this.id = id;
            }

            public int getId() {
                return id;
            }
        }

        static class Philosopher implements Runnable {
            private final String name;
            private final Fork leftFork;
            private final Fork rightFork;

            public Philosopher(String name, Fork leftFork, Fork rightFork) {
                this.name = name;
                this.leftFork = leftFork;
                this.rightFork = rightFork;
            }

            @Override
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        think();
                        eat();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            private void think() throws InterruptedException {
                System.out.println(name + " is thinking");
                Thread.sleep((long) (Math.random() * 100));
            }

            private void eat() throws InterruptedException {
                Fork firstFork = leftFork.getId() < rightFork.getId() ? leftFork : rightFork;
                Fork secondFork = leftFork.getId() < rightFork.getId() ? rightFork : leftFork;

                synchronized (firstFork) {
                    System.out.println(name + " picked up fork " + firstFork.getId());
                    synchronized (secondFork) {
                        System.out.println(name + " picked up fork " + secondFork.getId());
                        System.out.println(name + " is eating");
                        Thread.sleep((long) (Math.random() * 100));
                    }
                    System.out.println(name + " put down fork " + secondFork.getId());
                }
                System.out.println(name + " put down fork " + firstFork.getId());
            }
        }

        public static void demonstrate() throws InterruptedException {
            System.out.println("\n=== Dining Philosophers (Deadlock Prevention) ===");
            int numPhilosophers = 5;
            Fork[] forks = new Fork[numPhilosophers];
            Thread[] philosophers = new Thread[numPhilosophers];

            for (int i = 0; i < numPhilosophers; i++) {
                forks[i] = new Fork(i);
            }

            for (int i = 0; i < numPhilosophers; i++) {
                Fork leftFork = forks[i];
                Fork rightFork = forks[(i + 1) % numPhilosophers];
                philosophers[i] = new Thread(
                    new Philosopher("Philosopher-" + i, leftFork, rightFork)
                );
                philosophers[i].start();
            }

            for (Thread philosopher : philosophers) {
                philosopher.join();
            }

            System.out.println("All philosophers have finished dining!");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Deadlock Examples and Prevention ===");

        demonstrateDeadlock();
        Thread.sleep(4000);

        demonstrateDeadlockPrevention();
        Thread.sleep(1000);

        demonstrateTryLock();
        Thread.sleep(1000);

        PhilosopherDeadlock.demonstrate();
    }
}