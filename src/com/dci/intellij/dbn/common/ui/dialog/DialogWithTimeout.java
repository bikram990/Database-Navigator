package com.dci.intellij.dbn.common.ui.dialog;

import javax.swing.JComponent;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;

public abstract class DialogWithTimeout extends DBNDialog<DialogWithTimeoutForm>{
    private Timer timeoutTimer;
    private int secondsLeft;

    protected DialogWithTimeout(Project project, String title, boolean canBeParent, int timeoutSeconds) {
        super(project, title, canBeParent);
        secondsLeft = timeoutSeconds;
        timeoutTimer = new Timer("DBN - Timeout Dialog Task [" + getProject().getName() + "]");
        timeoutTimer.schedule(new TimeoutTask(), TimeUtil.ONE_SECOND, TimeUtil.ONE_SECOND);
    }

    @NotNull
    @Override
    protected DialogWithTimeoutForm createComponent() {
        return new DialogWithTimeoutForm(secondsLeft);
    }

    @Override
    protected void init() {
        getComponent().setContentComponent(createContentComponent());
        super.init();
    }

    private class TimeoutTask extends TimerTask {
        public void run() {
            try {
                if (secondsLeft > 0) {
                    secondsLeft = secondsLeft -1;
                    getComponent().updateTimeLeft(secondsLeft);
                    if (secondsLeft == 0) {
                        new SimpleLaterInvocator() {
                            @Override
                            protected void execute() {
                                doDefaultAction();
                            }
                        }.start();

                    }
                }
            } catch (ProcessCanceledException ignore) {}
        }
    }

    protected abstract JComponent createContentComponent();

    public abstract void doDefaultAction();

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            timeoutTimer.cancel();
            timeoutTimer.purge();
        }
    }

}
