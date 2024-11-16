package com.ssharaev.k8s.env.plugin.services.providers;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.KubernetesService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public final class CombinedEnvProvider implements EnvProvider {

    private final List<EnvProvider> providers;

    public CombinedEnvProvider() {
        KubernetesService kubernetesService = ApplicationManager.getApplication().getService(KubernetesService.class);
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
        if (isSettingsInvalid(pluginSettings)) {
            return Map.of();
        }
        return providers.stream()
                .filter(p -> p.isApplicable(pluginSettings))
                .map(p -> p.getEnv(pluginSettings))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1));
    }

    private boolean isSettingsInvalid(PluginSettings pluginSettings) {
        return StringUtils.isBlank(pluginSettings.getNamespace()) ||

                pluginSettings.getEnvMode() == EnvMode.POD_ENV && StringUtils.isBlank(pluginSettings.getPodName()) ||

                pluginSettings.getEnvMode() == EnvMode.POD_VAULT && StringUtils.isBlank(pluginSettings.getPodName()) ||

                pluginSettings.getEnvMode() == EnvMode.CONFIGMAP_AND_SECRET
                        && CollectionUtils.isEmpty(pluginSettings.getConfigmapNames())
                        && CollectionUtils.isEmpty(pluginSettings.getSecretNames());
    }
}
