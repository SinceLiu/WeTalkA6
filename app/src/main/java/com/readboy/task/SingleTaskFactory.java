package com.readboy.task;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author oubin
 * @date 2019/2/12
 * 1.同时只会运行一个任务，过程有任务，结束任务后，运行一次任务；
 * 2.不可见过程，不执行任务；可见后，过程有任务队列，就执行；
 * 3.可加延迟处理，防止过多的短小运行。这样处理会损失时效性；
 */
public class SingleTaskFactory extends TaskFactory {

    private ExecutorService executorService;
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1);
    private Handler uiHandler;
    private Handler timerHandler = new Handler();

    public SingleTaskFactory(Handler handler) {
        this.uiHandler = handler;
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(getClass().getSimpleName());
            }
        };
        executorService = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS, workQueue, factory);
        Future<String> result = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return null;
            }
        });

        Future future = executorService.submit(new Runnable() {
            @Override
            public void run() {

            }
        }, "runnable");
        executorService.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

}
