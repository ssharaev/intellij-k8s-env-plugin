package com.ssharaev.k8s.env.plugin.services.providers;

import com.intellij.openapi.components.Service;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
public final class PodEnvProvider implements EnvProvider {

    private final KubernetesService kubernetesService;

    @Override
    public boolean isApplicable(PluginSettings pluginSettings) {
        return EnvMode.POD_ENV == pluginSettings.getEnvMode();
    }

    @Override
    public Map<String, String> getEnv(PluginSettings pluginSettings) {
        if (pluginSettings.getPodName() == null) {
            return Map.of();
        }
        return kubernetesService.getEnvFromPod(pluginSettings.getNamespace(), pluginSettings.getPodName());
    }
}
