package com.dci.intellij.dbn.debugger.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.options.DebuggerSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DebuggerSettingsForm extends ConfigurationEditorForm<DebuggerSettings> {
    private JPanel mainPanel;
    private DBNComboBox<DBDebuggerType> debuggerTypeComboBox;
    private JCheckBox useGenericRunnersCheckBox;
    private JPanel genericRunnersHintPanel;

    public DebuggerSettingsForm(DebuggerSettings settings) {
        super(settings);

        debuggerTypeComboBox.setValues(
                DBDebuggerType.JDBC,
                DBDebuggerType.JDWP);

        String genericRunnersHintText = "NOTE: Using generic runners prevents creating a run configuration for each method that is being debugged. ";
        DBNHintForm hintForm = new DBNHintForm(genericRunnersHintText, MessageType.INFO, false);
        genericRunnersHintPanel.add(hintForm.getComponent());


        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DebuggerSettings settings = getConfiguration();
        settings.setDebuggerType(debuggerTypeComboBox.getSelectedValue());
        settings.setUseGenericRunners(useGenericRunnersCheckBox.isSelected());
    }

    public void resetFormChanges() {
        DebuggerSettings settings = getConfiguration();
        debuggerTypeComboBox.setSelectedValue(settings.getDebuggerType());
        useGenericRunnersCheckBox.setSelected(settings.isUseGenericRunners());
    }
}