package com.ssharaev.k8s.env.plugin.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.util.Config;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public final class KubernetesService {

    private static final Logger LOGGER = Logger.getInstance(KubernetesService.class);

    private final CoreV1Api api;

    // TODO error handling
    public KubernetesService() {
        ApiClient client;
        try {
            client = Config.defaultClient();
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to the k8s cluster", e);
        }
        this.api = new CoreV1Api(client);
    }

    public Map<String, String> getEnvFromConfigmaps(@NotNull String namespace, @NotNull Collection<String> configmapNames) {
        if (configmapNames.isEmpty()) {
            return Map.of();
        }
        try {
            V1ConfigMapList configMapList = api.listNamespacedConfigMap(namespace)
                    .limit(10)
                    .execute();
            Set<String> names = new HashSet<>(configmapNames);
            return configMapList.getItems().stream()
                    .filter(Objects::nonNull)
                    .filter(configmap -> configmap.getMetadata() != null)
                    .filter(configmap -> configmap.getData() != null)
                    .filter(configmap -> names.contains(configmap.getMetadata().getName()))
                    .flatMap(configmap -> configmap.getData().entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1));
        } catch (ApiException e) {
            LOGGER.warn("Error while getting configmaps from namespace " + namespace);
            return Map.of();
        }
    }

}
