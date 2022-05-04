# HPTimeWheel

高效内存定时器，使用时间轮实现。

---

多层次时间轮【类似钟表,kafka-timeWheel】

- 使用时间轮+delayQueue：优化netty的时间轮出现空转和多层任务落在同一bucket时无效遍历的问题；
- 优化纯用delayQueue大量任务时频繁调整堆；

---

使用方式（参考测试用例）：
```
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
```
