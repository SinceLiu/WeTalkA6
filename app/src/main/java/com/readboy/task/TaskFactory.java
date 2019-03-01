package com.readboy.task;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * TODO，待完善，应用广泛，比如监听联系人后，联系人改变后执行
 *
 * @author oubin
 * @date 2019/2/12
 */
public class TaskFactory {

    protected Queue<Runnable> runnableQueue = new LinkedBlockingQueue<>();

    protected void addRunnable(Runnable runnable) {
        runnableQueue.offer(runnable);
    }

}
