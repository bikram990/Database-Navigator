package com.dci.intellij.dbn.editor.session.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.dci.intellij.dbn.data.find.SearchableDataComponent;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableScrollPane;
import com.dci.intellij.dbn.editor.data.ui.table.cell.DatasetTableCellEditor;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBSplitter;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.UIUtil;

public class SessionBrowserForm extends DBNFormImpl implements SearchableDataComponent {
    private JPanel actionsPanel;
    private JScrollPane editorTableScrollPane;
    private JPanel mainPanel;
    private JLabel loadingLabel;
    private JPanel loadingIconPanel;
    private JPanel searchPanel;
    private JLabel loadTimestampLabel;
    private JPanel detailsPanel;
    private JPanel editorPanel;
    private SessionBrowserTable editorTable;
    private DataSearchComponent dataSearchComponent;

    private SessionBrowser sessionBrowser;
    private SessionBrowserDetailsForm detailsForm;

    public SessionBrowserForm(SessionBrowser sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
        try {
            editorTable = new SessionBrowserTable(sessionBrowser);
            editorTableScrollPane.setViewportView(editorTable);
            editorTableScrollPane.getViewport().setBackground(editorTable.getBackground());
            editorTable.initTableGutter();
            detailsForm = new SessionBrowserDetailsForm(sessionBrowser);
            detailsPanel.add(detailsForm.getComponent(), BorderLayout.CENTER);

            loadTimestampLabel.setForeground(Colors.HINT_COLOR);
            GuiUtils.replaceJSplitPaneWithIDEASplitter(editorPanel);
            JBSplitter splitter = (JBSplitter) editorPanel.getComponent(0);
            splitter.setProportion((float) 0.6);

            refreshLoadTimestamp();

            JPanel panel = new JPanel();
            panel.setBorder(UIUtil.getTableHeaderCellBorder());
            editorTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);

            ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.SessionBrowser");
            actionToolbar.setTargetComponent(actionsPanel);

            actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
            loadingIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);
            hideLoadingHint();

            ActionUtil.registerDataProvider(actionsPanel, sessionBrowser);

            Disposer.register(this, editorTable);
            Disposer.register(this, detailsForm);
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(
                    sessionBrowser.getProject(),
                    "Error",
                    "Error opening session browser for connection " + getConnectionHandler().getName(), e);
        }
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public SessionBrowserDetailsForm getDetailsForm() {
        return detailsForm;
    }

    public void showLoadingHint() {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                loadingLabel.setVisible(true);
                loadingIconPanel.setVisible(true);
                loadTimestampLabel.setVisible(false);
                refreshLoadTimestamp();
            }
        }.start();
    }

    public void hideLoadingHint() {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                loadingLabel.setVisible(false);
                loadingIconPanel.setVisible(false);
                refreshLoadTimestamp();

            }
        }.start();
    }

    public void refreshLoadTimestamp() {
        boolean visible = !loadingLabel.isVisible();
        if (visible) {
            SessionBrowserModel model = getEditorTable().getModel();
            long timestamp = model.getTimestamp();
/*
            RegionalSettings regionalSettings = RegionalSettings.getInstance(sessionBrowser.getProject());
            String dateTime = regionalSettings.getFormatter().formatTime(new Date(timestamp));
            loadTimestampLabel.setText("Updated: " + dateTime + " (" + DateFormatUtil.formatPrettyDateTime(timestamp)+ ")");
*/

            loadTimestampLabel.setText("Updated: " + DateFormatUtil.formatPrettyDateTime(timestamp));
        }
        loadTimestampLabel.setVisible(visible);
    }


    @NotNull
    public SessionBrowserTable getEditorTable() {
        return FailsafeUtil.get(editorTable);
    }

    public void dispose() {
        super.dispose();
        sessionBrowser = null;
        detailsForm = null;
        editorTable = null;
        dataSearchComponent = null;    }

    @NotNull
    public SessionBrowser getSessionBrowser() {
        return FailsafeUtil.get(sessionBrowser);
    }

    @NotNull
    private ConnectionHandler getConnectionHandler() {
        return getSessionBrowser().getConnectionHandler();
    }

    public float getHorizontalScrollProportion() {
        editorTableScrollPane.getHorizontalScrollBar().getModel();
        return 0;
    }

    /*********************************************************
     *              SearchableDataComponent                  *
     *********************************************************/
    public void showSearchHeader() {
        getEditorTable().clearSelection();

        if (dataSearchComponent == null) {
            dataSearchComponent = new DataSearchComponent(this);
            searchPanel.add(dataSearchComponent, BorderLayout.CENTER);

            Disposer.register(this, dataSearchComponent);
        } else {
            dataSearchComponent.initializeFindModel();
        }
        if (searchPanel.isVisible()) {
            dataSearchComponent.getSearchField().selectAll();
        } else {
            searchPanel.setVisible(true);    
        }
        dataSearchComponent.getSearchField().requestFocus();

    }

    public void hideSearchHeader() {
        dataSearchComponent.resetFindModel();
        searchPanel.setVisible(false);
        SessionBrowserTable editorTable = getEditorTable();
        editorTable.revalidate();
        editorTable.repaint();
        editorTable.requestFocus();
    }

    @Override
    public void cancelEditActions() {}

    @Override
    public String getSelectedText() {
        TableCellEditor cellEditor = getEditorTable().getCellEditor();
        if (cellEditor instanceof DatasetTableCellEditor) {
            DatasetTableCellEditor tableCellEditor = (DatasetTableCellEditor) cellEditor;
            return tableCellEditor.getTextField().getSelectedText();
        }
        return null;
    }

    @Override
    public BasicTable getTable() {
        return getEditorTable();
    }

    private void createUIComponents() {
        editorTableScrollPane = new BasicTableScrollPane();
    }
}
