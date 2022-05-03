package org.songcf.hptimewheel;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author songcf
 * @version : Bucket.java
 */
public class Bucket implements Delayed {

    private final AtomicLong  expireTimeMillis = new AtomicLong(0L);
    private final Queue<Task> taskList         = new ConcurrentLinkedQueue<>();

    public void expire(HPMemoryTimer.ExpireCallback cb) {
        taskList.forEach(cb::onTime);
        taskList.clear();
    }

    public void add(Task task) {
        //concurrentQueue
        taskList.add(task);
    }

    public long getExpireTimeMillis() {
        return expireTimeMillis.get();
    }

    public boolean setExpireTimeMillis(long expireTs) {
        //true：过期后的bucket会重新设置时间，新时间和旧时间会不同
        //false：未过期的bucket
        return expireTimeMillis.getAndSet(expireTs) != expireTs;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long dt = expireTimeMillis.get() - HPMemoryTimer.getCurrentMilliSecond();
        return dt > 0 ? dt : 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.expireTimeMillis.get(), ((Bucket) o).getExpireTimeMillis());
    }
}