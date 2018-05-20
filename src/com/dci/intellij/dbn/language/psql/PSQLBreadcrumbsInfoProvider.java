package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import com.intellij.xml.breadcrumbs.BreadcrumbsInfoProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PSQLBreadcrumbsInfoProvider extends BreadcrumbsInfoProvider {

    private static final Language[] LANGUAGES = {PSQLLanguage.INSTANCE};

    @Override
    public Language[] getLanguages() {
        return LANGUAGES;
    }

    @Override
    public boolean acceptElement(@NotNull PsiElement psiElement) {
        IdentifierPsiElement identifierPsiElement = getBreadcrumbIdentifier(psiElement);
        return identifierPsiElement != null;
    }

    @NotNull
    @Override
    public String getElementInfo(@NotNull PsiElement psiElement) {
        IdentifierPsiElement identifierPsiElement = getBreadcrumbIdentifier(psiElement);

        return identifierPsiElement != null ? identifierPsiElement.getText() : "";
    }

    @Nullable
    public Icon getElementIcon(@NotNull PsiElement psiElement) {
        IdentifierPsiElement identifierPsiElement = getBreadcrumbIdentifier(psiElement);
        if (identifierPsiElement != null) {
            return identifierPsiElement.getIcon(false);
        }
        return null;
    }

    @Nullable
    @Override
    public String getElementTooltip(@NotNull PsiElement element) {
        if (element instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) element;
            return basePsiElement.getElementType().getDescription();
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement getParent(@NotNull PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) parent;
            return basePsiElement.findEnclosingScopePsiElement();
        }
        return null;
    }

    @NotNull
    public List<PsiElement> getChildren(@NotNull PsiElement element) {
        return Collections.emptyList();
    }

    @Nullable
    private IdentifierPsiElement getBreadcrumbIdentifier(@NotNull PsiElement psiElement) {
        if (psiElement instanceof NamedPsiElement) {
            NamedPsiElement namedPsiElement = (NamedPsiElement) psiElement;
            boolean isObject =
                    namedPsiElement.is(ElementTypeAttribute.OBJECT_DEFINITION) ||
                    namedPsiElement.is(ElementTypeAttribute.OBJECT_DECLARATION) ||
                    namedPsiElement.is(ElementTypeAttribute.OBJECT_SPECIFICATION);

            if (isObject) {
                BasePsiElement subject = namedPsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                if (subject instanceof IdentifierPsiElement) {
                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) subject;
                    DBObjectType objectType = identifierPsiElement.getObjectType();
                    if (objectType.matchesOneOf(
                            DBObjectType.METHOD,
                            DBObjectType.PROGRAM,
                            DBObjectType.SYNONYM,
                            DBObjectType.TYPE,
                            DBObjectType.CURSOR,
                            DBObjectType.TRIGGER)) {
                        return identifierPsiElement;
                    }
                }
            }
        }
        return null;
    }
}
