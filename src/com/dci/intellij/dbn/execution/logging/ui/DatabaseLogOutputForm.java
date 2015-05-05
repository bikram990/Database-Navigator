package com.dci.intellij.dbn.execution.logging.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.logging.DatabaseLogOutput;
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

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class DatabaseLogOutputForm extends DBNFormImpl implements ExecutionResultForm<DatabaseLogOutput>{
    private JPanel mainPanel;
    private JPanel consolePanel;
    private JPanel actionsPanel;

    private DatabaseLogOutput databaseLogOutput;
    private DatabaseLogOutputConsole console;

    public DatabaseLogOutputForm(Project project, DatabaseLogOutput databaseLogOutput) {
        super(project);
        this.databaseLogOutput = databaseLogOutput;
        ConnectionHandler connectionHandler = databaseLogOutput.getConnectionHandler();
        VirtualFile sourceFile = databaseLogOutput.getSourceFile();
        console = new DatabaseLogOutputConsole(connectionHandler, sourceFile, databaseLogOutput.getName(), false);
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

            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.Close"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.Settings"), Constraints.LAST);
            ActionToolbar actionToolbar = actionManager.createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false);
            actionsPanel.add(actionToolbar.getComponent());
            actionToolbar.setTargetComponent(actionsPanel);
        }


        Disposer.register(this, console);
        ActionUtil.registerDataProvider(mainPanel, databaseLogOutput);
    }

    public DatabaseLogOutput getDatabaseLogOutput() {
        return databaseLogOutput;
    }

    public DatabaseLogOutputConsole getConsole() {
        return console;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void setExecutionResult(DatabaseLogOutput executionResult) {}

    @Override
    public DatabaseLogOutput getExecutionResult() {
        return databaseLogOutput;
    }

    @Override
    public void dispose() {
        super.dispose();
        console = null;
        databaseLogOutput = null;
    }
}
