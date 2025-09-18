package com.interview.concurrency.patterns;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumer {

    static class WaitNotifyQueue {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;

        public WaitNotifyQueue(int capacity) {
            this.capacity = capacity;
        }

        public synchronized void produce(int item) throws InterruptedException {
            while (queue.size() == capacity) {
                System.out.println("Queue is full, producer waiting...");
                wait();
            }
            queue.offer(item);
            System.out.println("Produced: " + item);
            notifyAll();
        }

        public synchronized int consume() throws InterruptedException {
            while (queue.isEmpty()) {
                System.out.println("Queue is empty, consumer waiting...");
                wait();
            }
            int item = queue.poll();
            System.out.println("Consumed: " + item);
            notifyAll();
            return item;
        }
    }

    static class LockConditionQueue {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;
        private final Lock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        public LockConditionQueue(int capacity) {
            this.capacity = capacity;
        }

        public void produce(int item) throws InterruptedException {
            lock.lock();
            try {
                while (queue.size() == capacity) {
                    System.out.println("Queue is full, producer waiting...");
                    notFull.await();
                }
                queue.offer(item);
                System.out.println("Produced: " + item);
                notEmpty.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public int consume() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    System.out.println("Queue is empty, consumer waiting...");
                    notEmpty.await();
                }
                int item = queue.poll();
                System.out.println("Consumed: " + item);
                notFull.signalAll();
                return item;
            } finally {
                lock.unlock();
            }
        }
    }

    static class BlockingQueueExample {
        private final BlockingQueue<Integer> queue;

        public BlockingQueueExample(int capacity) {
            this.queue = new ArrayBlockingQueue<>(capacity);
        }

        public void produce(int item) throws InterruptedException {
            queue.put(item);
            System.out.println("Produced: " + item);
        }

        public int consume() throws InterruptedException {
            int item = queue.take();
            System.out.println("Consumed: " + item);
            return item;
        }
    }

    static class Producer implements Runnable {
        private final java.util.function.Consumer<Integer> produceMethod;
        private final int startValue;
        private final int count;

        public Producer(java.util.function.Consumer<Integer> produceMethod, int startValue, int count) {
            this.produceMethod = produceMethod;
            this.startValue = startValue;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = startValue; i < startValue + count; i++) {
                    produceMethod.accept(i);
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final java.util.function.Supplier<Integer> consumeMethod;
        private final int count;

        public Consumer(java.util.function.Supplier<Integer> consumeMethod, int count) {
            this.consumeMethod = consumeMethod;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < count; i++) {
                    consumeMethod.get();
                    Thread.sleep(150);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void demonstrateWaitNotify() throws InterruptedException {
        System.out.println("\n=== Wait/Notify Implementation ===");
        WaitNotifyQueue queue = new WaitNotifyQueue(5);

        Thread producer = new Thread(new Producer(item -> {
            try {
                queue.produce(item);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 1, 10));

        Thread consumer = new Thread(new Consumer(() -> {
            try {
                return queue.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }, 10));

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
    }

    public static void demonstrateLockCondition() throws InterruptedException {
        System.out.println("\n=== Lock/Condition Implementation ===");
        LockConditionQueue queue = new LockConditionQueue(5);

        Thread producer = new Thread(new Producer(item -> {
            try {
                queue.produce(item);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 1, 10));

        Thread consumer = new Thread(new Consumer(() -> {
            try {
                return queue.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }, 10));

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
    }

    public static void demonstrateBlockingQueue() throws InterruptedException {
        System.out.println("\n=== BlockingQueue Implementation ===");
        BlockingQueueExample queue = new BlockingQueueExample(5);

        Thread producer1 = new Thread(new Producer(item -> {
            try {
                queue.produce(item);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 1, 5));

        Thread producer2 = new Thread(new Producer(item -> {
            try {
                queue.produce(item);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 100, 5));

        Thread consumer1 = new Thread(new Consumer(() -> {
            try {
                return queue.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }, 5));

        Thread consumer2 = new Thread(new Consumer(() -> {
            try {
                return queue.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }, 5));

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();

        producer1.join();
        producer2.join();
        consumer1.join();
        consumer2.join();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Producer-Consumer Pattern Examples ===");

        demonstrateWaitNotify();
        Thread.sleep(1000);

        demonstrateLockCondition();
        Thread.sleep(1000);

        demonstrateBlockingQueue();
    }
}