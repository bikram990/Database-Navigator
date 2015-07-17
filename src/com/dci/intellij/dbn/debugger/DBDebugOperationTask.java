package com.dci.intellij.dbn.debugger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.intellij.openapi.project.Project;

public abstract class DBDebugOperationTask<T> implements RunnableTask<T> {
    private T handle;

    public static final ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable, "DBN - Database Debug Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        }
    });

    private Project project;
    private String operationDescription;


    public DBDebugOperationTask(Project project, String operationDescription) {
        this.project = project;
        this.operationDescription = operationDescription;
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        int initialPriority = currentThread.getPriority();
        currentThread.setPriority(Thread.MIN_PRIORITY);
        try {
            execute();
        } catch (Exception e) {
            handleException(e);
        } finally {
            currentThread.setPriority(initialPriority);
        }

    }

    public abstract void execute() throws Exception;

    public final void start() {
        try {
            POOL.submit(this);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        NotificationUtil.sendErrorNotification(project, "Debugger", "Error performing debug operation (" + operationDescription + ").", e.getMessage());
    }

    @Override
    public void setHandle(T handle) {
        this.handle = handle;
    }

    @Override
    public T getHandle() {
        return handle;
    }
}