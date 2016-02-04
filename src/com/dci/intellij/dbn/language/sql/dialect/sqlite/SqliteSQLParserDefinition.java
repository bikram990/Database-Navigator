package com.dci.intellij.dbn.language.sql.dialect.sqlite;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.sql.SQLParserDefinition;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;


public class SqliteSQLParserDefinition extends SQLParserDefinition {

    public SqliteSQLParserDefinition(SqliteSQLParser parser) {
        super(parser);
    }

    @NotNull
    public Lexer createLexer(Project project) {
        return new FlexAdapter(new SqliteSQLParserFlexLexer(getTokenTypes()));
    }

}