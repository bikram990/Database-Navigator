package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatus;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.dci.intellij.dbn.connection.ConnectionId;

public class TabbedBrowserTreeModel extends BrowserTreeModel {
    public TabbedBrowserTreeModel(ConnectionHandler connectionHandler) {
        super(connectionHandler.getObjectBundle());
        EventUtil.subscribe(connectionHandler.getProject(), this, ConnectionHandlerStatusListener.TOPIC, connectionHandlerStatusListener);
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return getConnectionHandler() == node.getConnectionHandler();
    }

    public ConnectionHandler getConnectionHandler() {
        return getRoot().getConnectionHandler();
    }

    private final ConnectionHandlerStatusListener connectionHandlerStatusListener = new ConnectionHandlerStatusListener() {
        @Override
        public void statusChanged(ConnectionId connectionId, ConnectionHandlerStatus status) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (connectionHandler.getId() == connectionId) {
                notifyListeners(connectionHandler.getObjectBundle(), TreeEventType.NODES_CHANGED);
            }
        }
    };
}
