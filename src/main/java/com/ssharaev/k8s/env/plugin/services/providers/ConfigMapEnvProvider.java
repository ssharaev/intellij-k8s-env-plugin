package com.ssharaev.k8s.env.plugin.services.providers;

import com.intellij.openapi.components.Service;
import com.ssharaev.k8s.env.plugin.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;

import java.util.List;
import java.util.Map;

@Service
public final class ConfigMapEnvProvider implements EnvProvider {

    private static final PluginSettings PLUGIN_SETTINGS =
            new PluginSettings("dev", List.of("dev-configmap", "dev-configmap-two"));

    private final KubernetesService kubernetesService = new KubernetesService();

    @Override
    public Map<String, String> getEnv(PluginSettings pluginSettings) {
        if (pluginSettings == null) {
            pluginSettings = PLUGIN_SETTINGS;
        }
        return kubernetesService.getEnvFromConfigmaps(pluginSettings.getNamespace(), pluginSettings.getConfigmapNames());
    }
}
