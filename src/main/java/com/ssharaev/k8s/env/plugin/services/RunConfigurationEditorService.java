package com.ssharaev.k8s.env.plugin.services;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import org.apache.commons.lang3.EnumUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.ssharaev.k8s.env.plugin.Utils.DELIMITER;
import static com.ssharaev.k8s.env.plugin.Utils.splitIfNotEmpty;

@Service
public final class RunConfigurationEditorService {

    private static final String ENV_MODE_FIELD = "mode";
    private static final String NAMESPACE_FIELD = "namespace";
    private static final String CONFIGMAP_NAMES_FIELD = "configmapNames";
    private static final String SECRET_NAMES_FIELD = "secretNames";
    private static final String POD_NAME_FIELD = "podName";
    
    private final KubernetesService kubernetesService;

    public RunConfigurationEditorService() {
        kubernetesService = ApplicationManager.getApplication().getService(KubernetesService.class);;
    }

    public @NotNull String getSerializationId() {
        return "com.ssharaev.k8sEnv";
    }

    public void readExternal(@NotNull RunConfigurationBase<?> runConfiguration, @NotNull Element element) {
        EnvMode mode = EnumUtils.getEnum(
                EnvMode.class, JDOMExternalizerUtil.readField(element, POD_NAME_FIELD), EnvMode.CONFIGMAP_AND_SECRET);
        String namespace = JDOMExternalizerUtil.readField(element, NAMESPACE_FIELD);
        List<String> configmapNames = splitIfNotEmpty(JDOMExternalizerUtil.readField(element, CONFIGMAP_NAMES_FIELD));
        List<String> secretNames = splitIfNotEmpty(JDOMExternalizerUtil.readField(element, SECRET_NAMES_FIELD));
        String podName = JDOMExternalizerUtil.readField(element, POD_NAME_FIELD);
        PluginSettings settings = PluginSettings.builder()
                .envMode(mode)
                .namespace(namespace)
                .configmapNames(configmapNames)
                .secretNames(secretNames)
                .podName(podName)
                .build();
        PluginSettingsProvider.putData(runConfiguration, settings);
    }

    // TODO fix validation
    public void validateConfiguration(@NotNull RunConfigurationBase<?> configuration, boolean isExecution) throws ConfigurationException {
        try {
            kubernetesService.connected();
        } catch (Exception e) {
            throw new RuntimeConfigurationWarning("Unable to fetch info from kubernetes cluster!");
        }
        validatePluginSettings(PluginSettingsProvider.getPluginSetting(configuration));
    }

    private void validatePluginSettings(PluginSettings pluginSettings) throws RuntimeConfigurationWarning {
        if (pluginSettings.getNamespace() == null) {
            throw new RuntimeConfigurationWarning("Namespace is empty, Env from kubernetes disabled!");
        }
    }

    public void writeExternal(@NotNull RunConfigurationBase<?> runConfiguration, @NotNull Element element) {
        PluginSettings pluginSetting = PluginSettingsProvider.getPluginSetting(runConfiguration);

        JDOMExternalizerUtil.writeField(element, ENV_MODE_FIELD, pluginSetting.getEnvMode().name());
        JDOMExternalizerUtil.writeField(element, NAMESPACE_FIELD, pluginSetting.getNamespace());
        JDOMExternalizerUtil.writeField(element, CONFIGMAP_NAMES_FIELD, String.join(DELIMITER, pluginSetting.getConfigmapNames()));
        JDOMExternalizerUtil.writeField(element, SECRET_NAMES_FIELD, String.join(DELIMITER, pluginSetting.getSecretNames()));
        JDOMExternalizerUtil.writeField(element, POD_NAME_FIELD, pluginSetting.getPodName());
    }
}
