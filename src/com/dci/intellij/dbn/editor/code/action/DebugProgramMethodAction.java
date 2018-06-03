package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.util.ActionUtil.SEPARATOR;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class DebugProgramMethodAction extends GroupPopupAction {
    public DebugProgramMethodAction() {
        super("Debug Method", "", Icons.METHOD_EXECUTION_DEBUG);
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        List<AnAction> actions = new ArrayList<AnAction>();
        Project project = e.getProject();
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        if (project != null && sourceCodeFile != null) {
            DBSchemaObject schemaObject = sourceCodeFile.getObject();
            if (schemaObject.getObjectType().matches(DBObjectType.PROGRAM)) {

                MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                MethodExecutionHistory executionHistory = methodExecutionManager.getExecutionHistory();
                List<DBMethod> recentMethods = executionHistory.getRecentlyExecutedMethods((DBProgram) schemaObject);

                if (recentMethods != null) {
                    for (DBMethod method : recentMethods) {
                        RunMethodAction action = new RunMethodAction(method);
                        actions.add(action);
                    }
                    actions.add(SEPARATOR);
                }

                List<? extends DBObject> objects = schemaObject.getChildObjects(DBObjectType.METHOD);
                for (DBObject object : objects) {
                    if (recentMethods == null || !recentMethods.contains(object)) {
                        RunMethodAction action = new RunMethodAction((DBMethod) object);
                        actions.add(action);
                    }
                }
            }
        }

        return actions.toArray(new AnAction[0]);
    }

    @Nullable
    protected DBSourceCodeVirtualFile getSourcecodeFile(AnActionEvent e) {
        VirtualFile virtualFile = getVirtualFile(e);
        return virtualFile instanceof DBSourceCodeVirtualFile ? (DBSourceCodeVirtualFile) virtualFile : null;
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        boolean visible = false;
        if (sourceCodeFile != null) {
            DBSchemaObject schemaObject = sourceCodeFile.getObject();
            if (schemaObject.getObjectType().matches(DBObjectType.PROGRAM) && DatabaseFeature.DEBUGGING.isSupported(schemaObject)) {
                visible = true;
            }
        }

        presentation.setVisible(visible);
        presentation.setText("Debug Method");
    }

    public class RunMethodAction extends AnObjectAction<DBMethod> {
        public RunMethodAction(DBMethod method) {
            super(method);
        }


        @Override
        public void actionPerformed(AnActionEvent e) {
            Project project = e.getProject();
            DBMethod method = getObject();

            if (project != null && method != null) {
                DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
                debuggerManager.startMethodDebugger(method);
            }
        }
    }
}
