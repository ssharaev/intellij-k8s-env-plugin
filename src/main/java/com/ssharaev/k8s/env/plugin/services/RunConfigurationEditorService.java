package com.ssharaev.k8s.env.plugin.services;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.util.xmlb.Constants;
import com.ssharaev.k8s.env.plugin.model.EnvMode;
import com.ssharaev.k8s.env.plugin.model.PluginSettings;
import com.ssharaev.k8s.env.plugin.model.ReplacementEntity;
import com.ssharaev.k8s.env.plugin.services.providers.CombinedEnvProvider;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.ssharaev.k8s.env.plugin.Utils.DELIMITER;
import static com.ssharaev.k8s.env.plugin.Utils.NOTIFICATION_TITLE;
import static com.ssharaev.k8s.env.plugin.Utils.getEnum;
import static com.ssharaev.k8s.env.plugin.Utils.isEmpty;
import static com.ssharaev.k8s.env.plugin.Utils.splitIfNotEmpty;

@Service
public final class RunConfigurationEditorService {

    private static final Logger LOGGER = Logger.getInstance(CombinedEnvProvider.class);

    private static final String ENV_MODE_FIELD = "mode";
    private static final String NAMESPACE_FIELD = "namespace";
    private static final String CONFIGMAP_NAMES_FIELD = "configmapNames";
    private static final String SECRET_NAMES_FIELD = "secretNames";
    private static final String POD_NAME_FIELD = "podName";
    private static final String REPLACEMENT_ENTITY_FIELD = "replacementEntity";
    private static final String PARENT_REPLACEMENT_ENTITY_FIELD = "parentReplacementEntity";
    private static final String FIND_ATTRIBUTE = "find";
    private static final String REPLACE_ATTRIBUTE = "replace";


    public @NotNull String getSerializationId() {
        return "com.ssharaev.k8s-env-run-configuration";
    }

    public void readExternal(@NotNull RunConfigurationBase<?> runConfiguration, @NotNull Element element) {
        EnvMode mode = getEnum(
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
                .replacementEntities(readReplacementEntities(element))
                .build();
        PluginSettingsProvider.putData(runConfiguration, settings);
    }

    private List<ReplacementEntity> readReplacementEntities(@NotNull Element element) {
        Optional<Element> parentReplacement = (element.getChildren(Constants.LIST))
                .stream()
                .filter(e -> PARENT_REPLACEMENT_ENTITY_FIELD.equals(e.getAttributeValue(Constants.NAME)))
                .findFirst();
        return parentReplacement.stream()
                .map(Element::getChildren)
                .flatMap(List::stream)
                .filter(e -> REPLACEMENT_ENTITY_FIELD.equals(e.getAttributeValue(Constants.NAME)))
                .map(this::readReplacementEntity)
                .toList();
    }

    private ReplacementEntity readReplacementEntity(@NotNull Element element) {
        return new ReplacementEntity(element.getAttributeValue(FIND_ATTRIBUTE), element.getAttributeValue(REPLACE_ATTRIBUTE));
    }

    public void validateConfiguration(@NotNull RunConfigurationBase<?> configuration, boolean isExecution) {
        LOGGER.debug("Validating runConfiguration: " + configuration + ", isExecution: " + isExecution);
        if (!isExecution) {
            return;
        }
        try {
            PluginSettings pluginSetting = PluginSettingsProvider.getPluginSetting(configuration);
            validatePluginSettings(pluginSetting);
            if (pluginSetting.getEnvMode() != EnvMode.DISABLED) {
                enableK8sEnvProvider(configuration);
            }
        } catch (Exception e) {
            NotificationService.notifyWarn(NOTIFICATION_TITLE, e.getMessage());
            disableK8sEnvProvider(configuration);
        }
    }

    private void enableK8sEnvProvider(@NotNull RunConfigurationBase<?> runConfiguration) {
        setEnabledEnvProvider(runConfiguration, true);
    }

    private void disableK8sEnvProvider(@NotNull RunConfigurationBase<?> runConfiguration) {
        setEnabledEnvProvider(runConfiguration, false);
    }

    private void setEnabledEnvProvider(@NotNull RunConfigurationBase<?> runConfiguration, boolean enabled) {
        PluginSettings pluginSetting = PluginSettingsProvider.getPluginSetting(runConfiguration);
        pluginSetting.setEnabled(enabled);
        PluginSettingsProvider.putData(runConfiguration, pluginSetting);
    }

    private void validatePluginSettings(PluginSettings pluginSettings) throws ExecutionException {
        if (isSettingsInvalid(pluginSettings)) {
            throw new ExecutionException("Settings is invalid");
        }

    }

    private boolean isSettingsInvalid(PluginSettings pluginSettings) {
        return pluginSettings.getNamespace() == null ||
                pluginSettings.getEnvMode() == EnvMode.POD_ENV && pluginSettings.getPodName() == null ||
                pluginSettings.getEnvMode() == EnvMode.POD_VAULT && pluginSettings.getPodName() == null ||
                pluginSettings.getEnvMode() == EnvMode.CONFIGMAP_AND_SECRET
                        && isEmpty(pluginSettings.getConfigmapNames())
                        && isEmpty(pluginSettings.getSecretNames());
    }


    public void writeExternal(@NotNull RunConfigurationBase<?> runConfiguration, @NotNull Element element) {
        PluginSettings pluginSetting = PluginSettingsProvider.getPluginSetting(runConfiguration);

        JDOMExternalizerUtil.writeField(element, ENV_MODE_FIELD, pluginSetting.getEnvMode().name());
        JDOMExternalizerUtil.writeField(element, NAMESPACE_FIELD, pluginSetting.getNamespace());
        JDOMExternalizerUtil.writeField(element, CONFIGMAP_NAMES_FIELD, String.join(DELIMITER, pluginSetting.getConfigmapNames()));
        JDOMExternalizerUtil.writeField(element, SECRET_NAMES_FIELD, String.join(DELIMITER, pluginSetting.getSecretNames()));
        JDOMExternalizerUtil.writeField(element, POD_NAME_FIELD, pluginSetting.getPodName());
        writeReplacementEntities(pluginSetting.getReplacementEntities(), element);
    }

    private void writeReplacementEntities(List<ReplacementEntity> replacementEntities, @NotNull Element element) {
        if (isEmpty(replacementEntities)) {
            return;
        }
        Element replacementEntitiesParent = new Element(Constants.LIST);
        replacementEntitiesParent.setAttribute(Constants.NAME, PARENT_REPLACEMENT_ENTITY_FIELD);
        replacementEntities.forEach(entity -> writeReplacementEntity(entity, replacementEntitiesParent));
        element.addContent(replacementEntitiesParent);
    }

    private void writeReplacementEntity(ReplacementEntity replacementEntity, @NotNull Element parentElement) {
        Element element = new Element(Constants.ENTRY);
        element.setAttribute(Constants.NAME, REPLACEMENT_ENTITY_FIELD);
        element.setAttribute(FIND_ATTRIBUTE, replacementEntity.getRegexp());
        element.setAttribute(REPLACE_ATTRIBUTE, replacementEntity.getReplacement());
        parentElement.addContent(element);
    }
}
