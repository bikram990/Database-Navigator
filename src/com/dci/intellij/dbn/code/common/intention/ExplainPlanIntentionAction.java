package com.dci.intellij.dbn.code.common.intention;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.execution.explain.ExplainPlanManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class ExplainPlanIntentionAction extends GenericIntentionAction {
    @NotNull
    public String getText() {
        return "Explain plan for statement";
    }

    @NotNull
    public String getFamilyName() {
        return "Statement execution intentions";
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.STMT_EXECUTION_EXPLAIN;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile.getVirtualFile().getFileType() instanceof DBLanguageFileType) {
            ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
            FileEditor fileEditor = EditorUtil.getFileEditor(editor);
            if (executable != null && fileEditor != null) {
                return executable.is(ElementTypeAttribute.DATA_MANIPULATION);
            }
        }
        return false;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
        FileEditor fileEditor = EditorUtil.getFileEditor(editor);
        if (executable != null && fileEditor != null) {
            ExplainPlanManager explainPlanManager = ExplainPlanManager.getInstance(project);
            explainPlanManager.explainPlan(fileEditor, executable);
        }
    }

    public boolean startInWriteAction() {
        return false;
    }
}
