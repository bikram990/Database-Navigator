package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class EditObjectDataAction extends DumbAwareAction {
    private DBObjectRef<DBSchemaObject> objectRef;
    public EditObjectDataAction(DBSchemaObject object) {
        super("Edit Data", null, Icons.OBEJCT_EDIT_DATA);
        objectRef = DBObjectRef.from(object);
        setDefaultIcon(true);
    }

    public DBSchemaObject getObject() {
        return DBObjectRef.getnn(objectRef);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseFileSystem fileSystem = DatabaseFileSystem.getInstance();
        DBSchemaObject object = getObject();
        fileSystem.openEditor(object, EditorProviderId.DATA, true);
    }
}