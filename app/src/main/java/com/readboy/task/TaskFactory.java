package com.readboy.task;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
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
