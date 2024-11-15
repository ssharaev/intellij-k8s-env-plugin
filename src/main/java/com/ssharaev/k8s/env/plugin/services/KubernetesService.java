package com.ssharaev.k8s.env.plugin.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.util.Config;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public final class KubernetesService {

    private static final String POD_ENV_DELIMITER = "=";

    private static final Logger LOGGER = Logger.getInstance(KubernetesService.class);

    private final CoreV1Api api;

    // TODO error handling
    public KubernetesService() {
        ApiClient client;
        try {
            client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);
            client.setHttpClient(client.getHttpClient().newBuilder()
                    .readTimeout(10, TimeUnit.SECONDS) // Infinite read timeout
                    .build());
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to the k8s cluster", e);
        }
        this.api = new CoreV1Api(client);
    }

    public Map<String, String> getEnvFromConfigmaps(@NotNull String namespace, @NotNull Collection<String> configmapNames) {
        LOGGER.debug("Start searching k8s env for namespace: " + namespace + " and configmap list: " + configmapNames);
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

    public Map<String, String> getEnvFromSecrets(@NotNull String namespace, @NotNull Collection<String> secretNames) {
        LOGGER.debug("Start searching k8s env for namespace: " + namespace + " and configmap list: " + secretNames);
        if (secretNames.isEmpty()) {
            return Map.of();
        }
        try {
            V1SecretList secretList = api.listNamespacedSecret(namespace)
                    .limit(10)
                    .execute();
            Set<String> names = new HashSet<>(secretNames);
            return secretList.getItems().stream()
                    .filter(Objects::nonNull)
                    .filter(secret -> secret.getMetadata() != null)
                    .filter(secret -> secret.getData() != null)
                    .filter(secret -> names.contains(secret.getMetadata().getName()))
                    .flatMap(secret -> secret.getData().entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, t -> new String(t.getValue()), (e1, e2) -> e1));
        } catch (ApiException e) {
            LOGGER.warn("Error while getting secrets from namespace " + namespace);
            return Map.of();
        }
    }

    public Map<String, String> getEnvFromPod(@NotNull String namespace, @NotNull String podName) {
        LOGGER.debug("Start searching pod k8s env for namespace: " + namespace + " and pod name: " + podName);
        return execInPod(namespace, podName, "printenv");
    }

    public Map<String, String> getVaultEnvFromPod(@NotNull String namespace, @NotNull String podName) {
        LOGGER.debug("Start searching pod k8s vault env for namespace: " + namespace + " and pod name: " + podName);
        return execInPod(namespace, podName, "printenv");
    }

    private Map<String, String> execInPod(@NotNull String namespace, @NotNull String podName, @NotNull String command) {
        Exec exec = new Exec();
        final Process proc;
        String message = "Error while executing command " + command + " in pod " + podName + " namespace " + namespace;
        try {
            proc = exec.exec(namespace, podName, new String[]{"sh", "-c", command}, false);
            try (BufferedReader bufferedReader = proc.inputReader()) {
                Map<String, String> result = splitEnv(bufferedReader.lines());
                proc.waitFor();
                return result;
            } catch (InterruptedException e) {
                LOGGER.warn(message, e);
                return Map.of();
            }
        } catch (ApiException | IOException e) {
            LOGGER.warn(message, e);
            return Map.of();
        }
    }

    private Map<String, String> splitEnv(Stream<String> lines) {
        return lines
                .filter(Objects::nonNull)
                .filter(s -> s.contains(POD_ENV_DELIMITER))
                .map(s -> s.split(POD_ENV_DELIMITER))
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1], (e1, e2) -> e1));
    }
}
