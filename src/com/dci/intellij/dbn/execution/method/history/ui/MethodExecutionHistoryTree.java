package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import java.util.List;

public class MethodExecutionHistoryTree extends DBNTree implements Disposable {
    private MethodExecutionHistoryDialog dialog;
    private MethodExecutionHistory executionHistory;
    private boolean grouped;
    private boolean debug;

    public MethodExecutionHistoryTree(MethodExecutionHistoryDialog dialog, MethodExecutionHistory executionHistory, boolean grouped, boolean debug) {
        super(grouped ?
                new MethodExecutionHistoryGroupedTreeModel(executionHistory.getExecutionInputs(), debug) :
                new MethodExecutionHistorySimpleTreeModel(executionHistory.getExecutionInputs(), debug));
        this.executionHistory = executionHistory;
        this.dialog = dialog;
        this.grouped = grouped;
        this.debug = debug;
        setCellRenderer(new TreeCellRenderer());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeUtil.expand(this, 4);

        addTreeSelectionListener(treeSelectionListener);
        getModel().addTreeModelListener(treeModelListener);
    }

    public Project getProject() {
        return dialog.getProject();
    }

    public void showGrouped(boolean grouped) {
        List<MethodExecutionInput> executionInputs = executionHistory.getExecutionInputs();
        MethodExecutionHistoryTreeModel model = grouped ?
                new MethodExecutionHistoryGroupedTreeModel(executionInputs, debug) :
                new MethodExecutionHistorySimpleTreeModel(executionInputs, debug);
        model.addTreeModelListener(treeModelListener);
        setModel(model);
        TreeUtil.expand(this, 4);
        this.grouped = grouped;
    }

    public void setSelectedInput(MethodExecutionInput executionInput) {
        if (executionInput != null) {
            MethodExecutionHistoryTreeModel model = (MethodExecutionHistoryTreeModel) getModel();
            getSelectionModel().setSelectionPath(model.getTreePath(executionInput));
        }
    }

    public boolean isGrouped() {
        return grouped;
    }

    @Nullable
    public MethodExecutionInput getSelectedExecutionInput() {
        Object selection = getLastSelectedPathComponent();
        if (selection instanceof MethodExecutionHistoryTreeModel.MethodTreeNode) {
            MethodExecutionHistoryTreeModel.MethodTreeNode methodNode = (MethodExecutionHistoryTreeModel.MethodTreeNode) selection;
            return methodNode.getExecutionInput();
        }
        return null;
    }

    public void dispose() {
        super.dispose();
        executionHistory = null;
        dialog = null;
    }

    private class TreeCellRenderer extends ColoredTreeCellRenderer {
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            try {
                MethodExecutionHistoryTreeNode node = (MethodExecutionHistoryTreeNode) value;
                setIcon(node.getIcon());
                append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                if (node instanceof MethodExecutionHistoryTreeModel.MethodTreeNode) {
                    MethodExecutionHistoryTreeModel.MethodTreeNode methodTreeNode = (MethodExecutionHistoryTreeModel.MethodTreeNode) node;
                    int overload = methodTreeNode.getOverload();
                    if (overload > 0) {
                        append(" #" + overload, SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                }
            } catch (ProcessCanceledException ignore) {}
        }
    }

    public void removeSelectedEntries() {
        MethodExecutionHistoryTreeNode treeNode = (MethodExecutionHistoryTreeNode)
                getSelectionPath().getLastPathComponent();
        MethodExecutionHistoryTreeNode parentTreeNode = (MethodExecutionHistoryTreeNode) treeNode.getParent();
        while (parentTreeNode != null &&
                parentTreeNode.getChildCount() == 1 && 
                !parentTreeNode.isRoot()) {
            getSelectionModel().setSelectionPath(TreeUtil.getPathFromRoot(parentTreeNode));
            parentTreeNode = (MethodExecutionHistoryTreeNode) parentTreeNode.getParent();
        }
        TreeUtil.removeSelected(this);
    }

    /**********************************************************
     *                         Listeners                      *
     **********************************************************/
    private TreeSelectionListener treeSelectionListener = new TreeSelectionListener(){
        public void valueChanged(TreeSelectionEvent e) {
            final MethodExecutionInput executionInput = getSelectedExecutionInput();
            if (executionInput != null) {
                TaskInstructions taskInstructions = new TaskInstructions("Loading Method details");
                new ConnectionAction("loading the execution history", executionInput, taskInstructions) {
                    @Override
                    protected void execute() {
                        final DBMethod method = executionInput.getMethod();
                        if (method != null) {
                            method.getArguments();
                        }

                        new SimpleLaterInvocator() {
                            @Override
                            protected void execute() {
                                if (dialog != null && !dialog.isDisposed()) {
                                    dialog.showMethodExecutionPanel(executionInput);
                                    dialog.setSelectedExecutionInput(executionInput);
                                    dialog.updateMainButtons(executionInput);
                                    if (method != null) {
                                        executionHistory.setSelection(executionInput.getMethodRef());
                                    }
                                }
                            }
                        }.start();
                    }
                }.start();
            }
        }
    };

    private TreeModelListener treeModelListener = new TreeModelHandler() {
        public void treeNodesRemoved(TreeModelEvent e) {
            dialog.setSaveButtonEnabled(true);
        }
    };
}
