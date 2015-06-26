package com.dci.intellij.dbn.common.editor;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.message.MessageType;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.PlatformColors;

public class EditorNotificationPanel extends JPanel{
    protected final JLabel label = new JLabel();
    protected final JPanel linksPanel;

    public EditorNotificationPanel(MessageType messageType) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(1, 15, 1, 15));

        setPreferredSize(new Dimension(-1, 24));

        add(label, BorderLayout.CENTER);
        Icon icon;
        Color background;

        switch (messageType) {
            case INFO: {
                icon = AllIcons.General.Information;
                background = HintUtil.INFORMATION_COLOR;
                break;
            }
            case ERROR:{
                icon = AllIcons.General.Error;
                background = HintUtil.ERROR_COLOR;
                break;
            }
            default:{
                icon = AllIcons.General.Information;
                background = HintUtil.INFORMATION_COLOR;
                break;
            }
        }

        label.setIcon(icon);
        setBackground(background);

        linksPanel = new JPanel(new FlowLayout());
        linksPanel.setBackground(background);
        add(linksPanel, BorderLayout.EAST);
    }

    public void setText(@NotNull String text) {
        label.setText(text);
    }

    public void setIcon(@NotNull Icon icon) {
        label.setIcon(icon);
    }

    public HyperlinkLabel createActionLabel(final String text, @NonNls final String actionId) {
        return createActionLabel(text, new Runnable() {
            @Override
            public void run() {
                executeAction(actionId);
            }
        });
    }

    public HyperlinkLabel createActionLabel(final String text, final Runnable action) {
        HyperlinkLabel label = new HyperlinkLabel(text, PlatformColors.BLUE, getBackground(), PlatformColors.BLUE);
        label.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(HyperlinkEvent e) {
                action.run();
            }
        });
        linksPanel.add(label);
        return label;
    }

    protected void executeAction(final String actionId) {
        final AnAction action = ActionManager.getInstance().getAction(actionId);
        final AnActionEvent event = new AnActionEvent(null, DataManager.getInstance().getDataContext(this), ActionPlaces.UNKNOWN,
                action.getTemplatePresentation(), ActionManager.getInstance(),
                0);
        action.beforeActionPerformedUpdate(event);
        action.update(event);

        if (event.getPresentation().isEnabled() && event.getPresentation().isVisible()) {
            action.actionPerformed(event);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }
}