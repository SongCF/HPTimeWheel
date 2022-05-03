package org.songcf.hptimewheel;

/**
 * @author songcf
 * @version : TimerTask.java
 */
public class Task implements Runnable {

    private volatile boolean                      cancelled = false;
    private final    long                         expireTimeMillis;
    private final    Object                       data;
    private final    HPMemoryTimer.ExpireCallback callback;

    public Task(long expireTimeMillis, Object data, HPMemoryTimer.ExpireCallback cb) {
        this.expireTimeMillis = expireTimeMillis;
        this.data = data;
        this.callback = cb;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public long getExpireTimeMillis() {
        return expireTimeMillis;
    }

    public Object getData() {
        return data;
    }

    @Override
    public void run() {
        if (callback != null) {
            callback.onTime(this);
        }
    }
}