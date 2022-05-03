package org.songcf.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author songcf
 * @version : SingletonRunnable
 */
public abstract class AbstractLoopRunnable implements Runnable {

    //private static final int           IDLE          = 0;
    //private static final int           RUNNING       = 1;
    //private static final int           TERMINATED    = 2;
    //private final        AtomicInteger threadStatus  = new AtomicInteger(IDLE);
    private volatile Thread currentThread = null;

    @Override
    final public void run() {
        try {
            LogUtil.info("thread onStart: " + selfName());
            onStart();
            process();
        } finally {
            currentThread = null;
            onShutdown();
            LogUtil.info("thread onShutdown: " + selfName());
        }
    }

    /**
     * 处理器
     */
    public void process() {
        long sleepMillis = 0;
        long lastReturnTimestamp = 0;
        Map<String, Object> args = new HashMap<>();
        while (true) {
            try {
                sleepMillis = onLoop(args, lastReturnTimestamp);
                lastReturnTimestamp = System.currentTimeMillis();
                if (sleepMillis > 0) {
                    Thread.sleep(sleepMillis);
                }
            } catch (InterruptedException e) {
                LogUtil.warn("thread isInterrupted: " + selfName(), e);
                break;
            } catch (Exception e) {
                String strStackTrace = LogUtil.getStackTrace(e);
                LogUtil.error("thread onLoop failed: " + selfName(), e, "\ntrace:", strStackTrace);
            }
            if (currentThread.isInterrupted()) {
                LogUtil.warn("thread isInterrupted: " + selfName());
                break;
            }
        }
    }

    /**
     * 循环体
     *
     * @param context             上下文
     * @param lastReturnTimestamp 上次loop返回时的时间
     * @return
     * @throws InterruptedException
     */
    abstract protected long onLoop(Map<String, Object> context, long lastReturnTimestamp) throws InterruptedException;

    protected void onStart() {
    }

    protected void onShutdown() {
    }

    /**
     * 运行中
     *
     * @return
     */
    public boolean isRunning() {
        return currentThread != null;
    }

    /**
     * 启动
     */
    public void startThread() {
        synchronized (this) {
            if (currentThread == null) {
                currentThread = new Thread(this);
                currentThread.start();
            } else {
                LogUtil.error("thread already started: " + selfName());
            }
        }
    }

    /**
     * 停止
     */
    public void stopThread() {
        synchronized (this) {
            if (currentThread != null) {
                currentThread.interrupt();
            }
        }
    }
    
    private String selfName() {
        return this.getClass().getSimpleName();
    }

}