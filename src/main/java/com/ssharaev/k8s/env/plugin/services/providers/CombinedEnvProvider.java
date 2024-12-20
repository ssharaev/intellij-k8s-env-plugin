package com.ssharaev.k8s.env.plugin.services.providers;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.NotificationService;
import com.ssharaev.k8s.env.plugin.services.ReplacementService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ssharaev.k8s.env.plugin.Utils.NOTIFICATION_TITLE;
import static com.ssharaev.k8s.env.plugin.Utils.getKubernetesService;

@Service
public final class CombinedEnvProvider implements EnvProvider {

    private static final Logger LOGGER = Logger.getInstance(CombinedEnvProvider.class);

    private final List<EnvProvider> providers;

    public CombinedEnvProvider() {
        ConfigMapEnvProvider configMapEnvProvider = new ConfigMapEnvProvider();
        SecretEnvProvider secretEnvProvider = new SecretEnvProvider();
        PodEnvProvider podEnvProvider = new PodEnvProvider();
        PodVaultEnvProvider podVaultEnvProvider = new PodVaultEnvProvider();
        this.providers = List.of(configMapEnvProvider, secretEnvProvider, podEnvProvider, podVaultEnvProvider);
    }

    @Override
    public boolean isApplicable(PluginSettings pluginSettings) {
        return true;
    }

    @Override
    public Map<String, String> getEnv(PluginSettings pluginSettings) {
        if (!pluginSettings.isEnabled()) {
            return Map.of();
        }
        try {
            getKubernetesService().connected();
        } catch (Exception e) {
            NotificationService.notifyWarn(NOTIFICATION_TITLE, e.getMessage());
        }
        try {
            Map<String, String> result = providers.stream()
                    .filter(p -> p.isApplicable(pluginSettings))
                    .map(p -> p.getEnv(pluginSettings))
                    .map(Map::entrySet)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1));
            ReplacementService replacementService =
                    ApplicationManager.getApplication().getService(ReplacementService.class);
            return replacementService.proceedReplacement(result, pluginSettings.getReplacementEntities());
        } catch (Exception e) {
            LOGGER.warn("Unable to get env form k8s!", e);
            NotificationService.notifyWarn("Unable to get env form k8s", "Error: " + e.getMessage());
            return Map.of();
        }
    }
}
