package com.ssharaev.k8s.env.plugin.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.services.PluginSettingsProvider;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.ssharaev.k8s.env.plugin.Utils.CONFIGMAP_DELIMITER;

public class EnvK8sRunConfigurationEditor {

    private static final String NAMESPACE_FIELD = "namespace";
    private static final String CONFIGMAP_NAMES_FIELD = "configmapNames";

    public static @NotNull String getSerializationId() {
        return "com.ssharaev.k8s-env-run-configuration";
    }

    public static void readExternal(@NotNull RunConfigurationBase<?> runConfiguration, @NotNull Element element) {
        String namespace = JDOMExternalizerUtil.readField(element, NAMESPACE_FIELD);
        List<String> configmapNames = Optional.ofNullable(JDOMExternalizerUtil.readField(element, CONFIGMAP_NAMES_FIELD))
                .map(str -> str.split(CONFIGMAP_DELIMITER))
                .map(List::of)
                .orElse(null);
        if (namespace == null && configmapNames == null) {
            return;
        }
        PluginSettings settings = PluginSettings.builder()
                .namespace(namespace)
                .configmapNames(configmapNames)
                .build();
        PluginSettingsProvider.putData(runConfiguration, settings);
    }

    public static void validateConfiguration(@NotNull RunConfigurationBase<?> configuration, boolean isExecution) {
        // TODO check k8s client etc
    }

    public static void writeExternal(@NotNull RunConfigurationBase<?> runConfiguration, @NotNull Element element) {
        PluginSettings pluginSetting = PluginSettingsProvider.getPluginSetting(runConfiguration);
        if (pluginSetting == null) {
            return;
        }
        JDOMExternalizerUtil.writeField(element, NAMESPACE_FIELD, pluginSetting.getNamespace());
        JDOMExternalizerUtil.writeField(element, CONFIGMAP_NAMES_FIELD, String.join(CONFIGMAP_DELIMITER, pluginSetting.getConfigmapNames()));
    }
}
