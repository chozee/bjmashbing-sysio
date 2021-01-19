package com.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chozee on 2021-01-18 23:30.
 */
public class TwoThreadWaitNotify {
    private static int stopSize = 5;
    private final static ReentrantLock lock = new ReentrantLock();

    public TwoThreadWaitNotify() {
    }

    static Thread producer = null, consumer = null;

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0;i < 100;i ++) {
            System.out.println("----");
//            lockSupport();
            semaphore();
            System.out.println("----");
            Thread.sleep(100);
        }
    }
    public static void semaphore() throws InterruptedException {
        final List<Long> container = new ArrayList<>(10);
        Semaphore read = new Semaphore(0);
        Semaphore write = new Semaphore(0);

        Thread producer = new Thread(() -> {
            int count = 10;
            try {
                for (int i = 0; i < count; i++) {
                    container.add((long) i);
                    System.out.println("add " + i);

                    if (stopSize-1 == i) {
                        read.release();
                        write.acquire();
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("error " + Thread.currentThread().getName());
            }
        }, "producer");

        Thread consuerm = new Thread(() -> {
            try {
                read.acquire();
                System.out.println("stop size " + container.size());
                write.release();
            } catch (InterruptedException e) {
                System.out.println("error " + Thread.currentThread().getName());
            }
        }, "consumer");

        consuerm.start();
        Thread.sleep(200);
        producer.start();
    }

    public static void lockSupport() {
        final List<Long> container = new ArrayList<>(10);

        consumer = new Thread(() -> {
             LockSupport.park();
             System.out.println("stop size " + container.size());
             LockSupport.unpark(producer);
        }, "consumer");

         producer = new Thread(() -> {
            int count = 10;
            try {
                for (int i = 0; i < count; i++) {
                    container.add((long) i);
                    System.out.println("add " + i);

                    if (stopSize-1 == i) {
                        LockSupport.unpark(consumer);
                        LockSupport.park();
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("error " + Thread.currentThread().getName());
            }
        }, "productor");

        producer.start();
        consumer.start();
    }
    public static void countDown() throws InterruptedException {
        CountDownLatch stop = new CountDownLatch(1);
        CountDownLatch write = new CountDownLatch(1);
        final List<Long> container = new ArrayList<>(10);

        new Thread(() -> {
            int count = 10;
            try {
                for (int i = 0; i < count; i++) {
                    container.add((long) i);
                    System.out.println("add " + i);

                    if (stopSize-1 == i) {
                        stop.countDown();
                        write.await();
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("error " + Thread.currentThread().getName());
            }
        }, "producer").start();



        new Thread(() -> {
            try {
                stop.await();
                System.out.println("stop size " + container.size());
                write.countDown();
            } catch (InterruptedException e) {
                System.out.println("error " + Thread.currentThread().getName());
            }
        }, "consumer").start();

    }

    /**
     * wait/notify模型必须保证,客户端先wait, 不能producer先notify
     *
     * @throws InterruptedException
     */
    public static void waitNotify() throws InterruptedException {
//        ThdCommunication container = new ThdCommunication();
        int stopSize = 5;
        List<Long> container = new ArrayList<>(10);
        Semaphore consumerStartFirst = new Semaphore(0);// 保证客户端先启动

        new Thread(() -> {
            int count = 10;
            try {
                consumerStartFirst.acquire();
                System.out.println("im producer ");
                    for (int i = 0; i < count; i++) {
                        container.add((long) i);
                        System.out.println("add " + i);

                        if (stopSize-1 == i) {
                            synchronized (lock) {
                                lock.notifyAll();
                                System.out.println("I will wait for consumer notify");
                                lock.wait();
                            }
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                    System.out.println("error " + Thread.currentThread().getName());
                }
        }, "producer").start();



        new Thread(() -> {
            try {
                consumerStartFirst.release();
                System.out.println("im client.");

                synchronized (lock) {
                    System.out.println(" i will wait until being notified.");
                    lock.wait();
                    System.out.println("container size " + container.size() + " i will notify producer.");
                    lock.notifyAll();
                }

            } catch (InterruptedException e) {
                System.out.println("error " + Thread.currentThread().getName());
            }
        }, "consumer").start();

    }
}
