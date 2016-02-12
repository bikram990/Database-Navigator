package com.dci.intellij.dbn.database.sqlite;

import java.sql.Connection;
import java.sql.SQLException;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.code.sql.style.options.SQLCodeStyleSettings;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.database.common.DatabaseDDLInterfaceImpl;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

public class SqliteDDLInterface extends DatabaseDDLInterfaceImpl {
    public SqliteDDLInterface(DatabaseInterfaceProvider provider) {
        super("sqlite_ddl_interface.xml", provider);
    }


    public String createDDLStatement(Project project, DatabaseObjectTypeId objectTypeId, String userName, String schemaName, String objectName, DBContentType contentType, String code, String alternativeDelimiter) {
        if (StringUtil.isEmpty(alternativeDelimiter)) {
            alternativeDelimiter = getProvider().getCompatibilityInterface().getDefaultAlternativeStatementDelimiter();
        }

        DDLFileSettings ddlFileSettings = DDLFileSettings.getInstance(project);
        boolean useQualified = ddlFileSettings.getGeneralSettings().isUseQualifiedObjectNames();
        boolean makeRerunnable = ddlFileSettings.getGeneralSettings().isMakeScriptsRerunnable();

        CodeStyleCaseSettings caseSettings = SQLCodeStyleSettings.getInstance(project).getCaseSettings();
        CodeStyleCaseOption kco = caseSettings.getKeywordCaseOption();
        CodeStyleCaseOption oco = caseSettings.getObjectCaseOption();


        if (objectTypeId.isOneOf(DatabaseObjectTypeId.VIEW, DatabaseObjectTypeId.DATASET_TRIGGER)) {
            if (objectTypeId == DatabaseObjectTypeId.DATASET_TRIGGER) {
                objectTypeId = DatabaseObjectTypeId.TRIGGER;
            }
            String objectType = objectTypeId.toString().toLowerCase();
            code = updateNameQualification(code, useQualified, objectType, schemaName, objectName, caseSettings);
            String dropStatement =
                    kco.format("drop " + objectType + " if exists ") +
                    oco.format((useQualified ? schemaName + "." : "") + objectName) + alternativeDelimiter + "\n";
            String createStatement = kco.format("create \n") + code + alternativeDelimiter + "\n";
            return (makeRerunnable ? dropStatement : "") + createStatement;
        }
        return code;
    }

    @Override
    public void computeSourceCodeOffsets(SourceCodeContent content, DatabaseObjectTypeId objectTypeId, String objectName) {
        super.computeSourceCodeOffsets(content, objectTypeId, objectName);
    }

    /*********************************************************
     *                   CHANGE statements                   *
     *********************************************************/
    public void updateView(String viewName, String code, Connection connection) throws SQLException {
        // try create
        String objectType = "VIEW";
        String tempViewName = getTempObjectName(objectType);
        dropObjectIfExists(objectType, tempViewName, connection);
        createView(tempViewName, code, connection);
        dropObjectIfExists(objectType, tempViewName, connection);

        // create
        dropObjectIfExists(objectType, viewName, connection);
        createView(viewName, code, connection);
    }

    @Override
    public void updateTrigger(String tableOwner, String tableName, String triggerName, String oldCode, String newCode, Connection connection) throws SQLException {
        String objectType = "TRIGGER";
        String tempTriggerName = getTempObjectName(objectType);
        dropObjectIfExists(objectType, tempTriggerName, connection);
        createObject(newCode.replaceFirst("(?i)" + triggerName, tempTriggerName), connection);
        dropObjectIfExists(objectType, tempTriggerName, connection);

        dropObjectIfExists(objectType, triggerName, connection);
        createObject(newCode, connection);
    }

    public void updateObject(String objectName, String objectType, String oldCode, String newCode, Connection connection) throws SQLException {
        dropObjectIfExists(objectType, objectName, connection);
        try {
            createObject(newCode, connection);
        } catch (SQLException e) {
            createObject(oldCode, connection);
            throw e;
        }
    }

    /*********************************************************
     *                     DROP statements                   *
     *********************************************************/
    private void dropObjectIfExists(String objectType, String objectName, Connection connection) throws SQLException {
        executeUpdate(connection, "drop-object-if-exists", objectType, objectName);
    }

    /*********************************************************
     *                   CREATE statements                   *
     *********************************************************/
    public void createMethod(MethodFactoryInput method, Connection connection) throws SQLException {
        throw new SQLException("Operation not supported: [create method]");
    }

}
