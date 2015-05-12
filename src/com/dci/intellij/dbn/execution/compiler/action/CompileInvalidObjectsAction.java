package com.dci.intellij.dbn.execution.compiler.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class CompileInvalidObjectsAction extends AnAction {
    private DBObjectRef<DBSchema> schemaRef;
    public CompileInvalidObjectsAction(DBSchema schema) {
        super("Compile invalid objects");
        this.schemaRef = DBObjectRef.from(schema);
    }

    @NotNull
    public DBSchema getSchema() {
        return DBObjectRef.getnn(schemaRef);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSchema schema = getSchema();
        Project project = schema.getProject();
        DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
        compilerManager.compileInvalidObjects(schema, getCompilerSettings(project).getCompileTypeOption());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DBSchema schema = getSchema();
        CompileTypeOption compileType = getCompilerSettings(schema.getProject()).getCompileTypeOption();
        String text = "Compile Invalid Objects";
        if (compileType == CompileTypeOption.DEBUG) text = text + " (Debug)";
        if (compileType == CompileTypeOption.ASK) text = text + "...";

        e.getPresentation().setText(text);
    }

    private static CompilerSettings getCompilerSettings(Project project) {
        return OperationSettings.getInstance(project).getCompilerSettings();
    }
}