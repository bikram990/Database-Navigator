package com.dci.intellij.dbn.editor.data.statusbar;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class DatasetEditorStatusBarWidget extends AbstractProjectComponent implements CustomStatusBarWidget, FileEditorManagerListener {
    private static final String WIDGET_ID = DatasetEditorStatusBarWidget.class.getName();
    private static final Key<Boolean> SELECTION_LISTENER_SET_KEY = Key.create("SELECTION_LISTENER_SET_KEY");

    private JLabel textLabel;
    private Alarm updateAlarm = new Alarm(this);
    private JPanel component = new JPanel(new BorderLayout());

    DatasetEditorStatusBarWidget(@NotNull Project project) {
        super(project);
        textLabel = new JLabel();
        component.add(textLabel, BorderLayout.WEST);
    }

    public static DatasetEditorStatusBarWidget getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatasetEditorStatusBarWidget.class);
    }


    @NotNull
    @Override
    public String ID() {
        return WIDGET_ID;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType type) {
        return null;
    }

    @Nullable
    private DatasetEditor getSelectedEditor() {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
        FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
        if (selectedEditor instanceof DatasetEditor) {
            return (DatasetEditor) selectedEditor;
        }
        return null;
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        update();
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        update();
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        update();
    }

    @Nullable
    private DatasetEditorTable getEditorTable() {
        DatasetEditor selectedEditor = getSelectedEditor();
        return selectedEditor == null ? null : selectedEditor.getEditorTable();
    }

    public void update() {
        updateAlarm.cancelAllRequests();
        updateAlarm.addRequest(() -> {
            DatasetEditorTable editorTable = getEditorTable();
            if (editorTable == null || editorTable.getSelectionSum() == null) {
                textLabel.setText("");
                textLabel.setIcon(null);
            } else {
                BigDecimal selectionSum = editorTable.getSelectionSum();
                BigDecimal selectionAverage = editorTable.getSelectionAverage();
                textLabel.setText("Sum " + selectionSum + "   Average " + selectionAverage);
                textLabel.setIcon(Icons.DBO_TMP_TABLE);
            }
            GUIUtil.repaint(getComponent());
        }, 100);
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        EventUtil.subscribe(getProject(), this, FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
    }

    @Override
    public JComponent getComponent() {
        return component;
    }
}