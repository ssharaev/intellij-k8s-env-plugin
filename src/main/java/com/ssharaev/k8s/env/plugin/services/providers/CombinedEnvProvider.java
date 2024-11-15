package com.ssharaev.k8s.env.plugin.services.providers;

import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CombinedEnvProvider implements EnvProvider {

    private final List<EnvProvider> providers;

    public CombinedEnvProvider() {
        KubernetesService kubernetesService = new KubernetesService();
        ConfigMapEnvProvider configMapEnvProvider = new ConfigMapEnvProvider(kubernetesService);
        SecretEnvProvider secretEnvProvider = new SecretEnvProvider(kubernetesService);
        PodEnvProvider podEnvProvider = new PodEnvProvider(kubernetesService);
        PodVaultEnvProvider podVaultEnvProvider = new PodVaultEnvProvider(kubernetesService);
       this.providers = List.of(configMapEnvProvider, secretEnvProvider, podEnvProvider, podVaultEnvProvider);
    }

    @Override
    public boolean isApplicable(PluginSettings pluginSettings) {
        return true;
    }

    @Override
    public Map<String, String> getEnv(PluginSettings pluginSettings) {
        return providers.stream()
                .filter(p -> p.isApplicable(pluginSettings))
                .map(p -> p.getEnv(pluginSettings))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1));
    }
}
