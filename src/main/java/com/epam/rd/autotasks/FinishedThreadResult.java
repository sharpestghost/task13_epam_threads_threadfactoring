package com.epam.rd.autotasks;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FinishedThreadResult implements ThreadUnion {
    private static final String THREAD_TITLE = "-worker-";
    private final String threadName;
    private final LocalDateTime finished;
    private final Throwable throwable;
    private final List<Thread> threadList = new ArrayList<>();
    private final List<FinishedThreadResult> finishedThreadList = new ArrayList<>();
    private boolean isShutdown;

    public FinishedThreadResult(final String threadName) {
        this(threadName, null);
    }

    public FinishedThreadResult(final String threadName, final Throwable throwable) {
        this.threadName = threadName;
        this.throwable = throwable;
        this.finished = LocalDateTime.now();
    }

    public String getThreadName() {
        return threadName;
    }

    public LocalDateTime getFinished() {
        return finished;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public int totalSize() {
        return threadList.size();
    }

    @Override
    public int activeSize() {
        return (int) threadList.stream().filter(Thread::isAlive).count();
    }

    @Override
    public synchronized void shutdown() {
        Thread.yield();
        threadList.forEach(Thread::interrupt);
        isShutdown = true;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public synchronized void awaitTermination() {
        while (!isFinished()) {
            //i don't understand what i really need to do here
            Thread.yield();
            shutdown();
        }
    }


    @Override
    public boolean isFinished() {
        return isShutdown() && activeSize() == 0;
    }

    @Override
    public synchronized List<FinishedThreadResult> results() {
        return new ArrayList<>(finishedThreadList);
    }

    @Override
    public synchronized Thread newThread(@Nonnull Runnable r) {
        if (!isShutdown) {
            Thread t = new Thread(r, threadName + THREAD_TITLE + threadList.size()) {
                @Override
                public synchronized void run() {
                    boolean finallyExecution = true;
                    try {
                        super.run();
                    } catch (Exception e) {
                        finishedThreadList.add(new FinishedThreadResult(this.getName(), e));
                        finallyExecution = false;
                    } finally {
                        if (finallyExecution) {
                            finishedThreadList.add(new FinishedThreadResult(this.getName()));
                        }
                    }
                }
            };
            threadList.add(t);
            return t;
        } else {
            throw new IllegalStateException();
        }
    }

}
