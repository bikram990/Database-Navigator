package com.dci.intellij.dbn.connection.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.InitializationInfo;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.intellij.openapi.diagnostic.Logger;

public class DBNConnection extends DBNConnectionBase {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private ConnectionType type;

    private Set<DBNStatement> statements = new HashSet<>();
    private ConnectionStatusMonitor statusMonitor = new ConnectionStatusMonitor();

    public DBNConnection(Connection connection, ConnectionType type) {
        super(connection);
        this.type = type;
    }

    protected <S extends Statement> S wrap(S statement) {
        if (statement instanceof CallableStatement) {
            CallableStatement callableStatement = (CallableStatement) statement;
            statement = (S) new DBNCallableStatement(callableStatement, this);

        } else  if (statement instanceof PreparedStatement) {
            PreparedStatement preparedStatement = (PreparedStatement) statement;
            statement = (S) new DBNPreparedStatement(preparedStatement, this);

        } else {
            statement = (S) new DBNStatement<>(statement, this);
        }

        if (isPoolConnection()) {
            for (DBNStatement currentStatement : statements) {
                InitializationInfo initInfo = currentStatement.initInfo;
                if (TimeUtil.isOlderThan(initInfo.getTimestamp(), TimeUtil.ONE_MINUTE)) {
                    LOGGER.error("Statement not released", initInfo.getStack());
                }
            }
        }

        statements.add((DBNStatement) statement);
        return statement;
    }

    protected void release(DBNStatement statement) {
        statements.remove(statement);
    }

    @Override
    public boolean isClosedInner() throws SQLException {
        return inner.isClosed();
    }

    @Override
    public void closeInner() throws SQLException {
        inner.close();
    }

    @Override
    public boolean isInvalidInner() throws SQLException {
        return !isActive() && !inner.isValid(2);
    }

    @Override
    public void invalidateInner() throws SQLException {
        // do nothing
    }

    public ConnectionType getType() {
        return type;
    }

    public boolean isPoolConnection() {
        return type == ConnectionType.POOL;
    }

    public boolean isMainConnection() {
        return type == ConnectionType.MAIN;
    }

    public boolean isTestConnection() {
        return type == ConnectionType.TEST;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        super.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return super.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        inner.commit();
        statusMonitor.resetDataChanges();
    }

    @Override
    public void rollback() throws SQLException {
        inner.rollback();
        statusMonitor.resetDataChanges();
    }

    @Override
    public void close() {
        super.close();
        statusMonitor.resetDataChanges();
    }

    public ConnectionStatusMonitor getStatusMonitor() {
        return statusMonitor;
    }

    public boolean isReserved() {
        return statusMonitor.isReserved();
    }

    public void setReserved(boolean reserved) {
        statusMonitor.setReserved(reserved);
    }

    public boolean isActive() {
        return statusMonitor.isActive();
    }

    @Override
    public String toString() {
        return type + " / " + (isActive() ? "active " : "idle") + " / " + (isReserved() ? "reserved" : "free");
    }
}
