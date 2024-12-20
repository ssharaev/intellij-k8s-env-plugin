package com.ssharaev.k8s.env.plugin.services.providers;

import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static com.ssharaev.k8s.env.plugin.Utils.getKubernetesService;

@RequiredArgsConstructor
public final class ConfigMapEnvProvider implements EnvProvider {

    @Override
    public boolean isApplicable(PluginSettings pluginSettings) {
        return EnvMode.CONFIGMAP_AND_SECRET == pluginSettings.getEnvMode();
    }

    @Override
    public Map<String, String> getEnv(PluginSettings pluginSettings) {
        return getKubernetesService().getEnvFromConfigmaps(pluginSettings.getNamespace(), pluginSettings.getConfigmapNames());
    }
}
