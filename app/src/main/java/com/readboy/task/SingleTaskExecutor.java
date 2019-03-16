package com.readboy.task;

import android.os.Handler;
import android.os.HandlerThread;

import java.security.interfaces.RSAKey;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO 未完善。如果针对数据库监听等处理，可以使用LoaderManager，而不是ContentObserver.
 * 思路，可参考{@link android.app.LoaderManager}, {@link android.content.CursorLoader}的设计思路。
 * 其他场景待考虑。
 *
 * @author oubin
 * @date 2019/2/12
 * 1.同时只会运行一个任务，过程有任务，结束任务后，运行一次任务；
 * 2.不可见过程，不执行任务；可见后，过程有任务队列，就执行；
 * 3.可加延迟处理，防止过多的短小运行。这样处理会损失时效性；
 * 4.对执行任务的需求，执行完会反馈给UI线程更新数据，最好有返回值给UI线程
 */
public class SingleTaskExecutor extends AbstractExecutorService {
    private static final String TAG = "oubin-SingleTaskExecutor";

    private ThreadPoolExecutor executorService;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>(1);
    private RejectedExecutionHandler executionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
    private Handler uiHandler;
    private Handler timerHandler = new Handler();
    private HandlerThread handlerThread;

    public SingleTaskExecutor(Handler handler) {
        this.uiHandler = handler;
        ThreadFactory factory = new DefaultThreadFactory();
        executorService = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS, workQueue, factory, executionHandler);
        handlerThread = new HandlerThread(this.getClass().getSimpleName());
        handlerThread.start();
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(task, result);
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(command);
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
