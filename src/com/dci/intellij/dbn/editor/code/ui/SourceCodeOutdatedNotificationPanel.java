package com.dci.intellij.dbn.editor.code.ui;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.diff.MergeAction;
import com.dci.intellij.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.util.text.DateFormatUtil;

import static com.dci.intellij.dbn.vfs.VirtualFileStatus.MODIFIED;

public class SourceCodeOutdatedNotificationPanel extends SourceCodeEditorNotificationPanel{
    public SourceCodeOutdatedNotificationPanel(final DBSourceCodeVirtualFile sourceCodeFile, final SourceCodeEditor sourceCodeEditor) {
        super(MessageType.WARNING);
        final DBSchemaObject editableObject = sourceCodeFile.getObject();
        final Project project = editableObject.getProject();
        String presentableChangeTime =
                DatabaseFeature.OBJECT_CHANGE_TRACING.isSupported(editableObject) ?
                        DateFormatUtil.formatPrettyDateTime(sourceCodeFile.getDatabaseChangeTimestamp()).toLowerCase() : "";


        String text = "Outdated version";
        boolean mergeRequired = sourceCodeFile.isMergeRequired();
        if (sourceCodeFile.is(MODIFIED) && !mergeRequired) {
            text += " (MERGED)";
        }
        text += ". The " + editableObject.getQualifiedNameWithType() + " was changed in database by another user (" + presentableChangeTime + ")";

        setText(text);
        createActionLabel("Show Diff", new Runnable() {
            @Override
            public void run() {
                if (!project.isDisposed()) {
                    SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
                    diffManager.opedDatabaseDiffWindow(sourceCodeFile);
                }
            }
        });

        if (mergeRequired) {
            createActionLabel("Merge", new Runnable() {
                @Override
                public void run() {
                    if (!project.isDisposed()) {
                        try {
                            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                            SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
                            SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(editableObject, sourceCodeEditor.getContentType());
                            String databaseContent = sourceCodeContent.getText().toString();
                            diffManager.openCodeMergeDialog(databaseContent, sourceCodeFile, sourceCodeEditor, MergeAction.MERGE);
                        }catch (Exception e) {
                            MessageUtil.showErrorDialog(project, "Could not load sources from database.", e);

                        }
                    }
                }
            });
        }

        createActionLabel(sourceCodeFile.is(MODIFIED) ? "Revert local changes" : "Reload", new Runnable() {
            @Override
            public void run() {
                if (!project.isDisposed()) {
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    sourceCodeManager.loadSourceCode(sourceCodeFile, true);
                }
            }
        });
    }
}
