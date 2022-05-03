package org.songcf.hptimewheel;

import org.songcf.utils.AbstractLoopRunnable;

import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Timer.createTimer().startWith(100, 20);
 *
 * @author songcf
 * @version : Timer.java
 */
public class HPMemoryTimer extends AbstractLoopRunnable {

    public interface ExpireCallback {

        /**
         * 到期回调
         *
         * @param task
         */
        void onTime(Task task);
    }

    private final AtomicLong         taskCount  = new AtomicLong(0L);
    private final DelayQueue<Bucket> delayQueue = new DelayQueue<Bucket>();
    private final ReadWriteLock      rwLock     = new ReentrantReadWriteLock();
    private       TimeWheel          timeWheel;
    private       ExecutorService    threadPoolExecutor;

    public void startWith(int tickMilliSecond, int wheelSize) {
        this.startWith(tickMilliSecond, wheelSize, Executors.newFixedThreadPool(4));
    }

    public void startWith(int tickMilliSecond, int wheelSize, ExecutorService threadPoolExecutor) {
        synchronized (this) {
            if (isRunning()) {
                return;
            }
            this.timeWheel = new TimeWheel(wheelSize, tickMilliSecond, delayQueue);
            this.threadPoolExecutor = threadPoolExecutor;
            startThread();
        }
    }

    public void shutdown() {
        stopThread();
    }

    public void add(Task task) {
        boolean ok;
        //lock ==> 时间轮滚动的时候不能添加任务
        rwLock.readLock().lock();
        try {
            ok = this.timeWheel.addTask(task);
        } finally {
            rwLock.readLock().unlock();
        }
        if (ok) {
            taskCount.incrementAndGet();
        } else if (!task.isCancelled()) {
            //已经超时
            threadPoolExecutor.submit(task);
        }
    }

    public long size() {
        return taskCount.get();
    }

    @Override
    protected long onLoop(Map<String, Object> context, long lastReturnTimestamp) throws InterruptedException {
        Bucket bucket = delayQueue.take();
        //lock ==> 时间轮滚动的时候不能添加任务
        rwLock.writeLock().lock();
        try {
            timeWheel.advanceClock(bucket.getExpireTimeMillis());
            bucket.expire(this::flush);
        } finally {
            //unlock
            rwLock.writeLock().unlock();
        }
        return 0;
    }

    @Override
    protected void onShutdown() {
        taskCount.set(0);
        delayQueue.clear();
        timeWheel = null;
        threadPoolExecutor.shutdown();
        threadPoolExecutor = null;
    }

    public static long getCurrentMilliSecond() {
        return System.currentTimeMillis();
    }

    private void flush(Task task) {
        //最小轮添加会失败，进入submit
        //其它轮超时后，会迁移刷新到小轮次中
        boolean ok = this.timeWheel.addTask(task);
        if (ok) {
            taskCount.incrementAndGet();
        } else if (task.isCancelled()) {
            taskCount.decrementAndGet();
        } else {
            //已经超时
            taskCount.decrementAndGet();
            threadPoolExecutor.submit(task);
        }
    }

}