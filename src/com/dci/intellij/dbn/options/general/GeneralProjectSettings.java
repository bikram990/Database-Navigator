package com.dci.intellij.dbn.options.general;

import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.dci.intellij.dbn.options.general.ui.GeneralProjectSettingsForm;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class GeneralProjectSettings extends CompositeProjectConfiguration<GeneralProjectSettingsForm> implements TopLevelConfig {
    private RegionalSettings regionalSettings;
    private EnvironmentSettings environmentSettings;

    public GeneralProjectSettings(Project project) {
        super(project);
        regionalSettings = new RegionalSettings(project);
        environmentSettings = new EnvironmentSettings(project);
    }

    public static GeneralProjectSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getGeneralSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.GeneralSettings";
    }

    public String getDisplayName() {
        return "General";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.GENERAL;
    }

    @NotNull
    @Override
    public Configuration<GeneralProjectSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
    *                        Custom                         *
    *********************************************************/
    public RegionalSettings getRegionalSettings() {
        return regionalSettings;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        return environmentSettings;
    }

    /*********************************************************
     *                      Configuration                    *
     *********************************************************/
    @NotNull
    public GeneralProjectSettingsForm createConfigurationEditor() {
        return new GeneralProjectSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {regionalSettings, environmentSettings};
    }

}
