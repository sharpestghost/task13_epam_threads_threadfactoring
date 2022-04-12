package com.epam.rd.autotasks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class FinishedThreadResult implements ThreadUnion{
    private final String threadName;
    private final LocalDateTime finished;
    private final Throwable throwable;
    private int size;
    private boolean isShutdown = false;
    private List<Thread> threadList = new ArrayList<>();
    private List<FinishedThreadResult> finishedThreadList = new ArrayList<>();
    private static final String THREAD_TITLE = "testThreadName-worker-";

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
    public void shutdown() {
        threadList.forEach(Thread::interrupt);
        isShutdown = true;
    }


    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public synchronized void awaitTermination() {
        for (Thread thread : threadList) {
            try {
                finishedThreadList.add(new FinishedThreadResult(thread.getName()));
            } catch (Throwable e) {
                finishedThreadList.add(new FinishedThreadResult(thread.getName(), e));
            }
        }
    }

    @Override
    public boolean isFinished() {
        return isShutdown;
    }

    @Override
    public synchronized List<FinishedThreadResult> results() {
        return finishedThreadList;
    }
    @Override
    public synchronized Thread newThread(Runnable r) {
        if (!isShutdown) {
            Thread thread = new Thread(r, THREAD_TITLE +  threadList.size());
            threadList.add(thread);
            return thread;
        }
        else {
            throw new IllegalStateException();
        }
    }

}
