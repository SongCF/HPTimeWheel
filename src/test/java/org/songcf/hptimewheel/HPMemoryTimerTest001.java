package org.songcf.hptimewheel;

/**
 * test case
 *
 * @version : HPMemoryTimerTest001.java
 */
public class HPMemoryTimerTest001 {

    static final int CNT = 10;

    public static void main(String[] args) {
        //100ms精度、20刻度
        {
            HPMemoryTimer timer = new HPMemoryTimer();
            HPMemoryTimer.ExpireCallback hdl = new Consumer();
            //200ms精度、一个时间轮30个刻度
            timer.startWith(200, 30);
            for (int i = 0; i < CNT; i++) {
                Task task = new Task(System.currentTimeMillis() + i * 500, "task" + i, hdl);
                timer.add(task);
            }
        }

        try {
            System.out.println("waiting exit");
            Thread.sleep(10000);
        } catch (Exception e) {

        }
        System.exit(0);
    }

    public static class Consumer implements HPMemoryTimer.ExpireCallback {
        @Override
        public void onTime(Task task) {
            System.out.println("onTime: " + task.getData());
        }
    }
}