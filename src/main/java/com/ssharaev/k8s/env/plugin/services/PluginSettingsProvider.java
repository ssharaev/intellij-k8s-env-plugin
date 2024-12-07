package com.ssharaev.k8s.env.plugin.services;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.util.Key;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PluginSettingsProvider{

    private static final Key<PluginSettings> PLUGIN_SETTINGS_KEY = new Key<>("K8s env Settings");

    public static PluginSettings getPluginSetting(@NotNull RunConfigurationBase<?> runConfigurationBase) {
        return Optional.ofNullable(runConfigurationBase.getCopyableUserData(PLUGIN_SETTINGS_KEY)).orElse(new PluginSettings());
    }

    public static void putData(@NotNull RunConfigurationBase<?> runConfigurationBase, PluginSettings settings) {
        runConfigurationBase.putCopyableUserData(PLUGIN_SETTINGS_KEY, settings);
    }
}
