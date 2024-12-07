package com.ssharaev.k8s.env.plugin.services.providers;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
public final class ConfigMapEnvProvider implements EnvProvider {

    @Override
    public boolean isApplicable(PluginSettings pluginSettings) {
        return EnvMode.CONFIGMAP_AND_SECRET == pluginSettings.getEnvMode();
    }

    @Override
    public Map<String, String> getEnv(PluginSettings pluginSettings) {
        return ApplicationManager.getApplication().getService(KubernetesService.class).getEnvFromConfigmaps(pluginSettings.getNamespace(), pluginSettings.getConfigmapNames());
    }
}
