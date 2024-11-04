package com.ssharaev.k8s.env.plugin.services.providers;

import com.intellij.openapi.components.Service;
import com.ssharaev.k8s.env.plugin.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;

import java.util.Map;

@Service
public final class ConfigMapEnvProvider implements EnvProvider {

    private final KubernetesService kubernetesService = new KubernetesService();

    @Override
    public Map<String, String> getEnv(PluginSettings pluginSettings) {

        return kubernetesService.getEnvFromConfigmaps(pluginSettings.getNamespace(), pluginSettings.getConfigmapNames());
    }
}
