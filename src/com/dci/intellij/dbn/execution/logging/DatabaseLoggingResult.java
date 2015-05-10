package com.dci.intellij.dbn.execution.logging;

import javax.swing.Icon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLoggingResultConsole;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLoggingResultForm;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class DatabaseLoggingResult implements ExecutionResult {
    private LogOutputContext context;
    private DatabaseLoggingResultForm logOutputForm;

    public DatabaseLoggingResult(@NotNull LogOutputContext context) {
        this.context = context;
    }
    public DatabaseLoggingResultForm getForm(boolean create) {
        if (logOutputForm == null && create) {
            logOutputForm = new DatabaseLoggingResultForm(getProject(), this);
            Disposer.register(logOutputForm, this);
        }
        return logOutputForm;
    }

    @NotNull
    public LogOutputContext getContext() {
        return FailsafeUtil.get(context);
    }

    @Override
    @NotNull
    public String getName() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        VirtualFile sourceFile = context.getSourceFile();
        if (sourceFile == null) {
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            String databaseLogName = compatibilityInterface.getDatabaseLogName();

            return connectionHandler.getName() + " - " + CommonUtil.nvl(databaseLogName, "Log Output");
        } else {
            return connectionHandler.getName() + " - " + sourceFile.getName();
        }
    }

    public boolean matches(LogOutputContext context) {
        return this.context == context || this.context.matches(context);
    }

    @Override
    public Icon getIcon() {
        return Icons.EXEC_LOG_OUTPUT_CONSOLE;
    }

    @NotNull
    @Override
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Override
    public String getConnectionId() {
        return context.getConnectionId();
    }

    @Nullable
    public VirtualFile getSourceFile() {
        return context.getSourceFile();
    }

    @NotNull
    @Override
    public ConnectionHandler getConnectionHandler() {
        return context.getConnectionHandler();
    }

    @Override
    public PsiFile createPreviewFile() {
        return null;
    }

    public void write(LogOutputContext context, LogOutput output) {
        this.context = context;
        if (logOutputForm != null && ! logOutputForm.isDisposed()) {
            DatabaseLoggingResultConsole console = logOutputForm.getConsole();
            if (output.isScrollToEnd()) {
                ConsoleView consoleView = console.getConsole();
                if (consoleView instanceof ConsoleViewImpl) {
                    ConsoleViewImpl consoleViewImpl = (ConsoleViewImpl) consoleView;
                    consoleViewImpl.requestScrollingToEnd();
                }
            }
            console.writeToConsole(context, output);
        }
    }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.DATABASE_LOG_OUTPUT.is(dataId)) {
                return DatabaseLoggingResult.this;
            }
            return null;
        }
    };

    @Nullable
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        disposed = true;
        context = null;
    }
}