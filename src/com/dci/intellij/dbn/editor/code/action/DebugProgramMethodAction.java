package com.dci.intellij.dbn.editor.code.action;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class DebugProgramMethodAction extends GroupPopupAction {
    public DebugProgramMethodAction() {
        super("Debug Method", "", Icons.METHOD_EXECUTION_DEBUG);
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        List<AnAction> actions = new ArrayList<AnAction>();
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        if (virtualFile != null) {
            DBSchemaObject schemaObject = virtualFile.getObject();
            if (schemaObject.getObjectType().matches(DBObjectType.PROGRAM)) {
                List<DBObject> objects = schemaObject.getChildObjects(DBObjectType.METHOD);
                for (DBObject object : objects) {
                    RunMethodAction action = new RunMethodAction((DBMethod) object);
                    actions.add(action);
                }
            }
        }

        return actions.toArray(new AnAction[actions.size()]);
    }

    @Nullable
    protected DBSourceCodeVirtualFile getSourcecodeFile(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        return virtualFile instanceof DBSourceCodeVirtualFile ? (DBSourceCodeVirtualFile) virtualFile : null;
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        boolean visible = false;
        if (virtualFile != null) {
            DBSchemaObject schemaObject = virtualFile.getObject();
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