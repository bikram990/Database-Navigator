package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentConfigLocalListener;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

public class ConnectionDetailSettingsForm extends ConfigurationEditorForm<ConnectionDetailSettings>{
    private JPanel mainPanel;
    private DBNComboBox<CharsetOption> encodingComboBox;
    private DBNComboBox<EnvironmentType> environmentTypesComboBox;
    private JPanel generalGroupPanel;
    private JTextField maxPoolSizeTextField;
    private JTextField idleTimeTextField;
    private JTextField alternativeStatementDelimiterTextField;
    private JPanel autoConnectHintPanel;
    private JTextField passwordExpiryTextField;
    private JCheckBox databaseLoggingCheckBox;
    private JCheckBox ddlFileBindingCheckBox;
    private JCheckBox autoConnectCheckBox;
    private JCheckBox restoreWorkspaceCheckBox;
    private JCheckBox restoreWorkspaceDeepCheckBox;

    public ConnectionDetailSettingsForm(final ConnectionDetailSettings configuration) {
        super(configuration);

        updateBorderTitleForeground(generalGroupPanel);

        encodingComboBox.setValues(CharsetOption.ALL);

        List<EnvironmentType> environmentTypes = new ArrayList<EnvironmentType>(getEnvironmentTypes());
        environmentTypes.add(0, EnvironmentType.DEFAULT);
        environmentTypesComboBox.setValues(environmentTypes);
        resetFormChanges();

        registerComponent(mainPanel);

        environmentTypesComboBox.addListener(new ValueSelectorListener<EnvironmentType>() {
            @Override
            public void selectionChanged(EnvironmentType oldValue, EnvironmentType newValue) {
                notifyPresentationChanges();
            }
        });

        String autoConnectHintText = "NOTE: If \"Connect automatically\" is not selected, the system will not restore the workspace the next time you open the project (i.e. all open editors for this connection will not be reopened automatically).";
        DBNHintForm hintForm = new DBNHintForm(autoConnectHintText, MessageType.INFO, false);
        autoConnectHintPanel.add(hintForm.getComponent());

        boolean visibleHint = !autoConnectCheckBox.isSelected() && restoreWorkspaceCheckBox.isSelected();
        autoConnectHintPanel.setVisible(visibleHint);


        Project project = configuration.getProject();
        EventUtil.subscribe(project, this, EnvironmentConfigLocalListener.TOPIC, presentationChangeListener);
    }

    public void notifyPresentationChanges() {
        Project project = getConfiguration().getProject();
        ConnectionPresentationChangeListener listener = EventUtil.notify(project, ConnectionPresentationChangeListener.TOPIC);
        EnvironmentType environmentType = environmentTypesComboBox.getSelectedValue();
        Color color = environmentType == null ? null : environmentType.getColor();
        listener.presentationChanged(null, null, color, getConfiguration().getConnectionId(), null);
    }

    public EnvironmentType getSelectedEnvironmentType() {
        return environmentTypesComboBox.getSelectedValue();
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == autoConnectCheckBox || source == restoreWorkspaceCheckBox){
                    boolean visibleHint = !autoConnectCheckBox.isSelected() && restoreWorkspaceCheckBox.isSelected();
                    autoConnectHintPanel.setVisible(visibleHint);
                }
                if (source == restoreWorkspaceCheckBox) {
                    restoreWorkspaceDeepCheckBox.setEnabled(restoreWorkspaceCheckBox.isSelected());
                    if (!restoreWorkspaceCheckBox.isSelected()) {
                        restoreWorkspaceDeepCheckBox.setSelected(false);
                    }
                }
                getConfiguration().setModified(true);
            }
        };
    }

    private List<EnvironmentType> getEnvironmentTypes() {
        Project project = getConfiguration().getProject();
        EnvironmentSettings environmentSettings = GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
        return environmentSettings.getEnvironmentTypes().getEnvironmentTypes();
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        final ConnectionDetailSettings configuration = getConfiguration();

        boolean newDdlFileBinding = ddlFileBindingCheckBox.isSelected();
        boolean newDatabaseLogging = databaseLoggingCheckBox.isSelected();
        EnvironmentType newEnvironmentType = environmentTypesComboBox.getSelectedValue();
        final String newEnvironmentTypeId = newEnvironmentType.getId();

        final boolean settingsChanged =
                !configuration.getCharset().equals(encodingComboBox.getSelectedValue().getCharset()) ||
                configuration.isEnableDdlFileBinding() != newDdlFileBinding ||
                configuration.isEnableDatabaseLogging() != newDatabaseLogging;

        final boolean environmentChanged =
                !configuration.getEnvironmentType().getId().equals(newEnvironmentTypeId);


        applyFormChanges(configuration);


        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                Project project = configuration.getProject();
                if (environmentChanged) {
                    EnvironmentManagerListener listener = EventUtil.notify(project, EnvironmentManagerListener.TOPIC);
                    listener.configurationChanged();
                }

                if (settingsChanged) {
                    ConnectionStatusListener listener = EventUtil.notify(project, ConnectionStatusListener.TOPIC);
                    listener.statusChanged(configuration.getConnectionId());
                }
            }
        };
    }

    @Override
    public void applyFormChanges(ConnectionDetailSettings configuration) throws ConfigurationException {
        CharsetOption charsetOption = encodingComboBox.getSelectedValue();
        EnvironmentType environmentType = environmentTypesComboBox.getSelectedValue();

        configuration.setEnvironmentTypeId(environmentType == null ? "" : environmentType.getId());
        configuration.setCharset(charsetOption == null ? null : charsetOption.getCharset());
        configuration.setRestoreWorkspace(restoreWorkspaceCheckBox.isSelected());
        configuration.setRestoreWorkspaceDeep(restoreWorkspaceDeepCheckBox.isSelected());
        configuration.setConnectAutomatically(autoConnectCheckBox.isSelected());
        configuration.setEnableDdlFileBinding(ddlFileBindingCheckBox.isSelected());
        configuration.setEnableDatabaseLogging(databaseLoggingCheckBox.isSelected());
        configuration.setAlternativeStatementDelimiter(alternativeStatementDelimiterTextField.getText());
        int idleTimeToDisconnect = ConfigurationEditorUtil.validateIntegerInputValue(idleTimeTextField, "Idle time to disconnect (minutes)", true, 0, 60, "");
        int passwordExpiryTime = ConfigurationEditorUtil.validateIntegerInputValue(passwordExpiryTextField, "Idle time to request password (minutes)", true, 0, 60, "");
        int maxPoolSize = ConfigurationEditorUtil.validateIntegerInputValue(maxPoolSizeTextField, "Max connection pool size", true, 3, 20, "");
        configuration.setIdleTimeToDisconnect(idleTimeToDisconnect);
        configuration.setPasswordExpiryTime(passwordExpiryTime);
        configuration.setMaxConnectionPoolSize(maxPoolSize);    }

    @Override
    public void resetFormChanges() {
        ConnectionDetailSettings configuration = getConfiguration();
        encodingComboBox.setSelectedValue(CharsetOption.get(configuration.getCharset()));
        ddlFileBindingCheckBox.setSelected(configuration.isEnableDdlFileBinding());
        databaseLoggingCheckBox.setSelected(configuration.isEnableDatabaseLogging());
        autoConnectCheckBox.setSelected(configuration.isConnectAutomatically());
        restoreWorkspaceCheckBox.setSelected(configuration.isRestoreWorkspace());
        restoreWorkspaceDeepCheckBox.setSelected(configuration.isRestoreWorkspaceDeep());
        environmentTypesComboBox.setSelectedValue(configuration.getEnvironmentType());
        idleTimeTextField.setText(Integer.toString(configuration.getIdleTimeToDisconnect()));
        passwordExpiryTextField.setText(Integer.toString(configuration.getPasswordExpiryTime()));
        maxPoolSizeTextField.setText(Integer.toString(configuration.getMaxConnectionPoolSize()));
        alternativeStatementDelimiterTextField.setText(configuration.getAlternativeStatementDelimiter());
    }

    private EnvironmentConfigLocalListener presentationChangeListener = new EnvironmentConfigLocalListener() {
        @Override
        public void settingsChanged(EnvironmentTypeBundle environmentTypes) {
            EnvironmentType selectedItem = environmentTypesComboBox.getSelectedValue();
            String selectedId = selectedItem == null ? EnvironmentType.DEFAULT.getId() : selectedItem.getId();
            selectedItem = environmentTypes.getEnvironmentType(selectedId);

            List<EnvironmentType> newEnvironmentTypes = new ArrayList<EnvironmentType>(environmentTypes.getEnvironmentTypes());
            newEnvironmentTypes.add(0, EnvironmentType.DEFAULT);
            environmentTypesComboBox.setValues(newEnvironmentTypes);
            environmentTypesComboBox.setSelectedValue(selectedItem);
            notifyPresentationChanges();
        }
    };

    @Override
    public void dispose() {
        super.dispose();
    }
}
