package com.ssharaev.k8s.env.plugin.runConfiguration;

import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.ssharaev.k8s.env.plugin.services.PluginSettingsProvider;
import com.ssharaev.k8s.env.plugin.services.providers.CombinedEnvProvider;
import com.ssharaev.k8s.env.plugin.services.providers.EnvProvider;
import com.ssharaev.k8s.env.plugin.ui.RunConfigurationSettingsEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class IdeaRunConfigurationExtension extends RunConfigurationExtension {

    private final EnvProvider envProvider = new CombinedEnvProvider();

    @Nullable
    @Override
    protected String getEditorTitle() {
        return "Env from k8s";
    }

    @Nullable
    @Override
    protected <P extends RunConfigurationBase<?>> SettingsEditor<P> createEditor(@NotNull P configuration) {
        return new RunConfigurationSettingsEditor<>();
    }

    @NotNull
    @Override
    protected String getSerializationId() {
        return EnvK8sRunConfigurationEditor.getSerializationId();
    }

    @Override
    protected void writeExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws WriteExternalException {
        EnvK8sRunConfigurationEditor.writeExternal(runConfiguration, element);
    }

    @Override
    protected void readExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws InvalidDataException {
        EnvK8sRunConfigurationEditor.readExternal(runConfiguration, element);
    }

    @Override
    protected void validateConfiguration(@NotNull RunConfigurationBase configuration, boolean isExecution) {
        EnvK8sRunConfigurationEditor.validateConfiguration(configuration, isExecution);
    }

    /**
     * Unlike other extensions the IDEA extension
     * calls this method instead of RunConfigurationExtensionBase#patchCommandLine method
     * that we could have used to update environment variables.
     */
    @Override
    public <T extends RunConfigurationBase<?>> void updateJavaParameters(
            @NotNull final T configuration,
            @NotNull final JavaParameters params,
            final RunnerSettings runnerSettings
    ) {
        Map<String, String> env = envProvider.getEnv(PluginSettingsProvider.getPluginSetting(configuration));
        params.getEnv().putAll(env);
    }

    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return true;
    }
}
