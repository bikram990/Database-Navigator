package com.dci.intellij.dbn.connection.resource.ui;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.connection.transaction.PendingTransaction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

public class ResourceMonitorTable extends DBNTable {
    public ResourceMonitorTable(ResourceMonitorTableModel model) {
        super(model.getProject(), model, false);
        setDefaultRenderer(PendingTransaction.class, new CellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(true);
        adjustRowHeight(2);
        accommodateColumnsSize();
        addMouseListener(new MouseListener());
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (e.getID() != MouseEvent.MOUSE_DRAGGED && getChangeAtMouseLocation() != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            super.processMouseMotionEvent(e);
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public PendingTransaction getChangeAtMouseLocation() {
        Point location = MouseInfo.getPointerInfo().getLocation();
        location.setLocation(location.getX() - getLocationOnScreen().getX(), location.getY() - getLocationOnScreen().getY());

        int columnIndex = columnAtPoint(location);
        int rowIndex = rowAtPoint(location);
        if (columnIndex > -1 && rowIndex > -1) {
            return (PendingTransaction) getModel().getValueAt(rowIndex, columnIndex);
        }

        return null;
    }

    public class CellRenderer extends ColoredTableCellRenderer {
        @Override
        protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
            PendingTransaction change = (PendingTransaction) value;
            if (column == 0) {
                // TODO
            }
            else if (column == 1) {
                setIcon(change.getIcon());
                append(change.getDisplayFilePath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            } else if (column == 2) {
                append(change.getChangesCount() + " uncommitted changes", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
            setBorder(Borders.TEXT_FIELD_BORDER);

        }
    }

    public class MouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                int selectedRow = getSelectedRow();
                PendingTransaction change = (PendingTransaction) getModel().getValueAt(selectedRow, 0);
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
                VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(change.getFilePath());
                if (virtualFile != null) {
                    fileEditorManager.openFile(virtualFile, true);
                }
            }

        }
    }
}