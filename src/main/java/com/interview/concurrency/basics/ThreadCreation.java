package com.interview.concurrency.basics;

public class ThreadCreation {

    static class MyThread extends Thread {
        private final String name;

        public MyThread(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 5; i++) {
                System.out.println(name + " - Count: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(name + " was interrupted");
                    return;
                }
            }
        }
    }

    static class MyRunnable implements Runnable {
        private final String name;

        public MyRunnable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 5; i++) {
                System.out.println(name + " (Runnable) - Count: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(name + " was interrupted");
                    return;
                }
            }
        }
    }

    public static void demonstrateThreadCreation() {
        MyThread thread1 = new MyThread("Thread-1");
        thread1.start();

        Thread thread2 = new Thread(new MyRunnable("Thread-2"));
        thread2.start();

        Thread thread3 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Lambda Thread - Count: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void demonstrateThreadStates() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("State after creation: " + thread.getState()); // NEW

        thread.start();
        System.out.println("State after start: " + thread.getState()); // RUNNABLE

        try {
            Thread.sleep(100);
            System.out.println("State during sleep: " + thread.getState()); // TIMED_WAITING

            thread.join();
            System.out.println("State after completion: " + thread.getState()); // TERMINATED
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Thread Creation Examples ===");
        demonstrateThreadCreation();

        System.out.println("\n=== Thread States ===");
        demonstrateThreadStates();
    }
}