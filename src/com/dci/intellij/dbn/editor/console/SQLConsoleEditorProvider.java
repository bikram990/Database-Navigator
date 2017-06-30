package com.dci.intellij.dbn.editor.console;

import java.awt.BorderLayout;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.editor.BasicTextEditorProvider;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.console.ui.SQLConsoleEditorToolbarForm;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;


public class SQLConsoleEditorProvider extends BasicTextEditorProvider implements DumbAware{

    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return virtualFile instanceof DBConsoleVirtualFile;
    }

    @NotNull
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile virtualFile) {
        SQLConsoleEditorState editorState = new SQLConsoleEditorState();
        editorState.readState(sourceElement, project, virtualFile);
        return editorState;
    }

    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        if (state instanceof SQLConsoleEditorState) {
            SQLConsoleEditorState editorState = (SQLConsoleEditorState) state;
            editorState.writeState(targetElement, project);
        }
    }

    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) file;
        SQLConsoleEditor editor = new SQLConsoleEditor(project, consoleVirtualFile, "SQL Console", getEditorProviderId());
        SQLConsoleEditorToolbarForm toolbarForm = new SQLConsoleEditorToolbarForm(project, editor);
        editor.getComponent().add(toolbarForm.getComponent(), BorderLayout.NORTH);

        Document document = editor.getEditor().getDocument();
        int documentTracking = document.hashCode();
        if (document.hashCode() != consoleVirtualFile.getDocumentHashCode()) {
            document.addDocumentListener(consoleVirtualFile);
            consoleVirtualFile.setDocumentHashCode(documentTracking);
        }
        return editor;
    }

    public void disposeEditor(@NotNull FileEditor editor) {
        editor.dispose();
    }

    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    @NotNull
    @Override
    public EditorProviderId getEditorProviderId() {
        return EditorProviderId.CONSOLE;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.SQLConsoleEditorProvider";
    }

}
