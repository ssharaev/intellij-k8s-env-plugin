package com.ssharaev.k8s.env.plugin.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.util.Config;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public final class KubernetesService {

    private static final String POD_ENV_DELIMITER = "=";

    private static final Logger LOGGER = Logger.getInstance(KubernetesService.class);

    private CoreV1Api api;

    private CoreV1Api getApi() {
        if (api != null) {
            return api;
        }
        ApiClient client;
        try {
            client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);
            client.setHttpClient(client.getHttpClient().newBuilder()
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build());
            this.api = new CoreV1Api(client);
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to k8s cluster!", e);
        }
        return api;
    }

    public KubernetesService() {
        try {
            getApi();
        } catch (Exception e) {
            LOGGER.warn(e);
        }
    }

    public void connected() throws ApiException {
        CoreV1Api coreV1Api = getApi();
        if (coreV1Api == null) {
            return;
        }
        coreV1Api.listNamespace().execute();
    }

    public List<String> getNamespaces() {
        try {
            CoreV1Api coreV1Api = getApi();
            if (coreV1Api == null) {
                LOGGER.warn("Error while getting namespaces!");
                return List.of();
            }
            return coreV1Api.listNamespace().execute().getItems()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(V1Namespace::getMetadata)
                    .filter(Objects::nonNull)
                    .map(V1ObjectMeta::getName)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (ApiException | RuntimeException e) {
            LOGGER.warn("Error while getting namespaces!", e);
            return List.of();
        }
    }

    public Map<String, String> getEnvFromConfigmaps(@NotNull String namespace, @NotNull Collection<String> configmapNames) {
        LOGGER.debug("Start searching k8s env for namespace: " + namespace + " and configmap list: " + configmapNames);
        if (configmapNames.isEmpty()) {
            return Map.of();
        }
        try {
            CoreV1Api coreV1Api = getApi();
            if (coreV1Api == null) {
                LOGGER.warn("Error while getting configmaps from namespace " + namespace);
                return Map.of();
            }
            V1ConfigMapList configMapList = coreV1Api.listNamespacedConfigMap(namespace)
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
        } catch (ApiException | RuntimeException e) {
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
            CoreV1Api coreV1Api = getApi();
            if (coreV1Api == null) {
                LOGGER.warn("Error while getting secrets from namespace " + namespace);
                return Map.of();
            }
            V1SecretList secretList = coreV1Api.listNamespacedSecret(namespace)
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
        } catch (ApiException | RuntimeException e) {
            LOGGER.warn("Error while getting secrets from namespace " + namespace);
            return Map.of();
        }
    }

    public Map<String, String> getEnvFromPod(@NotNull String namespace, @NotNull String podName) {
        LOGGER.debug("Start searching pod k8s env for namespace: " + namespace + " and pod name: " + podName);
        return execInPod(namespace, podName, "printenv");
    }

    // TODO change command
    public Map<String, String> getVaultEnvFromPod(@NotNull String namespace, @NotNull String podName) {
        LOGGER.debug("Start searching pod k8s vault env for namespace: " + namespace + " and pod name: " + podName);
        return execInPod(namespace, podName, "/vault/vault-env env");
    }

    private Map<String, String> execInPod(@NotNull String namespace, @NotNull String podName, @NotNull String command) {
        Exec exec = new Exec();
        final Process proc;
        String message = "Error while executing command " + command + " in pod " + podName + " namespace " + namespace;
        try {
            CoreV1Api coreV1Api = getApi();
            if (coreV1Api == null) {
                LOGGER.warn(message);
                return Map.of();
            }
            V1Pod pod = coreV1Api.listNamespacedPod(namespace)
                    .execute().getItems()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getMetadata() != null)
                    .filter(p -> p.getMetadata().getName() != null)
                    .filter(p -> p.getMetadata().getName().startsWith(podName))
                    .findFirst()
                    .orElse(null);
            if (pod == null) {
                return Map.of();
            }
            proc = exec.exec(pod, new String[]{"sh", "-c", command}, false, false);
            try (BufferedReader bufferedReader = proc.inputReader()) {
                Map<String, String> result = splitEnv(bufferedReader.lines());
                proc.waitFor();
                return result;
            } catch (InterruptedException e) {
                LOGGER.warn(message, e);
                return Map.of();
            }
        } catch (ApiException | IOException | RuntimeException e) {
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
