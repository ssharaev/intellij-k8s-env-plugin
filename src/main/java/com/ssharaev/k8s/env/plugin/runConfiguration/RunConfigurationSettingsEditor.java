package com.ssharaev.k8s.env.plugin.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.SettingsEditor;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.PluginSettingsProvider;
import com.ssharaev.k8s.env.plugin.ui.RunConfigurationPanelProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RunConfigurationSettingsEditor<T extends RunConfigurationBase<?>> extends SettingsEditor<T> {

    private final RunConfigurationPanelProvider panelProvider;

    public RunConfigurationSettingsEditor() {
        this.panelProvider = new RunConfigurationPanelProvider();
    }

    @Override
    protected void resetEditorFrom(@NotNull T configuration) {
        PluginSettings state = PluginSettingsProvider.getPluginSetting(configuration);
        panelProvider.setState(state);
    }

    @Override
    protected void applyEditorTo(@NotNull T configuration) {
        PluginSettings state = panelProvider.getState();
        if (state != null) {
            PluginSettingsProvider.putData(configuration, state);
        }
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panelProvider.getPanel();
    }
}
