package com.dci.intellij.dbn.execution.logging.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.intellij.ide.actions.NextOccurenceToolbarAction;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class DatabaseLoggingResultForm extends DBNFormImpl implements ExecutionResultForm<DatabaseLoggingResult>{
    private JPanel mainPanel;
    private JPanel consolePanel;
    private JPanel actionsPanel;

    private DatabaseLoggingResult loggingResult;
    private DatabaseLoggingResultConsole console;

    public DatabaseLoggingResultForm(Project project, DatabaseLoggingResult loggingResult) {
        super(project);
        this.loggingResult = loggingResult;
        ConnectionHandler connectionHandler = loggingResult.getConnectionHandler();
        VirtualFile sourceFile = loggingResult.getSourceFile();
        console = new DatabaseLoggingResultConsole(connectionHandler, loggingResult.getName(), false);
        consolePanel.add(console.getComponent(), BorderLayout.CENTER);

        ActionManager actionManager = ActionManager.getInstance();
        //ActionGroup actionGroup = (ActionGroup) actionManager.getAction("DBNavigator.ActionGroup.DatabaseLogOutput");
        DefaultActionGroup toolbarActions = (DefaultActionGroup) console.getToolbarActions();
        if (toolbarActions != null) {
            for (AnAction action : toolbarActions.getChildActionsOrStubs()) {
                if (action instanceof PreviousOccurenceToolbarAction || action instanceof NextOccurenceToolbarAction) {
                    toolbarActions.remove(action);
                }
            }

            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.KillProcess"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.Close"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.Settings"), Constraints.LAST);
            ActionToolbar actionToolbar = actionManager.createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false);
            actionsPanel.add(actionToolbar.getComponent());
            actionToolbar.setTargetComponent(console.getToolbarContextComponent());
        }


        Disposer.register(this, console);
        ActionUtil.registerDataProvider(mainPanel, loggingResult);
    }

    public DatabaseLoggingResult getLoggingResult() {
        return loggingResult;
    }

    public DatabaseLoggingResultConsole getConsole() {
        return console;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void setExecutionResult(DatabaseLoggingResult executionResult) {}

    @Override
    public DatabaseLoggingResult getExecutionResult() {
        return loggingResult;
    }

    @Override
    public void dispose() {
        super.dispose();
        console = null;
        loggingResult = null;
    }
}