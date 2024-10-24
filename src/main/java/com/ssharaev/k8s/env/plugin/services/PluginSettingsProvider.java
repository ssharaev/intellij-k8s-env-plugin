package com.ssharaev.k8s.env.plugin.services;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.util.Key;
import com.ssharaev.k8s.env.plugin.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class PluginSettingsProvider{

    private static final Key<PluginSettings> PLUGIN_SETTINGS_KEY = new Key<>("EnvFile Settings");

    public static PluginSettings getEnvFileSetting(@NotNull RunConfigurationBase<?> runConfigurationBase) {
        return runConfigurationBase.getCopyableUserData(PLUGIN_SETTINGS_KEY);
    }

    public static Key<PluginSettings> getKey() {
        return PLUGIN_SETTINGS_KEY;
    }
}
