package com.ssharaev.k8s.env.plugin.services.providers;

import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static com.ssharaev.k8s.env.plugin.Utils.getKubernetesService;

@RequiredArgsConstructor
public final class PodEnvProvider implements EnvProvider {

    @Override
    public boolean isApplicable(PluginSettings pluginSettings) {
        return EnvMode.POD_ENV == pluginSettings.getEnvMode();
    }

    @Override
    public Map<String, String> getEnv(PluginSettings pluginSettings) {
        if (pluginSettings.getPodName() == null) {
            return Map.of();
        }
        return getKubernetesService().getEnvFromPod(pluginSettings.getNamespace(), pluginSettings.getPodName());
    }
}
