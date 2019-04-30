package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBConstraintColumnMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConstraintColumnMetadataImpl extends DBObjectMetadataBase implements DBConstraintColumnMetadata {

    public DBConstraintColumnMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getConstraintName() throws SQLException {
        return resultSet.getString("CONSTRAINT_NAME");
    }

    @Override
    public String getColumnName() throws SQLException {
        return resultSet.getString("COLUMN_NAME");
    }

    public String getDatasetName() throws SQLException {
        return resultSet.getString("DATASET_NAME");
    }

    public int getPosition() throws SQLException {
        return resultSet.getInt("POSITION");
    }
}