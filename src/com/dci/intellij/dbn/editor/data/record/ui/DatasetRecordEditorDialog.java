package com.dci.intellij.dbn.editor.data.record.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.intellij.openapi.project.Project;

public class DatasetRecordEditorDialog extends DBNDialog<DatasetRecordEditorForm> {
    public DatasetRecordEditorDialog(Project project, DatasetEditorModelRow row) {
        super(project, row.getModel().isEditable() ? "Edit record" : "View record", true);
        setModal(true);
        setResizable(true);
        component = new DatasetRecordEditorForm(this, row);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
}
