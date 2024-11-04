package com.ssharaev.k8s.env.plugin.ui;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.SettingsEditor;
import com.ssharaev.k8s.env.plugin.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.PluginSettingsProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RunConfigurationSettingsEditor<T extends RunConfigurationBase<?>> extends SettingsEditor<T> {

    private final RunConfigurationPanel panel;

    public RunConfigurationSettingsEditor() {
        this.panel = new RunConfigurationPanel();
    }

    @Override
    protected void resetEditorFrom(@NotNull T configuration) {
        PluginSettings state = PluginSettingsProvider.getPluginSetting(configuration);
        if (state != null) {
            panel.setState(state);
        }
    }

    @Override
    protected void applyEditorTo(@NotNull T configuration) {
        PluginSettings state = panel.getState();
        if (state != null) {
            PluginSettingsProvider.putData(configuration, state);
        }
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }
}
